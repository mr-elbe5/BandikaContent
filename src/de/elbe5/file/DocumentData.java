package de.elbe5.file;

import de.elbe5.base.BinaryFile;
import de.elbe5.request.RequestData;

public class DocumentData extends FileData {

    public DocumentData() {
    }

    public String getControllerKey() {
        return DocumentController.KEY;
    }

    public String getEditURL(){
        return "/WEB-INF/_jsp/file/editDocument.ajax.jsp";
    }

    // multiple data

    @Override
    public void readSettingsRequestData(RequestData rdata) {
        super.readSettingsRequestData(rdata);
        if (!isNew()){
            return;
        }
        BinaryFile file = rdata.getAttributes().getFile("file");
        createFromBinaryFile(file);
        if (getDisplayName().isEmpty()) {
            setDisplayName(file.getFileNameWithoutExtension());
        }
        else{
            adjustFileNameToDisplayName();
        }
    }

}
