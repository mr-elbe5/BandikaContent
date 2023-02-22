/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.servlet;

import de.elbe5.application.Configuration;
import de.elbe5.content.ContentController;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestKeys;
import de.elbe5.request.RequestType;
import de.elbe5.response.IResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ContentServlet extends WebServlet {

    protected void processRequest(String method, HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding(Configuration.ENCODING);
        RequestData rdata = new RequestData(method, RequestType.content, request);
        request.setAttribute(RequestKeys.KEY_REQUESTDATA, rdata);
        rdata.readRequestParams();
        rdata.initSession();
        try {
            IResponse result;
            result = ContentController.getInstance().show(request.getRequestURI(), rdata);
            if (rdata.hasCookies())
                rdata.setCookies(response);
            result.processResponse(getServletContext(), rdata, response);
        }
        catch (ResponseException ce){
            handleException(request,response,ce.getResponseCode());
        }
        catch (Exception e){
            handleException(request,response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
