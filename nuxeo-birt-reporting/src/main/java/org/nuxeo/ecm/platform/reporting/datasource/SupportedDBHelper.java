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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to find JDBC jar from the database type. Since BIRT used ODA to
 * wrap JDBC we must provide the JDBC JAR.
 *
 * This class directly finds the target Class in the server ClassLoader and
 * extract the associated Jar.
 *
 * @author Tiry (tdelprat@nuxeo.com)
 *
 */
public class SupportedDBHelper {

    protected static Map<String, String> driverMapping;

    public static final String H2 = "h2";

    public static final String PGSQL = "postgresql";

    public static Map<String, String> getMapping() {
        if (driverMapping == null) {
            driverMapping = new HashMap<String, String>();
            driverMapping.put(H2, "org.h2.Driver");
            driverMapping.put(PGSQL, "org.postgresql.Driver");
        }
        return driverMapping;
    }

    public static String getDriver(String name) {
        return getMapping().get(name);
    }

    public static String getDriverJar(String name) {
        String javaName = getDriver(name);
        String className = javaName.replace(".", "/");
        className = className + ".class";

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource(className);

        if (url == null) {
            return null;
        }
        return url.getFile().split("!")[0].replace("file:", "");
    }

    public static List<String> getDriverJars() {

        List<String> jars = new ArrayList<String>();
        for (String name : getMapping().keySet()) {
            String jar = getDriverJar(name);
            if (jar != null) {
                jars.add(jar);
            }
        }
        return jars;
    }
}
