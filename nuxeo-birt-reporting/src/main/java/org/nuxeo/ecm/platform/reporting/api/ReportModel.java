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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.reporting.report.ReportParameter;

public interface ReportModel {

    String getId();

    InputStream getReportFileAsStream() throws Exception;

    List<ReportParameter> getReportParameters() throws Exception;

    Map<String, String> getStoredParameters() throws ClientException;

    void setParameter(ReportParameter param) throws Exception;

    void setParameter(String name, Object value) throws Exception;

    String getReportName() throws ClientException;

    void parseParametersDefinition() throws Exception;

    void updateMetadata() throws Exception;

    DocumentModel getDoc();

}
