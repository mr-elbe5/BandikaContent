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
<%@ page import="de.elbe5.file.DocumentData" %>
<%@ page import="de.elbe5.request.ContentRequestKeys" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    ContentData contentData = rdata.getCurrentDataInRequestOrSession(ContentRequestKeys.KEY_CONTENT, ContentData.class);
    List<Class<? extends DocumentData>> documentClasses=contentData.getDocumentClasses();
    int fileId=rdata.getAttributes().getInt("fileId");
%>
        <li class="documents open">
            <span>[<%=$SH("_documents")%>]</span>
            <%if (contentData.hasUserEditRight(rdata)) {%>
            <div class="icons">
                <% if (rdata.hasClipboardData(ContentRequestKeys.KEY_DOCUMENT)) {%>
                <a class="icon fa fa-paste" href="/ctrl/document/pasteDocument?parentId=<%=contentData.getId()%>" title="<%=$SH("_pasteDocument")%>"> </a>
                <%}
                    if (!documentClasses.isEmpty()) {
                        if (documentClasses.size() == 1){%>
                <a class="icon fa fa-plus" onclick="return openModalDialog('/ctrl/document/openCreateDocument?parentId=<%=contentData.getId()%>&type=<%=documentClasses.get(0).getName()%>');" title="<%=$SH("_newDocument")%>"></a>
                        <%} else {%>
                <a class="icon fa fa-plus dropdown-toggle" data-toggle="dropdown" title="<%=$SH("_newDocument")%>"></a>
                <div class="dropdown-menu">
                    <%for (Class<? extends DocumentData> documentType : documentClasses) {
                        String name = $SH("class."+documentType);
                    %>
                    <a class="dropdown-item" onclick="return openModalDialog('/ctrl/document/openCreateDocument?parentId=<%=contentData.getId()%>&type=<%=documentType.getName()%>');"><%=name%>
                    </a>
                        <%}%>
                </div>
                    <%}
                }%>
            </div>
            <%}%>
            <ul>
                <%
                    List<DocumentData> documents = contentData.getFiles(DocumentData.class);
                    for (DocumentData document : documents) {%>
                <li class="<%=fileId==document.getId() ? "current" : ""%>">
                    <div class="treeline">
                        <span id="<%=document.getId()%>">
                            <%=document.getDisplayName()%>
                        </span>
                        <div class="icons">
                            <a class="icon fa fa-eye" href="<%=document.getStaticURL()%>" target="_blank" title="<%=$SH("_view")%>"> </a>
                            <a class="icon fa fa-download" href="<%=document.getStaticURL()%>?download=true" title="<%=$SH("_download")%>"> </a>
                            <a class="icon fa fa-pencil" href="" onclick="return openModalDialog('/ctrl/document/openEditDocument/<%=document.getId()%>');" title="<%=$SH("_edit")%>"> </a>
                            <a class="icon fa fa-scissors" href="" onclick="return linkTo('/ctrl/document/cutDocument/<%=document.getId()%>');" title="<%=$SH("_cut")%>"> </a>
                            <a class="icon fa fa-copy" href="" onclick="return linkTo('/ctrl/document/copyDocument/<%=document.getId()%>');" title="<%=$SH("_copy")%>"> </a>
                            <a class="icon fa fa-trash-o" href="" onclick="if (confirmDelete()) return linkTo('/ctrl/document/deleteDocument/<%=document.getId()%>');" title="<%=$SH("_delete")%>"> </a>
                        </div>
                    </div>
                </li>
                <%}%>
            </ul>
        </li>


