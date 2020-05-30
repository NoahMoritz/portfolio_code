/*
 *  Copyright (c) NOAMO Tech - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Noah Moritz Hölterhoff <noah.hoelterhoff@gmail.com>, 20.5.2020
 */

package de.noamo.util;

import java.util.Arrays;

/**
 * Als Klasse für ein einheitliches Base64 (ermöglicht eine niederiger Android und Java Version)
 *
 * @author Noah Moritz Hölterhoff
 * @version 26.04.2020
 */
public class Base64 {
    private static final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    private static final int[] IA = new int[256];

    static { // Wird bei jedem Start ausgeführt
        Arrays.fill(IA, -1);
        for (int i = 0, iS = CA.length; i < iS; i++)
            IA[CA[i]] = i;
        IA['='] = 0;
    }

    /**
     * Entschlüsselt einen String (z.B. aus Netzwerkverbindung) zu einem Byte Array
     * unter Verwendung des BASE64 Verfahrens
     *
     * @param enc Der String in BASE64
     * @return Ein Byte Array, dass den entschlüsselten String wiederspiegelt
     */
    public final static byte[] decode(String enc) {
        char[] sArr = enc.toCharArray();
        int sLen = sArr != null ? sArr.length : 0;
        if (sLen == 0) return new byte[0];
        int sepCnt = 0;
        for (int i = 0; i < sLen; i++)
            if (IA[sArr[i]] < 0) sepCnt++;
        if ((sLen - sepCnt) % 4 != 0) return null;
        int pad = 0;
        for (int i = sLen; i > 1 && IA[sArr[--i]] <= 0; )
            if (sArr[i] == '=') pad++;
        int len = ((sLen - sepCnt) * 6 >> 3) - pad;
        byte[] dArr = new byte[len];
        for (int s = 0, d = 0; d < len; ) {
            int i = 0;
            for (int j = 0; j < 4; j++) {
                int c = IA[sArr[s++]];
                if (c >= 0) i |= c << (18 - j * 6);
                else j--;
            }
            dArr[d++] = (byte) (i >> 16);
            if (d < len) {
                dArr[d++] = (byte) (i >> 8);
                if (d < len)
                    dArr[d++] = (byte) i;
            }
        }
        return dArr;
    }

    /**
     * Wandelt eine ByteArray nach dem BASE64 Standart zu einem String um
     *
     * @param sArr Ein Byte Arrays
     * @return Ein String, der über das Netzwerk versendet werden kann
     */
    public final static String encode(byte[] sArr) {
        int sLen = sArr != null ? sArr.length : 0;
        if (sLen == 0) return "";
        int eLen = (sLen / 3) * 3;
        int cCnt = ((sLen - 1) / 3 + 1) << 2;
        int dLen = cCnt + (cCnt - 1) / 76 << 1;
        char[] dArr = new char[dLen];
        for (int s = 0, d = 0, cc = 0; s < eLen; ) {
            int i = (sArr[s++] & 0xff) << 16 | (sArr[s++] & 0xff) << 8 | (sArr[s++] & 0xff);
            dArr[d++] = CA[(i >>> 18) & 0x3f];
            dArr[d++] = CA[(i >>> 12) & 0x3f];
            dArr[d++] = CA[(i >>> 6) & 0x3f];
            dArr[d++] = CA[i & 0x3f];
        }
        int left = sLen - eLen;
        if (left > 0) {
            int i = ((sArr[eLen] & 0xff) << 10) | (left == 2 ? ((sArr[sLen - 1] & 0xff) << 2) : 0);
            dArr[dLen - 4] = CA[i >> 12];
            dArr[dLen - 3] = CA[(i >>> 6) & 0x3f];
            dArr[dLen - 2] = left == 2 ? CA[i & 0x3f] : '=';
            dArr[dLen - 1] = '=';
        }
        return new String(dArr);
    }
}