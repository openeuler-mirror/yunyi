package com.tongtech.console.enums;

public enum NodeSecureEnum {
    /**
     * Telnet none password
     */
    ANONYMOUS(0),

    /**
     * SSL none password
     */
    SSL(1),

    /**
     * Telnet and password
     */
    PASSWORD(2),

    /**
     * SSL and password
     */
    SSL_PASSWORD(3);




    private int secure;

    NodeSecureEnum(int secure) {
        this.secure = secure;
    }

    public Integer getSecure() {
        return secure;
    }

    public static NodeSecureEnum parse(int secure) {
        switch(secure) {
            case 0:
                return ANONYMOUS;
            case 1:
                return SSL;
            case 2:
                return PASSWORD;
            case 3:
                return SSL_PASSWORD;
            default:
                return ANONYMOUS;
        }
    }
}
