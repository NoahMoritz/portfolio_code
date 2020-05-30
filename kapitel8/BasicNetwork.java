/*
 *  Copyright (c) NOAMO Tech - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Noah Moritz Hölterhoff <noah.hoelterhoff@gmail.com>, 20.5.2020
 */

package de.noamo.util;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Alle Methoden für Netzwerke
 * @version 29.04.2020
 */
public abstract class BasicNetwork {
    private final static String END_CHAR = "*E_N_D*";
    private static final SecretKey secretKey = AES.generateKey();
    private static final String initVector;

    static {
        byte[] temp = new byte[16];
        new SecureRandom().nextBytes(temp);
        initVector = Base64.encode(temp);
    }

    public static String[] sendRequest(String pHost, int pPort, String[] pText) throws IOException {
        if (secretKey == null || initVector == null) return null;

        try (Socket socket = new Socket(pHost, pPort);
             PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            // verschlüsselte Verbindung herstellen
            String temp_rsaPublic = bufferedReader.readLine();
            PublicKey rsa_publicKey = RSA.importKey(temp_rsaPublic);
            printWriter.println(RSA.encrypt(rsa_publicKey, AES.exportKey(secretKey)));
            printWriter.println(RSA.encrypt(rsa_publicKey, initVector));
            printWriter.flush();

            // Nachricht senden
            for (String line : pText) {
                printWriter.println(AES.encrypt(secretKey, line, initVector));
            }
            printWriter.println(END_CHAR);
            printWriter.flush();

            // Nachricht empfangen
            String temp;
            StringBuilder stringBuilder = new StringBuilder();
            while (!(temp = bufferedReader.readLine()).equals(END_CHAR)) {
                stringBuilder.append(AES.decrypt(secretKey, temp, initVector)).append(System.lineSeparator());
            }
            return stringBuilder.toString().split(System.lineSeparator());
        } catch (GeneralSecurityException ex){
            ex.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void startService(int pPort, Service pHandler) throws IOException {
        final ServerSocket serverSocket = new ServerSocket(pPort);
        final KeyPair keyPair = RSA.generateKey();
        new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    new Thread(() -> {
                        try (PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                            // verschlüsselte Verbindung herstellen
                            printWriter.println(RSA.exportKey(keyPair.getPublic()));
                            printWriter.flush();
                            SecretKey secretKey = AES.importKey(RSA.decrypt(keyPair.getPrivate(), bufferedReader.readLine()));
                            String initVector = RSA.decrypt(keyPair.getPrivate(), bufferedReader.readLine());

                            // Nachricht empfangen
                            String temp;
                            StringBuilder stringBuilder = new StringBuilder();
                            while (!(temp = bufferedReader.readLine()).equals(END_CHAR)) {
                                stringBuilder.append(AES.decrypt(secretKey, temp, initVector)).append(System.lineSeparator());
                            }

                            // Interface nutzen
                            String[] afterProcess = pHandler.process(stringBuilder.toString().split(System.lineSeparator()));

                            // Nachricht verarbeiten
                            for (String line : afterProcess) {
                                printWriter.println(AES.encrypt(secretKey, line, initVector));
                            }
                            printWriter.println(END_CHAR);
                            printWriter.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            Util.tryCloseCloseable(socket);
                        }
                    }).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public interface Service {
        String[] process(String[] pText);
    }
}
