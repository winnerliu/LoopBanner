package com.example.winner.loopbanner;

import com.kad.banner.entity.AbstractPagerData;

/**
 * Created by liuweiniang on 2016/11/10.
 */

public class StringData extends AbstractPagerData {
    String text;
    String name;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
