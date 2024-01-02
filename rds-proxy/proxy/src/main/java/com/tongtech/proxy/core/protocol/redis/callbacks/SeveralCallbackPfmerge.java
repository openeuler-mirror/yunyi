package com.tongtech.proxy.core.protocol.redis.callbacks;

import com.tongtech.proxy.core.protocol.DataResult;
import com.tongtech.proxy.core.server.ProxyController;
import com.tongtech.proxy.core.slices.ServiceMapping;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class SeveralCallbackPfmerge extends SeveralCallback {
    private static final int HLL_REGISTERS = 1 << 14;
    private final static int REGISTER_SIZE = 6;     //每个register占5位,代码里有一些细节涉及到这个5位，所以仅仅改这个参数是会报错的
    private final static int REGISTER_MASK = (1 << REGISTER_SIZE) - 1;
    private final static int REGISTER_HEAD = 16;
    private final static int HEAD_CARDIN_START = 8;
    private final Vector<byte[]> Datas = new Vector<>();
    private final byte[] DescKey;

    public SeveralCallbackPfmerge(DataResult result, List request) throws IOException {
        super(result, request, "get", false, 0);
        DescKey = request.size() > 1 ? (byte[]) request.get(1) : null;
    }

    @Override
    protected void process(Object o) {
        if (o == null && Datas.size() == 0) {
            Datas.add(newRegisterSet());
        } else if (o instanceof byte[]) {
            byte[] data = (byte[]) o;
            if (isDense(data)) {
                Datas.add(data);
            }
        }
    }

    @Override
    protected void result(DataResult result) throws IOException {
        if (Datas.size() == 0 || DescKey == null) {
            result.setErr(-9, "ERR wrong number of arguments for 'pfmerge' command");
        } else {
            if (Datas.size() > 1) {
                merge(Datas);
            }
            ServiceMapping manager = ProxyController.INSTANCE.getNodeManager(DescKey);
            Vector cmd = new Vector();
            cmd.add("set");
            cmd.add(DescKey);
            cmd.add(Datas.get(0));
            manager.exchangeRedisData(NullCallback.INSTANCE, cmd, result.getTableId());
            result.setOk();
        }
    }

    private static byte[] merge(List<byte[]> data) {
        if (data == null) {
            return null;
        } else if (data.size() == 1) {
            return data.get(0);
        }

        byte[] ret = data.get(0);
        for (int bucket = 0; bucket < HLL_REGISTERS; ++bucket) {
            int value = 0;
            for (int i = 0; i < data.size(); ++i) {
                int v = get(bucket, data.get(i));
                if (value < v) {
                    value = v;
                }
            }
            if (value > 0) {
                set(bucket, value, ret);
            }
        }
        ret[HEAD_CARDIN_START + 7] = -1;

        return ret;
    }

    private static int get(int position, byte[] values) {
        int b0 = REGISTER_SIZE * position / 8;
        int fb = REGISTER_SIZE * position % 8;
        int db = values[REGISTER_HEAD + b0] & 0xff;
        int b = (db >>> fb) & REGISTER_MASK;
        if (fb > 8 - REGISTER_SIZE) {
            db = values[REGISTER_HEAD + b0 + 1] & 0xff;
            b |= (db << (8 - fb)) & REGISTER_MASK;
        }
        return b;
    }

    private static boolean isDense(byte[] data) {
        if (data != null && data.length == getSizeForCount(HLL_REGISTERS)
                && data[0] == 'H' && data[1] == 'Y' && data[2] == 'L' && data[3] == 'L' && data[4] == 0) {
            return true;
        }
        return false;
    }

    private static void set(int position, int value, byte[] values) {
        int b0 = REGISTER_SIZE * position / 8;
        int fb = REGISTER_SIZE * position % 8;
        int b0_mask = ((1 << (8 - fb)) - 1) << fb;
        values[REGISTER_HEAD + b0] = (byte) ((values[REGISTER_HEAD + b0] & ~b0_mask) | ((value << fb) & b0_mask));
        if (fb > 8 - REGISTER_SIZE) {
            int shit = fb - 8 + REGISTER_SIZE;
            int b1_mask = (1 << shit) - 1;
            values[REGISTER_HEAD + b0 + 1] = (byte) ((values[REGISTER_HEAD + b0 + 1] & ~b1_mask) | ((value >>> REGISTER_SIZE - shit) & b1_mask));
        }
    }

    private static byte[] newRegisterSet() {
        byte[] data = new byte[getSizeForCount(HLL_REGISTERS)];
        data[0] = 'H';
        data[1] = 'Y';
        data[2] = 'L';
        data[3] = 'L';
        data[4] = 0;

        return data;
    }

    private static int getSizeForCount(int count) {
        int bits = getBytes(count);
        int ret;
        if (bits == 0) {
            ret = 1;
        } else if (((count * REGISTER_SIZE) % 8) == 0) {
            ret = bits;
        } else {
            ret = bits + 1;
        }
        return ret + REGISTER_HEAD;
    }

    private static int getBytes(int count) {
        return count * REGISTER_SIZE / 8;
    }

}
