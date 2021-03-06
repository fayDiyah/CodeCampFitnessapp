package com.codecamp.bitfit.database;

import com.codecamp.bitfit.util.Util;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.UUID;

/**
 * Created by MaxBreida on 15.02.18.
 */

public class Workout extends BaseModel {

    @PrimaryKey
    UUID id;

    @Column
    long durationInMillis;

    @Column
    double calories;

    @Column
    String currentDate;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setRandomId() { setId(UUID.randomUUID()); }

    public boolean hasId() { return id != null; }

    public long getDurationInMillis() {
        return durationInMillis;
    }

    public void setDurationInMillis(long durationInMillis) {
        this.durationInMillis = durationInMillis;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = Util.roundTwoDecimals(calories);
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate() {
        this.currentDate = Util.getCurrentDateAsString();
    }

    // initialized user for One to Many relation
    @ForeignKey(stubbedRelationship = true)
    User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
