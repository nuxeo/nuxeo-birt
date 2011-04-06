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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.reporting.api.ReportInstance;
import org.nuxeo.ecm.platform.reporting.api.ReportModel;
import org.nuxeo.ecm.platform.reporting.api.ReportService;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * Component Implementation of the {@link ReportService} interface.
 *
 * Mainly encapsulate NXQL queries.
 *
 * @author Tiry (tdelprat@nuxeo.com)
 *
 */
public class ReportComponent extends DefaultComponent implements ReportService {

    protected static final Log log = LogFactory.getLog(ReportComponent.class);

    public static final String BIRT_REPORTS_CONTAINER_PATH = "/report-models";

    @Override
    public void activate(ComponentContext context) throws Exception {
    }

    @Override
    public void deactivate(ComponentContext context) throws Exception {
        BirtEngine.destroyBirtEngine();
    }

    @Override
    public List<ReportInstance> getReportInstanceByModelName(
            CoreSession session, String reportModelName) throws ClientException {
        String uuid = getReportModelByName(session, reportModelName).getId();
        String query = "SELECT * FROM BirtReport WHERE birt:modelRef='" + uuid
                + "'";
        DocumentModelList reports = session.query(query);
        List<ReportInstance> result = new ArrayList<ReportInstance>();
        for (DocumentModel doc : reports) {
            ReportInstance report = doc.getAdapter(ReportInstance.class);
            if (report != null) {
                result.add(report);
            }
        }
        return result;
    }

    @Override
    public ReportInstance getReportInstanceByKey(CoreSession session, String key)
            throws ClientException {
        String query = "SELECT * FROM BirtReport WHERE birt:reportKey='" + key
                + "'";
        DocumentModelList reports = session.query(query);
        if (reports.isEmpty()) {
            return null;
        }
        return reports.get(0).getAdapter(ReportInstance.class);
    }

    @Override
    public String getReportModelsContainer() {
        return BIRT_REPORTS_CONTAINER_PATH;
    }

    @Override
    public ReportModel getReportModelByName(CoreSession session,
            String reportModelName) throws ClientException {
        String query = "SELECT * FROM BirtReportModel WHERE birtmodel:reportName='"
                + reportModelName + "'";
        DocumentModelList reports = session.query(query);
        if (reports.isEmpty()) {
            return null;
        }
        return reports.get(0).getAdapter(ReportModel.class);
    }

    public List<ReportModel> getReportAvailableModels(CoreSession session)
            throws ClientException {
        String query = "SELECT * FROM BirtReportModel WHERE ecm:currentLifeCycleState != 'deleted'";
        DocumentModelList reports = session.query(query);
        List<ReportModel> result = new ArrayList<ReportModel>();
        for (DocumentModel doc : reports) {
            ReportModel report = doc.getAdapter(ReportModel.class);
            if (report != null) {
                result.add(report);
            }
        }
        return result;
    }

}
