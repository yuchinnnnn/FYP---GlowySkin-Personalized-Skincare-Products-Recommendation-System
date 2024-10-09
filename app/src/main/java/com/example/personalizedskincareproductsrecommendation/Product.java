package com.example.personalizedskincareproductsrecommendation;

public class Product {
    private String brand, name, country, function, ingredients, afterUse, type, side_effect, skinType;
    private String imageUrl;

    // Default constructor is required for Firestore
    public Product() {}

    public Product(String brand, String name, String country, String function, String ingredients, String afterUse, String type, String side_effect,String imageUrl) {
        this.brand = brand;
        this.name = name;
        this.country = country;
        this.function = function;
        this.ingredients = ingredients;
        this.afterUse = afterUse;
        this.type = type;
        this.side_effect = side_effect;
        this.imageUrl = imageUrl;
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

    public String getSideEffect() {
        return side_effect;
    }

    public void setSideEffect(String side_effect) {
        this.side_effect = side_effect;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

}

