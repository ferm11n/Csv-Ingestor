package com.datawasher.api.model;

import java.util.List;

public class FileResponse {
    private String fileId;
    private String fileName;
    private List<String> headers;
    private List<String[]> rows;

    public FileResponse() {}

    public FileResponse(String fileId, String fileName, List<String> headers, List<String[]> rows) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.headers = headers;
        this.rows = rows;
    }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public List<String> getHeaders() { return headers; }
    public void setHeaders(List<String> headers) { this.headers = headers; }

    public List<String[]> getRows() { return rows; }
    public void setRows(List<String[]> rows) { this.rows = rows; }
}