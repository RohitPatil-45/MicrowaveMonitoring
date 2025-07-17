/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.npm.dao;

import com.npm.datasource.Datasource;
import com.npm.main.MicrowaveMonitoring;
import com.npm.model.DmbsModel;
import com.npm.model.MicrowaveTxMuteStatusHistory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;

/**
 *
 * @author NPM
 */
public class DatabaseHelper {

    public HashMap<String, DmbsModel> getNodeData() {
        HashMap<String, DmbsModel> mapNodeData = new HashMap();

        String selectQuery = "select DEVICE_IP, DEVICE_NAME, DEVICE_TYPE, COMPANY from add_node where DEVICE_IP='172.30.252.42'";
        try (
                Connection con = Datasource.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(selectQuery);) {

            while (rs.next()) {
                DmbsModel model = new DmbsModel();
                model.setDeviceIP(rs.getString("DEVICE_IP"));
                model.setDeviceName(rs.getString("DEVICE_NAME"));
                model.setDeviceType(rs.getString("DEVICE_TYPE"));
                model.setCompany(rs.getString("COMPANY"));

                mapNodeData.put(rs.getString("DEVICE_IP"), model);
            }

        } catch (Exception e) {
            System.out.println("Exception while fetching microwave device ip = " + e);
        }
        return mapNodeData;
    }

    public void rssiThresholdLog(String device_ip, String deviceName, int threshold, double actual_value, String status, Timestamp logDateTime) {
        PreparedStatement pstmtThresholdLog = null;
        Connection threshold_log_con = null;
        String msgBody1 = null;
        String msgFormat = null;
        String mail_sub_msg = null;

//        String clientRadioID = device_ip.split("_")[1];
//        String mrId = device_ip.split("_")[0];
        try {
            threshold_log_con = Datasource.getConnection();

            pstmtThresholdLog = threshold_log_con.prepareStatement("INSERT INTO microwave_threshold_log (DEVICE_IP,DEVICE_NAME,SERVICE_TYPE,VALUE,STATUS,"
                    + "EVENT_TIMESTAMP) VALUES (?,?,?,?,?,?)");
            pstmtThresholdLog.setString(1, device_ip);
            pstmtThresholdLog.setString(2, deviceName);
            pstmtThresholdLog.setString(3, "RSSI");
            pstmtThresholdLog.setString(4, String.valueOf(actual_value));
            pstmtThresholdLog.setString(5, status);
            pstmtThresholdLog.setTimestamp(6, logDateTime);
            pstmtThresholdLog.executeUpdate();

//            if (latency_status.equalsIgnoreCase("High")) {
//                msgBody1 = "Project Name: NMS\nNode IP:" + device_ip + " has crossed the latency threshold above " + latency_threshold + "ms \nCurrent Latency:" + avg_responce + "ms \nDate:" + logDateTime;
//                msgFormat = "Dear Sir/Madam,\r \nPlease Check For\r \n" + msgBody1 + "\r \nKindly take appropriate action.\r \nThanks And Regards,\r \nNMS Team";
//                mail_sub_msg = "Alert: Latency threshold crossed above " + latency_threshold + "ms || " + device_ip;
//
//                log.sendMailAlert(device_ip, logDateTime, msgFormat, mail_sub_msg, "Latency_Threshold", "Normal");
//            }
        } catch (Exception e1) {
            System.out.println("Exception latency High" + e1);
        } finally {
            try {
                if (pstmtThresholdLog != null) {
                    pstmtThresholdLog.close();
                }
                if (threshold_log_con != null) {
                    threshold_log_con.close();
                }
            } catch (SQLException e) {
                System.out.println("Error 1" + e);
            }
        }
        Connection threshold_update_con = null;
        PreparedStatement pstmtThresholdUpdate = null;
        try {
            threshold_update_con = Datasource.getConnection();
            pstmtThresholdUpdate = threshold_update_con.prepareStatement("UPDATE microwave_monitoring SET RSSI_STATUS=?,RSSI=?,EVENT_TIMESTAMP=?,RSSI_Generated_Time=?,"
                    + "RSSI_Cleared_Time=? WHERE DEVICE_IP=?");
            pstmtThresholdUpdate.setString(1, status);
            pstmtThresholdUpdate.setString(2, String.valueOf(actual_value));
            pstmtThresholdUpdate.setTimestamp(3, logDateTime);
            pstmtThresholdUpdate.setTimestamp(4, status.equalsIgnoreCase("High") ? logDateTime : null);
            pstmtThresholdUpdate.setTimestamp(5, status.equalsIgnoreCase("Low") ? logDateTime : null);
            pstmtThresholdUpdate.setString(6, device_ip);
            pstmtThresholdUpdate.executeUpdate();
        } catch (Exception e) {
            System.out.println("UPDATE microwave_monitoring alert exception normal:" + e);
        } finally {
            try {
                if (pstmtThresholdUpdate != null) {
                    pstmtThresholdUpdate.close();
                }
                if (threshold_update_con != null) {
                    threshold_update_con.close();
                }
            } catch (Exception exp) {
                System.out.println("insert microwave_monitoring log exp:" + exp);
            }
        }
    }

