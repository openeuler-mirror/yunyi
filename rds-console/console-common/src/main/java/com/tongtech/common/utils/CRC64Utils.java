package com.tongtech.common.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CRC64Utils {

    private static final long POLY32REV = 0xEDB88320;
    private static final long POLY64REV = 0x95AC9329AC4BC9B5L;
    private static long[] Table = new long[256];
    private static final String crcFileSuffix = ".crc";

    static {
        for (int i = 0; i < 256; i++) {
            long r = i;
            for (int j = 0; j < 8; j++)
                if ((r & 1) != 0)
                    r = (r >>> 1) ^ POLY64REV;
                else
                    r >>>= 1;
            Table[i] = r;
        }
    }

    public static String generateFileCRC(File file) throws IOException {
        long nResult = 0;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] buff = new byte[1024];
            CRC64Utils crc64 = new CRC64Utils();
            crc64.Init();
            while (true) {
                int nRead = fis.read(buff);
                if (nRead > 0)
                    crc64.Update(buff, 0, nRead);
                if (nRead < 1024)
                    break;
            }
            nResult = crc64.GetDigest();
        } finally {
            try {
                fis.close();
            } catch (Throwable t2) {
            }
        }
        return Long.toHexString(nResult);
    }

    /**
     * 在对应文件夹生成一个同名.crc文件。
     * @param file 源文件
     * @return  生成的CRC文件路径
     * @throws IOException
     */
    public static File saveFileCRC(File file) throws IOException {
        String content = generateFileCRC(file);
        File crcFile = new File(buildCRCFileUrl(file.getAbsolutePath() ));
        FileUtils.writeByteArrayToFile(crcFile, content.getBytes(StandardCharsets.UTF_8));

        return crcFile;
    }

    public static String readFileCRC(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    private static String buildCRCFileUrl(String filePath) {
        return filePath + crcFileSuffix;
    }

    private long _value = -1;

    public void Init() {
        _value = -1;
    }

    public void Update(byte[] data) {
        Update(data, 0, data.length);
    }

    public void Update(byte[] data, int offset, int size) {
        for (int i = 0; i < size; i++) {
            UpdateByte(data[offset + i]);
        }
    }

    public void UpdateByte(int b) {
        _value = Table[((int) _value ^ b) & 0xFF] ^ (_value >>> 8);
    }

    public long GetDigest() {
        return _value ^ (-1);
    }
}
