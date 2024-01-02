package com.tongtech.proxy.core.center;

import com.tongtech.proxy.core.utils.OriginalSocketAddress;

import java.util.ArrayList;

public class CenterConfig {
    private final ArrayList<OriginalSocketAddress> Addresses = new ArrayList<>();
    private volatile String Password = null;

    public ArrayList<OriginalSocketAddress> getAddresses() {
        return Addresses;
    }

    public void setAddresses(ArrayList<OriginalSocketAddress> addresses) {
        Addresses.clear();
        if (addresses != null) {
            Addresses.addAll(addresses);
        }
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String connPassword) {
        Password = connPassword;
    }
}
