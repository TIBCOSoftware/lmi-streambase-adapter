/*
 * Copyright © 2020. TIBCO Software Inc.
 * This file is subject to the license terms contained
 * in the license file that is distributed with this file.
 */
package com.loglogic.adapter.uldp_output;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.slf4j.Logger;

import com.streambase.sb.*;
import com.streambase.sb.Schema.Field;
import com.streambase.sb.adapter.*;
import com.streambase.sb.operator.*;
import com.tibco.loglogic.logging.uldpclient.UldpConnectionSettings;
import com.tibco.loglogic.logging.uldpclient.UldpLogMessage;
import com.tibco.loglogic.logging.uldpclient.UldpSender;
import com.tibco.loglogic.logging.uldpclient.UldpSyslogMessage;

/**
 * Adapter to send tuples out to an LMI instance using the ULDP protocol
 *
 */
public class UldpOutput extends OutputAdapter implements Parameterizable {

    public static enum OutputFormatEnum {
        KVP("key-value pair"), COLUMNAR("columnar");

        private final String rep;

        private OutputFormatEnum(String s) {
            rep = s;
        }

        public String toString() {
            return rep;
        }
    }

    public static final long serialVersionUID = 1544544234294L;
    private Logger logger;
    // Properties
    private String uldpHost;
    private String uldpCollectionDomain;
    private String displayName = "TIBCO LogLogic LMI ULDP adapter";
    private String shortName = "LogLogic ULDP";
    private String prefix = "";
    private UldpConnectionSettings uldpSettings;
    private UldpSender uldpSender;
    private OutputFormatEnum outputFormat = OutputFormatEnum.KVP;
    private String srcIP;
    private InetAddress srcInetAddress;

    /**
     * The constructor is called when the Adapter instance is created, but before
     * the Adapter is connected to the StreamBase application. The constructor sets
     * default values for adapter parameters. These values will be displayed in StreamBase Studio when
     * a new adapter is dragged to the canvas, and serve as the default values for
     * omitted optional parameters.
     */
    public UldpOutput() {
        super();
        logger = getLogger();
        setPortHints(1, 0);
        setDisplayName(displayName);
        setShortDisplayName(shortName);
        setUldpHost("");
        setUldpCollectionDomain("");
        setOutputFormat(OutputFormatEnum.KVP);
        setPrefix("");
        setSrcIP(null);
    }

    /**
     * Typecheck this adapter. The typecheck method is called after the adapter
     * instance is constructed, and once input ports are connected. The adapter
     * should validate its parameters and throw PropertyTypecheckException (or
     * TypecheckException) if any problems are found. The message associated with
     * the thrown exceptions will be displayed in StreamBase Studio during
     * authoring, or printed on the console by sbd. Input adapters should set the
     * schema of each output port by calling the setOutputSchema(portNum, schema)
     * method for each output port. If the adapter needs to change the number of
     * input ports based on parameter values, it should call
     * requireInputPortCount(portCount) at this point.
     */
    public void typecheck() throws TypecheckException {
        int colonIndex = uldpHost.indexOf(':');
        if (colonIndex == -1) {
            uldpSettings = new UldpConnectionSettings(uldpHost);
        } else {
            try {
                String host = uldpHost.substring(0, colonIndex);
                int port = Integer.parseInt(uldpHost.substring(colonIndex + 1));
                uldpSettings = new UldpConnectionSettings(host, port);
            } catch (NumberFormatException e) {
                throw new PropertyTypecheckException("UldpHost", e);
            }
        }
        uldpSettings.setDomainName(uldpCollectionDomain);
        uldpSender = new UldpSender(uldpSettings);
        if (srcIP == null || srcIP.length() == 0) {
            try {
                srcInetAddress = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                throw new PropertyTypecheckException("SrcIP", e);
            }
        } else if (srcIP != null) {
            try {
                srcInetAddress = InetAddress.getByName(srcIP);
            } catch (UnknownHostException e) {
                throw new PropertyTypecheckException("SrcIP", e);
            }
        }
    }

    /**
     * Initialize the adapter. If typecheck succeeds, the init method is called
     * before the StreamBase application is started. Note that your adapter is not
     * required to define the init method, unless you need to register a runnable or
     * perform initialization of a resource such as, for example, a JDBC pool.
     */
    public void init() throws StreamBaseException {
        super.init();
        try {
            uldpSender.connect();
        } catch (IOException e) {
            throw new StreamBaseException(e);
        }
    }

    /**
     * Shutdown adapter by cleaning up any resources used (e.g. close an output file
     * if it has been opened). When the application is shutting down, the adapter's
     * shutdown. method will be called first. Once this has returned, all threads
     * should exit and the adapter is considered shutdown.
     */
    public void shutdown() {
        try {
            uldpSender.close();
        } catch (IOException e) {
            // not much we can do...
        }
    }

    private void addField(StringBuffer msg, String name, String value) {
        if (value == null)
            value = "null";
        if (outputFormat == OutputFormatEnum.KVP) {
            msg.append("\"" + name + "\"=\"" + value.replace('"', ' ') + "\"");
        } else {
            msg.append(value.replace(",", "\\,"));
        }
        msg.append(",");
    }

