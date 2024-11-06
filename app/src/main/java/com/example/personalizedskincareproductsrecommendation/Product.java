package com.example.personalizedskincareproductsrecommendation;

import com.google.firebase.database.PropertyName;

public class Product {
    private String id, brand, name, country, function, ingredients, afterUse, type, side_effect, skinType;
    private String image_url;

    // Default constructor is required for Firestore
    public Product() {}

    public Product(String id, String brand, String name, String country, String function, String ingredients, String afterUse, String type, String side_effect,String image_url) {
        this.id = id;
        this.brand = brand;
        this.name = name;
        this.country = country;
        this.function = function;
        this.ingredients = ingredients;
        this.afterUse = afterUse;
        this.type = type;
        this.side_effect = side_effect;
        this.image_url = image_url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSkinType() {
        return skinType;
    }

    public void setSkinType(String skinType) {
        this.skinType = skinType;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSide_effect() {
        return side_effect;
    }

    public void setSide_effect(String side_effect) {
        this.side_effect = side_effect;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getAfterUse() {
        return afterUse;
    }

    public void setAfterUse(String afterUse) {
        this.afterUse = afterUse;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageUrl() {
        return image_url;
    }

    public void setImageUrl(String image_url) {
        this.image_url = image_url;
    }
}

