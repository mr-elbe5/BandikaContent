<%--
  Bandika CMS - A Java based modular Content Management System
  Copyright (C) 2009-2021 Michael Roennau

  This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
--%><%response.setContentType("text/html;charset=UTF-8");%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@include file="/WEB-INF/_jsp/_include/_functions.inc.jsp" %>
<%@ page import="de.elbe5.request.RequestData" %>
<%@ page import="java.util.List" %>
<%@ page import="de.elbe5.content.ContentData" %>
<%@ page import="de.elbe5.file.ImageData" %>
<%@ page import="de.elbe5.request.ContentRequestKeys" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    ContentData contentData = rdata.getCurrentDataInRequestOrSession(ContentRequestKeys.KEY_CONTENT, ContentData.class);
    List<Class<? extends ImageData>> imageClasses=contentData.getImageClasses();
    int fileId=rdata.getAttributes().getInt("fileId");
%>
        <li class="images open">
            <span>[<%=$SH("_images")%>]</span>
            <%if (contentData.hasUserEditRight(rdata)) {%>
            <div class="icons">
                <% if (rdata.hasClipboardData(ContentRequestKeys.KEY_IMAGE)) {%>
                <a class="icon fa fa-paste" href="/ctrl/image/pasteImage?parentId=<%=contentData.getId()%>" title="<%=$SH("_pasteImage")%>"> </a>
                <%}
                    if (!imageClasses.isEmpty()) {
                        if (imageClasses.size() == 1){%>
                <a class="icon fa fa-plus" onclick="return openModalDialog('/ctrl/image/openCreateImage?parentId=<%=contentData.getId()%>&type=<%=imageClasses.get(0).getName()%>');" title="<%=$SH("_newImage")%>"></a>
                    <%} else {%>
                <a class="icon fa fa-plus dropdown-toggle" data-toggle="dropdown" title="<%=$SH("_newImage")%>"></a>
                <div class="dropdown-menu">
                    <%for (Class<? extends ImageData> imageType : imageClasses) {
                        String name = $SH("class."+imageType);
                    %>
                    <a class="dropdown-item" onclick="return openModalDialog('/ctrl/image/openCreateImage?parentId=<%=contentData.getId()%>&type=<%=imageType.getName()%>');"><%=name%>
                    </a>
                    <%}%>
                </div>
                    <%}
                }%>
            </div>
            <%}%>
            <ul>
                <%
                    List<ImageData> images = contentData.getFiles(ImageData.class);
                    for (ImageData image : images) {%>
                <li class="<%=fileId==image.getId() ? "current" : ""%>">
                    <div class="treeline">
                        <span class="treeImage" id="<%=image.getId()%>">
                            <%=image.getDisplayName()%>
                            <span class="hoverImage">
                                <img src="/ctrl/image/showPreview/<%=image.getId()%>" alt="<%=$H(image.getFileName())%>"/>
                            </span>
                        </span>
                        <div class="icons">
                            <a class="icon fa fa-eye" href="<%=image.getStaticURL()%>" target="_blank" title="<%=$SH("_view")%>"> </a>
                            <a class="icon fa fa-download" href="<%=image.getStaticURL()%>?download=true" title="<%=$SH("_download")%>"> </a>
                            <a class="icon fa fa-pencil" href="" onclick="return openModalDialog('/ctrl/image/openEditImage/<%=image.getId()%>');" title="<%=$SH("_edit")%>"> </a>
                            <a class="icon fa fa-scissors" href="" onclick="return linkTo('/ctrl/image/cutImage/<%=image.getId()%>');" title="<%=$SH("_cut")%>"> </a>
                            <a class="icon fa fa-copy" href="" onclick="return linkTo('/ctrl/image/copyImage/<%=image.getId()%>');" title="<%=$SH("_copy")%>"> </a>
                            <a class="icon fa fa-trash-o" href="" onclick="if (confirmDelete()) return linkTo('/ctrl/image/deleteImage/<%=image.getId()%>');" title="<%=$SH("_delete")%>"> </a>
                        </div>
                    </div>
                </li>
                <%
                    }%>
            </ul>
        </li>

