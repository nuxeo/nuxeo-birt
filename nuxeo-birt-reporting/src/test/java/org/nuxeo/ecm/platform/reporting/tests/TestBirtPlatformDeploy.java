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
import java.util.List;

import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IParameterDefn;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.platform.reporting.engine.BirtEngine;
import org.nuxeo.ecm.platform.reporting.report.ReportHelper;
import org.nuxeo.runtime.test.NXRuntimeTestCase;

public class TestBirtPlatformDeploy extends NXRuntimeTestCase {

    String reportPath = null;

    public void testDeploy() {
        IReportEngine engine = BirtEngine.getBirtEngine();
        assertNotNull(engine);
        System.out.println("Birt Engine started");
        System.out.println(engine.getVersion());
        System.out.println(engine.getConfig());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (reportPath != null) {
            FileUtils.deleteTree(new File(reportPath));
            reportPath = null;
        }
    }

    public void testLoadReport() throws Exception {

        File report = FileUtils.getResourceFileFromContext("reports/test1.rptdesign");
        assertNotNull(report);
        IReportRunnable runnableReport = ReportHelper.getReport(new FileInputStream(
                report));
        assertNotNull(runnableReport);
        System.out.println(runnableReport.getReportName());

    }

    public void testGetReportParameters() throws Exception {

        File report = FileUtils.getResourceFileFromContext("reports/test1.rptdesign");
        assertNotNull(report);
        IReportRunnable runnableReport = ReportHelper.getReport(new FileInputStream(
                report));
        List<IParameterDefn> params = ReportHelper.getReportParameter(runnableReport);
        assertNotNull(params);
        assertEquals(2, params.size());
        for (IParameterDefnBase param : params) {
            System.out.print(param.getDisplayName());
            System.out.print(" - ");
            System.out.println(param.getName());
        }
    }

    public void testReportHtmlRendering() throws Exception {
        File report = FileUtils.getResourceFileFromContext("reports/test.rptdesign");
        IReportRunnable runnableReport = ReportHelper.getReport(new FileInputStream(
                report));
        IRunAndRenderTask task = BirtEngine.getBirtEngine().createRunAndRenderTask(
                runnableReport);

        String dirPath = new Path(System.getProperty("java.io.tmpdir")).append(
                "birt-test-report" + System.currentTimeMillis()).toString();
        reportPath = dirPath;
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

    }

    public void testReportHtmlRenderingWithParams() throws Exception {
        File report = FileUtils.getResourceFileFromContext("reports/test1.rptdesign");

        IReportRunnable runnableReport = ReportHelper.getReport(new FileInputStream(
                report));
        IRunAndRenderTask task = BirtEngine.getBirtEngine().createRunAndRenderTask(
                runnableReport);

        String dirPath = new Path(System.getProperty("java.io.tmpdir")).append(
                "birt-test-report" + System.currentTimeMillis()).toString();
        reportPath = dirPath;
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

        HashMap inputValues = new HashMap();
        inputValues.put("Cust", 1);

        task.setParameterValues(inputValues);

        task.setRenderOption(options);

        task.run();
        task.close();

        out.close();

    }

    public void testReportPDFRenderingWithParams() throws Exception {
        File report = FileUtils.getResourceFileFromContext("reports/test1.rptdesign");

        IReportRunnable runnableReport = ReportHelper.getReport(new FileInputStream(
                report));
        IRunAndRenderTask task = BirtEngine.getBirtEngine().createRunAndRenderTask(
                runnableReport);

        String dirPath = new Path(System.getProperty("java.io.tmpdir")).append(
                "birt-test-report" + System.currentTimeMillis()).toString();
        reportPath = dirPath;
        File baseDir = new File(dirPath);
        baseDir.mkdir();

        File result = new File(dirPath + "/report.pdf");

        OutputStream out = new FileOutputStream(result);

        PDFRenderOption options = new PDFRenderOption();
        options.setOutputFormat(PDFRenderOption.OUTPUT_FORMAT_PDF);
        options.setOutputStream(out);
        HashMap inputValues = new HashMap();
        inputValues.put("Cust", 1);
        task.setParameterValues(inputValues);
        task.setRenderOption(options);

        task.run();
        task.close();

        out.close();

    }

}
