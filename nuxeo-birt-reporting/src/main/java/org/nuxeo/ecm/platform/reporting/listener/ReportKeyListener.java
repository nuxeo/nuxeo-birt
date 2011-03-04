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

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.reporting.api.Constants;
import org.nuxeo.ecm.platform.reporting.api.ReportInstance;

/**
 * Synchronous {@link EventListener} used to compute unique
 * {@link ReportInstance} keys
 *
 * @author Tiry (tdelprat@nuxeo.com)
 *
 */
public class ReportKeyListener implements EventListener {

    protected static final Log log = LogFactory.getLog(ReportKeyListener.class);

    @Override
    public void handleEvent(Event event) throws ClientException {
        EventContext ctx = event.getContext();
        if (ctx instanceof DocumentEventContext) {
            DocumentEventContext docCtx = (DocumentEventContext) ctx;
            DocumentModel doc = docCtx.getSourceDocument();

            if (doc.hasSchema(Constants.BIRT_REPORT_INSTANCE_SCHEMA)) {
                ReportInstance reportInstance = doc.getAdapter(ReportInstance.class);
                if (reportInstance != null) {
                    try {
                        // give default title if needed
                        String title = doc.getTitle();
                        if (title == null || title.isEmpty()) {
                            title = reportInstance.getModel().getDoc().getTitle();
                            doc.setPropertyValue("dc:title", title);
                        }
                        // compute report key if needed
                        String key = reportInstance.getReportKey();
                        if (key == null || key.isEmpty()) {
                            reportInstance.setReportKey(generateReportKey(reportInstance));
                        }
                    } catch (Exception e) {
                        log.error("Error while parsing report parameters", e);
                    }
                }
            }
        }
    }

    protected String generateReportKey(ReportInstance reportInstance)
            throws ClientException {
        String name = reportInstance.getModel().getReportName();
        if (name == null) {
            name = reportInstance.getModel().getDoc().getName();
        }

        StringBuffer key = new StringBuffer();
        key.append(IdUtils.generateId(name, "_", true, 20));
        key.append("-");

        Random rnd = new Random(System.currentTimeMillis());
        key.append(rnd.nextInt(1000));

        return key.toString();
    }

}
