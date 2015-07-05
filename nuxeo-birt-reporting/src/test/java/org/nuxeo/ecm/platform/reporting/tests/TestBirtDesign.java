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

package org.nuxeo.ecm.platform.reporting.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.TransactionalFeature;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.reporting.engine.BirtEngine;
import org.nuxeo.ecm.platform.reporting.report.ReportHelper;
import org.nuxeo.ecm.platform.reporting.tests.TestBirtDesign.RepositoryInit;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.RuntimeHarness;

@RunWith(FeaturesRunner.class)
@Features({ TransactionalFeature.class, CoreFeature.class })
@RepositoryConfig(init = RepositoryInit.class)
@Deploy("org.nuxeo.runtime.datasource")
public class TestBirtDesign {

    @Inject
    protected RuntimeHarness harness;

    @Inject
    protected CoreFeature coreFeature;

    String reportPath = null;

    protected static DocumentModel folder1 = null;

    protected static DocumentModel file1 = null;

    public static class RepositoryInit extends DefaultRepositoryInit {

        @Override
        public void populate(CoreSession session) {
            super.populate(session);
            folder1 = session.createDocumentModel("/", "folder1", "Folder");
            folder1.setProperty("dublincore", "title", "My Super Folder");
            folder1 = session.createDocument(folder1);

            file1 = session.createDocumentModel("/", "file1", "File");
            file1.setProperty("dublincore", "title", "My Super File");
            file1 = session.createDocument(file1);

            session.save();
        }
    }

    @Before
    public void checkDB() {
        // SQL Server has problem initializing the repo when the repo datasource is reconfigured
        assumeTrue(!coreFeature.getStorageConfiguration().isVCSSQLServer());
    }

    @After
    public void cleanFilesystem() throws Exception {
        if (reportPath != null) {
            FileUtils.deleteTree(new File(reportPath));
            reportPath = null;
        }
    }

    @Test
    public void testNuxeoReport() throws Exception {
        // not done with LocalDeploy because SQL Server has problem initializing the repo
        // when the repo datasource is reconfigured
        harness.deployContrib("org.nuxeo.ecm.platform.birt.reporting.test", "repo-ds.xml");
        try {
            doTestNuxeoReport();
        } finally {
            harness.undeployContrib("org.nuxeo.ecm.platform.birt.reporting.test", "repo-ds.xml");
        }
    }

    public void doTestNuxeoReport() throws Exception {

        File report = FileUtils.getResourceFileFromContext("reports/testNX2.rptdesign");

        IReportRunnable nuxeoReport = ReportHelper.getNuxeoReport(new FileInputStream(report));

        IRunAndRenderTask task = BirtEngine.getBirtEngine().createRunAndRenderTask(nuxeoReport);

        String dirPath = new Path(System.getProperty("java.io.tmpdir")).append(
                "birt-test-report-modified" + System.currentTimeMillis()).toString();
        File baseDir = new File(dirPath);
        baseDir.mkdir();

        File imagesDir = new File(dirPath + "/images");
        imagesDir.mkdir();

        File result = new File(dirPath + "/report");

        OutputStream out = new FileOutputStream(result);

        HTMLRenderOption options = new HTMLRenderOption();
        options.setImageHandler(new HTMLServerImageHandler());
        options.setOutputFormat(IRenderOption.OUTPUT_FORMAT_HTML);
        options.setOutputStream(out);
        options.setBaseImageURL("images");
        options.setImageDirectory(imagesDir.getAbsolutePath());

        task.setRenderOption(options);

        task.run();
        task.close();

        out.close();

        String generatedHtml = FileUtils.readFile(result);
        // query result
        assertTrue(generatedHtml.contains(folder1.getId()));
        assertTrue(generatedHtml.contains(file1.getId()));

    }

    @Test
    public void testNuxeoReportWithParams() throws Exception {
        harness.deployContrib("org.nuxeo.ecm.platform.birt.reporting.test", "repo-ds.xml");
        try {
            doTestNuxeoReportWithParams();
        } finally {
            harness.undeployContrib("org.nuxeo.ecm.platform.birt.reporting.test", "repo-ds.xml");
        }
    }

    public void doTestNuxeoReportWithParams() throws Exception {

        File report = FileUtils.getResourceFileFromContext("reports/simpleVCSReport.rptdesign");

        IReportRunnable nuxeoReport = ReportHelper.getNuxeoReport(new FileInputStream(report));

        IRunAndRenderTask task = BirtEngine.getBirtEngine().createRunAndRenderTask(nuxeoReport);

        String dirPath = new Path(System.getProperty("java.io.tmpdir")).append(
                "birt-test-report-modified" + System.currentTimeMillis()).toString();
        File baseDir = new File(dirPath);
        baseDir.mkdir();

        File imagesDir = new File(dirPath + "/images");
        imagesDir.mkdir();

        File result = new File(dirPath + "/report");

        OutputStream out = new FileOutputStream(result);

        HTMLRenderOption options = new HTMLRenderOption();
        options.setImageHandler(new HTMLServerImageHandler());
        options.setOutputFormat(IRenderOption.OUTPUT_FORMAT_HTML);
        options.setOutputStream(out);
        options.setBaseImageURL("images");
        options.setImageDirectory(imagesDir.getAbsolutePath());

        Map inputValues = new HashMap();
        inputValues.put("docType", "Folder");
        task.setParameterValues(inputValues);

        task.setRenderOption(options);

        task.run();
        task.close();

        out.close();

        String generatedHtml = FileUtils.readFile(result);
        // query result
        assertTrue(generatedHtml.contains(folder1.getId()));
        assertFalse(generatedHtml.contains(file1.getId()));

    }

}
