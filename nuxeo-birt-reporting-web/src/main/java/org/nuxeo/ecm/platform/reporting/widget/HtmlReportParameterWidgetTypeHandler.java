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

package org.nuxeo.ecm.platform.reporting.widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.CompositeFaceletHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagAttributes;
import javax.faces.view.facelets.TagConfig;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.forms.layout.api.BuiltinWidgetModes;
import org.nuxeo.ecm.platform.forms.layout.api.Widget;
import org.nuxeo.ecm.platform.forms.layout.api.exceptions.WidgetException;
import org.nuxeo.ecm.platform.forms.layout.facelets.FaceletHandlerHelper;
import org.nuxeo.ecm.platform.forms.layout.facelets.RenderVariables;
import org.nuxeo.ecm.platform.forms.layout.facelets.plugins.AbstractWidgetTypeHandler;
import org.nuxeo.ecm.platform.reporting.api.ReportInstance;
import org.nuxeo.ecm.platform.reporting.api.ReportModel;
import org.nuxeo.ecm.platform.reporting.report.ReportParameter;
import org.nuxeo.ecm.platform.ui.web.tag.handler.LeafFaceletHandler;

/**
 * Custom Widget Handler to managing Reports parameters configuration
 *
 * @author Tiry (tdelprat@nuxeo.com)
 */
public class HtmlReportParameterWidgetTypeHandler extends AbstractWidgetTypeHandler {

    public HtmlReportParameterWidgetTypeHandler(TagConfig config) {
        super(config);
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, Widget widget) throws WidgetException, IOException {

        FaceletHandlerHelper helper = new FaceletHandlerHelper(tagConfig);
        String mode = widget.getMode();
        String widgetId = widget.getId();

        String docVar = RenderVariables.globalVariables.value.name();
        DocumentModel doc = (DocumentModel) ctx.getAttribute(docVar);

        ReportInstance reportInstance = doc.getAdapter(ReportInstance.class);
        List<ReportParameter> reportParams;
        try {
            if (reportInstance != null) {
                reportParams = reportInstance.getReportParameters();
            } else {
                ReportModel reportModel = doc.getAdapter(ReportModel.class);
                reportParams = reportModel.getReportParameters();
            }
        } catch (IOException e) {
            throw new WidgetException("Unable to resolve report parameters", e);
        }

        TagAttributes attributes = helper.getTagAttributes(widgetId, widget);
        FaceletHandler leaf = new LeafFaceletHandler();

        FaceletHandler[] handlers = new FaceletHandler[reportParams.size() * 4];
        int idx = 0;
        int row = 0;
        for (ReportParameter param : reportParams) {

            // label
            List<TagAttribute> attrs = new ArrayList<TagAttribute>();
            attrs.add(helper.createIdAttribute(ctx, "paramName" + idx));
            attrs.add(helper.createAttribute("value", param.getName()));
            ComponentHandler pName = helper.getHtmlComponentHandler(FaceletHandlerHelper.getTagAttributes(attrs), leaf,
                    HtmlOutputText.COMPONENT_TYPE, null);

            // value
            List<TagAttribute> attrs2 = new ArrayList<TagAttribute>();
            attrs2.add(helper.createIdAttribute(ctx, "paramValue" + idx));
            String globalEL = attributes.get("value").getValue();

            String locator = "[" + row + "]['pValue']}";
            String value = globalEL.replace("}", locator);
            if (value == null) {
                value = "unset";
            }
            ComponentHandler pValue;
            if (BuiltinWidgetModes.EDIT.equals(mode) && param.isEditable()) {
                attrs2.add(helper.createAttribute("value", value));
                pValue = helper.getHtmlComponentHandler(FaceletHandlerHelper.getTagAttributes(attrs2), leaf,
                        HtmlInputText.COMPONENT_TYPE, null);
            } else {
                if (!param.isEditable()) {
                    attrs2.add(helper.createAttribute("value", param.getStringValue()));
                } else {
                    attrs2.add(helper.createAttribute("value", value));
                }
                pValue = helper.getHtmlComponentHandler(FaceletHandlerHelper.getTagAttributes(attrs2), leaf,
                        HtmlOutputText.COMPONENT_TYPE, null);
            }

            // spacer 1
            List<TagAttribute> attrs3 = new ArrayList<TagAttribute>();
            attrs3.add(helper.createIdAttribute(ctx, "spacer1" + idx));
            attrs3.add(helper.createAttribute("value", "&nbsp;:&nbsp;"));
            attrs3.add(helper.createAttribute("escape", "false"));
            ComponentHandler spacer = helper.getHtmlComponentHandler(FaceletHandlerHelper.getTagAttributes(attrs3),
                    leaf, HtmlOutputText.COMPONENT_TYPE, null);

            // spacer 2
            List<TagAttribute> attrs4 = new ArrayList<TagAttribute>();
            attrs4.add(helper.createIdAttribute(ctx, "spacer2" + idx));
            attrs4.add(helper.createAttribute("value", "<br/>"));
            attrs4.add(helper.createAttribute("escape", "false"));
            ComponentHandler spacer2 = helper.getHtmlComponentHandler(FaceletHandlerHelper.getTagAttributes(attrs4),
                    leaf, HtmlOutputText.COMPONENT_TYPE, null);

            handlers[idx] = pName;
            handlers[idx + 1] = spacer;
            handlers[idx + 2] = pValue;
            handlers[idx + 3] = spacer2;
            idx = idx + 4;
            row += 1;
        }
        FaceletHandler h = new CompositeFaceletHandler(handlers);
        h.apply(ctx, parent);
    }

}
