package com.smartfarm.www.activity;

public class ListViewItem {
    private String titleStr;
    private String contentStr;

    public void setTitle(String titleStr) {
        this.titleStr = titleStr;
    }

    public void setContent(String contentStr) {
        this.contentStr = contentStr;
    }

    public String getTitle() {
        return titleStr;
    }

    public String getContent() {
        return contentStr;
    }
}
