package com.npm.main;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Velox
 */



import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.UnsignedInteger32;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import org.snmp4j.ScopedPDU;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;

public class SNMPUtil {

    public enum SNMPCounterType {

        Counter32bit, Counter64bit
    };
    public final String STARTING_OID = "1.3.6.1.2.1.2.1.0";
    private TransportMapping<?> transport = null;
    private Snmp snmp = null;
    private UdpAddress localUDPAddress = null;

    public UdpAddress getUdpAddress() {
        return localUDPAddress;
    }

    public void setUdpAddress(UdpAddress udpaddr) {
        localUDPAddress = udpaddr;
    }

    /**
     * Source and return address of the form "<ip>/<port>"
     */
    public void setUdpAddress(String udpaddr) {
        setUdpAddress(new UdpAddress(udpaddr));
    }
    // -----------------------------------------------------
    // 1.3.6.1.2.1.1.1.0	Value: Linux TS-870 4.1.2
    public final String SYSNAME_ID = "hostName";
    public final String SYSNAME_OID = "1.3.6.1.4.1.9.2.1.3.0";
    public final String SYSNAME_STRING = "Host Name";
    public final String IFnNAME_ID = "if[%d] ";
    public final String IFnNAME_OID = "1.3.6.1.2.1.31.1.1.1.1";
    public final String IFnNAME_STRING = "if[%d] ";
    public final String IFnMAC_ID = "ifMAC";
    public final String IFnMAC_OID = "3.6.1.2.1.2.2.1.6";
    public final String IFnMAC_STRING = "if[%d] MAC Address";
    public final String IFnDESCR_ID = "Descr";
//	public  final String	IFnDESCR_OID 	= "1.3.6.1.2.1.2.2.1.2";
    public final String IFnDESCR_OID = "1.3.6.1.4.1.9.2.2.1.1.28"; // Cisco
    public final String IFnDESCR_STRING = "description";
//	public  final String	IFnDESCR_OID 	= "1.3.6.1.2.1.2.2.1.2";
//	public  final String	IFnDESCR_STRING	= "if[%d]";
    // -----------------------------------------------------
//	public  final String	UPTIME_ID 		= "sysORDUpTime";
//	public  final String	UPTIME_OID 		= "1.3.6.1.2.1.1.9.1.4";
    public final String UPTIME_IDX = "sysUpTime";
    public final String UPTIME_OIDX = "1.3.6.1.2.1.1.3.0";
    public final String UPTIME_STRINGX = "System Uptime";
    public final String UPTIME_ID = "hrSystemUptime.0";
    public final String UPTIME_OID = "1.3.6.1.2.1.25.1.1.0";
    public final String UPTIME_STRING = "System Uptime";
    public final String IFHCINOCT_OID = "1.3.6.1.2.1.31.1.1.1.6";
    public final String IFHCINOCT_ID = "HCInOctets";
    public final String IFHCINOCT_STRING = "HC Octets In";
    public final String IFINOCT_OID = "1.3.6.1.2.1.2.2.1.10";
    public final String IFINOCT_ID = "InOctets";
    public final String IFINOCT_STRING = "Octets In";
    public final String IFHCOUTOCT_OID = "1.3.6.1.2.1.31.1.1.1.10";
    public final String IFHCOUTOCT_ID = "OutOctets";
    public final String IFHCOUTOCT_STRING = "HC Octets Out";
    public final String IFOUTOCT_OID = "1.3.6.1.2.1.2.2.1.16";
    public final String IFOUTOCT_ID = "OutOctets";
    public final String IFOUTOCT_STRING = "Octets Out";
    public final String IFERRORS_ID = "InErrors";
    public final String IFERRORS_OID = "1.3.6.1.2.1.2.2.1.14";
    public final String IFERRORS_STRING = "Error Packets";
    public String[] bandwidth = new String[2];
    // --------------------------------------------------------------------

    public void stop() {
        try {
            transport.close();

        } catch (Exception e) {
            //System.out.println("Close Exceptin:" + e);
        }
        try {
            //transport.close();
            transport.removeTransportListener(null);
            // snmp.close();
        } catch (Exception e) {
            // System.out.println("close2 exceptin:" + e);
        }
        try {
            // transport.close();
            //transport.removeTransportListener(null);
            snmp.close();
        } catch (Exception e) {
            //  System.out.println("Close 3 Exception:" + e);
        }

    }

