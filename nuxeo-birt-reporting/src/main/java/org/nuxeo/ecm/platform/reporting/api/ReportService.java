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

import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;

/**
 *
 * Service interface to do Report adapters lookups.
 *
 * Main rendering and parameter management APIS are directly hold by adapters
 *
 * @author Tiry (tdelprat@nuxeo.com)
 *
 */
public interface ReportService {

    ReportModel getReportModelByName(CoreSession session, String reportModelName)
            throws ClientException;

    List<ReportInstance> getReportInstanceByModelName(CoreSession session,
            String reportModelName) throws ClientException;

    List<ReportModel> getReportAvailableModels(CoreSession session)
            throws ClientException;

    ReportInstance getReportInstanceByKey(CoreSession session, String key)
            throws ClientException;

    String getReportModelsContainer();
}
