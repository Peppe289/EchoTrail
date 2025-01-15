package com.peppe289.echotrail.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CardItem {
    @NonNull
    private final String author;
    @NonNull
    private final String description;
    @NonNull
    private final String date;
    @NonNull
    private final String city;
    @NonNull
    private final String id;
    @Nullable
    private final String userId;

    public CardItem(@NonNull String author, @NonNull String description, @NonNull String date, @NonNull String city, @Nullable String userId, @NonNull String id) {
        this.author = author;
        this.description = description;
        this.date = date;
        this.city = city;
        this.userId = userId;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public String getUserId() {
        return userId;
    }

    @NonNull
    public String getAuthor() {
        return author;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    @NonNull
    public String getCity() {
        return city;
    }
}
