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
public class MicrowaveTxMuteStatusHistory {
    
    private String deviceIP;
    
    private String tx_mute_status;
    
    private Timestamp eventTimestamp;

    public String getDeviceIP() {
        return deviceIP;
    }

    public void setDeviceIP(String deviceIP) {
        this.deviceIP = deviceIP;
    }

    public String getTx_mute_status() {
        return tx_mute_status;
    }

    public void setTx_mute_status(String tx_mute_status) {
        this.tx_mute_status = tx_mute_status;
    }

    public Timestamp getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(Timestamp eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }
    
    
    
}
