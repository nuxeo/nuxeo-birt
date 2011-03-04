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

package org.nuxeo.ecm.platform.reporting.listener;

import static org.nuxeo.ecm.platform.reporting.api.Constants.BIRT_REPORT_INSTANCE_SCHEMA;
import static org.nuxeo.ecm.platform.reporting.api.Constants.BIRT_REPORT_MODEL_SCHEMA;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.reporting.api.ReportInstance;
import org.nuxeo.ecm.platform.reporting.api.ReportModel;

/**
 * Synchronous {@link EventListener} used to extract ReportParameters from the
 * Birt ReportDesign file
 *
 * @author Tiry (tdelprat@nuxeo.com)
 *
 */
public class ReportParserListener implements EventListener {

    protected static final Log log = LogFactory.getLog(ReportParserListener.class);

    @Override
    public void handleEvent(Event event) throws ClientException {

        EventContext ctx = event.getContext();
        if (ctx instanceof DocumentEventContext) {
            DocumentEventContext docCtx = (DocumentEventContext) ctx;
            DocumentModel doc = docCtx.getSourceDocument();

            if (doc.hasSchema(BIRT_REPORT_MODEL_SCHEMA)) {
                ReportModel reportModel = doc.getAdapter(ReportModel.class);
                if (reportModel != null) {
                    try {
                        if (doc.getProperty("file:content").isDirty()) {
                            reportModel.parseParametersDefinition();
                            reportModel.updateMetadata();
                        }
                    } catch (Exception e) {
                        log.error(
                                "Error while parsing report model parameters",
                                e);
                    }
                }
            } else if (doc.hasSchema(BIRT_REPORT_INSTANCE_SCHEMA)) {
                ReportInstance reportInstance = doc.getAdapter(ReportInstance.class);
                if (reportInstance != null) {
                    try {
                        reportInstance.initParameterList();
                    } catch (Exception e) {
                        log.error("Error initializing report parameters", e);
                    }
                }
            }
        }
    }

}