    public OID makeOID(String oidstr, int index) {
        OID retVal = new OID(oidstr);
        if (index > 0) {
            retVal.append(index);
        }
        return retVal;
    }
    // ------------------------------------------

    public OIDHolder makeOIDHolder(OIDHolder.SNMPDataType type, String oidstr, String id, String desc, int index) {
        OIDHolder retVal = null;
        OID oid = new OID(oidstr);
        String nametmp = id;
        if (index > -1) {
            oid.append(index);
            if (nametmp.contains("%")) {
                nametmp = String.format(id, index);
            }
            if (desc.contains("%")) {
                desc = String.format(desc, index);
            }
        }
//			retVal = new VariableBinding(oid, nametmp);
        retVal = new OIDHolder(type, nametmp, oid, desc);
        return retVal;
    }
    // ------------------------------------------

    public OIDHolder makeOIDHolder(OIDHolder.SNMPDataType type, String oidstr, String id, String desc) {
        return makeOIDHolder(type, oidstr, id, desc, -1);
    }
    // ------------------------------------------
    // ------------------------------------------
    private Map<String, OIDHolder> oidMap;
    // ------------------------------------------

    protected boolean addEntry(OIDHolder entry) {
        oidMap.put(entry.getName(), entry);
        oidMap.put(entry.getOidString(), entry);
        return true;
    }
    // ------------------------------------------

    protected OIDHolder addEntry(String key) {
        OIDHolder retVal = null;
        if (key != null) {
            retVal = oidMap.get(key);
        }
        return retVal;
    }
    // ------------------------------------------

    public OIDHolder getOIDHolder(String key) {
        OIDHolder retVal = null;
        if (key != null) {
            retVal = oidMap.get(key);
        }
        return retVal;
    }

    // ------------------------------------------
    public String getStringVar(Variable var) {
        String retVal = null;
        if (!(var instanceof Null)) {
            retVal = var.toString();
        }
        return retVal;
    }
    // ------------------------------------------

    public Variable getVariableBinding(PDU pdu, OID varoid) {
        Variable retVal = null;
        if (pdu != null && varoid != null) {
            retVal = pdu.getVariable(varoid);
        }
        return retVal;
    }
    // ------------------------------------------

    public String getVariableValue(PDU pdu, OID varoid) {
        String retVal = null;
        Variable var = getVariableBinding(pdu, varoid);
        if (var != null) {
            retVal = var.toString();
        }
        return retVal;
    }
    // ------------------------------------------

    public Variable getVariableBinding(PDU pdu, String varname) {
        Variable retVal = null;
        OIDHolder oidh = getOIDHolder(varname);
        if (pdu != null) {
            if (oidh != null) {
                retVal = getVariableBinding(pdu, oidh.getOid());
            } else {
                retVal = getVariableBinding(pdu, new OID(varname));
            }
        }
        return retVal;
    }
    // ------------------------------------------

    public String getVariableValue(PDU pdu, String varname) {
        String retVal = null;
        Variable var = getVariableBinding(pdu, varname);
        if (var != null) {
            retVal = var.toString();
        }
        return retVal;
    }

