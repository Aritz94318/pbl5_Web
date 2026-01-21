package edu.mondragon.we2.pinkalert.model;

public class Notification {
    private String email;
    private String topic;
    private String message;
    private String date;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Notification() {
    }

    public Notification(String email, String topic, String message, String date) {
        this.email = email;
        this.topic = topic;
        this.message = message;
        this.date = date;
    }

}
