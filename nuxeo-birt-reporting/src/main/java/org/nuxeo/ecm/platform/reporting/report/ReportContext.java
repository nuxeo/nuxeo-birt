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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

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

    public static final String CURRENT_SUPERSPACE_PATH = "currentSuperSpacePath";

    public static final String CURRENT_SUPERSPACE_ID = "currentSuperSpaceId";

    public static final String CURRENT_WORKSPACE_PATH = "currentWorkspacePath";

    public static final String CURRENT_WORKSPACE_ID = "currentWorkspaceId";

    public static final String CURRENT_DOMAIN_PATH = "currentDomainPath";

    public static final String CURRENT_DOMAIN_ID = "currentDomainId";

    public static final Pattern PATTERN_TO_CHECK = Pattern.compile("\\$\\{(\\w+)\\}");

    public static void setContextualParameters(
            List<ReportParameter> reportParams, DocumentModel doc)
            throws Exception {
        Map<String, String> contextualParameters = buildContextualParametersMap(doc);
        for (ReportParameter parameter : reportParams) {
            String value = parameter.getStringValue();
            if (value != null) {
                Matcher matcher = PATTERN_TO_CHECK.matcher(value);
                if (matcher.matches()) {
                    String parameterName = matcher.group(1);
                    if (contextualParameters.containsKey(parameterName)) {
                        parameter.setValue(contextualParameters.get(parameterName));
                    }
                }
            }
        }
    }

    private static Map<String, String> buildContextualParametersMap(
            DocumentModel doc) throws ClientException {
        Map<String, String> contextualParameters = new HashMap<String, String>();
        contextualParameters.put(USER_NAME,
                doc.getCoreSession().getPrincipal().getName());
        contextualParameters.put(DOC_TYPE, doc.getType());
        contextualParameters.put(CURRENT_PATH, doc.getPathAsString());
        contextualParameters.put(CURRENT_REPOSITORY, doc.getRepositoryName());

        DocumentModel currentDomain = getFirstParentWithType(doc,
                DOMAIN_DOCUMENT_TYPE);
        if (currentDomain != null) {
            contextualParameters.put(CURRENT_DOMAIN_ID, currentDomain.getId());
            contextualParameters.put(CURRENT_DOMAIN_PATH,
                    currentDomain.getPathAsString());
        }

        DocumentModel currentWorkspace = getFirstParentWithType(doc,
                WORKSPACE_DOCUMENT_TYPE);
        if (currentWorkspace != null) {
            contextualParameters.put(CURRENT_WORKSPACE_ID,
                    currentWorkspace.getId());
            contextualParameters.put(CURRENT_WORKSPACE_PATH,
                    currentWorkspace.getPathAsString());
        }

        DocumentModel currentSuperSpace = getCurrentSuperSpace(doc);
        if (currentSuperSpace != null) {
            contextualParameters.put(CURRENT_SUPERSPACE_ID,
                    currentSuperSpace.getId());
            contextualParameters.put(CURRENT_SUPERSPACE_PATH,
                    currentSuperSpace.getPathAsString());
        }
        return contextualParameters;
    }

    private static DocumentModel getFirstParentWithType(DocumentModel doc,
            String type) throws ClientException {
        List<DocumentModel> parents = doc.getCoreSession().getParentDocuments(
                doc.getRef());
        for (DocumentModel parent : parents) {
            if (parent.getType().equals(type)) {
                return parent;
            }
        }
        return null;
    }

    private static DocumentModel getCurrentSuperSpace(DocumentModel doc)
            throws ClientException {
        return doc.getCoreSession().getSuperSpace(doc);
    }

}
