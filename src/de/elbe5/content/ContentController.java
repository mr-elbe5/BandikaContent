/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.content;

import de.elbe5.application.Configuration;
import de.elbe5.base.LocalizedStrings;
import de.elbe5.data.BaseData;
import de.elbe5.request.ContentRequestKeys;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestKeys;
import de.elbe5.rights.SystemZone;
import de.elbe5.servlet.Controller;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.response.CloseDialogResponse;
import de.elbe5.response.IResponse;
import de.elbe5.response.ForwardResponse;
import de.elbe5.servlet.ResponseException;

import jakarta.servlet.http.HttpServletResponse;
import java.util.*;

public class ContentController extends Controller {

    public static final String KEY = "content";

    private static ContentController instance = null;

    public static void setInstance(ContentController instance) {
        ContentController.instance = instance;
    }

    public static ContentController getInstance() {
        return instance;
    }

    public static void register(ContentController controller){
        setInstance(controller);
        ControllerCache.addController(controller.getKey(),getInstance());
    }

    protected IResponse openJspPage(String jsp) {
        JspContentData contentData = new JspContentData();
        contentData.setJsp(jsp);
        return new ContentResponse(contentData);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    //frontend
    public IResponse show(RequestData rdata) {
        assertSessionCall(rdata);
        //Log.log("show");
        int contentId = rdata.getId();
        ContentData data = ContentCache.getContent(contentId);
        checkRights(data.hasUserReadRight(rdata));
        increaseViewCount(data);
        return data.getDefaultView();
    }

    //frontend
    public IResponse show(String url, RequestData rdata) {
        assertSessionCall(rdata);
        ContentData data;
        data = ContentCache.getContent(url);
        checkRights(data.hasUserReadRight(rdata));
        //Log.log("show: "+data.getClass().getSimpleName());
        increaseViewCount(data);
        return data.getDefaultView();
    }

    protected void increaseViewCount(ContentData data){
        if (Configuration.isLogContent()) {
            ContentBean.getInstance().increaseViewCount(data.getId());
        }
    }

    //backend
    public IResponse openCreateData(RequestData rdata) {
        assertSessionCall(rdata);
        int parentId = rdata.getAttributes().getInt("parentId");
        ContentData parentData = ContentCache.getContent(parentId);
        checkRights(parentData.hasUserEditRight(rdata));
        String type = rdata.getAttributes().getString("type");
        ContentData data = ContentBean.getInstance().getNewContentData(type);
        data.setCreateValues(parentData, rdata);
        data.setRanking(parentData.getChildren().size());
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT, data);
        return showEditData(data);
    }

