package com.tongtech.proxy.core.protocol;

public final class StatusString implements StatusMessage {
    private final String message;

    public StatusString(String msg) {
        this.message = msg;
    }

    @Override
    public int hashCode() {
        return this.message != null ? this.message.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof StatusString) {
            String other = ((StatusString) o).message;

            if (this.message == other) {
                return true;
            } else if (this.message == null && other == null) {
                return true;
            } else if ((this.message == null && other != null) || (this.message != null && other == null)) {
                return false;
            } else {
                return this.message.equals(other);
            }
        }
        return false;
    }

    public String toString() {
        return this.message;
    }
}
