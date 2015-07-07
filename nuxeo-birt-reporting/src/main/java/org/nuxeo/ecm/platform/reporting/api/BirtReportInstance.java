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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.platform.reporting.engine.BirtEngine;
import org.nuxeo.ecm.platform.reporting.report.ReportContext;
import org.nuxeo.ecm.platform.reporting.report.ReportHelper;
import org.nuxeo.ecm.platform.reporting.report.ReportParameter;

/**
 * This is the implementation of the {@link ReportInstance} adapter. Holds most of the rendering logic.
 *
 * @author Tiry (tdelprat@nuxeo.com)
 */
public class BirtReportInstance extends BaseBirtReportAdapter implements ReportInstance {

    protected static final String PREFIX = "birt";

    public BirtReportInstance(DocumentModel doc) {
        super(doc);
    }

    @Override
    public ReportModel getModel() {
        String modelUUID = (String) doc.getPropertyValue(PREFIX + ":modelRef");
        final IdRef modelRef = new IdRef(modelUUID);

        final DocumentModel[] docs = new DocumentModel[1];
        new UnrestrictedSessionRunner(getSession()) {
            @Override
            public void run() {
                DocumentModel doc = session.getDocument(modelRef);
                doc.detach(true);
                docs[0] = doc;
            }
        }.runUnrestricted();

        return docs[0].getAdapter(ReportModel.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> getStoredParameters() {

        Map<String, String> params = new HashMap<String, String>();

        List<Map<String, Serializable>> localParams = (List<Map<String, Serializable>>) doc.getPropertyValue(PREFIX
                + ":parameters");
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
    public List<ReportParameter> getReportParameters() throws IOException {

        List<ReportParameter> params = getModel().getReportParameters();
        Map<String, String> modelParams = getModel().getStoredParameters();
        Map<String, String> localParams = getStoredParameters();

        for (ReportParameter param : params) {
            String value = modelParams.get(param.getName());
            if (value != null && !value.isEmpty()) {
                param.setValue(value);
                param.setEditable(false);
            }
            value = localParams.get(param.getName());
            if (value != null && !value.isEmpty()) {
                param.setValue(value);
            }
        }
        return params;
    }

    @Override
    public List<ReportParameter> getReportUserParameters() throws IOException {
        List<ReportParameter> params = getReportParameters();
        ReportContext.setContextualParameters(params, doc);

        List<ReportParameter> userParams = new ArrayList<ReportParameter>();
        for (ReportParameter param : params) {
            if (param.getStringValue() == null || param.getStringValue().isEmpty()) {
                userParams.add(param);
            }
        }
        return userParams;
    }

    @Override
    public void initParameterList() throws IOException {
        String oldModelRef = (String) doc.getPropertyValue(PREFIX + ":oldModelRef");
        String modelRef = (String) doc.getPropertyValue(PREFIX + ":modelRef");

        if (oldModelRef == null || oldModelRef.isEmpty() || !oldModelRef.equals(modelRef)) {
            doc.setPropertyValue(PREFIX + ":oldModelRef", modelRef);
            // get model params
            List<ReportParameter> params = getModel().getReportParameters();
            // remove all stored params
            List<Map<String, Serializable>> localParams = new ArrayList<Map<String, Serializable>>();
            doc.setPropertyValue(getPrefix() + ":parameters", (Serializable) localParams);
            // init params
            for (ReportParameter param : params) {
                setParameter(param.getName(), null, false);
            }
        }
    }

    @Override
    public void render(IRenderOption options, Map<String, Object> userParameters) throws IOException {
        // get Stored params
        List<ReportParameter> params = getReportParameters();
        // fill with user supplied parameters
        for (ReportParameter param : params) {
            if (userParameters.containsKey(param.getName())) {
                param.setObjectValue(userParameters.get(param.getName()));
            }
        }
        // replace values by contextual parameters if needed
        ReportContext.setContextualParameters(params, doc);

        InputStream report = getModel().getReportFileAsStream();
        IReportRunnable nuxeoReport = ReportHelper.getNuxeoReport(report, doc.getRepositoryName());
        IRunAndRenderTask task = BirtEngine.getBirtEngine().createRunAndRenderTask(nuxeoReport);
        Map<String, Object> birtParams = computeParametersForBirt(params);
        task.setParameterValues(birtParams);
        task.setRenderOption(options);
        try {
            task.run();
        } catch (BirtException e) {
            throw new NuxeoException(e);
        }
        task.close();
    }

    protected Map<String, Object> computeParametersForBirt(List<ReportParameter> parameters) {
        Map<String, Object> birtParameters = new HashMap<String, Object>();
        for (ReportParameter param : parameters) {
            Object value = param.getObjectValue();
            if (value instanceof Date) {
                Date date = (Date) value;
                value = new java.sql.Date(date.getTime());
            }
            birtParameters.put(param.getName(), value);
        }
        return birtParameters;
    }

    @Override
    public String getReportKey() {
        try {
            return (String) doc.getPropertyValue(PREFIX + ":reportKey");
        } catch (PropertyException e) {
            return null;
        }
    }

    @Override
    public void setReportKey(String key) {
        doc.setPropertyValue(PREFIX + ":reportKey", key);
    }

    @Override
    protected String getPrefix() {
        return PREFIX;
    }

}
