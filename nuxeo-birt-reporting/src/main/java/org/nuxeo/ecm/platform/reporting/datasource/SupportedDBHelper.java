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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to find JDBC jar from the database type. Since BIRT used ODA to wrap JDBC we must provide the JDBC JAR.
 * This class directly finds the target Class in the server ClassLoader and extract the associated Jar.
 *
 * @author Tiry (tdelprat@nuxeo.com)
 */
public class SupportedDBHelper {

    protected static Map<String, String> driverMapping;

    public static final String H2 = "h2";

    public static final String PGSQL = "postgresql";

    public static final String MSSQL = "mssql";

    public static final String MYSQL = "mysql";

    public static final String ORACLE = "oracle";

    public static Map<String, String> getMapping() {
        if (driverMapping == null) {
            driverMapping = new HashMap<String, String>();
            driverMapping.put(H2, "org.h2.Driver");
            driverMapping.put(PGSQL, "org.postgresql.Driver");
            driverMapping.put(MSSQL, "net.sourceforge.jtds.jdbc.Driver");
            driverMapping.put(MYSQL, "com.mysql.jdbc.Driver");
            driverMapping.put(ORACLE, "oracle.jdbc.OracleDriver");
        }
        return driverMapping;
    }

    public static String getDriver(String name) {
        return getMapping().get(name);
    }

    public static URL getDriverJar(String name) throws MalformedURLException {
        String javaName = getDriver(name);
        String classPath = javaName.replace(".", "/");
        classPath = classPath + ".class";

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource(classPath);
        if (url == null) {
            return null;
        }
        String protocol = url.getProtocol();
        String file = url.getFile();
        if ("vfszip".equals(protocol)) {
            return new URL("vfszip:" + file.substring(0, file.length() - classPath.length() - 1));
        } else if ("jar".equals(protocol)) {
            return new URL(file.substring(0, file.length() - classPath.length() - 2));
        } else {
            throw new Error("Cannot loate jar location of '" + name + "' JDBC Driver, unsupported protocol '"
                    + protocol + "'");
        }
    }

    public static List<URL> getDriverJars() throws MalformedURLException {
        List<URL> jars = new ArrayList<URL>();
        for (String name : getMapping().keySet()) {
            URL jar = getDriverJar(name);
            if (jar != null) {
                jars.add(jar);
            }
        }
        return jars;
    }

}
