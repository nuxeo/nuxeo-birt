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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
import org.nuxeo.ecm.platform.reporting.engine.BirtEngine;
import org.nuxeo.ecm.platform.reporting.report.ReportHelper;

public class TestBirtDesign extends SQLRepositoryTestCase {

    String reportPath = null;

    DocumentModel folder1 = null;

    DocumentModel file1 = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        openSession();

        folder1 = session.createDocumentModel("/", "folder1", "Folder");
        folder1.setProperty("dublincore", "title", "My Super Folder");
        folder1 = session.createDocument(folder1);

        file1 = session.createDocumentModel("/", "file1", "File");
        file1.setProperty("dublincore", "title", "My Super File");
        file1 = session.createDocument(file1);

        session.save();

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (reportPath != null) {
            FileUtils.deleteTree(new File(reportPath));
            reportPath = null;
        }
    }

    public void testNuxeoReport() throws Exception {

        File report = FileUtils.getResourceFileFromContext("reports/testNX2.rptdesign");

        IReportRunnable nuxeoReport = ReportHelper.getNuxeoReport(new FileInputStream(
                report));

        IRunAndRenderTask task = BirtEngine.getBirtEngine().createRunAndRenderTask(
                nuxeoReport);

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
        options.setOutputFormat(HTMLRenderOption.OUTPUT_FORMAT_HTML);
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

    public void testNuxeoReportWithParams() throws Exception {

        File report = FileUtils.getResourceFileFromContext("reports/simpleVCSReport.rptdesign");

        IReportRunnable nuxeoReport = ReportHelper.getNuxeoReport(new FileInputStream(
                report));

        IRunAndRenderTask task = BirtEngine.getBirtEngine().createRunAndRenderTask(
                nuxeoReport);

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
        options.setOutputFormat(HTMLRenderOption.OUTPUT_FORMAT_HTML);
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
