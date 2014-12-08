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

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.core.storage.sql.coremodel.SQLRepositoryService;
import org.nuxeo.runtime.api.Framework;

/**
 * Helper class used to extract JDBC DB settings from the Live SQLRepository configuration
 *
 * @author Tiry (tdelprat@nuxeo.com)
 */
public class DSHelper {

    protected static Map<String, NuxeoDSConfig> detectedDS;

    public static Map<String, NuxeoDSConfig> getDSForRepos() throws Exception {
        if (detectedDS == null) {
            Map<String, NuxeoDSConfig> configs = new HashMap<String, NuxeoDSConfig>();
            String singleDS = Framework.getProperty("nuxeo.db.singleDataSource", null);
            if (singleDS == null || singleDS.isEmpty()) {
                // look for ds in repositories
                SQLRepositoryService sqlRepositoryService = Framework.getService(SQLRepositoryService.class);
                for (String repositoryName : sqlRepositoryService.getRepositoryNames()) {
                    Map<String, String> properties = new HashMap<>();
                    String xaDataSourceName = sqlRepositoryService.getRepositoryDataSourceAndProperties(repositoryName,
                            properties);
                    NuxeoDSConfig config = new NuxeoDSConfig(xaDataSourceName, properties);
                    configs.put(repositoryName, config);
                }
            }
            detectedDS = configs;
        }
        return detectedDS;
    }

    public static NuxeoDSConfig getDefaultRepoDS(String repositoryName) throws Exception {
        Map<String, NuxeoDSConfig> configs = getDSForRepos();

        if (configs.size() == 0) {
            return null;
        }
        if (configs.size() == 1) {
            return configs.get(configs.keySet().iterator().next());
        }

        if (repositoryName == null) {
            repositoryName = "default";
        }
        return configs.get(repositoryName);
    }

    public static NuxeoDSConfig getReplacementDS(String birtDSName, String repositoryName) throws Exception {
        String name = birtDSName.toLowerCase();
        if (name.equals("nuxeo") || name.equals("nuxeovcs")) {
            return getDefaultRepoDS(repositoryName);
        }
        if (name.startsWith("nuxeo-")) {
            name = name.replace("nuxeo-", "");
        }
        Map<String, NuxeoDSConfig> configs = getDSForRepos();
        return configs.get(name);
    }

}
