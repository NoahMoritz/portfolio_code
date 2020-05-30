/*
 *  Copyright (c) NOAMO Tech - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Noah Moritz Hölterhoff <noah.hoelterhoff@gmail.com>, 20.5.2020
 */

package de.noamo.util;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

/**
 * Implentierung des asymetirschen Verschlüsselungsverfahren RSA
 * <p>
 * Angpasste Klasse, um eine Plattformübergreifende Kompatiblität zu garantieren
 * (für Android, Oracle Java und Open JDK getestet).
 *
 * @author Noah Moritz Hölterhoff
 * @version 26.04.2020
 * @since 26.04.2020
 */
public class RSA {

    /**
     * Entschlüsselt eine mit RSA und Base64 kodierte Nachricht
     *
     * @param pk   PrivateKey
     * @param text Der Text
     * @return Der entschlüsselte Text
     * @throws GeneralSecurityException Falls ein Fehler bei der Entschlüsselung auftritt
     */
    public static String decrypt(PrivateKey pk, String text) throws GeneralSecurityException {
        if (pk == null || text == null || text.equals("")) {
            throw new GeneralSecurityException("Falsche Input-Parameter in der RSA.decrypt()-Methode!");
        }
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, pk);
        byte[] base64_temp = de.noamo.util.Base64.decode(text);
        if (base64_temp == null) {
            throw new GeneralSecurityException("Fehlerhafte Base64-Rückgabe in der RSA.decrypt()-Methode!");
        }
        return new String(cipher.doFinal(base64_temp));
    }

    /**
     * Verschlüsselt einen String mir RSA und gibt ihn Base64-Kodiert zurück.
     *
     * @param pk   Der PublicKey (zum verschlüsseln)
     * @param text (Der Text, der verschlüsselt werden soll)
     * @return RSA -> Base64 verschlüsselter String
     * @throws GeneralSecurityException Für Fehler beim Prozess der Verschlüsselung
     */
    public static String encrypt(PublicKey pk, String text) throws GeneralSecurityException {
        if (pk == null || text == null) {
            throw new GeneralSecurityException("Falsche Input-Parameter in der RSA.encrypt()-Methode!");
        }
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pk);
        return de.noamo.util.Base64.encode(cipher.doFinal(text.getBytes()));
    }

    /**
     * Exportiert einen PublicKey zu einem String (mit Base64 kodiert)
     *
     * @param pk Public Key eines RSA KeyPairs
     * @return Der Public Key als String
     */
    public static String exportKey(PublicKey pk) {
        return de.noamo.util.Base64.encode(pk.getEncoded());
    }

    public static KeyPair generateKey() {
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(512);
            return keygen.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Imporitiert einen RSA Public Key aus einem String (kodiert mit Base64)
     *
     * @param key Der Base64 verschlüsselte Key
     * @return Ein RSA Public Key
     * @throws GeneralSecurityException Falls ein Problem bei der Erstellung des Schlüssels auftritt
     */
    public static PublicKey importKey(String key) throws GeneralSecurityException {
        if (key == null || key.equals("")) {
            throw new GeneralSecurityException("Es wurde versucht einen leeren String zu importieren");
        }
        byte[] import_rsa_key = de.noamo.util.Base64.decode(key);
        if (import_rsa_key == null || import_rsa_key.length == 0) {
            throw new GeneralSecurityException("Fehlerhafte Base64-Rückgabe RSA.importKey()-Methode");
        }
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(import_rsa_key);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(keySpec);
    }
}
