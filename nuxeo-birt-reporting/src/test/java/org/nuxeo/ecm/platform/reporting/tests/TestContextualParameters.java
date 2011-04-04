/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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

package org.nuxeo.ecm.platform.reporting.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.reporting.api.ReportInstance;
import org.nuxeo.ecm.platform.reporting.report.ReportContext;
import org.nuxeo.ecm.platform.reporting.report.ReportParameter;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(init = BirtRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.core.convert.plugins",
        "org.nuxeo.ecm.platform.birt.reporting" })
public class TestContextualParameters {

    @Inject
    private CoreSession session;

    @Test
    public void contextualParametersShouldBeReplacedInsideWorkspace()
            throws Exception {
        DocumentModel instance = createReportInsideWorkspace();
        ReportInstance reportInstance = instance.getAdapter(ReportInstance.class);

        reportInstance.setParameter("docType", "${docType}");
        reportInstance.setParameter("modelParam", "${currentDomainId}");
        reportInstance.setParameter("instanceParam", "${currentWorkspaceId}");
        reportInstance.setParameter("userParam", "${currentSuperSpaceId}");

        List<ReportParameter> reportParameters = reportInstance.getReportParameters();
        ReportContext.setContextualParameters(reportParameters, instance);

        DocumentModel domain = session.getDocument(new PathRef(
                "/default-domain"));
        DocumentModel workspace = session.getDocument(new PathRef(
                "/default-domain/workspaces/workspace"));
        assertEquals(instance.getType(),
                reportParameters.get(0).getStringValue());
        assertEquals(domain.getId(), reportParameters.get(1).getStringValue());
        assertEquals(workspace.getId(),
                reportParameters.get(2).getStringValue());
        assertEquals(workspace.getId(),
                reportParameters.get(3).getStringValue());
    }

    private DocumentModel createReportInsideWorkspace() throws ClientException {
        DocumentModel reportModel = createReportModel();
        return createReport(reportModel.getId(),
                "/default-domain/workspaces/workspace");
    }

    private DocumentModel createReportModel() throws ClientException {
        DocumentModel model = session.createDocumentModel("/", "model",
                "BirtReportModel");
        File report = FileUtils.getResourceFileFromContext("reports/VCSReportWithParams.rptdesign");
        model.setPropertyValue("dc:title", "My model");
        model.setPropertyValue("birtmodel:reportName", "VCSReportWithParams");
        model.setPropertyValue("file:content", new FileBlob(report));
        model = session.createDocument(model);
        session.save();
        return model;
    }

    private DocumentModel createReport(String reportModelId, String path)
            throws ClientException {
        DocumentModel instance = session.createDocumentModel(path, "instance",
                "BirtReport");
        instance.setPropertyValue("dc:title", "My instance");
        instance.setPropertyValue("birt:modelRef", reportModelId);
        instance = session.createDocument(instance);
        session.save();
        return instance;
    }

    private DocumentModel createReportOutsideWorkspace() throws ClientException {
        DocumentModel reportModel = createReportModel();
        return createReport(reportModel.getId(), "/");
    }

    @Test
    public void contextualParametersShouldNotBeReplacedOutsideWorkspace()
            throws Exception {
        DocumentModel instance = createReportOutsideWorkspace();
        ReportInstance reportInstance = instance.getAdapter(ReportInstance.class);

        reportInstance.setParameter("docType", "${docType}");
        reportInstance.setParameter("modelParam", "${currentDomainId}");
        reportInstance.setParameter("instanceParam", "${currentWorkspaceId}");
        reportInstance.setParameter("userParam", "${currentSuperSpaceId}");

        List<ReportParameter> reportParameters = reportInstance.getReportParameters();
        ReportContext.setContextualParameters(reportParameters, instance);

        assertEquals(instance.getType(),
                reportParameters.get(0).getStringValue());
        assertEquals("${currentDomainId}",
                reportParameters.get(1).getStringValue());
        assertEquals("${currentWorkspaceId}",
                reportParameters.get(2).getStringValue());
        assertEquals(session.getRootDocument().getId(),
                reportParameters.get(3).getStringValue());
    }

    @Test
    public void unknownParametersShouldNotBeReplaced() throws Exception {
        DocumentModel instance = createReportInsideWorkspace();
        ReportInstance reportInstance = instance.getAdapter(ReportInstance.class);

        reportInstance.setParameter("docType", "${docType}");
        reportInstance.setParameter("modelParam", "${currentDomainPath}");
        reportInstance.setParameter("instanceParam", "${unknownParameter}");

        List<ReportParameter> reportParameters = reportInstance.getReportParameters();
        ReportContext.setContextualParameters(reportParameters, instance);

        DocumentModel domain = session.getDocument(new PathRef(
                "/default-domain"));
        assertEquals(instance.getType(),
                reportParameters.get(0).getStringValue());
        assertEquals(domain.getPathAsString(),
                reportParameters.get(1).getStringValue());
        assertEquals("${unknownParameter}",
                reportParameters.get(2).getStringValue());
    }

}
