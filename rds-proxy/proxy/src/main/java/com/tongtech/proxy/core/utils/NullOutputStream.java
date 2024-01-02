package com.tongtech.proxy.core.utils;

import java.io.IOException;
import java.io.OutputStream;

public class NullOutputStream extends OutputStream {
    @Override
    public void write(int b) throws IOException {
    }

    @Override
    public void write(byte[] b) throws IOException {
    }

    @Override
    public void write(byte[] b, int offset, int len) throws IOException {
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }
}
