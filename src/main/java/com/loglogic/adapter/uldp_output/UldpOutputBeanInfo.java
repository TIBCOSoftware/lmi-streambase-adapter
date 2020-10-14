/*
 * Copyright © 2020. TIBCO Software Inc.
 * This file is subject to the license terms contained
 * in the license file that is distributed with this file.
 */
package com.loglogic.adapter.uldp_output;

import java.beans.IntrospectionException;

import com.streambase.sb.operator.parameter.JavaEnumPropertyDescriptor;
import com.streambase.sb.operator.parameter.SBPropertyDescriptor;
import com.streambase.sb.operator.parameter.SBSimpleBeanInfo;

/**
 * A BeanInfo class controls what properties are exposed, add metadata about
 * properties (such as which properties are optional), and access special types
 * of properties that can't be automatically derived via reflection. If a
 * BeanInfo class is present, only the properties explicitly declared in this
 * class will be exposed by StreamBase.
 */
public class UldpOutputBeanInfo extends SBSimpleBeanInfo {

    /*
     * The order of properties below determines the order they are displayed within
     * the StreamBase Studio property view.
     */
    public SBPropertyDescriptor[] getPropertyDescriptorsChecked() throws IntrospectionException {
        SBPropertyDescriptor[] p = {
                new SBPropertyDescriptor("UldpHost", UldpOutput.class).displayName("ULDP Host").description("Host name or IP address of the LMI instance."),
                new SBPropertyDescriptor("UldpCollectionDomain", UldpOutput.class)
                        .displayName("ULDP Collection Domain").description("Collection domain to use for forwarding the messages.").optional(),
                new JavaEnumPropertyDescriptor<>("OutputFormat", UldpOutput.class,
                        UldpOutput.OutputFormatEnum.class).displayName("Output message format").description("Choose the format of the message.").optional(),
                new SBPropertyDescriptor("Prefix", UldpOutput.class).displayName("Message prefix").description("Message prefix will be added in front of the message.").optional(),
                new SBPropertyDescriptor("SrcIP", UldpOutput.class).displayName("Source IP").description("This will appear as the device's source IP address in LMI.").optional(),
        };
        return p;
    }

}