    private void valueToString(StringBuffer msgIn, String name, Object value, CompleteDataType completeDataType) {
        switch (completeDataType.getDataType()) {
            case BLOB:
                // value = tuple.getBlobBuffer(field);
                break;
            case BOOL:
                String val = Boolean.toString((Boolean) value);
                addField(msgIn, name, val);
                break;
            case DOUBLE:
                val = Double.toString((Double) value);
                addField(msgIn, name, val);
                break;
            case INT:
                val = Integer.toString((Integer) value);
                addField(msgIn, name, val);
                break;
            case LIST:
                List<?> list = (List<?>) value;
                if (outputFormat == OutputFormatEnum.KVP) {

                    for (int i = 0; i < list.size(); i++) {
                        Object subValue = list.get(i);
                        String subName = name + "[" + i + "]";
                        valueToString(msgIn, subName, subValue, completeDataType.getElementType());
                    }
                } else {
                    StringBuffer valBuf = new StringBuffer();
                    for (int i = 0; i < list.size(); i++) {
                        if (valBuf.length() != 0) {
                            valBuf.append(",");
                        }
                        Object subValue = list.get(i);

                        valueToString(msgIn, null, subValue, completeDataType.getElementType());
                    }
                    addField(msgIn, null, valBuf.toString());
                }
                break;
            case LONG:
                val = Long.toString((Long) value);
                addField(msgIn, name, val);
                break;
            case STRING:
                addField(msgIn, name, (String) value);
                break;
            case TIMESTAMP:
                Timestamp timestamp = (Timestamp) value;
                if (timestamp != null) {
                    val = timestamp.toString();
                    addField(msgIn, name, val);
                    break;
                }
                break;
            case TUPLE:
                Tuple tuple = (Tuple) value;
                Schema schema = completeDataType.getSchema();
                for (Field field : schema) {
                    String subName = (name == null ? "" : name + ".") + field.getShortName();
                    CompleteDataType subType = field.getCompleteDataType();
                    Object subValue = tuple.getField(field);
                    valueToString(msgIn, subName, subValue, subType);
                }
                break;
            case CAPTURE:
                break;
            case FUNCTION:
                break;
            default:
                break;
        }

    }

    /**
     * This method will be called by the StreamBase server for each Tuple given to
     * the adapter to process. Output adapters should override this method to
     * process tuples.
     * <p>
     * The default implementation does nothing.
     *
     * @param inputPort the input port that the tuple is from (ports are zero based)
     * @param tuple     the tuple from the given input port
     * @throws StreamBaseException Terminates the application.
     *
     */
    public void processTuple(int inputPort, Tuple tuple) throws StreamBaseException {
        if (logger.isInfoEnabled()) {
            logger.info("processing a tuple at input port " + inputPort);
        }
        String msg;

        Schema schema = getRuntimeInputSchema(0);
        CompleteDataType completeDataType = new CompleteDataType.TupleType(schema);
        StringBuffer msgBuf = new StringBuffer();
        valueToString(msgBuf, null, tuple, completeDataType);
        msg = "TIBStreambase " + prefix + (prefix.length() != 0 ? " " : "") + msgBuf.substring(0, msgBuf.length() - 1);

        logger.debug(msg);
        UldpLogMessage uldpSyslogMessage;
        uldpSyslogMessage = new UldpSyslogMessage(System.currentTimeMillis(), srcInetAddress, msg);

        try {
            uldpSender.sendMessage(uldpSyslogMessage);
            uldpSender.flush();
        } catch (IOException e) {
            throw new StreamBaseException(e);
        }
    }

    /***************************************************************************************
     * The getter and setter methods provided by the Parameterizable object. *
     * StreamBase Studio uses them to determine the name and type of each property *
     * and obviously, to set and get the property values. *
     ***************************************************************************************/

    public void setUldpHost(String uldpHost) {
        this.uldpHost = uldpHost;
    }

    public String getUldpHost() {
        return this.uldpHost;
    }

    public void setUldpCollectionDomain(String uldpCollectionDomain) {
        this.uldpCollectionDomain = uldpCollectionDomain;
    }

    public String getUldpCollectionDomain() {
        return this.uldpCollectionDomain;
    }

    public OutputFormatEnum getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(OutputFormatEnum outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSrcIP() {
        return srcIP;
    }

    public void setSrcIP(String srcIP) {
        this.srcIP = srcIP;
    }

    /**
     * For detailed information about shouldEnable methods, see interface
     * Parameterizable java doc
     *
     * @see Parameterizable
     */

    public boolean shouldEnableUldpHost() {
        return true;
    }

    public boolean shouldEnableUldpCollectionDomain() {
        return true;
    }

    public boolean shouldEnableOutputFormat() {
        return true;
    }

    public boolean shouldEnablePrefix() {
        return true;
    }

    public boolean shouldEnableSrcIP() {
        return true;
    }
}
