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

package org.nuxeo.ecm.platform.reporting.seam;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.platform.reporting.api.Constants;
import org.nuxeo.ecm.platform.reporting.api.ReportModel;
import org.nuxeo.ecm.platform.reporting.api.ReportService;
import org.nuxeo.ecm.webapp.contentbrowser.DocumentActions;
import org.nuxeo.runtime.api.Framework;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

/**
 * Seam Bean used to manage Edit form
 *
 * @author Tiry (tdelprat@nuxeo.com)
 *
 */
@Name("reportActions")
@Scope(ScopeType.PAGE)
@Install(precedence = FRAMEWORK)
public class ReportActions implements Serializable {

    private static final long serialVersionUID = -1155863420157051403L;

    protected static final Log log = LogFactory.getLog(ReportActions.class);

    protected DocumentModel currentCreationReportModel = null;

    protected boolean showForm = false;

    protected String defaultPath = null;

    @In(create = true)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient DocumentActions documentActions;

    @Factory(value = "reportModels", scope = ScopeType.EVENT, autoCreate = true)
    public List<ReportModel> getAvailableModels() {
        try {
            ReportService rs = Framework.getLocalService(ReportService.class);
            return rs.getReportAvailableModels(documentManager);
        } catch (ClientException e) {
            log.error("Error while getting reports models", e);
            return new ArrayList<ReportModel>();
        }
    }

    public ReportModel getReportModel(String docId) {
        try {
            DocumentModel reportModelDoc = documentManager.getDocument(new IdRef(
                    docId));
            return reportModelDoc.getAdapter(ReportModel.class);
        } catch (Exception e) {
            return null;
        }
    }

    public DocumentModel getBareReportModel() throws ClientException {
        return documentManager.createDocumentModel(
                Constants.BIRT_REPORT_MODEL_TYPE);
    }

    public DocumentModel getCurrentCreationReportModel() throws ClientException {
        if (currentCreationReportModel == null) {
            currentCreationReportModel = getBareReportModel();
        }
        return currentCreationReportModel;
    }

    public void saveDocument() throws ClientException {
        documentActions.saveDocument(currentCreationReportModel);
        resetDocument();
        toggleForm();
    }

    private void resetDocument() {
        currentCreationReportModel = null;
    }

    public String getDefaultPath() throws Exception {
        if (defaultPath == null) {
            defaultPath = Framework.getService(ReportService.class).getReportModelRoot();
        }
        return defaultPath;
    }

    public boolean isShowForm() {
        return showForm;
    }

    public void toggleForm() {
        showForm = !showForm;
    }

    public void toggleAndReset() {
        toggleForm();
        resetDocument();
    }
}
