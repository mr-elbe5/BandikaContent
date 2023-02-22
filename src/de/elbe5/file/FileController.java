/*
 Bandika CMS - A Java based modular File Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.file;

import de.elbe5.application.ApplicationPath;
import de.elbe5.base.LocalizedStrings;
import de.elbe5.base.Log;
import de.elbe5.content.ContentCache;
import de.elbe5.content.ContentData;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestKeys;
import de.elbe5.response.StatusResponse;
import de.elbe5.servlet.Controller;
import de.elbe5.response.IResponse;
import de.elbe5.response.ForwardResponse;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;

public abstract class FileController extends Controller {

    @Deprecated
    public IResponse show(RequestData rdata) {
        int id = rdata.getId();
        Log.warn("deprecated call of file show for id " + id);
        FileData data = ContentCache.getFile(id);
        return show(data, rdata);
    }

    @Deprecated
    public IResponse download(RequestData rdata) {
        return downloadFile(rdata);
    }

    @Deprecated
    private IResponse downloadFile(RequestData rdata) {
        int id = rdata.getId();
        FileData data = ContentCache.getFile(id);
        rdata.getAttributes().put("download", "true");
        return show(data, rdata);
    }

    private IResponse show(FileData data, RequestData rdata){
        ContentData parent=ContentCache.getContent(data.getParentId());
        if (!parent.hasUserReadRight(rdata)) {
            //todo
            //String token = rdata.getAttributes().getString("token");
            //checkRights(Token.matchToken(data.getId(), token));
        }
        File file = new File(ApplicationPath.getAppFilePath(), data.getFileName());
        // if not exists, create from database
        if (!file.exists() && !FileBean.getInstance().createTempFile(file)) {
            return new StatusResponse(HttpServletResponse.SC_NOT_FOUND);
        }
        RangeInfo rangeInfo = null;
        String rangeHeader = rdata.getRequest().getHeader("Range");
        if (rangeHeader != null) {
            rangeInfo = new RangeInfo(rangeHeader, file.length());
        }
        return new FileResponse(file, data.getDisplayFileName(), rangeInfo);
    }

    public IResponse deleteFile(RequestData rdata) {
        int contentId = rdata.getId();
        int parentId = ContentCache.getFileParentId(contentId);
        ContentData parent=ContentCache.getContent(parentId);
        checkRights(parent.hasUserReadRight(rdata));
        FileData data = ContentCache.getFile(contentId);
        FileBean.getInstance().deleteFile(data);
        ContentCache.setDirty();
        rdata.getAttributes().put("contentId", Integer.toString(parentId));
        rdata.setMessage(LocalizedStrings.string("_fileDeleted"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return showContentAdministration(rdata,parentId);
    }

    protected IResponse showContentAdministration(RequestData rdata, int contentId) {
        return new ForwardResponse("/ctrl/admin/openContentAdministration?contentId=" + contentId);
    }

}