    // ------------------------------------------
    // ------------------------------------------
    {
        oidMap = new LinkedHashMap<String, OIDHolder>();
//		addEntry(getOIDHolder(String oidstr, String id, String desc, int index));
        // -------------------------------------------------------------------
        addEntry(makeOIDHolder(OIDHolder.SNMPDataType.STRING, SYSNAME_OID, SYSNAME_ID, SYSNAME_STRING));
        OIDHolder tmph = makeOIDHolder(OIDHolder.SNMPDataType.STRING, IFnDESCR_OID, IFnDESCR_ID, IFnDESCR_STRING);
        addEntry(tmph);
        tmph.setDescriptionHolder(makeOIDHolder(OIDHolder.SNMPDataType.STRING, IFnNAME_OID, IFnNAME_ID, IFnNAME_STRING));
        addEntry(tmph.getDescriptionHolder());

        // -------------------------------------------------------------------
        addEntry(makeOIDHolder(OIDHolder.SNMPDataType.TICKS, UPTIME_OID, UPTIME_ID, UPTIME_STRING));
        addEntry(makeOIDHolder(OIDHolder.SNMPDataType.OCTIN, IFINOCT_OID, IFINOCT_ID, IFINOCT_STRING));
        addEntry(makeOIDHolder(OIDHolder.SNMPDataType.OCTIN, IFHCINOCT_OID, IFHCINOCT_ID, IFHCINOCT_STRING));
        addEntry(makeOIDHolder(OIDHolder.SNMPDataType.OCTOUT, IFOUTOCT_OID, IFOUTOCT_ID, IFOUTOCT_STRING));
        addEntry(makeOIDHolder(OIDHolder.SNMPDataType.OCTOUT, IFHCOUTOCT_OID, IFHCOUTOCT_ID, IFHCOUTOCT_STRING));
        addEntry(makeOIDHolder(OIDHolder.SNMPDataType.COUNT, IFERRORS_OID, IFERRORS_ID, IFERRORS_STRING));
        // -------------------------------------------------------------------
    }
    // -----------------------------------------------------

    public List<OIDHolder> getSYSOIDHolder(List<OIDHolder> retVal) {
        retVal.add(getOIDHolder(UPTIME_OID));
        return retVal;
    }
    // -----------------------------------------------------

    public OIDHolder getIFOIDHolder(int index, SNMPCounterType type) {
        OIDHolder retVal = null;
        OIDHolder meta = null;

        switch (type) {
            case Counter32bit:
                retVal = makeOIDHolder(OIDHolder.SNMPDataType.STRING, IFnNAME_OID, IFnNAME_ID, IFnNAME_STRING, index);
                meta = makeOIDHolder(OIDHolder.SNMPDataType.STRING, IFnDESCR_OID, IFnDESCR_ID, IFnDESCR_STRING, index);
                break;
            case Counter64bit:
                retVal = makeOIDHolder(OIDHolder.SNMPDataType.STRING, IFnNAME_OID, IFnNAME_ID, IFnNAME_STRING, index);
                meta = makeOIDHolder(OIDHolder.SNMPDataType.STRING, IFnDESCR_OID, IFnDESCR_ID, IFnDESCR_STRING, index);
                break;
        }
        retVal.setDescriptionHolder(meta);
        return retVal;
    }
    // -----------------------------------------------------

    public List<OIDHolder> getIFOIDHolder(List<OIDHolder> retVal, int index) {
        retVal.add(makeOIDHolder(OIDHolder.SNMPDataType.OCTIN, IFINOCT_OID, IFINOCT_ID, IFINOCT_STRING, index));
        retVal.add(makeOIDHolder(OIDHolder.SNMPDataType.OCTIN, IFHCINOCT_OID, IFHCINOCT_ID, IFHCINOCT_STRING, index));
        retVal.add(makeOIDHolder(OIDHolder.SNMPDataType.OCTOUT, IFOUTOCT_OID, IFOUTOCT_ID, IFOUTOCT_STRING, index));
        retVal.add(makeOIDHolder(OIDHolder.SNMPDataType.OCTOUT, IFHCOUTOCT_OID, IFHCOUTOCT_ID, IFHCOUTOCT_STRING, index));
        retVal.add(makeOIDHolder(OIDHolder.SNMPDataType.COUNT, IFERRORS_OID, IFERRORS_ID, IFERRORS_STRING, index));
        return retVal;
    }

