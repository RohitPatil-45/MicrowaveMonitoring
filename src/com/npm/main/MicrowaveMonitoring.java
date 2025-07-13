/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.npm.main;

import com.npm.dao.DatabaseHelper;
import com.npm.datasource.Datasource;
import com.npm.model.DmbsModel;
import com.npm.model.MicrowaveModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Kratos
 */
public class MicrowaveMonitoring implements Runnable {

    public static HashMap<String, DmbsModel> mapNodeData = null;

    public static ArrayList<MicrowaveModel> updateList = null;
    public static ArrayList<MicrowaveModel> updateListTemp = null;

    public static ArrayList<MicrowaveModel> updatelogList = null;
    public static ArrayList<MicrowaveModel> updateListlogTemp = null;

    public static HashMap rssiThresholdMap = null;
    public static HashMap txPowerThresholdMap = null;
    public static HashMap txMuteStatusMap = null;

    public static int rssiThresholdParam;
    public static int txPowerThresholdParam;

    private static final int THREAD_POOL_SIZE = 8;
    private static final int MONITOR_INTERVAL_SECONDS = 30;

    @Override
    public void run() {

        updateList = new ArrayList<>();
        updateListTemp = new ArrayList<>();

        updatelogList = new ArrayList<>();
        updateListlogTemp = new ArrayList<>();

        rssiThresholdMap = new HashMap<>();
        txPowerThresholdMap = new HashMap<>();
        txMuteStatusMap = new HashMap<>();

        DatabaseHelper helper = new DatabaseHelper();
        mapNodeData = helper.getNodeData();
        System.out.println(mapNodeData.size() + ":MicrowaveMonitoring:" + mapNodeData);

        try (
                Connection con = Datasource.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT DEVICE_IP, RSSI_STATUS, TXP_STATUS, TX_MUTE_STATUS FROM microwave_monitoring");) {

            while (rs.next()) {
                rssiThresholdMap.put(rs.getString("DEVICE_IP"), rs.getString("RSSI_STATUS"));
                txPowerThresholdMap.put(rs.getString("DEVICE_IP"), rs.getString("TXP_STATUS"));
                txMuteStatusMap.put(rs.getString("DEVICE_IP"), rs.getString("TX_MUTE_STATUS"));
            }

        } catch (Exception e) {
            System.out.println("Exception while fetching microwave device ip along with initial threshold status= " + e);
        }

        try (
                Connection con = Datasource.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("select dmbs_rssi, dmbs_txPower from threshold_parameter");) {

            while (rs.next()) {
                rssiThresholdParam = rs.getInt(1);
                txPowerThresholdParam = rs.getInt(2);
            }

        } catch (Exception e) {
            System.out.println("Exception while fetching microwave threshold parameter = " + e);
        }

        Iterator<Map.Entry<String, DmbsModel>> itr = mapNodeData.entrySet().iterator();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);

        for (Map.Entry<String, DmbsModel> entry : mapNodeData.entrySet()) {
            DmbsModel model = entry.getValue();

            Runnable task = new MicrowaveMon(model);

            scheduler.scheduleAtFixedRate(task, 0, MONITOR_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

}
