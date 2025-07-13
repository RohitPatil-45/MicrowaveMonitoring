/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.npm.main;

import com.npm.dao.DatabaseHelper;

import com.npm.model.DmbsModel;
import com.npm.model.MicrowaveModel;
import java.sql.Timestamp;
import org.snmp4j.Target;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;

/**
 *
 * @author Kratos
 */
public class MicrowaveMon implements Runnable {

    DmbsModel model = null;
    DatabaseHelper db = new DatabaseHelper();

    public MicrowaveMon(DmbsModel obj) {
        this.model = obj;
    }

    @Override
    public void run() {

        String ber_val = null;
        String rssi_val = null;
        String txp_val = null;
        String txmute_val = null;

        String isAffected = "";
        String problem = "";

        String eventMsg = "";
        String netadmin_msg = "";
        String serviceId = "";

        String deviceIP = model.getDeviceIP();
        System.out.println("Device IP: " + deviceIP);

        String oid_ber = "1.3.6.1.4.1.2509.8.18.2.10.1.12.1"; // bit error ratio  10
        String oid_rssi = "1.3.6.1.4.1.2509.8.18.2.10.1.4.1";  // RSSI   -23
        String oid_txp = "1.3.6.1.4.1.2509.8.22.2.1.1.5.6";   // TX Power  1000
        String oid_txmute = "1.3.6.1.4.1.2509.8.10.2.14";        // TX Mute  1=On  2 =OFF 

        SNMPUtil su = new SNMPUtil();

        try {
            su.start();
            Target target = null;
            if (RequestSNMPMain.isSimulation) {
                target = su.getTarget("udp:127.0.0.1/161", "slot1", SnmpConstants.version2c);

            } else {
                target = su.getTarget("udp:" + deviceIP + "/161", "slot1", SnmpConstants.version2c);

            }

            ber_val = su.BandwidthGetVect(target, "Out", new OID(oid_ber));
            rssi_val = su.BandwidthGetVect(target, "Out", new OID(oid_rssi));
            txp_val = su.BandwidthGetVect(target, "Out", new OID(oid_txp));
            txmute_val = su.BandwidthGetVect(target, "Out", new OID(oid_txmute));

            System.out.println("BER Value     : " + ber_val);
            System.out.println("RSSI Value    : " + rssi_val);
            System.out.println("TX Power      : " + txp_val);
            System.out.println("TX Mute Value : " + txmute_val);

            MicrowaveModel microwave = new MicrowaveModel();
            microwave.setDeviceIP(deviceIP);
            microwave.setDeviceName(model.getDeviceName());
            microwave.setBitErrorRatio(ber_val);
            microwave.setRssi(rssi_val);
            microwave.setTx_mute(txmute_val);
            microwave.setTxp(txp_val);
            microwave.setEventTime(new Timestamp(System.currentTimeMillis()));

            MicrowaveMonitoring.updateList.add(microwave);
            MicrowaveMonitoring.updatelogList.add(microwave);

            String Old_txmute_val = MicrowaveMonitoring.txMuteMap.get(deviceIP).toString();
            System.out.println("Tx mute:"+deviceIP+":"+Old_txmute_val);
            if (!Old_txmute_val.equalsIgnoreCase(txmute_val)) {

                eventMsg = txmute_val.equalsIgnoreCase("2") ? "Microwave : " + model.getDeviceName() + " is Off" : "Microwave : " + model.getDeviceName() + " is On";
                isAffected = txmute_val.equalsIgnoreCase("2") ? "1" : "0";
                problem = txmute_val.equalsIgnoreCase("2") ? "problem" : "Cleared";
                netadmin_msg = eventMsg;
                serviceId = "tx_mute";
                MicrowaveMonitoring.txMuteMap.put(deviceIP, txmute_val);
                db.insertIntoEventLog(deviceIP, model.getDeviceName(), eventMsg, 4, "Tx Mute", new Timestamp(System.currentTimeMillis()), netadmin_msg, isAffected, problem, serviceId, model.getDeviceType());
            }

            checkRssiThreshold(Double.valueOf(rssi_val), deviceIP, model.getDeviceName());
            checkTxPowerThreshold(Double.valueOf(txp_val), deviceIP, model.getDeviceName());

        } catch (Exception e) {
            System.out.println("Exception while fetching SNMP values: " + e);
        } finally {
            try {
                su.stop();
            } catch (Exception ex2) {
                System.out.println("SNMP Close Exception: " + ex2);
            }
        }

    }

