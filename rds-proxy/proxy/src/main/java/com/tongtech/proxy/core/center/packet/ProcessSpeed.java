package com.tongtech.proxy.core.center.packet;

import java.util.ArrayList;
import java.util.Vector;

import static com.tongtech.proxy.core.center.ProxyData.OBJECTTYPE_PROCESSSPEED;

public class ProcessSpeed {
    private static final int TYPE = OBJECTTYPE_PROCESSSPEED;

    // 计算HISTORY次采样的平均值
    private static final int HISTORYS = 60;
    private float[] History = new float[HISTORYS];
    private int HisOffset = 0;

    // 最后一次更新的实际值
    private float Current = 0;

    // 最近HISTORYS次的总量
    private float Times_60 = 0;

    // 最近10次的总量
    private float Times_10 = 0;

    // 最近1小时的处理量
    private int LastHour = 0;

    private int CurrentHour = 0;
    private long DurationHour = 0;


    public ProcessSpeed() {
        for (int i = 0; i < HISTORYS; i++) {
            History[i] = 0;
        }
    }

    public synchronized void serialize(ArrayList data) {
        int head_offset = data.size();
        //type
        data.add(TYPE);
        // length
        data.add(null);
        int f2i = 0;
        // Current
        f2i = Float.floatToIntBits(Current);
        data.add(f2i);
        // Times_10
        f2i = Float.floatToIntBits(Times_10);
        data.add(f2i);
        // Times_60
        f2i = Float.floatToIntBits(Times_60);
        data.add(f2i);
        // LastHour
        data.add(LastHour);

        int len = data.size() - head_offset;
        data.set(head_offset + 1, len);
    }

    public synchronized void parse(Vector<Object> data, int offset) {
        if (data == null || (Long) data.get(offset++) != TYPE) {
            return;
        }

        int length = ((Long) data.get(offset++)).intValue();

        int i2f = 0;
        i2f = ((Long) data.get(offset++)).intValue();
        Current = Float.intBitsToFloat(i2f);

        i2f = ((Long) data.get(offset++)).intValue();
        Times_10 = Float.intBitsToFloat(i2f);

        i2f = ((Long) data.get(offset++)).intValue();
        Times_60 = Float.intBitsToFloat(i2f);

        i2f = ((Long) data.get(offset++)).intValue();
        LastHour = i2f;

    }

    synchronized String toJson() {
        StringBuilder buf = new StringBuilder(128);
        buf.append("{\"current\":").append(String.format("%.0f", Current));
        buf.append(",\"average10\":").append(String.format("%.1f", (Times_10 / 10)));
        buf.append(",\"average60\":").append(String.format("%.1f", (Times_60 / 60)));
        buf.append(",\"lasthour\":").append(LastHour);
        buf.append("}");
        return buf.toString();
    }

    public synchronized void setData(int times, long duration) {
        Current = (times * 1000.0f) / duration;

        int last_10 = HisOffset - 10;
        if (last_10 < 0) {
            last_10 += HISTORYS;
        }
        Times_10 -= History[last_10];
        Times_10 += Current;

        Times_60 -= History[HisOffset];
        Times_60 += Current;

        History[HisOffset++] = Current;
        if (HisOffset == HISTORYS) {
            HisOffset = 0;
        }

        CurrentHour += times;
        DurationHour += duration;
        if (DurationHour >= 3600000) {// 每小时计算1次
            LastHour = CurrentHour;
            CurrentHour = 0;
            DurationHour = 0;
        }
    }

    public synchronized float getCurrent() {
        return Current;
    }

    public synchronized float getTimes_60() {
        return Times_60;
    }

    public synchronized float getTimes_10() {
        return Times_10;
    }

    public synchronized int getLastHour() {
        return LastHour;
    }

    public synchronized String toString() {
        return String.format("ProcessSpeed::() Package process in a second: %.0f/%.1f/%.1f k/s",
                Current, Times_10 / 10, Times_60 / HISTORYS);
    }
}