    // -----------------------------------------------------
    public List<OIDHolder> getIFOIDHolderInst(List<OIDHolder> retVal, int index, SNMPCounterType type, boolean inoutonly) {
        if (!inoutonly) {
            retVal.add(makeOIDHolder(OIDHolder.SNMPDataType.OCTIN, IFINOCT_OID, IFINOCT_ID, IFINOCT_STRING, index));
            retVal.add(makeOIDHolder(OIDHolder.SNMPDataType.OCTOUT, IFOUTOCT_OID, IFOUTOCT_ID, IFOUTOCT_STRING, index));
            retVal.add(makeOIDHolder(OIDHolder.SNMPDataType.COUNT, IFERRORS_OID, IFERRORS_ID, IFERRORS_STRING, index));
        }

        switch (type) {
            case Counter32bit:
                retVal.add(makeOIDHolder(OIDHolder.SNMPDataType.OCTIN, IFINOCT_OID, IFINOCT_ID, IFINOCT_STRING, index));
                retVal.add(makeOIDHolder(OIDHolder.SNMPDataType.OCTOUT, IFOUTOCT_OID, IFOUTOCT_ID, IFOUTOCT_STRING, index));
                break;
            case Counter64bit:
                retVal.add(makeOIDHolder(OIDHolder.SNMPDataType.OCTIN, IFHCINOCT_OID, IFHCINOCT_ID, IFHCINOCT_STRING, index));
                retVal.add(makeOIDHolder(OIDHolder.SNMPDataType.OCTOUT, IFHCOUTOCT_OID, IFHCOUTOCT_ID, IFHCOUTOCT_STRING, index));
                break;
        }

        return retVal;
    }

    // ------------------------------------------
    public VariableBinding getOIDVar(String oidstr, String name, int index) {
        VariableBinding retVal = null;
        OID oid = new OID(oidstr);
        String nametmp = name;
        if (name.contains("%")) {
            oid.append(index);
            nametmp = String.format(name, index);
        }
//			retVal = new VariableBinding(oid, nametmp);
        retVal = new VariableBinding(oid);
        return retVal;
    }
    // -----------------------------------------------------

    public PDU getMetaInfoOIDs(PDU retVal, int index) {
        retVal.add(getOIDVar(SYSNAME_OID, SYSNAME_STRING, index));
        retVal.add(getOIDVar(IFnNAME_OID, IFnNAME_STRING, index));
        retVal.add(getOIDVar(IFnDESCR_OID, IFnDESCR_STRING, index));
        return retVal;
    }
    // -----------------------------------------------------

    public PDU getMetaInfoOIDs(int index) {
        PDU retVal = new PDU();
        return getMetaInfoOIDs(retVal, index);
    }
    // -----------------------------------------------------

    public PDU getIFInfoOIDs(PDU retVal, int index) {
        retVal.add(getOIDVar(UPTIME_OID, UPTIME_STRING, index));
        retVal.add(getOIDVar(IFINOCT_OID, IFINOCT_STRING, index));
        retVal.add(getOIDVar(IFHCINOCT_OID, IFINOCT_STRING, index));
        retVal.add(getOIDVar(IFOUTOCT_OID, IFOUTOCT_STRING, index));
        retVal.add(getOIDVar(IFHCOUTOCT_OID, IFHCOUTOCT_STRING, index));
        retVal.add(getOIDVar(IFERRORS_OID, IFERRORS_STRING, index));
        return retVal;
    }
    // -----------------------------------------------------

    public List<VariableBinding> getIFInfoOIDs(List<VariableBinding> retVal, int index) {
        retVal.add(getOIDVar(UPTIME_OID, UPTIME_STRING, index));
        retVal.add(getOIDVar(IFINOCT_OID, IFINOCT_STRING, index));
        retVal.add(getOIDVar(IFHCINOCT_OID, IFINOCT_STRING, index));
        retVal.add(getOIDVar(IFOUTOCT_OID, IFOUTOCT_STRING, index));
        retVal.add(getOIDVar(IFHCOUTOCT_OID, IFHCOUTOCT_STRING, index));
        retVal.add(getOIDVar(IFERRORS_OID, IFERRORS_STRING, index));
        return retVal;
    }
    // -----------------------------------------------------

    public PDU getIFInfoOIDs(int index) {
        PDU retVal = new ScopedPDU();
        return getIFInfoOIDs(retVal, index);
    }
    // -----------------------------------------------------
    //

    public boolean isListning() {
        return transport != null;
    }

