/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.npm.dao;

import com.npm.datasource.Datasource;
import com.npm.main.MicrowaveMonitoring;
import com.npm.model.MicrowaveModel;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 *
 * @author Kratos
 */
public class updateMicrowaveMonitoring implements Runnable {

    String updateQuery = null;

    @Override
    public void run() {
        System.out.println("Start update in microwave_monitoring");
        int count = 0;
        updateQuery = "update microwave_monitoring set BIT_ERROR_RATIO=?,RSSI=?, TXP=?, TX_MUTE=?, EVENT_TIMESTAMP=? where DEVICE_IP=?";
//        selectQuery = "select DEVICE_IP from snmp_trap_live_status";
//        insertQuery = "insert into snmp_trap_live_status (DEVICE_IP,DEVICE_NAME,SERVICE_NAME,SEVERITY,ALARM_STATUS,TRAP_VALUE,NODE_UPTIME) values(?,?,?,?,?,?,?)";
//        Set<String> existingDeviceIPs = new HashSet<>();

        try {
            MicrowaveMonitoring.updateListTemp.clear();
            MicrowaveMonitoring.updateListTemp.addAll(MicrowaveMonitoring.updateList);
            MicrowaveMonitoring.updateList.clear();

        } catch (Exception e) {
            System.out.println("Exception in batch update=" + e);
        }

        if (MicrowaveMonitoring.updateListTemp.isEmpty()) {
            System.out.println("No data to update.");
            return;
        }

        try (Connection connection = Datasource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

            connection.setAutoCommit(false);

            for (MicrowaveModel log : MicrowaveMonitoring.updateListTemp) {
                try {
//                    if (existingDeviceIPs.contains(trapLog.getDeviceIP())) {
                    preparedStatement.setString(1, log.getBitErrorRatio());
                    preparedStatement.setString(2, log.getRssi());
                    preparedStatement.setString(3, log.getTxp());
                    preparedStatement.setString(4, log.getTx_mute());
                    preparedStatement.setTimestamp(5, log.getEventTime());
                    preparedStatement.setString(6, log.getDeviceIP());
                    preparedStatement.addBatch();
//                    } else {
//                        System.out.println("inside insert");
//                        insertStmt.setString(1, trapLog.getDeviceIP());
//                        insertStmt.setString(2, trapLog.getDeviceName());
//                        insertStmt.setString(3, trapLog.getServiceName());
//                        insertStmt.setString(4, trapLog.getSeverity());
//                        insertStmt.setString(5, trapLog.getAlarmStatus());
//                        insertStmt.setString(6, trapLog.getTrapValue());
//                        insertStmt.setString(7, trapLog.getNodeUptime());
//                        insertStmt.addBatch();
//                    }
                    if (++count % 1 == 0) {
                        System.out.println("inside update batch");
                        preparedStatement.executeBatch();
//                        insertStmt.executeBatch();
                        preparedStatement.clearBatch();
//                        insertStmt.clearBatch();

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("update error: " + e);
                }
            }

            preparedStatement.executeBatch();
//            insertStmt.executeBatch();
            connection.commit();
            System.out.println("update " + count + " microwave_monitoring records.");

        } catch (Exception exp) {
            System.out.println("DB Exception: " + exp);
        }

    }

}
