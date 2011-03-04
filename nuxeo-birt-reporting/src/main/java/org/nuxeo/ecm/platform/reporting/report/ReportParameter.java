/*
 * (C) Copyright 2006-20011 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 */

package org.nuxeo.ecm.platform.reporting.report;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.report.engine.api.IParameterDefn;
import org.eclipse.birt.report.engine.api.impl.ScalarParameterDefn;

/**
 *
 * Wraps Birt Report parameters to manage Cast and Conversions
 *
 * @author Tiry (tdelprat@nuxeo.com)
 *
 */
public class ReportParameter {

    private static final Log log = LogFactory.getLog(ReportParameter.class);

    public static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS");

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd");

    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat(
            "hh:mm:ss");

    protected int type;

    protected String stringValue;

    protected String name;

    protected String displayName;

    protected String help;

    protected boolean required;

    protected boolean editable = true;

    public ReportParameter(IParameterDefn paramDef) {
        this(paramDef, null);
    }

    public ReportParameter(IParameterDefn paramDef, String value) {
        type = paramDef.getDataType();
        name = paramDef.getName();
        displayName = paramDef.getDisplayName();
        help = paramDef.getHelpText();
        required = paramDef.isRequired();
        if (value != null && value.isEmpty()) {
            stringValue = value;
        } else {
            if (paramDef instanceof ScalarParameterDefn) {
                ScalarParameterDefn scalarParam = (ScalarParameterDefn) paramDef;
                stringValue = scalarParam.getDefaultValue();

            }
        }
    }

    public void setValue(String value) {
        stringValue = value;
    }

    public void setValue(Calendar value) {
        setValue(value.getTime());
    }

    public void setValue(Date value) {
        if (type == IParameterDefn.TYPE_DATE) {
            stringValue = DATE_FORMAT.format(value);
        } else if (type == IParameterDefn.TYPE_DATE_TIME) {
            stringValue = DATETIME_FORMAT.format(value);
        } else if (type == IParameterDefn.TYPE_TIME) {
            stringValue = TIME_FORMAT.format(value);
        }
    }

    public void setValue(Integer value) {
        if (type == IParameterDefn.TYPE_INTEGER) {
            stringValue = value.toString();
        }
    }

    public void setValue(Float value) {
        if (type == IParameterDefn.TYPE_FLOAT) {
            stringValue = value.toString();
        }
    }

    public void setValue(Boolean value) {
        if (type == IParameterDefn.TYPE_BOOLEAN) {
            stringValue = value.toString();
        }
    }

    public String getStringValue() {
        return stringValue;
    }

    public Date getDateTimeValue() {
        try {
            if (type == IParameterDefn.TYPE_DATE) {
                return DATE_FORMAT.parse(stringValue);
            } else if (type == IParameterDefn.TYPE_DATE_TIME) {
                return DATETIME_FORMAT.parse(stringValue);
            } else if (type == IParameterDefn.TYPE_TIME) {
                return TIME_FORMAT.parse(stringValue);
            }
        } catch (Exception e) {
            String message = String.format(
                    "Error while parsing the '%s' date value", stringValue);
            log.error(message, e);
        }
        return null;
    }

    public Boolean getBooleanValue() {
        return Boolean.parseBoolean(stringValue);
    }

    public Integer getIntegerValue() {
        return Integer.parseInt(stringValue);
    }

    public Float getFloatValue() {
        return Float.parseFloat(stringValue);
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        if (displayName == null || displayName.isEmpty()) {
            return name;
        }
        return displayName;
    }

    public String getHelp() {
        return help;
    }

    public boolean isRequired() {
        return required;
    }

    public Object getObjectValue() {
        if (type == IParameterDefn.TYPE_DATE) {
            return getDateTimeValue();
        } else if (type == IParameterDefn.TYPE_DATE_TIME) {
            return getDateTimeValue();
        } else if (type == IParameterDefn.TYPE_TIME) {
            return getDateTimeValue();
        } else if (type == IParameterDefn.TYPE_BOOLEAN) {
            return getBooleanValue();
        } else if (type == IParameterDefn.TYPE_INTEGER) {
            return getIntegerValue();
        } else if (type == IParameterDefn.TYPE_FLOAT) {
            return getFloatValue();
        } else if (type == IParameterDefn.TYPE_DECIMAL) {
            return getFloatValue();
        }
        return stringValue;
    }

    public void setObjectValue(Object value) {
        if (value instanceof Calendar) {
            setValue((Calendar) value);
        } else if (value instanceof Date) {
            setValue((Date) value);
        } else if (value instanceof Boolean) {
            setValue((Boolean) value);
        } else if (value instanceof Integer) {
            setValue((Integer) value);
        } else if (value instanceof Float) {
            setValue((Float) value);
        } else {
            if (value != null) {
                setValue(value.toString());
            } else {
                setValue((String) null);
            }
        }
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public String toString() {
        return "" + name + ":" + type + "(" + stringValue + ")";
    }

}
