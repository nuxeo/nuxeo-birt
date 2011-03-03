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

package org.nuxeo.ecm.platform.reporting.jaxrs;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.platform.reporting.api.ReportInstance;
import org.nuxeo.ecm.platform.reporting.api.ReportService;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;
import org.nuxeo.runtime.api.Framework;

/**
 * Root module to give access to Report Listing and report execution
 *
 * @author Tiry (tdelprat@nuxeo.com)
 *
 */
@WebObject(type = "reports")
public class ReportModule extends ModuleRoot {

    protected ReportInstance findReport(String idOrKey) throws ClientException {

        boolean useIdResolution = true;
        try {
            UUID.fromString(idOrKey);
        } catch (IllegalArgumentException e) {
            useIdResolution = false;
        }

        CoreSession session = getContext().getCoreSession();
        if (useIdResolution) {
            IdRef ref = new IdRef(idOrKey);
            if (!session.exists(ref)) {
                return null;
            }
            DocumentModel reportDoc = session.getDocument(ref);
            return reportDoc.getAdapter(ReportInstance.class);
        } else {
            ReportService rs = Framework.getLocalService(ReportService.class);
            return rs.getReportInstanceByKey(session, idOrKey);
        }
    }

    @GET
    @Path("list")
    @Produces("text/html")
    public Object listReports() {

        // get report list by model

        return null;
    }

    @Path(value = "{reportIdOrName}")
    public Object getReport(@PathParam("reportIdOrName") String reportIdOrName)
            throws ClientException {

        ReportInstance report = findReport(reportIdOrName);
        if (report == null) {
            return Response.status(404).build();
        }

        return ctx.newObject("report", report);

    }

}
