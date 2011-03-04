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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.reporting.report.ReportParameter;

/**
 * Base class for the {@link ReportModel} and {@link ReportInstance} adapters
 * Contains common code for Parameters management.
 *
 * @author Tiry (tdelprat@nuxeo.com)
 *
 */
public abstract class BaseBirtReportAdapter {

    protected DocumentModel doc;

    public BaseBirtReportAdapter(DocumentModel doc) {
        this.doc = doc;
    }

    protected CoreSession getSession() {
        return doc.getCoreSession();
    }

    protected abstract String getPrefix();

    public abstract List<ReportParameter> getReportParameters()
            throws Exception;

    public void setParameter(ReportParameter param) throws Exception {
        setParameter(param, true);
    }

    public void setParameter(ReportParameter param, boolean save)
            throws Exception {
        setParameter(param.getName(), param.getStringValue(), save);
    }

    public void setParameter(String name, Object value) throws Exception {
        setParameter(name, value, true);
    }

    @SuppressWarnings("unchecked")
    public void setParameter(String name, Object value, boolean save)
            throws Exception {
        List<ReportParameter> reportParams = getReportParameters();
        ReportParameter targetParameter = null;
        for (ReportParameter reportParam : reportParams) {
            if (reportParam.getName().equals(name)) {
                targetParameter = reportParam;
                break;
            }
        }

        if (targetParameter == null) {
            // parameter does not exist !
            return;
        }

        targetParameter.setObjectValue(value);
        String stringValue = targetParameter.getStringValue();
        List<Map<String, Serializable>> localParams = (List<Map<String, Serializable>>) doc.getPropertyValue(getPrefix()
                + ":parameters");
        if (localParams == null) {
            localParams = new ArrayList<Map<String, Serializable>>();
        }

        boolean addParam = true;
        for (Map<String, Serializable> localParam : localParams) {
            String pName = (String) localParam.get("pName");
            if (name.equals(pName)) {
                localParam.put("pValue", stringValue);
                addParam = false;
                break;
            }
        }

        if (addParam) {
            Map<String, Serializable> newEntry = new HashMap<String, Serializable>();
            newEntry.put("pName", name);
            newEntry.put("pValue", stringValue);
            localParams.add(newEntry);
        }

        doc.setPropertyValue(getPrefix() + ":parameters",
                (Serializable) localParams);
        if (save) {
            doc = getSession().saveDocument(doc);
        }
    }

    public DocumentModel getDoc() {
        return doc;
    }

}