    //backend
    public IResponse openEditData(RequestData rdata) {
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        ContentData data = ContentBean.getInstance().getContent(contentId);
        checkRights(data.hasUserEditRight(rdata));
        data.setEditValues(ContentCache.getContent(data.getId()), rdata);
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT, data);
        return showEditData(data);
    }

    //backend
    public IResponse saveData(RequestData rdata) {
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        ContentData data = rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, ContentData.class);
        checkRights(data.hasUserEditRight(rdata));
        if (data.isNew())
            data.readCreateRequestData(rdata);
        else
            data.readUpdateRequestData(rdata);
        if (!rdata.checkFormErrors()) {
            return showEditData(data);
        }
        data.setChangerId(rdata.getUserId());
        if (!ContentBean.getInstance().saveContent(data)) {
            setSaveError(rdata);
            return showEditData(data);
        }
        data.setNew(false);
        rdata.removeSessionObject(ContentRequestKeys.KEY_CONTENT);
        ContentCache.setDirty();
        rdata.setMessage(LocalizedStrings.string("_contentSaved"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return new CloseDialogResponse("/ctrl/admin/openContentAdministration?contentId=" + data.getId());
    }

    //backend
    public IResponse openEditRights(RequestData rdata) {
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        ContentData data = ContentBean.getInstance().getContent(contentId);
        checkRights(data.hasUserEditRight(rdata));
        data.setEditValues(ContentCache.getContent(data.getId()), rdata);
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT, data);
        return showEditRights(data);
    }

    //backend
    public IResponse saveRights(RequestData rdata) {
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        ContentData data = rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, ContentData.class);
        checkRights(data.hasUserEditRight(rdata));
        data.readRightsRequestData(rdata);
        if (!rdata.checkFormErrors()) {
            return showEditRights(data);
        }
        data.setChangerId(rdata.getUserId());
        if (!ContentBean.getInstance().saveRights(data)) {
            setSaveError(rdata);
            return showEditRights(data);
        }
        rdata.removeSessionObject(ContentRequestKeys.KEY_CONTENT);
        ContentCache.setDirty();
        rdata.setMessage(LocalizedStrings.string("_rightsSaved"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return new CloseDialogResponse("/ctrl/admin/openContentAdministration?contentId=" + data.getId());
    }

    //backend
    public IResponse cutContent(RequestData rdata) {
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        ContentData data = ContentBean.getInstance().getContent(contentId);
        checkRights(data.hasUserEditRight(rdata));
        rdata.setClipboardData(ContentRequestKeys.KEY_CONTENT, data);
        return showContentAdministration(rdata,data.getId());
    }

    //backend
    public IResponse copyContent(RequestData rdata) {
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        ContentData srcData = ContentBean.getInstance().getContent(contentId);
        checkRights(srcData.hasUserEditRight(rdata));
        ContentData data = ContentBean.getInstance().getNewContentData(srcData.getType());
        data.copyData(srcData, rdata);
        data.setChangerId(rdata.getUserId());
        rdata.setClipboardData(ContentRequestKeys.KEY_CONTENT, data);
        return showContentAdministration(rdata,data.getId());
    }

    //backend
    public IResponse pasteContent(RequestData rdata) {
        assertSessionCall(rdata);
        int parentId = rdata.getAttributes().getInt("parentId");
        ContentData data=rdata.getClipboardData(ContentRequestKeys.KEY_CONTENT,ContentData.class);
        if (data==null){
            rdata.setMessage(LocalizedStrings.string("_actionNotExcecuted"), RequestKeys.MESSAGE_TYPE_ERROR);
            return showContentAdministration(rdata, parentId);
        }
        ContentData parent = ContentCache.getContent(parentId);
        if (parent == null){
            rdata.setMessage(LocalizedStrings.string("_actionNotExcecuted"), RequestKeys.MESSAGE_TYPE_ERROR);
            return showContentAdministration(rdata, parentId);
        }
        checkRights(parent.hasUserEditRight(rdata));
        Set<Integer> parentIds=new HashSet<>();
        parent.collectParentIds(parentIds);
        if (parentIds.contains(data.getId())){
            rdata.setMessage(LocalizedStrings.string("_actionNotExcecuted"), RequestKeys.MESSAGE_TYPE_ERROR);
            return showContentAdministration(rdata, parentId);
        }
        data.setParentId(parentId);
        data.setParent(parent);
        data.generatePath();
        data.setChangerId(rdata.getUserId());
        ContentBean.getInstance().saveContent(data);
        rdata.clearClipboardData(ContentRequestKeys.KEY_CONTENT);
        ContentCache.setDirty();
        rdata.setMessage(LocalizedStrings.string("_contentPasted"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return showContentAdministration(rdata,data.getId());
    }

    //backend
    public IResponse clearClipboard(RequestData rdata) {
        assertSessionCall(rdata);
        checkRights(rdata.hasSystemRight(SystemZone.CONTENTEDIT));
        rdata.clearAllClipboardData();
        return showContentAdministration(rdata, 1);
    }

    //backend
    public IResponse deleteContent(RequestData rdata) {
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        ContentData data=ContentCache.getContent(contentId);
        checkRights(data.hasUserEditRight(rdata)) ;
        if (contentId < BaseData.ID_MIN) {
            rdata.setMessage(LocalizedStrings.string("_notDeletable"), RequestKeys.MESSAGE_TYPE_ERROR);
            return showContentAdministration(rdata, contentId);
        }
        int parentId = ContentCache.getParentContentId(contentId);
        ContentBean.getInstance().deleteContent(contentId);
        ContentCache.setDirty();
        rdata.getAttributes().put("contentId", Integer.toString(parentId));
        ContentCache.setDirty();
        rdata.setMessage(LocalizedStrings.string("_contentDeleted"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return showContentAdministration(rdata,parentId);
    }

    //backend
    public IResponse openSortChildContents(RequestData rdata) {
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        ContentData data = ContentCache.getContent(contentId);
        checkRights(data.hasUserEditRight(rdata));
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT, data);
        return showSortChildContents();
    }

    //backend
    public IResponse saveChildRankings(RequestData rdata) {
        assertSessionCall(rdata);
        int contentId = rdata.getId();
        ContentData data = rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, ContentData.class);
        checkRights(data.hasUserEditRight(rdata));
        for (ContentData child : data.getChildren()){
            int ranking=rdata.getAttributes().getInt("select"+child.getId(),-1);
            if (ranking!=-1){
                child.setRanking(ranking);

            }
        }
        Collections.sort(data.getChildren());
        ContentBean.getInstance().updateChildRankings(data);
        rdata.removeSessionObject(ContentRequestKeys.KEY_CONTENT);
        ContentCache.setDirty();
        rdata.setMessage(LocalizedStrings.string("_newRankingSaved"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return new CloseDialogResponse("/ctrl/admin/openContentAdministration?contentId=" + contentId);
    }

    public IResponse openCreateContent(RequestData rdata) {
        throw new ResponseException(HttpServletResponse.SC_UNAUTHORIZED);
    }

    //frontend
    public IResponse openEditContent(RequestData rdata) {
        throw new ResponseException(HttpServletResponse.SC_UNAUTHORIZED);
    }

    //frontend
    public IResponse showEditContent(RequestData rdata) {
        throw new ResponseException(HttpServletResponse.SC_UNAUTHORIZED);
    }

    //frontend
    public IResponse saveContent(RequestData rdata) {
        throw new ResponseException(HttpServletResponse.SC_UNAUTHORIZED);
    }

    public IResponse cancelEditContent(RequestData rdata) {
        assertSessionCall(rdata);
        ContentData data = rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT, ContentData.class);
        checkRights(data.hasUserEditRight(rdata));
        rdata.removeSessionObject(ContentRequestKeys.KEY_CONTENT);
        return show(rdata);
    }

    protected IResponse showEditData(ContentData contentData) {
        return new ForwardResponse(contentData.getContentDataJsp());
    }

    protected IResponse showEditRights(ContentData contentData) {
        return new ForwardResponse("/WEB-INF/_jsp/content/editGroupRights.ajax.jsp");
    }

    protected IResponse showSortChildContents() {
        return new ForwardResponse("/WEB-INF/_jsp/content/sortChildContents.ajax.jsp");
    }

    protected IResponse showContentAdministration(RequestData rdata, int contentId) {
        return new ForwardResponse("/ctrl/admin/openContentAdministration?contentId=" + contentId);
    }

}
