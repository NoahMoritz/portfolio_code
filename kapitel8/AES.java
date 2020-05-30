/*
 *  Copyright (c) NOAMO Tech - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Noah Moritz Hölterhoff <noah.hoelterhoff@gmail.com>, 20.5.2020
 */

package de.noamo.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

/**
 * Eine Implemetierung des symetrischen Verschlüsselungsverfaren AES nach dem Grundaufbau GEDIE (Generate, Encrypt,
 * Decrypt, Import Key, Export Key). Ist zu verwenden, wenn eine hohe Geschwindigkeit erforderlich ist. Das einzigste
 * Problem dabei ist der Schlüsselaustausch. Dieser sollte mit {@link de.noamo.util.RSA} passieren.
 *
 * @author Noah Moritz Hölterhoff
 * @version 16.05.2020
 * @since 26.04.2020
 */
@SuppressWarnings("ConstantConditions")
public class AES {
    private static final String METHODE = "AES/CBC/PKCS5Padding";

    /**
     * Entschlüsselt eine Nachricht mit AES und gibt sie als String zurück. Der String muss mit
     * {@link de.noamo.util.Base64} verschlüsselt worden sein, nachdem er mit AES
     * verschlüsselt wurde.
     *
     * @param sk                   Der {@link SecretKey}, mit dem die Nachricht entschlüsselt werden soll
     * @param text                 Der String, der entschlüsselt werden soll
     * @param initializationVector Der InitVector (mit Base64 kodiert)
     * @return Der entschlüsselte String
     * @throws GeneralSecurityException Falls ein Problem bei der Verschlüsselung auftritt
     */
    public static String decrypt(SecretKey sk, String text, String initializationVector) throws GeneralSecurityException {
        if (sk == null || text == null) throw new GeneralSecurityException("Key or Text can not be null!");
        Cipher cipher = Cipher.getInstance(METHODE);
        cipher.init(Cipher.DECRYPT_MODE, sk, new IvParameterSpec(de.noamo.util.Base64.decode(initializationVector)));
        return new String(cipher.doFinal(de.noamo.util.Base64.decode(text)));
    }

    /**
     * Verschlüsselt eine Nachricht mit AES und gibt sie als String zurück. Der String ist geignet für Netzwerke,
     * da er mit {@link de.noamo.util.Base64} verschlüsselt wurde.
     *
     * @param sk                   Der {@link SecretKey}, mit dem die Nachricht verschlüsselt werden soll
     * @param text                 Der String, der verschlüsselt werden soll
     * @param initializationVector Der InitVector (mit BAse64 kodiert)
     * @return Den verschlüsselten String
     * @throws GeneralSecurityException Falls ein Problem bei der Verschlüsselung auftritt
     */
    public static String encrypt(SecretKey sk, String text, String initializationVector) throws GeneralSecurityException {
        if (sk == null || text == null || initializationVector == null)
            throw new GeneralSecurityException("Key or Text can not be null!");
        Cipher cipher = Cipher.getInstance(METHODE);
        cipher.init(Cipher.ENCRYPT_MODE, sk, new IvParameterSpec(de.noamo.util.Base64.decode(initializationVector)));
        return de.noamo.util.Base64.encode(cipher.doFinal(text.getBytes()));
    }

    /**
     * Exportiert einen SecretKey und kodiert ihn mit Base64
     *
     * @param sk Der SecretKey
     * @return Der Key als Base64 kodierter String
     */
    public static String exportKey(SecretKey sk) {
        return de.noamo.util.Base64.encode(sk.getEncoded());
    }

    /**
     * Generiert einen SecretKey für AES mit 128 Bit
     *
     * @return Der Secret Key für AES
     */
    public static SecretKey generateKey() {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            keygen.init(128);
            return keygen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Importiert einen {@link SecretKey} aus einem String. Dieser String muss mit
     * {@link de.noamo.util.Base64} verschlüsselt worden sein.
     *
     * @param key Der Schlüssel in Form eines Strings
     * @return Ein {@link SecretKey} für AES
     */
    public static SecretKey importKey(String key) {
        if (key == null || key.equals("")) return null;
        byte[] sk = de.noamo.util.Base64.decode(key);
        return new SecretKeySpec(sk, "AES");
    }
}