/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.content;

import de.elbe5.base.*;
import de.elbe5.data.BaseData;
import de.elbe5.file.*;
import de.elbe5.group.GroupBean;
import de.elbe5.group.GroupData;
import de.elbe5.request.ContentRequestKeys;
import de.elbe5.request.RequestData;
import de.elbe5.response.IMasterInclude;
import de.elbe5.rights.Right;
import de.elbe5.rights.SystemZone;
import de.elbe5.user.UserCache;
import de.elbe5.user.UserData;
import de.elbe5.response.IResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.*;

public class ContentData extends BaseData implements IMasterInclude, Comparable<ContentData> {

    public static final String ACCESS_TYPE_OPEN = "OPEN";
    public static final String ACCESS_TYPE_INHERITS = "INHERIT";
    public static final String ACCESS_TYPE_INDIVIDUAL = "INDIVIDUAL";

    public static final String NAV_TYPE_NONE = "NONE";
    public static final String NAV_TYPE_HEADER = "HEADER";
    public static final String NAV_TYPE_FOOTER = "FOOTER";

    public static final String VIEW_TYPE_SHOW = "SHOW";
    public static final String VIEW_TYPE_SHOWPUBLISHED = "PUBLISHED";
    public static final String VIEW_TYPE_EDIT = "EDIT";
    public static final String VIEW_TYPE_PUBLISH = "PUBLISH";

    public static final int ID_ROOT = 1;

    public static List<Class<? extends ContentData>> childClasses = new ArrayList<>();
    public static List<Class<? extends FileData>> fileClasses = new ArrayList<>();

    // base data
    private String name = "";
    private String path = "";
    private String displayName = "";
    private String description = "";
    private String accessType = ACCESS_TYPE_OPEN;
    private String navType = NAV_TYPE_NONE;
    private boolean active = true;
    private Map<Integer, Right> groupRights = new HashMap<>();

    // tree data
    protected int parentId = 0;
    protected ContentData parent = null;
    protected int ranking = 0;
    private final List<ContentData> children = new ArrayList<>();
    private final List<FileData> files = new ArrayList<>();

    //runtime

    protected boolean openAccess = true;
    protected String viewType = VIEW_TYPE_SHOW;

    public ContentData() {
    }

    public String getType() {
        return getClass().getName();
    }

    public ContentBean getBean() {
        return ContentBean.getInstance();
    }

    public List<Class<? extends ContentData>> getChildClasses(){
        return ContentData.childClasses;
    }

