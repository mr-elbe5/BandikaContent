/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.file;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.IJsonData;
import de.elbe5.base.Log;
import de.elbe5.base.FileHelper;
import de.elbe5.base.ImageHelper;
import de.elbe5.base.StringHelper;
import de.elbe5.request.RequestData;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

public class ImageData extends FileData implements IJsonData {

    public static int MAX_PREVIEW_WIDTH = 200;
    public static int MAX_PREVIEW_HEIGHT = 200;

    protected int width = 0;
    protected int height = 0;
    protected byte[] previewBytes = null;
    protected boolean hasPreview = false;

    public int maxWidth=0;
    public int maxHeight=0;
    public int maxPreviewWidth= MAX_PREVIEW_WIDTH;
    public int maxPreviewHeight= MAX_PREVIEW_HEIGHT;

    public ImageData() {
    }

    public boolean isImage() {
        return true;
    }

    // base data

    public String getPreviewURL(){
        return "/ctrl/image/showPreview/"+getId();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public byte[] getPreviewBytes() {
        return previewBytes;
    }

    public void setPreviewBytes(byte[] previewBytes) {
        this.previewBytes = previewBytes;
    }

    public boolean hasPreview() {
        return hasPreview;
    }

    public void setHasPreview(boolean hasPreview) {
        this.hasPreview = hasPreview;
    }

    public String getPreviewName(){
        return "preview_" + getId() + ".jpg";
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getMaxPreviewWidth() {
        return maxPreviewWidth;
    }

    public void setMaxPreviewWidth(int maxPreviewWidth) {
        this.maxPreviewWidth = maxPreviewWidth;
    }

    public int getMaxPreviewHeight() {
        return maxPreviewHeight;
    }

    public void setMaxPreviewHeight(int maxPreviewHeight) {
        this.maxPreviewHeight = maxPreviewHeight;
    }

    // multiple data

    @Override
    public void readSettingsRequestData(RequestData rdata) {
        super.readSettingsRequestData(rdata);
        if (!isNew()){
            return;
        }
        BinaryFile file = rdata.getAttributes().getFile("file");
        if (maxWidth != 0 || maxHeight != 0)
            createFromBinaryFile(file, maxWidth, maxHeight, getMaxPreviewWidth(), getMaxPreviewHeight(), true);
        else
            createFromBinaryFile(file, getMaxPreviewWidth(), getMaxPreviewHeight());
    }

    // helper

    public boolean createFromBinaryFile(BinaryFile file, int maxTumbnailWidth, int maxThumbnailHeight) {
        boolean success=false;
        if (file != null && file.isImage() && file.getBytes() != null && file.getFileName().length() > 0 && !StringHelper.isNullOrEmpty(file.getContentType())) {
            setFileName(file.getFileName());
            setBytes(file.getBytes());
            setFileSize(file.getBytes().length);
            setContentType(file.getContentType());
            try {
                createPreview(maxTumbnailWidth, maxThumbnailHeight);
                success = true;
            } catch (IOException e) {
                Log.warn("could not create buffered image");
            }
        }
        return success;
    }

    public boolean createFromBinaryFile(BinaryFile file, int maxWidth, int maxHeight, int maxTumbnailWidth, int maxThumbnailHeight, boolean expand) {
        boolean success=false;
        if (file != null && file.isImage() && file.getBytes() != null && file.getFileName().length() > 0 && !StringHelper.isNullOrEmpty(file.getContentType())) {
            setFileName(file.getFileName());
            setBytes(file.getBytes());
            setFileSize(file.getBytes().length);
            setContentType(file.getContentType());
            try {
                createResizedImage(maxWidth, maxHeight, expand);
                createPreview(maxTumbnailWidth, maxThumbnailHeight);
                success = true;
            } catch (IOException e) {
                Log.warn("could not create buffered image");
            }
        }
        return success;
    }

    public boolean createJpegFromBinaryFile(BinaryFile file, int maxWidth, int maxHeight, int maxTumbnailWidth, int maxThumbnailHeight, boolean expand) {
        boolean success=false;
        if (file != null && file.isImage() && file.getBytes() != null && file.getFileName().length() > 0 && !StringHelper.isNullOrEmpty(file.getContentType())) {
            setFileName(file.getFileName());
            setBytes(file.getBytes());
            setFileSize(file.getBytes().length);
            setContentType(file.getContentType());
            try {
                createResizedJpeg(maxWidth, maxHeight, expand);
                createPreview(maxTumbnailWidth, maxThumbnailHeight);
                success = true;
            } catch (IOException e) {
                Log.warn("could not create buffered image");
            }
        }
        return success;
    }

    public void createResizedImage(int width, int height, boolean expand) throws IOException {
        BufferedImage bi = ImageHelper.createResizedImage(getBytes(), getContentType(), width, height, expand);
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(getContentType());
        if (writers.hasNext()) {
            setContentType(getContentType());
        } else {
            writers = ImageIO.getImageWritersBySuffix(FileHelper.getExtension(getFileName()));
            if (writers.hasNext()) {
                setContentType("");
            } else {
                setFileName(FileHelper.getFileNameWithoutExtension(getFileName()) + ".jpg");
                writers = ImageIO.getImageWritersByMIMEType("image/jpeg");
                setContentType("image/jpeg");
            }
        }
        ImageWriter writer = writers.next();
        setBytes(ImageHelper.writeImage(writer, bi));
        setFileSize(getBytes().length);
        setWidth(bi.getWidth());
        setHeight(bi.getHeight());
    }

    public void createResizedJpeg(int width, int height, boolean expand) throws IOException {
        BufferedImage bi = ImageHelper.createResizedImage(getBytes(), getContentType(), width, height, expand);
        setFileName(FileHelper.getFileNameWithoutExtension(getFileName()) + ".jpg");
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/jpeg");
        setContentType("image/jpeg");
        ImageWriter writer = writers.next();
        setBytes(ImageHelper.writeImage(writer, bi));
        setFileSize(getBytes().length);
        setWidth(bi.getWidth());
        setHeight(bi.getHeight());
    }

    public void createScaledJpeg(int scalePercent) throws IOException {
        BufferedImage bi = ImageHelper.createScaledImage(getBytes(), getContentType(), scalePercent);
        setFileName(FileHelper.getFileNameWithoutExtension(getFileName()) + ".jpg");
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/jpeg");
        setContentType("image/jpeg");
        ImageWriter writer = writers.next();
        setBytes(ImageHelper.writeImage(writer, bi));
        setFileSize(getBytes().length);
        setWidth(bi.getWidth());
        setHeight(bi.getHeight());
    }

    public void createPreview(int maxTumbnailWidth, int maxThumbnailHeight) throws IOException{
        if (!isImage())
            return;
        BufferedImage source = ImageHelper.createImage(getBytes(), getContentType());
        if (source != null) {
            setWidth(source.getWidth());
            setHeight(source.getHeight());
            float factor = ImageHelper.getResizeFactor(source, maxTumbnailWidth, maxThumbnailHeight, false);
            BufferedImage image = ImageHelper.copyImage(source, factor);
            createJpegPreview(image);
        }
    }

    protected void createJpegPreview(BufferedImage image) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/jpeg");
        ImageWriter writer = writers.next();
        setPreviewBytes(ImageHelper.writeImage(writer, image));
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject getJson(){
        JSONObject json = new JSONObject();
        json.put("id",getId());
        json.put("fileName",getFileName());
        json.put("name",getDisplayName());
        json.put("displayName",getDisplayName());
        json.put("contentType",getContentType());
        json.put("width", getWidth());
        json.put("height", getHeight());
        return json;
    }
}
