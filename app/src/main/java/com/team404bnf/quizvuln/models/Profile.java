package com.team404bnf.quizvuln.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "profile")
public class Profile {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public int totalPoints;
    public String imagePath;

    // Default constructor (Room requires it)
    public Profile() {}

    // Convenience constructor
    public Profile(String name, int totalPoints, String imagePath) {
        this.name = name;
        this.totalPoints = totalPoints;
        this.imagePath = imagePath;
    }
}
