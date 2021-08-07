package com.example.userapplication;

public class Months {

    private String date;

    public Months(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Months{" +
                "date='" + date + '\'' +
                '}';
    }
}
