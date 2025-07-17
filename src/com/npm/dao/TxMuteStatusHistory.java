/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.npm.dao;

import com.npm.datasource.Datasource;
import com.npm.main.MicrowaveMonitoring;
import com.npm.model.MicrowaveTxMuteStatusHistory;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 *
 * @author Kratos
 */
public class TxMuteStatusHistory implements Runnable {

    String insertQuery = null;

    @Override
    public void run() {
        System.out.println("Start insert in TxMuteStatusHistory");
        int count = 0;
        insertQuery = "insert into microwave_tx_mute_status_history (DEVICE_IP, TX_MUTE_STATUS, EVENT_TIMESTAMP) VALUES (?,?,?)";

        try {
            MicrowaveMonitoring.txMuteStatusHistoryTemp.clear();
            MicrowaveMonitoring.txMuteStatusHistoryTemp.addAll(MicrowaveMonitoring.txMuteStatusHistory);
            MicrowaveMonitoring.txMuteStatusHistory.clear();

        } catch (Exception e) {
            System.out.println("Exception in batch update=" + e);
        }

        if (MicrowaveMonitoring.txMuteStatusHistoryTemp.isEmpty()) {
            System.out.println("No data to update.");
            return;
        }

        try (Connection connection = Datasource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            connection.setAutoCommit(false);

            for (MicrowaveTxMuteStatusHistory log : MicrowaveMonitoring.txMuteStatusHistoryTemp) {
                try {
//                    if (existingDeviceIPs.contains(trapLog.getDeviceIP())) {
                    preparedStatement.setString(1, log.getDeviceIP());
                    preparedStatement.setString(2, log.getTx_mute_status());
                    preparedStatement.setTimestamp(3, log.getEventTimestamp());

                    preparedStatement.addBatch();

                    if (++count % 1 == 0) {
                        System.out.println("inside update batch");
                        preparedStatement.executeBatch();
//                        insertStmt.executeBatch();
                        preparedStatement.clearBatch();
//                        insertStmt.clearBatch();

                    }

                } catch (Exception e) {
                    System.out.println("update error: " + e);
                }
            }

            preparedStatement.executeBatch();
//            insertStmt.executeBatch();
            connection.commit();
            System.out.println("update " + count + " TxMuteStatusHistory records.");

        } catch (Exception exp) {
            System.out.println("DB Exception: " + exp);
        }

    }

}
