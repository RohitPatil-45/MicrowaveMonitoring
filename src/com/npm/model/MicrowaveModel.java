/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.npm.model;

import java.sql.Timestamp;

/**
 *
 * @author Kratos
 */
public class MicrowaveModel {
    
    private String deviceIP;
    
    private String deviceName;
    
    private String bitErrorRatio;
    
    private String rssi;
    
    private String txp;
    
    private String tx_mute;
    
    private Timestamp eventTime;

    public String getDeviceIP() {
        return deviceIP;
    }

    public void setDeviceIP(String deviceIP) {
        this.deviceIP = deviceIP;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getBitErrorRatio() {
        return bitErrorRatio;
    }

    public void setBitErrorRatio(String bitErrorRatio) {
        this.bitErrorRatio = bitErrorRatio;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public String getTxp() {
        return txp;
    }

    public void setTxp(String txp) {
        this.txp = txp;
    }

    public String getTx_mute() {
        return tx_mute;
    }

    public void setTx_mute(String tx_mute) {
        this.tx_mute = tx_mute;
    }

    public Timestamp getEventTime() {
        return eventTime;
    }

    public void setEventTime(Timestamp eventTime) {
        this.eventTime = eventTime;
    }
    
    
    
}
