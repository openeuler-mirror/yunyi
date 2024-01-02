package com.tongtech.proxy.core.rescue;

import java.io.IOException;
import java.io.Reader;

public class RescureReader {
    private final Reader is;

    public RescureReader(Reader is) {
        this.is = is;
    }

    public String readLine() throws IOException {
        StringBuilder buf = new StringBuilder();
        int c;
        while ((c = this.is.read()) >= 0) {
            if (c == '\n') {
                break;
            } else {
                buf.append((char) c);
            }
        }
        if (buf.length() > 0) {
            return buf.toString();
        } else {
            return "";
        }
    }
}
