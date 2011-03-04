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

import static org.nuxeo.ecm.platform.reporting.api.Constants.BIRT_REPORT_INSTANCE_SCHEMA;
import static org.nuxeo.ecm.platform.reporting.api.Constants.BIRT_REPORT_MODEL_SCHEMA;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.adapter.DocumentAdapterFactory;

/**
 *
 * Factory for the {@link ReportModel} and {@link ReportInstance} adapters
 * Adapters and bound to document types.
 *
 * @author Tiry (tdelprat@nuxeo.com)
 *
 */
public class AdapterFactory implements DocumentAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(DocumentModel doc, Class adapterClass) {
        if (doc == null) {
            return null;
        }

        String adapterClassName = adapterClass.getSimpleName();
        if (adapterClassName.equals(ReportInstance.class.getSimpleName())) {
            if (doc.hasSchema(BIRT_REPORT_INSTANCE_SCHEMA)) {
                return new BirtReportInstance(doc);
            }
        } else if (adapterClassName.equals(ReportModel.class.getSimpleName())) {
            if (doc.hasSchema(BIRT_REPORT_MODEL_SCHEMA)) {
                return new BirtReportModel(doc);
            }
        }
        return null;
    }

}
