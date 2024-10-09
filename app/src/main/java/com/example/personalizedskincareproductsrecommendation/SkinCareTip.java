package com.example.personalizedskincareproductsrecommendation;

import java.util.List;

public class SkinCareTip {
    private String id, title, tip;
    private List<String> images;

    public SkinCareTip(String title, String tip, List<String> images) {
        this.title = title;
        this.tip = tip;
        this.images = images;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public void setImages(List<String> images){
        this.images=images;
    }

    public List<String> getImages() {
        return images;
    }
}

