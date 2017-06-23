package com.kad.banner.entity;

import java.io.Serializable;

/**
 * Created by Winner on 2016/10/22.
 */

public abstract class AbstractPagerData implements Serializable {
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