    public void checkRssiThreshold(double actual_value, String deviceID, String deviceName) {
        Timestamp logDateTime = new Timestamp(System.currentTimeMillis());
        int threshold = MicrowaveMonitoring.rssiThresholdParam;
        String isAffected = "";
        String problem = "";
        String serviceId = "microwave_rssi";
        String eventMsg = null;
        String netadmin_msg = null;
        try {
            String h_latencystatus = MicrowaveMonitoring.rssiThresholdMap.get(deviceID).toString();

            if (actual_value > threshold && h_latencystatus.equalsIgnoreCase("Low")) {
                System.out.println("Microwave rssi : High : " + actual_value + " / rssi threshold value = " + threshold + " / rssi status = " + "High" + " ip = " + deviceID);
                eventMsg = "Microwave rssi Threshold:High" + actual_value + " rssi threshold value=" + threshold + " rssi status=" + "High" + " Device Name=" + deviceName;
                netadmin_msg = "Microwave rssi Threshold : High : " + actual_value + " / rssi threshold value = " + threshold + " / rssi status = " + "High" + " / Device Name = " + deviceName;
                MicrowaveMonitoring.rssiThresholdMap.put(deviceID, "High");
                //DatabaseHelper db = new DatabaseHelper();

                isAffected = "1";
                problem = "problem";

                db.rssiThresholdLog(deviceID, deviceName, threshold, actual_value, "High", logDateTime); // Threshold Log and
                db.insertIntoEventLog(deviceID, deviceName, eventMsg, 4, "RSSI", logDateTime, netadmin_msg, isAffected, problem, serviceId, model.getDeviceType()); //Evrnt log

            } else if (actual_value < threshold && h_latencystatus.equalsIgnoreCase("High")) {
                System.out.println("Latency Threshold:Low" + actual_value + " latency threshold value=" + threshold + " latency status=" + "Low" + " ip=" + deviceName);
                MicrowaveMonitoring.rssiThresholdMap.put(deviceID, "Low");
                // DatabaseHelper db = new DatabaseHelper();
                isAffected = "0";
                problem = "Cleared";

                eventMsg = "Microwave rssi Threshold:Low" + actual_value + " rssi threshold value=" + threshold + " rssi status=" + "Low" + " Device Name=" + deviceName;
                netadmin_msg = "Microwave rssi Threshold : Low : " + actual_value + " / rssi threshold value = " + threshold + " / rssi status = " + "Low" + " / Device Name = " + deviceName;

                db.rssiThresholdLog(deviceID, deviceName, threshold, actual_value, "Low", logDateTime);

                db.insertIntoEventLog(deviceID, deviceName, eventMsg, 4, "RSSI", logDateTime, netadmin_msg, isAffected, problem, serviceId, model.getDeviceType()); //Evrnt log
            }
        } catch (Exception e4) {
            e4.printStackTrace();
            System.out.println(" microwave rssi threshold exception:" + e4);
        }
    }

    public void checkTxPowerThreshold(double actual_value, String deviceID, String deviceName) {
        Timestamp logDateTime = new Timestamp(System.currentTimeMillis());
        int threshold = MicrowaveMonitoring.txPowerThresholdParam;
        String isAffected = "";
        String problem = "";
        String serviceId = "microwave_txp";
        String eventMsg = null;
        String netadmin_msg = null;
        try {
            String h_latencystatus = MicrowaveMonitoring.txPowerThresholdMap.get(deviceID).toString();

            if (actual_value > threshold && h_latencystatus.equalsIgnoreCase("Low")) {
                System.out.println("Tx Power :High" + actual_value + " Tx Power value=" + threshold + " Tx Power status=" + "High" + " ip=" + deviceID);
                eventMsg = "Tx Power Threshold:High" + actual_value + " Tx Power threshold value=" + threshold + " Tx Power status=" + "High" + " Device Name=" + deviceName;
                netadmin_msg = "Tx Power Threshold : High : " + actual_value + " / Tx Power threshold value = " + threshold + " / Tx Power status = " + "High" + " / Device Name=" + deviceName;
                MicrowaveMonitoring.txPowerThresholdMap.put(deviceID, "High");
                //   DatabaseHelper db = new DatabaseHelper();
                isAffected = "1";
                problem = "problem";
                db.txPowerThresholdLog(deviceID, deviceName, threshold, actual_value, "High", logDateTime); // Threshold Log and
                db.insertIntoEventLog(deviceID, deviceName, eventMsg, 4, "TXP", logDateTime, netadmin_msg, isAffected, problem, serviceId, model.getDeviceType()); //Evrnt log

            } else if (actual_value < threshold && h_latencystatus.equalsIgnoreCase("High")) {
                System.out.println("microwave tx Threshold:Low" + actual_value + " microwave tx  threshold value=" + threshold + " microwave tx  status=" + "Low" + " ip=" + deviceName);
                MicrowaveMonitoring.txPowerThresholdMap.put(deviceID, "Low");
                // DatabaseHelper db = new DatabaseHelper();
                isAffected = "0";
                problem = "Cleared";
                eventMsg = "Tx Power Threshold:Low" + actual_value + " Tx Power threshold value=" + threshold + " Tx Power status=" + "Low" + " Device Name=" + deviceName;
                netadmin_msg = "Tx Power Threshold : Low : " + actual_value + " / Tx Power threshold value = " + threshold + " / Tx Power status = " + "Low" + " / Device Name=" + deviceName;
                db.txPowerThresholdLog(deviceID, deviceName, threshold, actual_value, "Low", logDateTime);

                db.insertIntoEventLog(deviceID, deviceName, eventMsg, 4, "TXP", logDateTime, netadmin_msg, isAffected, problem, serviceId, model.getDeviceType()); //Evrnt log
            }
        } catch (Exception e4) {
            System.out.println(" microwave tx threshold exception:" + e4);
        }
    }

}
