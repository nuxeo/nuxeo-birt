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

import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Holds the logic to extract parameters from the Document Context.
 *
 * @author Tiry (tdelprat@nuxeo.com)
 *
 */
public class ReportContext {

    public static final String DOMAIN_DOCUMENT_TYPE = "Domain";

    public static final String WORKSPACE_DOCUMENT_TYPE = "Workspace";

    public static final String USER_NAME = "userName";

    public static final String DOC_TYPE = "docType";

    public static final String CURRENT_PATH = "currentPath";

    public static final String CURRENT_REPOSITORY = "currentRepository";

    public static final String CURRENT_SUPERSPACE_PATH = "currentSuperspacePath";

    public static final String CURRENT_SUPERSPACE_ID = "currentSuperspaceId";

    public static final String CURRENT_WORKSPACE_PATH = "currentWorkspacePath";

    public static final String CURRENT_WORKSPACE_ID = "currentWorkspaceId";

    public static final String CURRENT_DOMAIN_PATH = "currentDomainPath";

    public static final String CURRENT_DOMAIN_ID = "currentDomainPath";

    public static void setContextualParameters(
            List<ReportParameter> reportParams, DocumentModel doc)
            throws Exception {
        DocumentModel currentDomain = getFirstParentWithType(doc, DOMAIN_DOCUMENT_TYPE);
        DocumentModel currentWorkspace = getFirstParentWithType(doc, WORKSPACE_DOCUMENT_TYPE);
        DocumentModel currentSuperSpace = getCurrentSuperSpace(doc);

        for (ReportParameter param : reportParams) {
            if (param.getName().equals(USER_NAME)) {
                param.setValue(doc.getCoreSession().getPrincipal().getName());
            } else if (param.getName().equals(DOC_TYPE)) {
                param.setValue(doc.getType());
            } else if (param.getName().equals(CURRENT_PATH)) {
                param.setValue(doc.getPathAsString());
            } else if (param.getName().equals(CURRENT_REPOSITORY)) {
                param.setValue(doc.getRepositoryName());
            } else if (param.getName().equals(CURRENT_DOMAIN_PATH)) {
                if (currentDomain != null) {
                    param.setValue(currentDomain.getPathAsString());
                }
            } else if (param.getName().equals(CURRENT_DOMAIN_ID)) {
                if (currentDomain != null) {
                    param.setValue(currentDomain.getId());
                }
            } else if (param.getName().equals(CURRENT_WORKSPACE_PATH)) {
                if (currentWorkspace != null) {
                    param.setValue(currentWorkspace.getPathAsString());
                }
            } else if (param.getName().equals(CURRENT_WORKSPACE_ID)) {
                if (currentWorkspace != null) {
                    param.setValue(currentWorkspace.getId());
                }
            } else if (param.getName().equals(CURRENT_SUPERSPACE_PATH)) {
                if (currentSuperSpace != null) {
                    param.setValue(currentSuperSpace.getPathAsString());
                }
            } else if (param.getName().equals(CURRENT_SUPERSPACE_ID)) {
                if (currentSuperSpace != null) {
                    param.setValue(currentSuperSpace.getId());
                }
            }
        }
    }

    private static DocumentModel getFirstParentWithType(DocumentModel doc, String type) throws ClientException {
        List<DocumentModel> parents = doc.getCoreSession().getParentDocuments(
                doc.getRef());
        for (DocumentModel parent : parents) {
            if (parent.getType().equals(type)) {
                return parent;
            }
        }
        return null;
    }

    private static DocumentModel getCurrentSuperSpace(DocumentModel doc) throws ClientException {
        return doc.getCoreSession().getSuperSpace(doc);
    }

}
