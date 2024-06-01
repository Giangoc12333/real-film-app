package com.example.RealFilm.model;

import com.google.gson.annotations.SerializedName;

public class Country {
    @SerializedName("code")
    private String code;

    @SerializedName("name")
    private String name;

    public Country(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
