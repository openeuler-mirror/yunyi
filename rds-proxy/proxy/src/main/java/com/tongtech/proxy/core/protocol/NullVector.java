package com.tongtech.proxy.core.protocol;

import java.util.Collection;
import java.util.Vector;

public class NullVector extends Vector {
    public NullVector() {
        super(0);
    }

    @Override
    public boolean add(Object o) {
        return false;
    }

    @Override
    public void add(int i, Object o) {
    }

    @Override
    public boolean addAll(Collection o) {
        return false;
    }

    @Override
    public boolean addAll(int i, Collection o) {
        return false;
    }

    @Override
    public void addElement(Object o) {
    }

    @Override
    public Object set(int i, Object o) {
        return Boolean.FALSE;
    }

    @Override
    public void setSize(int i) {
    }

    @Override
    public Object remove(int i) {
        return null;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }
}
