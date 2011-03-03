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

package org.nuxeo.ecm.platform.reporting.engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.IPlatformContext;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.model.api.DesignConfig;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.IDesignEngineFactory;
import org.nuxeo.common.utils.FileUtils;

/**
 * This is a Singleton used to trigger BIRT deployment and get access to the
 * Reporting and Design engine
 *
 * @author Tiry (tdelprat@nuxeo.com)
 *
 */
public class BirtEngine {

    private static IReportEngine birtEngine = null;

    private static IDesignEngine birtDesignEngine = null;

    private static Properties configProps = new Properties();

    private final static String configFile = "BirtConfig.properties";

    public static synchronized void initBirtConfig() {
        loadEngineProps();
    }

    protected static IPlatformContext contextDeployer = null;

    public static synchronized IReportEngine getBirtEngine() {
        if (birtEngine == null) {
            EngineConfig config = new EngineConfig();
            if (configProps != null) {
                String logLevel = configProps.getProperty("logLevel");
                Level level = Level.OFF;
                if ("SEVERE".equalsIgnoreCase(logLevel)) {
                    level = Level.SEVERE;
                } else if ("WARNING".equalsIgnoreCase(logLevel)) {
                    level = Level.WARNING;
                } else if ("INFO".equalsIgnoreCase(logLevel)) {
                    level = Level.INFO;
                } else if ("CONFIG".equalsIgnoreCase(logLevel)) {
                    level = Level.CONFIG;
                } else if ("FINE".equalsIgnoreCase(logLevel)) {
                    level = Level.FINE;
                } else if ("FINER".equalsIgnoreCase(logLevel)) {
                    level = Level.FINER;
                } else if ("FINEST".equalsIgnoreCase(logLevel)) {
                    level = Level.FINEST;
                } else if ("OFF".equalsIgnoreCase(logLevel)) {
                    level = Level.OFF;
                }

                config.setLogConfig(configProps.getProperty("logDirectory"),
                        level);
            }

            config.setEngineHome("");

            contextDeployer = new BirtFSDeployer();
            config.setBIRTHome(contextDeployer.getPlatform());
            config.setPlatformContext(contextDeployer);

            try {
                Platform.startup(config);
            } catch (BirtException e) {
                e.printStackTrace();
            }

            IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
            birtEngine = factory.createReportEngine(config);

            DesignConfig dconfig = new DesignConfig();
            IDesignEngineFactory df = (IDesignEngineFactory) Platform.createFactoryObject(IDesignEngineFactory.EXTENSION_DESIGN_ENGINE_FACTORY);
            birtDesignEngine = df.createDesignEngine(dconfig);
        }
        return birtEngine;
    }

    public static synchronized IDesignEngine getBirtDesignEngine() {
        if (birtDesignEngine == null) {
            getBirtEngine();
        }
        return birtDesignEngine;
    }

    public static synchronized void destroyBirtEngine() {
        if (birtEngine == null) {
            return;
        }
        String deployPath = null;
        if (contextDeployer != null) {
            deployPath = contextDeployer.getPlatform();
        }
        birtEngine.shutdown();
        Platform.shutdown();
        birtEngine = null;

        if (deployPath != null) {
            FileUtils.deleteTree(new File(deployPath));
        }
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    private static void loadEngineProps() {
        try {
            // Config File must be in classpath
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream in = null;
            in = cl.getResourceAsStream(configFile);
            if (in != null) {
                configProps.load(in);
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
