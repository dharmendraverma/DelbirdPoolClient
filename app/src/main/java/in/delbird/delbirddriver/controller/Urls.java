package in.delbird.delbirddriver.controller;

/**
 * Created by machine2 on 1/29/16.
 */
public class Urls {
    // Local Server.
    public static final String BASE_ADDRESS = "http://52.37.31.105:8080/delbirdMerchant/";


    public static final String SET_BIKE_ONLINE_STATUS = BASE_ADDRESS + "driver/homescreen";
    public static final String DRIVER_LOGIN = BASE_ADDRESS + "login/driver";
    public static final String ACCEPT_REJECT_REQUEST = BASE_ADDRESS + "accept/reject/ride";

    public static final String GET_RIDE_BY_ID = BASE_ADDRESS + "get/ride/id";
    public static final String RIDE_STATE_UPDATE = BASE_ADDRESS + "ride/state/update";

    public static final String PACKAGE_STATE_UPDATE = BASE_ADDRESS + "package/state/update";

    public static final String GET_PARCEL_LIST = BASE_ADDRESS + "get/ride/packages";

    public static final String FARE_SUMMARY = BASE_ADDRESS + "calculate/distance/cost";

    public static final String GET_RIDE_BY_DRIVER_ID = BASE_ADDRESS + "driver/get/id";

    public static final String UPDATE_DRIVER_LOCATION = BASE_ADDRESS + "driver/update/location";
    public static final String PAYMENT_SUCCESSFULL = BASE_ADDRESS + "ride/update/payment/status";

    public static final String CANCEL_RIDE = BASE_ADDRESS + "driver/cancel/ride";

    public static final String GET_RIDES_HISTORY = BASE_ADDRESS + "driver/history";


    public static final String GET_DRIVER_STATUS = BASE_ADDRESS + "driver/get/status";
}
