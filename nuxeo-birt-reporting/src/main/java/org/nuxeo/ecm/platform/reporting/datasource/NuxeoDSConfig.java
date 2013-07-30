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

package org.nuxeo.ecm.platform.reporting.datasource;

import java.util.Map;

import org.nuxeo.runtime.api.Framework;

/**
 * Wrapps a DataSource configuration
 *
 * @author Tiry (tdelprat@nuxeo.com)
 *
 */
public class NuxeoDSConfig {

    protected String driverClass;

    protected String url;

    protected String userName;

    protected String password;

    public static final String H2_PREFIX = "org.h2";

    public static final String PG_PREFIX = "org.postgresql";

    public static final String MSSQL_PREFIX = "net.sourceforge.jtds";

    public static final String MYSQL_PREFIX = "com.mysql.jdbc";

    public static final String ORACLE_PREFIX = "oracle.jdbc";

    public NuxeoDSConfig(String dataSourceName, Map<String, String> properties) {
        if (dataSourceName.startsWith(H2_PREFIX)) {
            initForH2(properties);
        } else if (dataSourceName.startsWith(PG_PREFIX)) {
            initForPostgreSQL(properties);
        } else if (dataSourceName.startsWith(MSSQL_PREFIX)) {
            initForMSSQL(properties);
        } else if (dataSourceName.startsWith(MYSQL_PREFIX)) {
            initForMySQL(properties);
        } else if (dataSourceName.startsWith(ORACLE_PREFIX)) {
            initForOracle(properties);
        }
    }

    protected void initForH2(Map<String, String> properties) {
        userName = getProp(properties, "User");
        password = getProp(properties, "Password");
        url = getProp(properties, "URL");
        driverClass = SupportedDBHelper.getDriver(SupportedDBHelper.H2);
    }

    protected String getIntegerProp(Map<String, String> properties, String name) {
        String value = getProp(properties, name);
        if (value != null) {
            return value;
        }
        return getProp(properties, name+"/Integer");
    }

    protected void initForPostgreSQL(Map<String, String> properties) {
        userName = getProp(properties, "User");
        password = getProp(properties, "Password");
        url = getProp(properties, "URL");
        if (url==null) {
            url = "jdbc:postgresql://" + getProp(properties, "ServerName") + ":"
                + getIntegerProp(properties, "PortNumber") + "/"
                + getProp(properties, "DatabaseName");
        }
        driverClass = getProp(properties, "Driver");
        if (driverClass==null) {
            driverClass = SupportedDBHelper.getDriver(SupportedDBHelper.PGSQL);
        }
    }

    protected void initForMSSQL(Map<String, String> properties) {
        userName = getProp(properties, "User");
        password = getProp(properties, "Password");
        url = getProp(properties, "URL");
        if (url==null) {
            url = "jdbc:jtds:sqlserver://" + getProp(properties, "ServerName")
                + ":" + getIntegerProp(properties, "PortNumber") + "/"
                + getProp(properties, "DatabaseName") + ";useCursors=true";
        }
        driverClass = getProp(properties, "Driver");
        if (driverClass==null) {
            driverClass = SupportedDBHelper.getDriver(SupportedDBHelper.MSSQL);
        }
    }

    protected void initForMySQL(Map<String, String> properties) {
        userName = getProp(properties, "User");
        password = getProp(properties, "Password");
        url = getProp(properties, "URL");
        driverClass = getProp(properties, "Driver");
        if (driverClass==null) {
            driverClass = SupportedDBHelper.getDriver(SupportedDBHelper.MYSQL);
        }
    }

    protected void initForOracle(Map<String, String> properties) {
        userName = getProp(properties, "User");
        password = getProp(properties, "Password");
        url = getProp(properties, "URL");
        driverClass = getProp(properties, "Driver");
        if (driverClass==null) {
            driverClass = SupportedDBHelper.getDriver(SupportedDBHelper.ORACLE);
        }
    }

    protected String getProp(Map<String, String> properties, String name) {
        final String value = properties.get(name);
        if (value == null) {
            return null;
        }
        return Framework.expandVars(value);
    }

    public String getDriverClass() {
        return driverClass;
    }

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("username:");
        sb.append(userName);
        sb.append("\npassword:");
        sb.append(password);
        sb.append("\ndriver:");
        sb.append(driverClass);
        sb.append("\nurl:");
        sb.append(url);
        return sb.toString();
    }

}
