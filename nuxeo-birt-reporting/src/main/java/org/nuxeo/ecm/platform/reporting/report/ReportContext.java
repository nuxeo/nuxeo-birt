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

import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Holds the logic to extract parameters from  the Document Context.
 *
 * @author Tiry (tdelprat@nuxeo.com)
 *
 */
public class ReportContext {

    public static String USER_NAME = "userName";

    public static String DOC_TYPE = "docType";

    public static String CURRENT_PATH = "currentPath";

    public static String CURRENT_REPOSITORY = "currentRepository";

    public static String CURRENT_WORKSPACE_PATH = "currentWorkspacePath";

    public static String CURRENT_DOMAIN_PATH = "currentDomainPath";

    public static void setContextualParameters(List<ReportParameter> reportParams, DocumentModel doc) throws Exception {
        for (ReportParameter param : reportParams) {

            if (param.getName().equals(USER_NAME)) {
                param.setValue(doc.getCoreSession().getPrincipal().getName());
            } else if (param.getName().equals(DOC_TYPE)) {
                param.setValue( doc.getType());
            } else if (param.getName().equals(CURRENT_PATH)) {
                param.setValue(doc.getPathAsString());
            } else if (param.getName().equals(CURRENT_REPOSITORY)) {
                param.setValue(doc.getRepositoryName());
            } else if (param.getName().equals(CURRENT_WORKSPACE_PATH)) {
                List<DocumentModel> parents = doc.getCoreSession().getParentDocuments(doc.getRef());
                for (DocumentModel parent : parents) {
                    if (parent.getType().equals("Workspace")) {
                        param.setValue(parent.getPathAsString());
                        break;
                    }
                }
            } else if (param.getName().equals(CURRENT_DOMAIN_PATH)) {
                List<DocumentModel> parents = doc.getCoreSession().getParentDocuments(doc.getRef());
                for (DocumentModel parent : parents) {
                    if (parent.getType().equals("Domain")) {
                        param.setValue(parent.getPathAsString());
                        break;
                    }
                }
            }
        }
    }


}
