// IDeviceidInterface.aidl
package com.zui.deviceidservice;

interface IDeviceidInterface {
    String getOAID();
     String getUDID();
    boolean isSupport();
}
