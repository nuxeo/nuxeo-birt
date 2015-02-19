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

import static org.jboss.seam.annotations.Install.FRAMEWORK;
import static org.nuxeo.ecm.platform.ui.web.component.file.InputFileMimetypeValidator.MIMETYPE_AUTHORIZED_EXTENSIONS_MESSAGE_ID;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

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
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.api.security.impl.ACLImpl;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
import org.nuxeo.ecm.platform.reporting.api.Constants;
import org.nuxeo.ecm.platform.reporting.api.ReportModel;
import org.nuxeo.ecm.platform.reporting.api.ReportService;
import org.nuxeo.ecm.platform.ui.web.component.file.InputFileChoice;
import org.nuxeo.ecm.platform.ui.web.component.file.InputFileInfo;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.webapp.contentbrowser.DocumentActions;
import org.nuxeo.runtime.api.Framework;

import com.sun.faces.util.MessageFactory;

/**
 * Seam Bean used to manage Edit form
 *
 * @author Tiry (tdelprat@nuxeo.com)
 */
@Name("reportActions")
@Scope(ScopeType.PAGE)
@Install(precedence = FRAMEWORK)
public class ReportActions implements Serializable {

    private static final long serialVersionUID = -1155863420157051403L;

    protected static final Log log = LogFactory.getLog(ReportActions.class);

    @In(create = true)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient DocumentActions documentActions;

    @In(create = true)
    protected transient UserManager userManager;

    protected String reportsContainerPath = null;

    protected DocumentModel newReportModel = null;

    protected boolean showForm = false;

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
            DocumentModel reportModelDoc = documentManager.getDocument(new IdRef(docId));
            return reportModelDoc.getAdapter(ReportModel.class);
        } catch (ClientException e) {
            return null;
        }
    }

    public DocumentModel getBareReportModel() throws ClientException {
        return documentManager.createDocumentModel(Constants.BIRT_REPORT_MODEL_TYPE);
    }

    public DocumentModel getNewReportModel() throws ClientException {
        if (newReportModel == null) {
            newReportModel = getBareReportModel();
        }
        return newReportModel;
    }

    public String saveDocument() throws ClientException {
        createReportsModelContainerIfNeeded();
        String view = documentActions.saveDocument(newReportModel);
        resetDocument();
        toggleForm();
        return view;
    }

    protected void createReportsModelContainerIfNeeded() throws ClientException {
        String path = getReportModelsContainerPath();
        if (!documentManager.exists(new PathRef(path))) {
            createReportsModelContainer(path);
        }
    }

    protected void createReportsModelContainer(String path) throws ClientException {
        new UnrestrictedReportModelsContainerCreator(documentManager, path).runUnrestricted();
    }

    protected void resetDocument() {
        newReportModel = null;
    }

    public String getReportModelsContainerPath() throws ClientException {
        if (reportsContainerPath == null) {
            ReportService reportService = Framework.getService(ReportService.class);
            reportsContainerPath = reportService.getReportModelsContainer();
        }
        return reportsContainerPath;
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

    public void validateReportExtension(FacesContext context, UIComponent component, Object value) {
        if (value instanceof InputFileInfo) {
            InputFileInfo info = (InputFileInfo) value;
            String choice = info.getConvertedChoice();
            if (!InputFileChoice.isUploadOrKeepTemp(choice)) {
                return;
            }
            String filename = info.getConvertedFilename();
            if (filename != null) {
                if (!filename.endsWith(".rptdesign")) {
                    throw new ValidatorException(MessageFactory.getMessage(context,
                            MIMETYPE_AUTHORIZED_EXTENSIONS_MESSAGE_ID, ".rptdesign"));
                }
            }
        }
    }

    public class UnrestrictedReportModelsContainerCreator extends UnrestrictedSessionRunner {

        protected String reportModelsContainerPath;

        protected UnrestrictedReportModelsContainerCreator(CoreSession session, String reportModelsContainerPath) {
            super(session);
            this.reportModelsContainerPath = reportModelsContainerPath;
        }

        @Override
        public void run() throws ClientException {
            if (!session.exists(new PathRef(reportModelsContainerPath))) {
                DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(),
                        reportModelsContainerPath.substring(1), Constants.BIRT_REPORT_MODELS_ROOT_TYPE);
                doc.setPropertyValue("dc:title", "Report Models");
                doc = session.createDocument(doc);

                ACP acp = new ACPImpl();
                ACL acl = new ACLImpl();
                for (String administratorGroup : userManager.getAdministratorsGroups()) {
                    ACE ace = new ACE(administratorGroup, SecurityConstants.EVERYTHING, true);
                    acl.add(ace);
                }
                acp.addACL(acl);
                doc.setACP(acp, true);
                session.save();
            }
        }
    }

}
