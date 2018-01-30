package org.entur.demo.ukur.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.*;

public class Subscription implements Serializable, Comparable {

    private String id;
    private String name;
    private ArrayList<String> fromStopPoints = new ArrayList<>();
    private ArrayList<String> toStopPoints = new ArrayList<>();
    private String pushAddress;
    @JsonIgnore
    private int numberOfMessages = 0;
    @JsonIgnore
    private String pushId;


    public ArrayList<String> getFromStopPoints() {
        return fromStopPoints;
    }

    public void addFromStopPoint(String stopPointRef) {
        fromStopPoints.add(stopPointRef);
    }

    public ArrayList<String> getToStopPoints() {
        return toStopPoints;
    }

    public void addToStopPoint(String stopPointRef) {
        toStopPoints.add(stopPointRef);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumberOfMessages() {
        return numberOfMessages;
    }

    public void setNumberOfMessages(int numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
    }

    public String getPushAddress() {
        return pushAddress;
    }

    public void setPushAddress(String pushAddress) {
        this.pushAddress = pushAddress;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Object o) {
        Subscription that = (Subscription) o;
        return this.pushId.compareTo(that.pushId);
    }
}
