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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.storage.sql.RepositoryDescriptor;
import org.nuxeo.ecm.core.storage.sql.RepositoryImpl;
import org.nuxeo.ecm.core.storage.sql.RepositoryResolver;
import org.nuxeo.ecm.core.storage.sql.ra.ConnectionFactoryImpl;
import org.nuxeo.ecm.core.storage.sql.ra.ManagedConnectionFactoryImpl;
import org.nuxeo.runtime.api.Framework;

/**
 * Helper class used to extract JDBC DB settings from the Live SQLRepository
 * configuration
 *
 * @author Tiry (tdelprat@nuxeo.com)
 *
 */
public class DSHelper {

    protected static Map<String, NuxeoDSConfig> detectedDS;

    public static Map<String, NuxeoDSConfig> getDSForRepos() throws Exception {
        if (detectedDS == null) {
            Map<String, NuxeoDSConfig> configs = new HashMap<String, NuxeoDSConfig>();

            RepositoryManager rm = Framework.getLocalService(RepositoryManager.class);
            for (Repository repo : rm.getRepositories()) {
                Object repoImpl = RepositoryResolver.getRepository(repo.getName());
                RepositoryDescriptor desc;
                if (repoImpl instanceof RepositoryImpl) {
                    RepositoryImpl sqlRepo = (RepositoryImpl) repoImpl;
                    desc = sqlRepo.getRepositoryDescriptor();
                } else {
                    ConnectionFactoryImpl cf = (ConnectionFactoryImpl) RepositoryResolver.getRepository(repo.getName());
                    ManagedConnectionFactoryImpl mcf = cf.getManagedConnectionFactory();
                    Field field = mcf.getClass().getDeclaredField("repository");
                    field.setAccessible(true);
                    RepositoryImpl sqlRepositoryImpl = (RepositoryImpl) field.get(mcf);
                    desc = sqlRepositoryImpl.getRepositoryDescriptor();
                }

                NuxeoDSConfig config = new NuxeoDSConfig(desc.xaDataSourceName,
                        desc.properties);
                configs.put(repo.getName(), config);
            }
            detectedDS = configs;
        }
        return detectedDS;
    }

    public static NuxeoDSConfig getDefaultRepoDS(String repositoryName)
            throws Exception {
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

    public static NuxeoDSConfig getReplacementDS(String birtDSName,
            String repositoryName) throws Exception {
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
