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
<%@ page import="de.elbe5.file.MediaData" %>
<%@ page import="de.elbe5.request.ContentRequestKeys" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    ContentData contentData = rdata.getCurrentDataInRequestOrSession(ContentRequestKeys.KEY_CONTENT, ContentData.class);
    List<String> mediaTypes = contentData.getMediaClasses();
    int fileId=rdata.getAttributes().getInt("fileId");
%>
        <li class="media open">
            <span>[<%=$SH("_media")%>]</span>
            <%if (contentData.hasUserEditRight(rdata)) {%>
            <div class="icons">
                <% if (rdata.hasClipboardData(ContentRequestKeys.KEY_MEDIA)) {%>
                <a class="icon fa fa-paste" href="/ctrl/media/pasteMedia?parentId=<%=contentData.getId()%>" title="<%=$SH("_pasteMedia")%>"> </a>
                <%}
                    if (!mediaTypes.isEmpty()) {
                        if (mediaTypes.size()==1){%>
                <a class="icon fa fa-plus" onclick="return openModalDialog('/ctrl/media/openCreateMedia?parentId=<%=contentData.getId()%>&type=<%=mediaTypes.get(0)%>');" title="<%=$SH("_newMedia")%>"></a>
                    <%}else{%>
                <a class="icon fa fa-plus dropdown-toggle" data-toggle="dropdown" title="<%=$SH("_newMedia")%>"></a>
                <div class="dropdown-menu">
                    <%for (String mediaType : mediaTypes) {
                        String name = $SH("class."+mediaType);
                    %>
                    <a class="dropdown-item" onclick="return openModalDialog('/ctrl/media/openCreateMedia?parentId=<%=contentData.getId()%>&type=<%=mediaType%>');"><%=name%>
                    </a>
                    <%}%>
                </div>
                    <%}
                }%>
            </div>
            <%}%>
            <ul>
                <%
                    List<MediaData> mediaFiles = contentData.getFiles(MediaData.class);
                    for (MediaData media : mediaFiles) {%>
                <li class="<%=fileId==media.getId() ? "current" : ""%>">
                    <div class="treeline">
                        <span id="<%=media.getId()%>">
                            <%=media.getDisplayName()%>
                        </span>
                        <div class="icons">
                            <a class="icon fa fa-eye" href="<%=media.getStaticURL()%>" target="_blank" title="<%=$SH("_view")%>"> </a>
                            <a class="icon fa fa-download" href="<%=media.getStaticURL()%>?download=true" title="<%=$SH("_download")%>"> </a>
                            <a class="icon fa fa-pencil" href="" onclick="return openModalDialog('/ctrl/media/openEditMedia/<%=media.getId()%>');" title="<%=$SH("_edit")%>"> </a>
                            <a class="icon fa fa-scissors" href="" onclick="return linkTo('/ctrl/media/cutMedia/<%=media.getId()%>');" title="<%=$SH("_cut")%>"> </a>
                            <a class="icon fa fa-copy" href="" onclick="return linkTo('/ctrl/media/copyMedia/<%=media.getId()%>');" title="<%=$SH("_copy")%>"> </a>
                            <a class="icon fa fa-trash-o" href="" onclick="if (confirmDelete()) return linkTo('/ctrl/media/deleteMedia/<%=media.getId()%>');" title="<%=$SH("_delete")%>"> </a>
                        </div>
                    </div>
                </li>
                <%}%>
            </ul>
        </li>


