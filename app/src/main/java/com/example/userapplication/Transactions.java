package com.example.userapplication;

public class Transactions {

    private String date_time, status, channel, amount_hist, reference;
    private boolean expandable;

    public Transactions(String date_time, String status, String channel, String amount_hist, String reference) {
        this.date_time = date_time;
        this.status = status;
        this.channel = channel;
        this.amount_hist = amount_hist;
        this.reference = reference;
        this.expandable = false;
    }

    public boolean isExpandable() {
        return expandable;
    }

    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getAmount_hist() {
        return amount_hist;
    }

    public void setAmount_hist(String amount_hist) {
        this.amount_hist = amount_hist;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }


    @Override
    public String toString() {
        return "Transactions{" +
                "date_time='" + date_time + '\'' +
                ", status='" + status + '\'' +
                ", channel='" + channel + '\'' +
                ", amount_hist='" + amount_hist + '\'' +
                ", reference='" + reference + '\'' +
                '}';
    }
}
