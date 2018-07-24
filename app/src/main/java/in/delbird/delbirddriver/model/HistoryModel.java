package in.delbird.delbirddriver.model;

/**
 * Created by machine2 on 2/8/16.
 */


import java.io.Serializable;

public class HistoryModel implements Serializable {
    Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    String picUrl;
    long creationTime;
    String rideType;
    long cost;
    String status;
    String paymentMode;
    long endTripTime;

    public long getEndTripTime() {
        return endTripTime;
    }

    public void setEndTripTime(long endTripTime) {
        this.endTripTime = endTripTime;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public String getRideType() {
        return rideType;
    }

    public void setRideType(String rideType) {
        this.rideType = rideType;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
