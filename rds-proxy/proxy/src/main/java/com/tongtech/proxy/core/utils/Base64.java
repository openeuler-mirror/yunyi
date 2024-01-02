/**
 * 自有算法的BASE64转换（非标准算法），经过自有算法转换的字符串，在比较大小时不改变原来的顺序
 * 转换举例：
 * 0 : A+**
 * 1 : AE**
 * 10 : AH+*
 * 11 : AH2*
 * 2 : AU**
 * 3 : Ak**
 * 4 : B+**
 * 5 : BE**
 * a : ME**
 * aa : MK2*
 * aaa : MK3V
 * aab : MK3W
 * aac: MK3X
 * b : MU**
 * bb : Ma6*
 * bbb : Ma7W
 * c : Mk**
 * cc : MqA*
 * ccc : MqBX
 * 16789 : AHMrC1Y*
 */
package com.tongtech.proxy.core.utils;


import java.nio.charset.StandardCharsets;

public class Base64 {
    private static final char PADDING = '*';

    private static final byte[] encodingTable = {
            (byte) '+', (byte) '/',

            /* 2 */
            (byte) '0', (byte) '1', (byte) '2',
            (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
            (byte) '8', (byte) '9',

            /* 12 */
            (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E',
            (byte) 'F', (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J',
            (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N', (byte) 'O',
            (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T',
            (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y',
            (byte) 'Z',

            /* 38 */
            (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd',
            (byte) 'e', (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i',
            (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n',
            (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's',
            (byte) 't', (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x',
            (byte) 'y', (byte) 'z',

    };
    private static final byte[] decodingTable;

    static {
        decodingTable = new byte[128];

        for (int i = 0; i < 128; i++) {
            decodingTable[i] = (byte) -1;
        }

        decodingTable['+'] = 0;
        decodingTable['/'] = 1;

        for (int i = '0'; i <= '9'; i++) {
            decodingTable[i] = (byte) (i - '0' + 2);
        }

        for (int i = 'A'; i <= 'Z'; i++) {
            decodingTable[i] = (byte) (i - 'A' + 12);
        }

        for (int i = 'a'; i <= 'z'; i++) {
            decodingTable[i] = (byte) (i - 'a' + 38);
        }
    }

    /**
     * 将输入的字节数组转换成可打印的Base64字符串
     *
     * @param data
     * @param offset
     * @param len
     * @return
     */
    public static String encode(byte[] data, int offset, int len) {
        byte[] bytes;

        int modulus = len % 3;

        if (modulus == 0) {
            bytes = new byte[(4 * len) / 3];
        } else {
            bytes = new byte[4 * ((len / 3) + 1)];
        }

        int dataLength = (len - modulus);
        int a1;
        int a2;
        int a3;

        for (int i = offset, j = 0; i < dataLength + offset; i += 3, j += 4) {
            a1 = data[i] & 0xff;
            a2 = data[i + 1] & 0xff;
            a3 = data[i + 2] & 0xff;

            bytes[j] = encodingTable[(a1 >>> 2) & 0x3f];
            bytes[j + 1] = encodingTable[((a1 << 4) | (a2 >>> 4)) & 0x3f];
            bytes[j + 2] = encodingTable[((a2 << 2) | (a3 >>> 6)) & 0x3f];
            bytes[j + 3] = encodingTable[a3 & 0x3f];
        }

        int b1;
        int b2;
        int b3;
        int d1;
        int d2;

        switch (modulus) {
            case 0: /* nothing left to do */
                break;

            case 1:
                d1 = data[offset + len - 1] & 0xff;
                b1 = (d1 >>> 2) & 0x3f;
                b2 = (d1 << 4) & 0x3f;

                bytes[bytes.length - 4] = encodingTable[b1];
                bytes[bytes.length - 3] = encodingTable[b2];
                bytes[bytes.length - 2] = PADDING;//(byte) '=';
                bytes[bytes.length - 1] = PADDING;//(byte) '=';

                break;

            case 2:
                d1 = data[offset + len - 2] & 0xff;
                d2 = data[offset + len - 1] & 0xff;

                b1 = (d1 >>> 2) & 0x3f;
                b2 = ((d1 << 4) | (d2 >>> 4)) & 0x3f;
                b3 = (d2 << 2) & 0x3f;

                bytes[bytes.length - 4] = encodingTable[b1];
                bytes[bytes.length - 3] = encodingTable[b2];
                bytes[bytes.length - 2] = encodingTable[b3];
                bytes[bytes.length - 1] = PADDING;// (byte) '=';

                break;
        }

        return new String(bytes);
    }

    /**
     * 将Base64的字符串恢复回真正的字节数据数据
     *
     * @param data
     * @return
     */
    public static byte[] decode(String data) {
        return decode(data.getBytes());
    }

    private static byte[] decode(byte[] data) {
        byte[] bytes;
        byte b1;
        byte b2;
        byte b3;
        byte b4;

        if (data == null) {
            return null;
        } else if (data.length == 0) {
            return data;
        } else {
            try {
                if (data[data.length - 2] == PADDING/* '=' */) {
                    bytes = new byte[(((data.length / 4) - 1) * 3) + 1];
                } else if (data[data.length - 1] == PADDING/* '=' */) {
                    bytes = new byte[(((data.length / 4) - 1) * 3) + 2];
                } else {
                    bytes = new byte[((data.length / 4) * 3)];
                }

                for (int i = 0, j = 0; i < (data.length - 4); i += 4, j += 3) {
                    b1 = decodingTable[data[i]];
                    b2 = decodingTable[data[i + 1]];
                    b3 = decodingTable[data[i + 2]];
                    b4 = decodingTable[data[i + 3]];

                    bytes[j] = (byte) ((b1 << 2) | (b2 >> 4));
                    bytes[j + 1] = (byte) ((b2 << 4) | (b3 >> 2));
                    bytes[j + 2] = (byte) ((b3 << 6) | b4);
                }

                if (data[data.length - 2] == PADDING/* '=' */) {
                    b1 = decodingTable[data[data.length - 4]];
                    b2 = decodingTable[data[data.length - 3]];

                    bytes[bytes.length - 1] = (byte) ((b1 << 2) | (b2 >> 4));
                } else if (data[data.length - 1] == PADDING/* '=' */) {
                    b1 = decodingTable[data[data.length - 4]];
                    b2 = decodingTable[data[data.length - 3]];
                    b3 = decodingTable[data[data.length - 2]];

                    bytes[bytes.length - 2] = (byte) ((b1 << 2) | (b2 >> 4));
                    bytes[bytes.length - 1] = (byte) ((b2 << 4) | (b3 >> 2));
                } else {
                    b1 = decodingTable[data[data.length - 4]];
                    b2 = decodingTable[data[data.length - 3]];
                    b3 = decodingTable[data[data.length - 2]];
                    b4 = decodingTable[data[data.length - 1]];

                    bytes[bytes.length - 3] = (byte) ((b1 << 2) | (b2 >> 4));
                    bytes[bytes.length - 2] = (byte) ((b2 << 4) | (b3 >> 2));
                    bytes[bytes.length - 1] = (byte) ((b3 << 6) | b4);
                }
            } catch (Throwable t) {
                bytes = null;
            }
            return bytes;
        }
    }

    /**
     * 计算BASE64编码的字符串的实际字节数
     *
     * @param str
     * @return
     */
    public static int lengthAsBase64(String str) {
        if (str == null | str.length() < 4) {
            return 0;
        }

        int len;
        int length = str.length();
        if (str.charAt(length - 2) == PADDING/* '=' */) {
            len = (((length / 4) - 1) * 3) + 1;
        } else if (str.charAt(length - 1) == PADDING/* '=' */) {
            len = (((length / 4) - 1) * 3) + 2;
        } else {
            len = ((length / 4) * 3);
        }
        return len;
    }

//    private static byte[] discardNonBase64Bytes(byte[] data) {
//        byte[] temp = new byte[data.length];
//        int bytesCopied = 0;
//
//        for (int i = 0; i < data.length; i++) {
//            if (isValidBase64Byte(data[i])) {
//                temp[bytesCopied++] = data[i];
//            }
//        }
//
//        byte[] newData = new byte[bytesCopied];
//
//        System.arraycopy(temp, 0, newData, 0, bytesCopied);
//
//        return newData;
//    }
//
//    private static String discardNonBase64Chars(String data) {
//        StringBuffer sb = new StringBuffer();
//
//        int length = data.length();
//
//        for (int i = 0; i < length; i++) {
//            if (isValidBase64Byte((byte) (data.charAt(i)))) {
//                sb.append(data.charAt(i));
//            }
//        }
//
//        return sb.toString();
//    }

//    private static boolean isValidBase64Byte(byte b) {
//        if (b == '=') {
//            return true;
//        } else if ((b < 0) || (b >= 128)) {
//            return false;
//        } else if (decodingTable[b] == -1) {
//            return false;
//        }
//
//        return true;
//    }

    public static void main(String[] args) {
        String data = "";
        byte[] bs = data.getBytes(StandardCharsets.UTF_8);
        String result = Base64.encode(bs, 0, bs.length);
        System.out.println(result);
        System.out.println();
        System.out.println(new String(Base64.decode(result)));
    }
}
