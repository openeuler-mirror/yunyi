package com.tongtech.proxy;

public class SM3 {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("\nUsage: SM3Encrypt text\n");
        } else {
            System.out.println("\nPlain text: " + args[0]);
            System.out.println("Encrypted: " + com.tongtech.proxy.core.crypto.SM3.hash(args[0]));
            System.out.println("");
        }
    }
}
