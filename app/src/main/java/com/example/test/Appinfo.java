package com.example.test;


import java.io.Serializable;
import java.util.Objects;

public class Appinfo implements Serializable {

    private String username;
    private String macaddress;
    private String appId;
    private String appName;
    private String appIcon;
    private String dayTotalFlowString;
    private String weekTotalFlowString;
    private String monthTotalFlowString;
    private String dayTotalTime;
    private String weekTotalTime;
    private String monthTotalTime;
    private String description;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMacaddress() {
        return macaddress;
    }

    public void setMacaddress(String macaddress) {
        this.macaddress = macaddress;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(String appIcon) {
        this.appIcon = appIcon;
    }

    public String getDayTotalFlowString() {
        return dayTotalFlowString;
    }

    public void setDayTotalFlowString(String dayTotalFlowString) {
        this.dayTotalFlowString = dayTotalFlowString;
    }

    public String getWeekTotalFlowString() {
        return weekTotalFlowString;
    }

    public void setWeekTotalFlowString(String weekTotalFlowString) {
        this.weekTotalFlowString = weekTotalFlowString;
    }

    public String getMonthTotalFlowString() {
        return monthTotalFlowString;
    }

    public void setMonthTotalFlowString(String monthTotalFlowString) {
        this.monthTotalFlowString = monthTotalFlowString;
    }

    public String getDayTotalTime() {
        return dayTotalTime;
    }

    public void setDayTotalTime(String dayTotalTime) {
        this.dayTotalTime = dayTotalTime;
    }

    public String getWeekTotalTime() {
        return weekTotalTime;
    }

    public void setWeekTotalTime(String weekTotalTime) {
        this.weekTotalTime = weekTotalTime;
    }

    public String getMonthTotalTime() {
        return monthTotalTime;
    }

    public void setMonthTotalTime(String monthTotalTime) {
        this.monthTotalTime = monthTotalTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
