package com.example.avatarmind.robotsystem.bean;

public class ContentBean {

    private String type;

    private String info;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "ContentBean{" +
                "type='" + type + '\'' +
                ", info='" + info + '\'' +
                '}';
    }
}
