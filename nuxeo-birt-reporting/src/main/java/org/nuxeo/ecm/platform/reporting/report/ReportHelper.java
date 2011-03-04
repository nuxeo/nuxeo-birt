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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefn;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.OdaDataSourceHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.SessionHandle;
import org.eclipse.birt.report.model.api.metadata.IElementPropertyDefn;
import org.eclipse.birt.report.model.elements.OdaDataSource;
import org.eclipse.birt.report.model.metadata.ElementPropertyDefn;
import org.nuxeo.common.utils.Base64;
import org.nuxeo.ecm.platform.reporting.datasource.DSHelper;
import org.nuxeo.ecm.platform.reporting.datasource.NuxeoDSConfig;
import org.nuxeo.ecm.platform.reporting.engine.BirtEngine;

import com.ibm.icu.util.ULocale;

/**
 * Helper class to make Birt API easier to use
 *
 * @author Tiry (tdelprat@nuxeo.com)
 *
 */
public class ReportHelper {

    protected static final Log log = LogFactory.getLog(ReportHelper.class);

    public static IReportRunnable getReport(String reportPath)
            throws EngineException {
        return BirtEngine.getBirtEngine().openReportDesign(reportPath);
    }

    public static IReportRunnable getReport(InputStream stream)
            throws EngineException {
        return BirtEngine.getBirtEngine().openReportDesign(stream);
    }

    public static IReportRunnable getNuxeoReport(InputStream stream)
            throws Exception {
        return getNuxeoReport(stream, null);
    }

    public static Map<String, String> getReportMetaData(InputStream stream)
            throws Exception {
        IDesignEngine dEngine = BirtEngine.getBirtDesignEngine();
        SessionHandle sh = dEngine.newSessionHandle(ULocale.ENGLISH);
        ReportDesignHandle designHandle = sh.openDesign((String) null, stream);

        Map<String, String> meta = new HashMap<String, String>();
        meta.put("title", designHandle.getTitle());
        meta.put("author", designHandle.getAuthor());
        meta.put("description", designHandle.getDescription());
        meta.put("displayName", designHandle.getDisplayName());

        sh.closeAll(false);
        return meta;
    }

    public static IReportRunnable getNuxeoReport(InputStream stream,
            String repositoryName) throws Exception {
        IDesignEngine dEngine = BirtEngine.getBirtDesignEngine();
        SessionHandle sh = dEngine.newSessionHandle(ULocale.ENGLISH);
        ReportDesignHandle designHandle = sh.openDesign((String) null, stream);

        for (Iterator i = designHandle.getDataSources().iterator(); i.hasNext();) {
            OdaDataSourceHandle dsh = (OdaDataSourceHandle) i.next();
            NuxeoDSConfig newConfig = DSHelper.getReplacementDS(dsh.getName(),
                    repositoryName);
            if (newConfig != null) {
                OdaDataSource ds = (OdaDataSource) dsh.getElement();
                log.debug("Editing ODA datasource " + ds.getName());
                List<IElementPropertyDefn> propNames = ds.getPropertyDefns();
                for (IElementPropertyDefn propName : propNames) {
                    ElementPropertyDefn prop = ds.getPropertyDefn(propName.getName());
                    if (propName.getName().equals("odaDriverClass")) {
                        log.debug(" ->changing odaDriverClass to "
                                + newConfig.getDriverClass());
                        ds.setProperty(prop, newConfig.getDriverClass());
                    } else if (propName.getName().equals("odaURL")) {
                        log.debug(" ->changing odaURL to " + newConfig.getUrl());
                        ds.setProperty(prop, newConfig.getUrl());
                    } else if (propName.getName().equals("odaUser")) {
                        log.debug(" ->changing odaUser to "
                                + newConfig.getUserName());
                        ds.setProperty(prop, newConfig.getUserName());
                    } else if (propName.getName().equals("odaPassword")) {
                        if (prop.isEncryptable()) {
                            String b64pwd = Base64.encodeBytes(newConfig.getPassword().getBytes());
                            ds.setProperty(prop, b64pwd);
                        } else {
                            ds.setProperty(prop, newConfig.getPassword());
                        }
                    }
                }
            }
        }

        IReportRunnable modifiedReport = BirtEngine.getBirtEngine().openReportDesign(
                designHandle);
        // Can we really?
        sh.closeAll(false);

        return modifiedReport;
    }

    public static List<IParameterDefn> getReportParameter(IReportRunnable report)
            throws EngineException {
        List<IParameterDefn> params = new ArrayList<IParameterDefn>();
        IGetParameterDefinitionTask task = BirtEngine.getBirtEngine().createGetParameterDefinitionTask(
                report);
        for (Object paramDefn : task.getParameterDefns(false)) {
            if (paramDefn instanceof IParameterDefn) {
                IParameterDefn param = (IParameterDefn) paramDefn;
                params.add(param);
            }
        }
        return params;
    }

}
