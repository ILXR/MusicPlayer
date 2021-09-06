package com.epic.localmusicnoserver.entity;

public class DeviceInfo {
    private String name;
    private String macAddress;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }



    public DeviceInfo(String name,String macAddress){
        this.name = name;
        this.macAddress = macAddress;
    }
}
