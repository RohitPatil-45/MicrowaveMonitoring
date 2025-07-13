package com.npm.main;

import com.npm.dao.MicrowaveMonitoringLog;
import com.npm.dao.updateMicrowaveMonitoring;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author testsys
 */
public class RequestSNMPMain {

    public static boolean isSimulation=true;
    public static void main(String[] args) {
        
        

        try {
            Thread t2 = null;
            t2 = new Thread(new MicrowaveMonitoring());
            t2.start();
        } catch (Exception e) {
            System.out.println("Exception MicrowaveMonitoring:" + e);
        }
        
        
//        try {
//            Thread t2 = null;
//            t2 = new Thread(new updateMicrowaveMonitoring());
//            t2.start();
//        } catch (Exception e) {
//            System.out.println("Exception updateMicrowaveMonitoring:" + e);
//        }
//        
//        try {
//            Thread t2 = null;
//            t2 = new Thread(new MicrowaveMonitoringLog());
//            t2.start();
//        } catch (Exception e) {
//            System.out.println("Exception MicrowaveMonitoringLog:" + e);
//        }
        
         try {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
            //Insertion in SNMPTrapLog Table
            scheduler.scheduleAtFixedRate(new updateMicrowaveMonitoring(), 0, 12, TimeUnit.SECONDS);

            //update in snmp_trap_live_status;
            scheduler.scheduleAtFixedRate(new MicrowaveMonitoringLog(), 0, 12, TimeUnit.SECONDS);
        } catch (Exception e) {
             System.out.println("Exception === "+e);
        }

    }

   
}
