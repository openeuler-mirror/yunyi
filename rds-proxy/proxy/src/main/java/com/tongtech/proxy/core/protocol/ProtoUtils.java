/**
 *
 */
package com.tongtech.proxy.core.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * @author liubing
 *
 */
public class ProtoUtils {

    private final static long[] sizeTable = {100, 10000, 1000000, 100000000, 10000000000l,
            1000000000000l, 100000000000000l, 10000000000000000l, 1000000000000000000l,
            Long.MAX_VALUE};

    private final static short[] DigitHundreds = {0x3030, 0x3031, 0x3032, 0x3033,
            0x3034, 0x3035, 0x3036, 0x3037, 0x3038, 0x3039, 0x3130, 0x3131, 0x3132,
            0x3133, 0x3134, 0x3135, 0x3136, 0x3137, 0x3138, 0x3139, 0x3230, 0x3231,
            0x3232, 0x3233, 0x3234, 0x3235, 0x3236, 0x3237, 0x3238, 0x3239, 0x3330,
            0x3331, 0x3332, 0x3333, 0x3334, 0x3335, 0x3336, 0x3337, 0x3338, 0x3339,
            0x3430, 0x3431, 0x3432, 0x3433, 0x3434, 0x3435, 0x3436, 0x3437, 0x3438,
            0x3439, 0x3530, 0x3531, 0x3532, 0x3533, 0x3534, 0x3535, 0x3536, 0x3537,
            0x3538, 0x3539, 0x3630, 0x3631, 0x3632, 0x3633, 0x3634, 0x3635, 0x3636,
            0x3637, 0x3638, 0x3639, 0x3730, 0x3731, 0x3732, 0x3733, 0x3734, 0x3735,
            0x3736, 0x3737, 0x3738, 0x3739, 0x3830, 0x3831, 0x3832, 0x3833, 0x3834,
            0x3835, 0x3836, 0x3837, 0x3838, 0x3839, 0x3930, 0x3931, 0x3932, 0x3933,
            0x3934, 0x3935, 0x3936, 0x3937, 0x3938, 0x3939};

//    private final static byte[] DigitOnes = {'0', '1', '2', '3', '4', '5',
//            '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8',
//            '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1',
//            '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4',
//            '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7',
//            '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
//            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3',
//            '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6',
//            '7', '8', '9',};

    private final static byte[] DigitOnes = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    /**
     * 将一个长整数的值写到ByteBuf对象中，过程中不新创建对象。
     * 该方法未用到成员变量，线程安全
     *
     * @param buf
     * @param value
     */
    public static void writeLongToByteBuf(ByteBuf buf, long value) {
        if (value < 0) {
            buf.writeByte('-');
            value = -value;
        }
        int size = 0;
        if (value == Long.MAX_VALUE) {
            size = sizeTable.length - 1;
        } else {
            while (value >= sizeTable[size]) {
                ++size;
            }
        }
        long q, r, k;
        boolean first = true;
        if (sizeTable[size] == Long.MAX_VALUE) {
            size -= 1;
            q = value / sizeTable[size];
            r = q * sizeTable[size];
            value -= r;
            first = false;
        }
        for (int i = size; i >= 0; --i) {
            if (i > 0) {
                k = sizeTable[i - 1];
                q = value / k;
                r = q * k;
                value -= r;
            } else {
                q = value;
            }
            if (q >= 10 || !first) {
                buf.writeShort(DigitHundreds[(int) q]);
            } else {
                buf.writeByte(DigitOnes[(int) q]);
            }
            first = false;
        }
    }

    public static void main(String[] args) {
        ByteBuf buf = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 32, 100);
        buf.clear();
        writeLongToByteBuf(buf, 1);
        System.out.println(buf.toString(StandardCharsets.UTF_8));
        buf.clear();
        writeLongToByteBuf(buf, 12);
        System.out.println(buf.toString(StandardCharsets.UTF_8));
        buf.clear();
        writeLongToByteBuf(buf, 103);
        System.out.println(buf.toString(StandardCharsets.UTF_8));
        buf.clear();
        writeLongToByteBuf(buf, 12345);
        System.out.println(buf.toString(StandardCharsets.UTF_8));
        buf.clear();
        writeLongToByteBuf(buf, -1);
        System.out.println(buf.toString(StandardCharsets.UTF_8));
        buf.clear();
        writeLongToByteBuf(buf, -1123);
        System.out.println(buf.toString(StandardCharsets.UTF_8));
        buf.clear();
        writeLongToByteBuf(buf, -1234567890);
        System.out.println(buf.toString(StandardCharsets.UTF_8));
        buf.clear();
        writeLongToByteBuf(buf, Long.MAX_VALUE - 12345678);
        System.out.println(buf.toString(StandardCharsets.UTF_8));
        buf.clear();
        writeLongToByteBuf(buf, Long.MAX_VALUE);
        System.out.println(buf.toString(StandardCharsets.UTF_8));
        buf.clear();
        writeLongToByteBuf(buf, -Long.MAX_VALUE);
        System.out.println(buf.toString(StandardCharsets.UTF_8));


//        for (long l = 0; l < 100000000000l; ++l) {
//            buf.clear();
//            writeLongToByteBuf(buf, l);
//            long l2 = Long.parseLong(buf.toString(StandardCharsets.UTF_8));
//            if (l != l2) {
//                System.out.println(l + " != " + l2);
//                System.exit(0);
//            }
//        }
//        for (int i = 0; i < 10; ++i) {
//            for (int j = 0; j < 10; ++j) {
//                System.out.print(String.format("0x%02x%02x, ", i + '0', j + '0'));
//            }
//        }
    }
}