    public List<Class<? extends FileData>> getFileClasses(){
        return ContentData.fileClasses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void generatePath() {
        if (getParent() == null)
            return;
        setPath(getParent().getPath() + "/" + StringHelper.toUrl(getName().toLowerCase()));
    }

    public String getUrl() {
        if (getPath().isEmpty())
            return "/home.html";
        return getPath() + ".html";
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNavDisplay(){
        return StringHelper.toHtml(getDisplayName());
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAccessType() {
        return accessType;
    }

    public boolean isOpenAccess() {
        return openAccess;
    }

    public boolean hasIndividualAccess(){
        return accessType.equals(ACCESS_TYPE_INDIVIDUAL);
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
        if (accessType.equals(ACCESS_TYPE_OPEN))
            openAccess=true;
    }

    public String getNavType() {
        return navType;
    }

    public boolean isInHeaderNav(){
        return navType.equals(ContentData.NAV_TYPE_HEADER);
    }

    public boolean isInFooterNav(){
        return navType.equals(ContentData.NAV_TYPE_FOOTER);
    }

    public void setNavType(String navType) {
        this.navType = navType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Map<Integer, Right> getGroupRights() {
        return groupRights;
    }

    public boolean isGroupRight(int id, Right right) {
        return groupRights.containsKey(id) && groupRights.get(id) == right;
    }

    public boolean hasAnyGroupRight(int id) {
        return groupRights.containsKey(id);
    }

    public void setGroupRights(Map<Integer, Right> groupRights) {
        this.groupRights = groupRights;
    }

    public boolean hasUserRight(UserData user,Right right){
        if (user==null)
            return false;
        for (int groupId : groupRights.keySet()){
            if (user.getGroupIds().contains(groupId) && groupRights.get(groupId).includesRight(right))
                return true;
        }
        return false;
    }

    public boolean hasUserReadRight(RequestData rdata) {
        if (isOpenAccess() && isPublished())
            return true;
        UserData user=rdata.getLoginUser();
        return user!=null && (user.hasSystemRight(SystemZone.CONTENTREAD) || (hasUserRight(user,Right.READ) && isPublished()) || hasUserEditRight(rdata));
    }

    public boolean hasUserEditRight(RequestData rdata) {
        UserData user=rdata.getLoginUser();
        return (user!=null && (user.hasSystemRight(SystemZone.CONTENTEDIT) || hasUserRight(user,Right.EDIT)));
    }

    public boolean hasUserApproveRight(RequestData rdata) {
        UserData user=rdata.getLoginUser();
        return (user!=null && (user.hasSystemRight(SystemZone.CONTENTAPPROVE) || hasUserRight(user,Right.APPROVE)));
    }

    // tree data

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        if (parentId == getId()) {
            Log.error("parentId must not be this: " + parentId);
            this.parentId = 0;
        } else {
            this.parentId = parentId;
        }
    }

    public ContentData getParent() {
        return parent;
    }

    public void setParent(ContentData parent) {
        this.parent = parent;
    }

    public boolean setParent(ContentData parent, Class<? extends ContentData> cls) {
        try {
            if (cls.isInstance(parent)) {
                this.parent = parent;
                return true;
            }
        }
        catch(NullPointerException | ClassCastException e){
            // ignore
        }
        this.parent = null;
        Log.error("could not set parent of correct class");
        return false;
    }

    public void collectParentIds(Set<Integer> ids) {
        ids.add(getId());
        if (parent != null)
            parent.collectParentIds(ids);
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public void inheritRightsFromParent() {
        getGroupRights().clear();
        if (!getAccessType().equals(ACCESS_TYPE_INHERITS) || parent == null) {
            return;
        }
        if (parent.isOpenAccess())
            openAccess=true;
        else
            getGroupRights().putAll(parent.getGroupRights());
    }

    public List<ContentData> getChildren() {
        return children;
    }

    public boolean hasChildren(){
        return getChildren().size()>0;
    }

    public<T extends ContentData> List<T> getChildren(Class<T> cls) {
        List<T> list = new ArrayList<>();
        try {
            for (ContentData data : getChildren()){
                if (cls.isInstance(data))
                    list.add(cls.cast(data));
            }
        }
        catch(NullPointerException | ClassCastException e){
            return null;
        }
        return list;
    }

    public void getAllChildren(List<ContentData> list) {
        if (!hasChildren())
            return;
        for (ContentData data : getChildren()) {
            list.add(data);
            data.getAllChildren(list);
        }
    }

    public<T extends ContentData> void getAllChildren(List<T> list,Class<T> cls) {
        if (!hasChildren())
            return;
        for (ContentData data : getChildren()) {
            try {
                if (cls.isInstance(data))
                    list.add(cls.cast(data));
            }
            catch(NullPointerException | ClassCastException e){
                // ignore
            }
            data.getAllChildren(list, cls);
        }
    }

    public void addChild(ContentData data) {
        children.add(data);
    }

    public void initializeChildren() {
        if (hasChildren()) {
            Collections.sort(children);
            for (ContentData child : children) {
                child.generatePath();
                child.inheritRightsFromParent();
                child.initializeChildren();
            }
        }
    }

    public List<FileData> getFiles() {
        return files;
    }

    public boolean hasFiles(){
        return getFiles().size()>0;
    }

    public<T extends FileData> List<T> getFiles(Class<T> cls) {
        List<T> list = new ArrayList<>();
        try {
            for (FileData data : getFiles()){
                if (cls.isInstance(data))
                    list.add(cls.cast(data));
            }
        }
        catch(NullPointerException | ClassCastException e){
            return null;
        }
        return list;
    }

    public<T extends FileData> T getFileWithId(int id, Class<T> cls) {
        try {
            for (FileData data : getFiles()){
                if (data.getId() == id) {
                    if (cls.isInstance(data))
                        return cls.cast(data);
                    else
                        return null;
                }
            }
        }
        catch(NullPointerException | ClassCastException e){
            return null;
        }
        return null;
    }

    public void addFile(FileData data) {
        files.add(data);
    }

    // defaults for overriding

    public String getKeywords(){
        return "";
    }

    public boolean isPublished() {
        return true;
    }

    public boolean hasUnpublishedDraft() {
        return false;
    }

    // view


    public String getViewType() {
        return viewType;
    }

    public boolean isEditing(){
        return viewType.equals(VIEW_TYPE_EDIT);
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

    public void stopEditing(){
        this.viewType=VIEW_TYPE_SHOW;
    }

    public void startEditing(){
        this.viewType=VIEW_TYPE_EDIT;
    }

    public boolean isPublishedView(){
        return viewType.equals(VIEW_TYPE_SHOWPUBLISHED);
    }

    public boolean isStandardView(){
        return viewType.equals(VIEW_TYPE_SHOW);
    }

    public IResponse getDefaultView(){
        return new ContentResponse(this);
    }

    public String getContentDataJsp() {
        return "/WEB-INF/_jsp/content/editContentData.ajax.jsp";
    }

    public String getContentTreeJsp() {
        return "/WEB-INF/_jsp/content/treeContent.inc.jsp";
    }

    //used in jsp
    public void displayTreeContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        if (hasUserReadRight(rdata)) {
            //backup
            ContentData currentContent = rdata.getCurrentDataInRequestOrSession(ContentRequestKeys.KEY_CONTENT, ContentData.class);
            rdata.setRequestObject(ContentRequestKeys.KEY_CONTENT, this);
            context.include(getContentTreeJsp(), true);
            //restore
            rdata.setRequestObject(ContentRequestKeys.KEY_CONTENT, currentContent);
        }
    }

    public String getAdminContentTreeJsp() {
        return "/WEB-INF/_jsp/content/adminTreeContent.inc.jsp";
    }

    //used in admin jsp
    public void displayAdminTreeContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        if (hasUserReadRight(rdata)) {
            //backup
            ContentData currentContent = rdata.getRequestObject(ContentRequestKeys.KEY_CONTENT, ContentData.class);
            rdata.setRequestObject(ContentRequestKeys.KEY_CONTENT, this);
            context.include(getAdminContentTreeJsp(), true);
            //restore
            rdata.setRequestObject(ContentRequestKeys.KEY_CONTENT, currentContent);
        }
    }

    //used in jsp/tag
    @Override
    public void displayContent(PageContext context, RequestData rdata) throws IOException, ServletException {
    }

    @Override
    public void appendContent(StringBuilder sb, RequestData rdata) {

    }

    // multiple data

    public void setCreateValues(ContentData parent, RequestData rdata) {
        setNew(true);
        setId(ContentBean.getInstance().getNextId());
        setCreatorId(rdata.getUserId());
        setChangerId(rdata.getUserId());
        setParentId(parent.getId());
        setParent(parent);
        inheritRightsFromParent();
    }

    public void setEditValues(ContentData cachedData, RequestData rdata) {
        if (cachedData == null)
            return;
        if (!isNew()) {
            setParent(cachedData.getParent());
            setPath(cachedData.getPath());
            for (ContentData subContent : cachedData.getChildren()) {
                getChildren().add(subContent);
            }
            for (FileData file : cachedData.getFiles()) {
                getFiles().add(file);
            }
        }
        setChangerId(rdata.getUserId());
    }

    public void copyData(ContentData data, RequestData rdata) {
        setNew(true);
        setId(ContentBean.getInstance().getNextId());
        setName(data.getName());
        setDisplayName(data.getDisplayName());
        setDescription(data.getDescription());
        setCreatorId(rdata.getUserId());
        setChangerId(rdata.getUserId());
        setAccessType(data.getAccessType());
        setNavType(data.getNavType());
        setActive(data.isActive());
        getGroupRights().clear();
        if (hasIndividualAccess()) {
            getGroupRights().putAll(data.getGroupRights());
        }
        setParentId(data.getParentId());
        setParent(data.getParent());
        setRanking(data.getRanking() + 1);
    }

    public void readCreateRequestData(RequestData rdata) {
        readRequestData(rdata);
    }

    public void readUpdateRequestData(RequestData rdata) {
        readRequestData(rdata);
    }

    public void readRequestData(RequestData rdata) {
        setDisplayName(rdata.getAttributes().getString("displayName").trim());
        setName(StringHelper.toSafeWebName(getDisplayName()));
        setDescription(rdata.getAttributes().getString("description"));
        setAccessType(rdata.getAttributes().getString("accessType"));
        setNavType(rdata.getAttributes().getString("navType"));
        setActive(rdata.getAttributes().getBoolean("active"));
        if (name.isEmpty()) {
            rdata.addIncompleteField("name");
        }
    }

    public void readFrontendCreateRequestData(RequestData rdata) {
        readFrontendRequestData(rdata);
    }

    public void readFrontendUpdateRequestData(RequestData rdata) {
        readFrontendRequestData(rdata);
    }

    public void readFrontendRequestData(RequestData rdata) {
        readRequestData(rdata);
    }

    public void readRightsRequestData(RequestData rdata) {
        List<GroupData> groups = GroupBean.getInstance().getAllGroups();
        getGroupRights().clear();
        for (GroupData group : groups) {
            if (group.getId() <= GroupData.ID_MAX_FINAL)
                continue;
            String value = rdata.getAttributes().getString("groupright_" + group.getId());
            if (!value.isEmpty())
                getGroupRights().put(group.getId(), Right.valueOf(value));
        }
    }

    @Override
    public int compareTo(ContentData data) {
        int i = getRanking() - data.getRanking();
        if (i!=0)
            return i;
        return getDisplayName().compareTo(data.getDisplayName());
    }

    @Override
    public JsonObject getJson() {
        return super.getJson()
        .add("name",getName())
        .add("displayName",getDisplayName())
        .add("description",getDescription());
    }
}
