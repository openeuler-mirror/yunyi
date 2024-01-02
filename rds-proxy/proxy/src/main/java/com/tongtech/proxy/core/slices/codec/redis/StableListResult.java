package com.tongtech.proxy.core.slices.codec.redis;

import java.util.Vector;

public class StableListResult extends Vector  {
    private final Object parent;
    private final int size;

    public StableListResult(Object parent, int size) {
        this.parent = parent;
        this.size = size;
    }

    @Override
    public boolean add(Object d) {
        if (size > size()) {
            return super.add(d);
        }
        throw new RedisProtocolError("too many items");
    }

    public byte[] getData() {
        return new byte[0];
    }

    public boolean isParseOK() {
        return size <= size();
    }

    public Object getParent() {
        return this.parent;
    }
}
