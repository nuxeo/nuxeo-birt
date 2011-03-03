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
import java.util.Map;

import org.eclipse.birt.report.engine.api.IRenderOption;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.reporting.report.ReportParameter;

/**
 *
 * Represents a report instance that is bound to a model.
 *
 * This interface holds the main methods for managing report parameter and
 * running the rendering.
 *
 * @author Tiry (tdelprat@nuxeo.com)
 *
 */
public interface ReportInstance {

    /**
     * Return the associated {@link ReportModel}
     *
     * @return
     * @throws ClientException
     */
    ReportModel getModel() throws ClientException;

    /**
     * Get report parameters that can be entered by the user.
     *
     * This is used to generate the parameter form at rendering time.
     *
     * @return
     * @throws Exception
     */
    List<ReportParameter> getReportUserParameters() throws Exception;

    /**
     * Get All reports parameters : merging model parameters with instance
     * parameters
     *
     * @return
     * @throws Exception
     */
    List<ReportParameter> getReportParameters() throws Exception;

    /**
     * Get parameters as storef in thi resport instance
     *
     * @return
     * @throws ClientException
     */
    Map<String, String> getStoredParameters() throws ClientException;

    /**
     * Sets a parameter
     *
     * @param param
     * @throws Exception
     */
    void setParameter(ReportParameter param) throws Exception;

    /**
     * Sets a parameter
     *
     * @param name
     * @param value
     * @throws Exception
     */
    void setParameter(String name, Object value) throws Exception;

    /**
     * Starts the rendering of the report according to {@link IRenderOption}
     *
     * @param options
     * @param parameters
     * @throws Exception
     */
    void render(IRenderOption options, Map<String, Object> parameters)
            throws Exception;

    /**
     * Gives access to the underlying {@link DocumentModel}
     *
     * @return
     */
    DocumentModel getDoc();

    /**
     * Get the key used to uniquely identify the report instance
     *
     * This key is used in REST urls.
     *
     * @return
     */
    String getReportKey();

    /**
     *
     * Sets the report key (used from a Core Listener)
     *
     * @param key
     * @throws ClientException
     */
    void setReportKey(String key) throws ClientException;

    /**
     * Initialize the parameters from the associated model
     *
     * @throws Exception
     */
    void initParameterList() throws Exception;
}
