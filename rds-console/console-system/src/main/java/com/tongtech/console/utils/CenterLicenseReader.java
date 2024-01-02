package com.tongtech.console.utils;



import com.tongtech.console.domain.CenterLicenseInfo;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class CenterLicenseReader {

    // license签名标签
    public final static String LIC_SIGNATURE_NAME = "RDS_LIC_SIGNATURE";

    // license客户信息标签
    public final static String LIC_CUSTINFO_NAME = "RDS_LIC_CUSTINFO";

    private static final long MAGIC_CODE = 0x526463686e706c72l;

    // RSA密钥转字符串的进制值（36进制）
    private static final int RADIX = 36;

    private static final int OFFSET_MAGIC = 0;
    private static final int OFFSET_VERSION = 8;
    private static final int OFFSET_TOTAL = 16;
    private static final int OFFSET_EXPIRE = 24;
    private static final int OFFSET_TYPE = 32;


    private static final int MAX_LINE_LENGTH_IN_LICENSE_FILE = 64;

    // 公钥
    private static final byte[] bytes_e = new byte[]{1, 0, 1};

    // 模
    private static final int[] model = {0xc061689c, 0xb7eaa0ea, 0x7b7394b2, 0x0ecd7e13, 0xcefbecf3, 0x4255d5f6, 0x2815a787, 0xfd50978d
            , 0x0aa63de8, 0x436b86a6, 0xca484476, 0x2a651196, 0xac80d4a7, 0xbe676e73, 0x04bff384, 0xc8dc3fd4
            , 0x88699d38, 0x1207f539, 0x47fea2b1, 0x1c729ff8, 0x15e5cfdc, 0x55d9cbfa, 0xc60eed15, 0x26586bff
            , 0x8f046d2e, 0xdb827eb8, 0x724557c2, 0x2d849432, 0x59aa8bff, 0x745dd97a, 0x6494562a, 0xf223b152
            , 0xf7865274, 0xdd9d16b9, 0x1394bfe2, 0x19220f25, 0xd4500fed, 0x8003b395, 0xc02f6917, 0x2e0ea312
            , 0xc1e91446, 0xf73d4b0f, 0xfc0d7005, 0xd6b96fe3, 0x41773e94, 0xd4c6fe97, 0x376a4e3e, 0x496687db
            , 0xf5379ae4, 0xd40e7b0f, 0x9725d2a3, 0x5c6d6942, 0xa92d103b, 0x20eb9741, 0x0ee03a63, 0xd57b5459
            , 0x830da717, 0xeb850345, 0xd415521c, 0xf8123ed6, 0xa1c5fea5, 0x911d1976, 0x37c85f78, 0xef1e5e11
            , 0x01f54eba, 0x02dd5fbf, 0x558c7764, 0x8c097528, 0xa0dbce36, 0xac205c22, 0xeb63702b, 0x370b8032
            , 0xcfca28c3, 0xdc1a590c, 0x85d1a5f4, 0xc0b7ee9c, 0xd30e382a, 0x162d4778, 0xfc7ad6f8, 0xe59b377b
            , 0x735be141, 0x8134d1c6, 0x2a4997df, 0x5280bad4, 0x40b84b41, 0xa3f1d9db, 0xcfc85558, 0x308f962a
            , 0x6e2bf59c, 0x70b74c6b, 0x5a7ebb95, 0x1e67f78b, 0xa0ea94a6, 0x9fd1bbcf, 0x158817b6, 0xfe7c5fdd
            , 0x557e29a7, 0x83e1dce7, 0xe7aaa671, 0x72bdd843, 0x6d085d93, 0x8352930d, 0x7664892f, 0xb4846080
            , 0x8da5c966, 0x7b53ed69, 0x11c5dce2, 0x71aac33f, 0x70f6fef4, 0x53b5ea46, 0x4a6842af, 0x234428cc
            , 0xd496c00d, 0xe0b08800, 0x3a3d6f43, 0x30e20dca, 0xc620d9db, 0x181c776d, 0x23e2e738, 0x5cb78397
            , 0xcb414b1a, 0xdeec7833, 0xa6ff057e, 0xdd0219f5, 0x97244a6f, 0x200fdf0c, 0x0b8be25f, 0x7831965b};



    // license允许使用的内存总量，单位byte
    private long TotalMemory = 0;

    // 当前license过期时间
    private long ExpiredTime = 0;

    // 当前license类型
    private long LicenseType = 0;

    private long LoadTimestamp = 0;

    private long Version;

    private String[] Context;

    private final BigInteger n = new BigInteger(ints2bytes(model));
    private final BigInteger e = new BigInteger(bytes_e);

    private byte[] ints2bytes(int[] n) {
        int len = n.length * 4 + 1;
        byte[] b = new byte[len];
        b[0] = 0;
        for (int i = 0; i < n.length; i++) {
            b[i * 4 + 1] = (byte) (n[i] >> 24);
            b[i * 4 + 2] = (byte) (n[i] >> 16);
            b[i * 4 + 3] = (byte) (n[i] >> 8);
            b[i * 4 + 4] = (byte) (n[i]);
        }
        return b;
    }

    private byte[] string2Bytes(String s) {
        if (s == null) {
            return null;
        }
        byte[] buf = new byte[s.length() >> 1];
        for (int i = 0; i < s.length() - 1; i += 2) {
            char c = s.charAt(i);
            int i1 = 0;
            if (c >= '0' && c <= '9') {
                i1 = c - '0';
            } else if (c >= 'a' && c <= 'f') {
                i1 = c - 'a' + 10;
            } else if (c >= 'A' && c <= 'F') {
                i1 = c - 'A' + 10;
            }

            c = s.charAt(i + 1);
            int i2 = 0;
            if (c >= '0' && c <= '9') {
                i2 = c - '0';
            } else if (c >= 'a' && c <= 'f') {
                i2 = c - 'a' + 10;
            } else if (c >= 'A' && c <= 'F') {
                i2 = c - 'A' + 10;
            }
            buf[i >> 1] = (byte) ((i1 << 4) | i2);
        }
        return buf;
    }

    // 解密用公钥
    private BigInteger decrypt(BigInteger m) {
        return m.modPow(e, n);
    }

    private long getLongFromBytes(byte[] b, int offset) {
        if (b == null || b.length < offset + 8) {
            throw new IllegalArgumentException("bytes is too short");
        }
        long l = 0;
        for (int i = 0; i < 8; i++) {
            l = (l << 8) | b[offset + i] & 0xffl;
        }
        return l;
    }

    /**
     * 读license文件
     *
     * @return 距离license到期时间的毫秒数.-1：永不过期；0：未读到license文件；>0：此时间后过期（最小值为1毫秒）。
     */
    public void loadLicense(String licString) {


        String[] licData=licString.split("\n");

        String lic_signature = null;
        String lic_custinfo = null;
        StringBuffer buf = new StringBuffer(1024);
        // 读license数据
        for(String line: licData) {
            line=line.trim();
            int sign_pos = line.indexOf('=');
            if (sign_pos > 0 && sign_pos < line.length()) {
                // 新的license格式是“key = value”形式
                if (line.startsWith(LIC_SIGNATURE_NAME)) {
                    // 是新格式的签名行
                    lic_signature = line.substring(sign_pos + 1).trim();
                } else if (line.startsWith(LIC_CUSTINFO_NAME)) {
                    // 是新格式的客户信息行
                    lic_custinfo = line.substring(sign_pos + 1).trim();
                }
            } else {
                // 是旧格式
                buf.append(line);
                if (line.length() < MAX_LINE_LENGTH_IN_LICENSE_FILE && lic_signature == null) {
                    // license确保是文件里面的第一段
                    lic_signature = buf.toString();
                    buf.setLength(0);
                }
            }
        }
        if (buf.length() > 0 && lic_custinfo == null) {
            lic_custinfo = buf.toString();
        }

        String[] contexts = null;
        if (lic_custinfo != null && lic_custinfo.length() > 0) {
            try {
                String contextStr = buf.toString();
                // 此处设计是为了预留扩展，将来有其他数据时，多段数据可通过”;“分隔。
                // 保证当前的程序可以和未来的license文件向后兼容
                contexts = lic_custinfo.split(";");
            }
            catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        try {
            long expiredInFile = 0;
            long memoryInFile = 0;

            // 按照36进制转换字符串
            BigInteger data = new BigInteger(lic_signature, RADIX);
            // 解密
            data = decrypt(data);
            // 得到未加密的license数据
            byte[] lic_data = data.toByteArray();
            // 解析license数据
            if (getLongFromBytes(lic_data, OFFSET_MAGIC) == MAGIC_CODE) {
                Version = getLongFromBytes(lic_data, OFFSET_VERSION);

                // 读license文件里的过期时间
                // 文件里存的是到期时间，ret返回的是距离到期时间的毫秒数
                expiredInFile = getLongFromBytes(lic_data, OFFSET_EXPIRE);
                if (Version >= 5 && expiredInFile <= 0) {
                    // 小于0为永久有效
                    expiredInFile = Long.MAX_VALUE;
                }

                // 取license文件中的内存授权数量
                memoryInFile = getLongFromBytes(lic_data, OFFSET_TOTAL);

                LicenseType = getLongFromBytes(lic_data, OFFSET_TYPE);

                boolean isLicenseOk = false;

                // Customer information
                if (Version >= 5) {
                    if (contexts != null && contexts.length > 0
                            && contexts[0] != null && contexts[0].length() > 0) {
                        try {
                            byte[] d = string2Bytes(contexts[0]);
                            String context = new String(d, StandardCharsets.UTF_8);
                            Context = context.split("\n");
                        } catch (Throwable e) {
                            throw new RuntimeException("License::() Parse error: ", e);
//                            ConfigMain.getLicenseInfo().setContext("error context: " + e.getMessage());
                        }
                        isLicenseOk = true;
                    } else {
                        isLicenseOk = true;
                    }

                    if (isLicenseOk) {
                        if (ExpiredTime < expiredInFile) {
                            ExpiredTime = expiredInFile;
                        }
                        if (TotalMemory < memoryInFile) {
                            TotalMemory = memoryInFile;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("License::() Parse error: ", e);
            //logger.debugLog("License::() Parse error: " + e.getMessage());
        }
    }

    public CenterLicenseInfo loadLicenseInfo(String licFile) {
        CenterLicenseInfo info = null;
        loadLicense(licFile);
        if(getContext() != null && getTotalMemory() != 0) {
            info = new CenterLicenseInfo(getLicenseType(), getExpiredTime(), getTotalMemory(), getContext());
        }
        else {
            throw new RuntimeException("License file format error！");
        }
        return info;
    }

    /**
     * 返回当前license的过期时间，正常为大于当前时间的长整数。异常：
     * 0为已经过期；
     * -1为读license文件失败；
     * -2为解密license异常；
     * -3为license数据错误
     *
     * @return
     */
    public long getExpiredTime() {
        return ExpiredTime;
    }

    public String[] getContext() {
        return Context;
    }

    /**
     * 返回license允许的内存使用总量，单位byte
     *
     * @return
     */
    public synchronized long getTotalMemory() {
        return TotalMemory;
    }

    /**
     * 返回license的类型码，大于等于 100 是企业版，小于100是标准版
     * @return
     */
    public long getLicenseType() {
        return LicenseType;
    }



}
