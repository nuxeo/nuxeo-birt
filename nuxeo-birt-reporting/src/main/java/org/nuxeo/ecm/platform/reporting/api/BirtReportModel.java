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

package org.nuxeo.ecm.platform.reporting.api;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.report.engine.api.IParameterDefn;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.platform.reporting.report.ReportHelper;
import org.nuxeo.ecm.platform.reporting.report.ReportParameter;

/**
 *
 * Implementation class for the {@link ReportModel} adapter interface
 *
 * @author Tiry (tdelprat@nuxeo.com)
 *
 */
public class BirtReportModel extends BaseBirtReportAdapter implements
        ReportModel {

    protected static final String PREFIX = "birtmodel";

    protected transient List<IParameterDefn> cachedParamsDef = null;

    public BirtReportModel(DocumentModel doc) {
        super(doc);
    }

    @Override
    public String getId() {
        return doc.getId();
    }

    public String getReportName() throws ClientException {
        return (String) doc.getPropertyValue(PREFIX + ":reportName");
    }

    public InputStream getReportFileAsStream() throws Exception {
        BlobHolder bh = doc.getAdapter(BlobHolder.class);
        return bh.getBlob().getStream();
    }

    public void parseParametersDefinition() throws Exception {
        List<IParameterDefn> paramsDef = getParameterDef();
        for (IParameterDefn def : paramsDef) {
            ReportParameter param = new ReportParameter(def);
            setParameter(param, false);
        }
    }

    public void updateMetadata() throws Exception {
        Map<String, String> meta = ReportHelper.getReportMetaData(getReportFileAsStream());

        String name = meta.get("displayName");
        if (name == null) {
            BlobHolder bh = doc.getAdapter(BlobHolder.class);
            name = bh.getBlob().getFilename();
        }
        doc.setPropertyValue(PREFIX + ":reportName", name);

        if (meta.get("title") != null) {
            doc.setPropertyValue("dc:title", meta.get("title"));
        }
        if (meta.get("description") != null) {
            doc.setPropertyValue("dc:description", meta.get("description"));
        }
    }

    protected List<IParameterDefn> getParameterDef() throws Exception {
        if (cachedParamsDef == null) {
            IReportRunnable report = ReportHelper.getReport(getReportFileAsStream());
            cachedParamsDef = ReportHelper.getReportParameter(report);
        }
        return cachedParamsDef;
    }

    public List<ReportParameter> getReportParameters() throws Exception {
        Map<String, String> storedParams = getStoredParameters();
        List<IParameterDefn> paramsDef = getParameterDef();

        List<ReportParameter> result = new ArrayList<ReportParameter>();
        for (IParameterDefn def : paramsDef) {
            ReportParameter param = new ReportParameter(def,
                    storedParams.get(def.getName()));
            result.add(param);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getStoredParameters() throws ClientException {
        List<Map<String, Serializable>> localParams = (List<Map<String, Serializable>>) doc.getPropertyValue(PREFIX
                + ":parameters");
        Map<String, String> params = new HashMap<String, String>();
        if (localParams != null) {
            for (Map<String, Serializable> localParam : localParams) {
                String name = (String) localParam.get("pName");
                String value = (String) localParam.get("pValue");
                if (value != null) {
                    params.put(name, value);
                }
            }
        }
        return params;
    }

    @Override
    protected String getPrefix() {
        return PREFIX;
    }

}
