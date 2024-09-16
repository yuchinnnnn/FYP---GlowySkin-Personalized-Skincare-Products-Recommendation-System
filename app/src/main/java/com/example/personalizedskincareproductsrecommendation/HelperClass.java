package com.example.personalizedskincareproductsrecommendation;

import java.util.HashMap;
import java.util.Map;

public class HelperClass {
    private String userId;
    private String username;
    private String email;
    private String password;
    private Map<String, Object> skinQuiz, skinGoals, skinAnalysis, history, products, skinLog;

    public HelperClass() {
        // Default constructor required for calls to DataSnapshot.getValue(HelperClass.class)
    }

    public HelperClass(String username, String email, String password, String userId, Map<String, Object> skinQuiz, Map<String, Object> skinGoals, Map<String, Object> skinAnalysis, Map<String, Object> history, Map<String, Object> products, Map<String, Object> skinLog) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.userId = userId;
        this.skinQuiz = skinQuiz;
        this.skinGoals = skinGoals;
        this.skinAnalysis = skinAnalysis;
        this.history = history;
        this.products = products;
        this.skinLog = skinLog;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, Object> getSkinQuiz() {
        return skinQuiz;
    }

    public void setSkinQuiz(Map<String, Object> skinQuiz) {
        this.skinQuiz = skinQuiz;
    }

    public Map<String, Object> getSkinGoals() {
        return skinGoals;
    }

    public void setSkinGoals(Map<String, Object> skinGoals) {
        this.skinGoals = skinGoals;
    }

    public Map<String, Object> getSkinAnalysis() {
        return skinAnalysis;
    }

    public void setSkinAnalysis(Map<String, Object> skinAnalysis) {
        this.skinAnalysis = skinAnalysis;
    }

    public Map<String, Object> getHistory() {
        return history;
    }

    public void setHistory(Map<String, Object> history) {
        this.history = history;
    }

    public Map<String, Object> getProducts() {
        return products;
    }

    public void setProducts(Map<String, Object> products) {
        this.products = products;
    }

    public Map<String, Object> getSkinLog() {
        return skinLog;
    }

    public void setSkinLog(Map<String, Object> skinLog) {
        this.skinLog = skinLog;
    }
}
