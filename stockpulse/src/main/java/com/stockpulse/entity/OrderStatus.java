package com.stockpulse.entity;

public enum OrderStatus {
    RESERVED,   // Stock 10 min ke liye hold par hai
    CONFIRMED,  // Payment ho gayi, stock pakka bik gaya
    EXPIRED     // 10 min mein payment nahi hui, stock wapas aa gaya
}