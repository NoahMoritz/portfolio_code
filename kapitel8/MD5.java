/*
 *  Copyright (c) NOAMO Tech - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Noah Moritz Hölterhoff <noah.hoelterhoff@gmail.com>, 20.5.2020
 */

package de.noamo.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Enthält die Implemtierung für die Verschlüsslung mit MD%. Sollte zur Speicherung von Passwörten verwendet werden
 *
 * @author Noah Moritz Hölterhoff
 * @version 26.04.2020
 */
public class MD5 {
    public static String encrypt(String input) {
        try {
            MessageDigest msgD = MessageDigest.getInstance("MD5");
            byte[] md = msgD.digest(input.getBytes());
            BigInteger bi = new BigInteger(1, md);
            String hashtext = bi.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
} 