    public void insertIntoEventLog(String deviceID, String deviceName, String eventMsg, int severity, String serviceName, Timestamp evenTimestamp, String netadmin_msg, String isAffected, String problem, String serviceId, String deviceType) {
        PreparedStatement preparedStatement1 = null;
        PreparedStatement preparedStatement2 = null;
        Connection connection = null;
        try {
            connection = Datasource.getConnection();
            preparedStatement1 = connection.prepareStatement("INSERT INTO event_log (device_id, device_name, service_name, event_msg, netadmin_msg, severity,"
                    + " event_timestamp, acknowledgement_status, isAffected, Problem_Clear, Service_ID, Device_Type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
            preparedStatement1.setString(1, deviceID);
            preparedStatement1.setString(2, deviceName);
            preparedStatement1.setString(3, serviceName);
            preparedStatement1.setString(4, eventMsg);
            preparedStatement1.setString(5, netadmin_msg);
            preparedStatement1.setInt(6, severity);
            preparedStatement1.setTimestamp(7, evenTimestamp);
            preparedStatement1.setBoolean(8, false);
            preparedStatement1.setString(9, isAffected);
            preparedStatement1.setString(10, problem);
            preparedStatement1.setString(11, serviceId);
            preparedStatement1.setString(12, deviceType);

            preparedStatement1.executeUpdate();

        } catch (Exception e) {
            System.out.println(deviceID + "inserting in event log Exception:" + e);
        } finally {
            try {
                if (preparedStatement1 != null) {
                    preparedStatement1.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception exp) {
                System.out.println("excep:" + exp);
            }
        }

        try {
            if ("Cleared".equalsIgnoreCase(problem)) {

                String updateQuery = "UPDATE event_log\n"
                        + "SET\n"
                        + "    Cleared_event_timestamp = ?,\n"
                        // + "    netadmin_msg = ?,\n"
                        + "netadmin_msg = CONCAT(netadmin_msg, ' => ', ?),\n"
                        + "    isAffected = ?\n"
                        + "WHERE\n"
                        + "    ID = (\n"
                        + "        SELECT id_alias.ID\n"
                        + "        FROM (\n"
                        + "            SELECT ID\n"
                        + "            FROM event_log\n"
                        + "            WHERE service_id = ?\n"
                        + "              AND device_id = ?\n"
                        + "            AND isaffected = '1' ORDER BY ID DESC\n"
                        + "            LIMIT 1\n"
                        + "        ) AS id_alias\n"
                        + "    )\n"
                        + ";";

                connection = Datasource.getConnection();

                preparedStatement2 = connection.prepareStatement(updateQuery);
                preparedStatement2.setTimestamp(1, evenTimestamp);

                preparedStatement2.setString(2, netadmin_msg); // To Do
                preparedStatement2.setString(3, "0");
                preparedStatement2.setString(4, serviceId);
                preparedStatement2.setString(5, deviceID);

                preparedStatement2.executeUpdate();
            }
        } catch (Exception e) {
            System.out.println("Exception in update event log = " + e);
        } finally {
            try {
                if (preparedStatement2 != null) {
                    preparedStatement2.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception exp) {
                System.out.println("excep:" + exp);
            }
        }
    }

    public void txPowerThresholdLog(String device_ip, String deviceName, int latency_threshold, double actual_value, String status, Timestamp logDateTime) {
        PreparedStatement pstmtThresholdLog = null;
        Connection threshold_log_con = null;

        try {
            threshold_log_con = Datasource.getConnection();

            pstmtThresholdLog = threshold_log_con.prepareStatement("INSERT INTO microwave_threshold_log (DEVICE_IP,DEVICE_NAME,SERVICE_TYPE,VALUE,STATUS,"
                    + "EVENT_TIMESTAMP) VALUES (?,?,?,?,?,?)");
            pstmtThresholdLog.setString(1, device_ip);
            pstmtThresholdLog.setString(2, deviceName);
            pstmtThresholdLog.setString(3, "TXP");
            pstmtThresholdLog.setString(4, String.valueOf(actual_value));
            pstmtThresholdLog.setString(5, status);
            pstmtThresholdLog.setTimestamp(6, logDateTime);
            pstmtThresholdLog.executeUpdate();

//            if (latency_status.equalsIgnoreCase("High")) {
//                msgBody1 = "Project Name: NMS\nNode IP:" + device_ip + " has crossed the latency threshold above " + latency_threshold + "ms \nCurrent Latency:" + avg_responce + "ms \nDate:" + logDateTime;
//                msgFormat = "Dear Sir/Madam,\r \nPlease Check For\r \n" + msgBody1 + "\r \nKindly take appropriate action.\r \nThanks And Regards,\r \nNMS Team";
//                mail_sub_msg = "Alert: Latency threshold crossed above " + latency_threshold + "ms || " + device_ip;
//
//                log.sendMailAlert(device_ip, logDateTime, msgFormat, mail_sub_msg, "Latency_Threshold", "Normal");
//            }
        } catch (Exception e1) {
            System.out.println("Exception latency High" + e1);
        } finally {
            try {
                if (pstmtThresholdLog != null) {
                    pstmtThresholdLog.close();
                }
                if (threshold_log_con != null) {
                    threshold_log_con.close();
                }
            } catch (SQLException e) {
                System.out.println("Error 1" + e);
            }
        }
        Connection threshold_update_con = null;
        PreparedStatement pstmtThresholdUpdate = null;
        try {
            threshold_update_con = Datasource.getConnection();
            pstmtThresholdUpdate = threshold_update_con.prepareStatement("UPDATE microwave_monitoring SET TXP_STATUS=?,TXP=?,EVENT_TIMESTAMP=?,"
                    + "TXP_Generated_Time=?,TXP_Cleared_Time=? WHERE DEVICE_IP=?");
            pstmtThresholdUpdate.setString(1, status);
            pstmtThresholdUpdate.setString(2, String.valueOf(actual_value));
            pstmtThresholdUpdate.setTimestamp(3, logDateTime);
            pstmtThresholdUpdate.setTimestamp(4, status.equalsIgnoreCase("High") ? logDateTime : null);
            pstmtThresholdUpdate.setTimestamp(5, status.equalsIgnoreCase("Low") ? logDateTime : null);
            pstmtThresholdUpdate.setString(6, device_ip);
            pstmtThresholdUpdate.executeUpdate();
        } catch (Exception e) {
            System.out.println("insert latency alert exception normal:" + e);
        } finally {
            try {
                if (pstmtThresholdUpdate != null) {
                    pstmtThresholdUpdate.close();
                }
                if (threshold_update_con != null) {
                    threshold_update_con.close();
                }
            } catch (Exception exp) {
                System.out.println("insert mars log exp:" + exp);
            }
        }
    }

    public void updateTxMuteStatus(String deviceIP, String txmute_val, Timestamp logtime) {
        Connection con = null;
        PreparedStatement pst = null;
        try {
            con = Datasource.getConnection();
            pst = con.prepareStatement("UPDATE microwave_monitoring SET TX_MUTE=?, TX_MUTE_STATUS=?,EVENT_TIMESTAMP=?, TX_MUTE_Generated_Time=?, "
                    + "TX_MUTE_Cleared_Time=? WHERE DEVICE_IP=?");
            pst.setString(1, txmute_val);
            pst.setString(2, txmute_val.equalsIgnoreCase("1") ? "On" : "Off");
            pst.setTimestamp(3, logtime);
            pst.setTimestamp(4, txmute_val.equalsIgnoreCase("2") ? logtime : null);
            pst.setTimestamp(5, txmute_val.equalsIgnoreCase("1") ? logtime : null);
            pst.setString(6, deviceIP);

            pst.executeUpdate();
        } catch (Exception e) {
            System.out.println("UPDATE microwave_monitoring exception normal:" + e);
        } finally {
            try {
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception exp) {
                System.out.println("update microwave_monitoring log exp:" + exp);
            }
        }
    }

    public void insertTxMuteStatusHistory(String deviceIP, String txmute_val, Timestamp logtime) {
        try {
            MicrowaveTxMuteStatusHistory node = new MicrowaveTxMuteStatusHistory();
            node.setDeviceIP(deviceIP);
            node.setTx_mute_status(txmute_val);
            node.setEventTimestamp(logtime);
            MicrowaveMonitoring.txMuteStatusHistory.add(node);
        } catch (Exception exp) {
            System.out.println(deviceIP + "Exception in adding insertTxMuteStatusHistory=" + exp);
        }
    }

}