    protected TransportMapping<?> getTransportMapping()
            throws IOException {
        TransportMapping<?> retVal = transport;
        if (retVal == null) {
            try {
                if (getUdpAddress() != null) {
                    retVal = new DefaultUdpTransportMapping(getUdpAddress());
                } else {
                    retVal = new DefaultUdpTransportMapping();
                }
            } catch (IOException e) {
                System.out.println("SNMPUtil.getTransportMapping(): Failed to create Transport Mapping " + getUdpAddress());
                // throw e;
            }
            transport = retVal;
        }
        return retVal;
    }
    // ---------------------------------------------------------
//	public 

    public void startSNMPListen() {
        if (!isListning()) {
            try {
                transport = getTransportMapping();
                snmp = new Snmp(transport);
                // Do not forget this line!
                transport.listen();
            } catch (IOException e) {
                System.out.println("SNMPUtil.startSNMPListen(): Failed to create Transport Mapping " + getUdpAddress());
                // TODO Auto-generated catch block
                // e.printStackTrace();
            }
        }
    }
    // -----------------------------------------------------

    public void stopSNMPListen() {
        if (isListning()) {
            try {
                snmp.close();
                transport.close();
                try {
                    Thread.sleep(100);
                } catch (Exception ex) {
                    System.out.println("Error:"+ex);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                System.out.println("SNMP Closed exception" + e);

            }
            snmp = null;
            transport = null;
        }
    }

    // -----------------------------------------------------
    public boolean init() {
        startSNMPListen();
        return isListning();
    }
    // -----------------------------------------------------

    public int getVersion(String versionstring) {
        int retVal = SnmpConstants.version2c;
        if (versionstring != null) {
            versionstring = versionstring.trim();
//			if(versionstring.contains('2'))){
//				retVal = SnmpConstants.version2c;
//			} else 
            if ("1".equals(versionstring)) {
                retVal = SnmpConstants.version1;
            } else if ("3".equals(versionstring)) {
                retVal = SnmpConstants.version3;
            }
        }
        return retVal;
    }
    // -----------------------------------------------------

    public Target getTarget(String address, String community, int version) {

        //Secureit2

        // *** Below Code Commented by Vivek on 15 March for SNMP Version 2 ** Start ** 

        //     UserTarget target = null;


//	 try {
//        Address targetAddress = GenericAddress.parse(address);
//         target = new UserTarget();
//        //  target.setCommunity(new OctetString(community));
//        target.setAddress(targetAddress);
//        target.setVersion(SnmpConstants.version3);
//        target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
//        target.setRetries(2);
//        target.setTimeout(17000);
//        target.setSecurityName(new OctetString("cedge-nw"));
//		 } catch (Exception ee) {
//            System.out.println("security exception:" + ee);
//        }
        //  target.setVersion(version);

        // ************** End ****************//


      //  System.out.println("@@Address:" + address + " :Comunity: " + community + " :Version:" + version);

        /*
         * Below Code is Added by Vivek Warule on 16 March 2017 for SNMP Version
         * 2 **Start*
         */
        CommunityTarget target = null;
        try {



            Address targetAddress = GenericAddress.parse(address);
            target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(targetAddress);
            target.setRetries(2);
            target.setTimeout(17000);
            target.setVersion(version);



        } catch (Exception ee) {
            System.out.println("security exception:" + ee);
        }
       // System.out.println("^^^^^^^^^^^^^Target^^^^^^^^^^^^^^:" + target);

        /*
         * Below Code is Added by Vivek Warule on 16 March 2017 for SNMP Version
         * 2 **End*
         */

        return target;
    }

    public void start() {

        try {
            stopSNMPListen();
        } catch (Exception e2) {
            System.out.println("Stop Exception:" + e2);
        }

        //Secureit1

        try {


            /*
             * Below Code is Commented by Vivek Warule on 16 March 2017 for SNMP
             * Version 2 ***Start**
             */

//            transport = new DefaultUdpTransportMapping();
//            snmp = new Snmp(transport);
//            //
//
//            USM usm = null;
//            usm = new USM(SecurityProtocols.getInstance(), new OctetString(
//                    MPv3.createLocalEngineID()), 0);
//            SecurityModels.getInstance().addSecurityModel(usm);
//            transport.listen();
////            snmp.getUSM().addUser(
////                    new OctetString("MD5DES"),
////                    //  new UsmUser(new OctetString("cactiuser"), AuthMD5.ID, new OctetString("d1sc0@321"), PrivDES.ID, new OctetString("$ecure@123")));
////                    new UsmUser(new OctetString(username), AuthMD5.ID, new OctetString(auth_pass), PrivDES.ID, new OctetString(privacy_pass)));
//
//            //new UsmUser(new OctetString(username), AuthSHA.ID, new OctetString(auth_pass),PrivDES.ID, new OctetString(privacy_pass)));
//
//
//            // if (authentication.equals("MD5") && privacy.equals("DES")) {
//            snmp.getUSM().addUser(
//                    new OctetString("MD5DES"),
//                    new UsmUser(new OctetString("cedge-nw"), AuthMD5.ID, new OctetString("Nw@Ce69e"), PrivDES.ID, new OctetString("*T6@CK-InD!@")));


            /*
             * Below Code is Commented by Vivek Warule on 16 March 2017 for SNMP
             * Version 2 ***End**
             */


            /*
             * Below Code is Added by Vivek Warule on 16 March 2017 for SNMP
             * Version 2 **Start*
             */

            transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();

            /*
             * Below Code is Added by Vivek Warule on 16 March 2017 for SNMP
             * Version 2 **End*
             */

        } catch (Exception e) {
            System.out.println(isListning() + "start method error Exception" + e);
        }
    }

    public Target getTarget(String protocol, String host, String port, String community, int version) {
        StringBuilder targetaddress = new StringBuilder();
        targetaddress.append((protocol != null && !protocol.isEmpty() ? protocol : "udp"));
        targetaddress.append(":");
        targetaddress.append(host);
        targetaddress.append("/");
        targetaddress.append(port);
        return getTarget(targetaddress.toString(), community, version);
    }
    // -----------------------------------------------------

    public ResponseEvent get(Target target, PDU pdu) throws IOException {
        ResponseEvent retVal = null;
        if (pdu != null) {
            pdu.setType(PDU.GET);
            retVal = snmp.send(pdu, target, null);
            if (retVal == null) {
                System.out.println("time out null ###################################");
                throw new RuntimeException("GET timed out");
            }
        }
        return retVal;
    }
    // -----------------------------------------------------

    public ResponseEvent getNoThrow(Target target, PDU pdu) {
        ResponseEvent retVal = null;
        try {
            retVal = get(target, pdu);
        } catch (Exception e) {
        }
        return retVal;
    }
    // -----------------------------------------------------

    public ResponseEvent get(Target target, OID oid) throws IOException {
        ResponseEvent retVal = null;
       // PDU pdu = new ScopedPDU();  // Commented by Vivek on 16 March 2017 for SNMP V2c
        PDU pdu = new PDU();  // Added by Vivek on 16 March 2017 for SNMP V2c
        // for (OID oid : oids) {
        pdu.add(new VariableBinding(oid));
        //    }
        return get(target, pdu);
    }

    // -----------------------------------------------------
    public List<VariableBinding> walk(Target target, OID oid) {
        List<VariableBinding> ret = new ArrayList<VariableBinding>();

        PDU requestPDU = new ScopedPDU();
        requestPDU.add(new VariableBinding(oid));
        requestPDU.setType(PDU.GETNEXT);
        boolean finished = false;
        try {
            while (!finished) {
                VariableBinding vb = null;

                ResponseEvent respEvt = snmp.send(requestPDU, target);
                PDU responsePDU = respEvt.getResponse();
                if (responsePDU != null) {
                    vb = responsePDU.get(0);
                }

                if (responsePDU == null) {
                    finished = true;
                } else if (responsePDU.getErrorStatus() != 0) {
                    finished = true;
                } else if (vb.getOid() == null) {
                    finished = true;
                } else if (vb.getOid().size() < oid.size()) {
                    finished = true;
                } else if (oid.leftMostCompare(oid.size(), vb.getOid()) != 0) {
                    finished = true;
                } else if (Null.isExceptionSyntax(vb.getVariable().getSyntax())) {
                    finished = true;
                } else if (vb.getOid().compareTo(oid) <= 0) {
                    finished = true;
                } else {
                    ret.add(vb);

                    // Set up the variable binding for the next entry.
                    requestPDU.setRequestID(new Integer32(0));
                    requestPDU.set(0, vb);
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Exception4:" + e);
        }
        return ret;
    }
    // -----------------------------------------------------

    // -----------------------------------------------------
    // -----------------------------------------------------
    public OIDHolder.SNMPDataType getDataType(Variable var) {
        OIDHolder.SNMPDataType retVal = OIDHolder.SNMPDataType.INT;
        /*
         * org.snmp4j.smi.Variable implementations AbstractVariable, BitString,
         * Counter32, Counter64, Gauge32, Integer32, UnsignedInteger32,
         * TimeTicks, OctetString, GenericAddress, IpAddress, Null, OID, Opaque,
         * SMIAddress, SshAddress, TcpAddress, TlsAddress, TransportIpAddress,
         * TsmSecurityParameters, UdpAddress, VariantVariable SNMPDataType:
         * STRING,INT,LONG,FLOAT,TICKS,OCTIN,OCTOUT,COUNT, OTHER
         */
        if (var instanceof TimeTicks) {
            retVal = OIDHolder.SNMPDataType.TICKS;
        } else if (var instanceof Counter64 || var instanceof Counter32) {
            retVal = OIDHolder.SNMPDataType.COUNT;
        } else if (var instanceof Integer32 || var instanceof UnsignedInteger32) {
            retVal = OIDHolder.SNMPDataType.COUNT;
        } else if (var instanceof Gauge32) {
            retVal = OIDHolder.SNMPDataType.COUNT;
        } else if (var instanceof OctetString) {
            retVal = OIDHolder.SNMPDataType.STRING;
        }
        return retVal;
    }
    // -----------------------------------------------------

    public Map<OID, Variable> convertToMap(PDU pdu) {
        Map<OID, Variable> retVal = null;
        if (pdu != null) {
            retVal = new HashMap<OID, Variable>();
            Vector<? extends VariableBinding> vect = pdu.getVariableBindings();
            for (VariableBinding curr : vect) {
                retVal.put(curr.getOid(), curr.getVariable());
            }
        }
        return retVal;
    }
    // -----------------------------------------------------

    public String dump(PDU pdu) {
        //secureitvelox2
        if (pdu != null) {
            //System.out.println("PDU:\t" + pdu.toString());
            return dump(pdu.getVariableBindings());
        } else {
            // System.out.println("PDU:\t IS NULL/ No response"+pdu);
            return null;
        }
    }
    // -----------------------------------------------------

    public String dump(ResponseEvent resp) {
        //Secureitvelox1

        ((Snmp) resp.getSource()).cancel(resp.getRequest(), null);
        //System.out.println(resp+"%Get Responce%"+resp.getResponse());
        return dump(resp.getResponse());
    }

    public String dump(List<? extends VariableBinding> list) {

        //Secureit4
        int idx = 0;
        String rst = null;
        try {
            if (list.size() == 2) {
                int i = 0;
                for (VariableBinding var : list) {
                    rst = var.getVariable().toString();
                    i++;
                    // System.out.println(list+"###1:"+ var.getVariable());
                }
            } else {
                for (VariableBinding var : list) {
                    rst = var.getVariable().toString();
                    // System.out.println(list+"###2:"+var.getVariable());
                }
            }
        } catch (Exception e) {
            System.out.println("Exceptionnnnnn%^%^%^%^%^5:" + e);
        }
        return rst;
    }
    // device=1.3.6.1.2.1.2.2.1.3.1,status=1.3.6.1.2.1.2.2.1.8.1,unit64=1.3.6.1.2.1.31.1.1.1.15.1
    // device=1.3.6.1.2.1.2.2.1.3.2,status=1.3.6.1.2.1.2.2.1.8.2,unit64=1.3.6.1.2.1.31.1.1.1.15.2
    // device=1.3.6.1.2.1.2.2.1.3.3,status=1.3.6.1.2.1.2.2.1.8.3,unit64=1.3.6.1.2.1.31.1.1.1.15.3
    // device=1.3.6.1.2.1.2.2.1.3.4,status=1.3.6.1.2.1.2.2.1.8.4,unit64=1.3.6.1.2.1.31.1.1.1.15.4
    // device=1.3.6.1.2.1.2.2.1.3.5,status=1.3.6.1.2.1.2.2.1.8.5,unit64=1.3.6.1.2.1.31.1.1.1.15.5
    // device=1.3.6.1.2.1.2.2.1.3.6,status=1.3.6.1.2.1.2.2.1.8.6,unit64=1.3.6.1.2.1.31.1.1.1.15.6
    // device=1.3.6.1.2.1.2.2.1.3.7,status=1.3.6.1.2.1.2.2.1.8.7,unit64=1.3.6.1.2.1.31.1.1.1.15.7
//    OID oids[] = {new OID("1.3.6.1.2.1.31.1.1.1.6.1"),new OID("1.3.6.1.2.1.31.1.1.1.10.1"),
//    			  new OID("1.3.6.1.2.1.2.2.1.3.1"),new OID("1.3.6.1.2.1.2.2.1.8.1"),new OID("1.3.6.1.2.1.31.1.1.1.15.1")};
    // -----------------------------------------------------
  public  String BandwidthGetVect(Target target, String oidStr, OID oidxml) {
      ResponseEvent retVal = null;


      //  OID oids[] = {new OID("1.3.6.1.4.1.9.2.2.1.1.6" + "." + i), new OID("1.3.6.1.4.1.9.2.2.1.1.8" + "." + i) , new OID("1.3.6.1.2.1.2.2.1.7" + "." + i) , new OID("1.3.6.1.2.1.2.2.1.8" + "." + i), new OID("1.3.6.1.4.1.9.2.2.1.1.12" + "." + i), new OID("" )};

        try {
          //  int j;

            retVal = get(target, oidxml);
        } catch (IOException e) {
            // TODO Auto-generated catch block
           // e.printStackTrace();
			System.out.println(target+":Exception1:"+e);
        }
        return dump(retVal);

        // return retVal;
    }

    // -----------------------------------------------------
    public ResponseEvent testGetMeta(Target target, int index) {
        ResponseEvent retVal = null;
        PDU pdu = getMetaInfoOIDs(index);
        try {
            retVal = get(target, pdu);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Exception2:" + e);
        }
        dump(retVal);
        return retVal;
    }
    // -----------------------------------------------------

    public ResponseEvent testGetVect(Target target, int index) {
        ResponseEvent retVal = null;
        PDU pdu = getIFInfoOIDs(index);
        try {
            retVal = get(target, pdu);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Exception3:" + e);
        }
        dump(retVal);
        return retVal;
    }
    // -----------------------------------------------------

    public void testWalk(Target target) {
        List<? extends VariableBinding> ret = walk(target, new OID("1.3.6.1.2.1.2.2.1.10"));
        dump(ret);
    }

    public void getInoctet(Target target) {
        List<? extends VariableBinding> ret = walk(target, new OID("1.3.6.1.2.1.2.2.1.10"));
        dump(ret);
    }
    
    
    //----------------------------- For version 3 -----------------------------
    //--------------------------- Added by shital waman on 11 th sep 2020 -----------------------
    public Target getTargetVersion3(String address,String user_name, String auth, String auth_pass, String privacy, String priv_pass, int version) 
    {

        UserTarget target = null;
        try {

            
            Address targetAddress = GenericAddress.parse(address);
            target = new UserTarget();
            target.setAddress(targetAddress);
            target.setRetries(1);
            target.setTimeout(11500);
            target.setVersion(SnmpConstants.version3);
            
            if((auth==null || auth.equals("NA")) && (privacy == null || privacy.equals("NA")))
            {
                target.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
            }else if((privacy == null || privacy.equals("NA"))&&(auth!=null || !auth.equals("NA")))
            {
                target.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
            }else
            {
                //System.out.println("In else");
                target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
            }
           // target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
            target.setSecurityName(new OctetString(user_name));

        } catch (Exception ee) {
            System.out.println("security exception:" + ee);
        }
    return target;
}

}