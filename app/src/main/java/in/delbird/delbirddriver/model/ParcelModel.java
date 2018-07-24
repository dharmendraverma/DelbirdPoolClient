package in.delbird.delbirddriver.model;

import java.io.Serializable;

/**
 * Created by machine2 on 1/27/16.
 */
public class ParcelModel implements Serializable {
    long parcelId;
    long rideId;
    String parcelName;
    String receiverName;
    String receiverPhone;
    boolean isDelivered;
    String flatNumber;
    String pinCode;
    String state;
    String city;
    String status;
    boolean isOptionButtonDisable=false;

    public boolean isOptionButtonDisable() {
        return isOptionButtonDisable;
    }

    public void setIsOptionButtonDisable(boolean isOptionButtonDisable) {
        this.isOptionButtonDisable = isOptionButtonDisable;
    }

    public String getParcelName() {
        return parcelName;
    }

    public void setParcelName(String parcelName) {
        this.parcelName = parcelName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getFlatNumber() {
        return flatNumber;
    }

    public void setFlatNumber(String flatNumber) {
        this.flatNumber = flatNumber;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public long getParcelId() {
        return parcelId;
    }

    public void setParcelId(long parcelId) {
        this.parcelId = parcelId;
    }

    public long getRideId() {
        return rideId;
    }

    public void setRideId(long rideId) {
        this.rideId = rideId;
    }


    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public boolean isDelivered() {
        return isDelivered;
    }

    public void setIsDelivered(boolean isDelivered) {
        this.isDelivered = isDelivered;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
