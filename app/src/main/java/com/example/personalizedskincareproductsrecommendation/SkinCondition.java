package com.example.personalizedskincareproductsrecommendation;

public class SkinCondition {
    private float acne;
    private float redness;
    private float wrinkles;
    private float darkSpot;
    private float darkCircle;
    private float pores;

    // Constructor
    public SkinCondition(float acne, float redness, float wrinkles, float darkSpot, float darkCircle, float pores) {
        this.acne = acne;
        this.redness = redness;
        this.wrinkles = wrinkles;
        this.darkSpot = darkSpot;
        this.darkCircle = darkCircle;
        this.pores = pores;
    }

    // Getters and Setters
    public float getAcne() {
        return acne;
    }

    public void setAcne(float acne) {
        this.acne = acne;
    }

    public float getRedness() {
        return redness;
    }

    public void setRedness(float redness) {
        this.redness = redness;
    }

    public float getWrinkles() {
        return wrinkles;
    }

    public void setWrinkles(float wrinkles) {
        this.wrinkles = wrinkles;
    }

    public float getDarkSpot() {
        return darkSpot;
    }

    public void setDarkSpot(float darkSpot) {
        this.darkSpot = darkSpot;
    }

    public float getDarkCircle() {
        return darkCircle;
    }

    public void setDarkCircle(float darkCircle) {
        this.darkCircle = darkCircle;
    }

    public float getPores() {
        return pores;
    }

    public void setPores(float pores) {
        this.pores = pores;
    }
}
