// IDeviceidInterface.aidl
package com.zui.deviceidservice;

interface IDeviceidInterface {
    String getOAID();
    String getUDID();
    boolean isSupport();
    String getVAID(String strpackage);
    String getAAID(String strpackage);
    boolean createAAIDForPackageName(String strpackage);
}
