/*
 * (C) Copyright 2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 * Contributors:
 *     Nuxeo - initial API and implementation
 */

package org.nuxeo.ecm.platform.reporting.seam;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.DEPLOYMENT;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.actions.Action;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 */
@Name("adminViews")
@Scope(CONVERSATION)
@Install(precedence = DEPLOYMENT)
public class AdminViewManager extends org.nuxeo.ecm.admin.AdminViewManager {

    public static final String VIEW_ADMIN = "view_admin";

    @In(create = true, required = false)
    protected transient NavigationContext navigationContext;

    protected DocumentModel lastVisitedDocument;

    public String enter() {
        lastVisitedDocument = navigationContext.getCurrentDocument();
        return VIEW_ADMIN;
    }

    @Override
    public String exit() throws ClientException {
        if (lastVisitedDocument != null) {
            return navigationContext.navigateToDocument(lastVisitedDocument);
        } else {
            return navigationContext.goHome();
        }
    }

    public String setCurrentViewIdAndRedirect(String currentViewId) {
        for (Action action : getAvailableActions()) {
            if (action.getId().equals(currentViewId)) {
                currentView = action;
                break;
            }
        }
        return VIEW_ADMIN;
    }

}
