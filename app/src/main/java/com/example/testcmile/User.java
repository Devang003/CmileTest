package com.example.testcmile;

public class User {
    private String id;
    private String username;
    private String number;
    private String Lat;
    private String Long;

    public User(String id, String username, String number, String lat, String aLong) {
        this.id = id;
        this.username = username;
        this.number = number;
        Lat = lat;
        Long = aLong;
    }
    public User(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getLat() {
        return Lat;
    }

    public void setLat(String lat) {
        this.Lat = lat;
    }

    public String getLong() {
        return Long;
    }

    public void setLong(String aLong) {
        this.Long = aLong;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", number='" + number + '\'' +
                '}';
    }

}
