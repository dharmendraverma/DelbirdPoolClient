package in.delbird.delbirddriver.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * Created by machine2 on 10/1/15.
 */
public class AddMarker {

    public static Marker addMarker(GoogleMap googleMap, Marker marker, LatLng latLng, int icon, String title) {
        if (marker != null) {
            marker.remove();
        }
        marker = googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(icon)).visible(true).title(title));
        return marker;
    }

    public static LatLng getLatLong(Context context, String address) {

        Geocoder geocoder = new Geocoder(context);
        try {
            int i = 1;
            List<Address> list = geocoder.getFromLocationName(address, i);
            while (list.size() == 0 && i < 4) {
                list = geocoder.getFromLocationName(address, i);
                i++;
            }
            if (list.size() > 0) {
                LatLng latLng = new LatLng(list.get(0).getLatitude(), list.get(0).getLongitude());
                Log.e("LatLNG", latLng + "");
                return latLng;
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    public static String getCurrentAddress(Context context, double lat, double lon) {
        double latitude = lat, longitude = lon;
        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(context);
            if (latitude != 0 || longitude != 0) {
                addresses = geocoder.getFromLocation(latitude,
                        longitude, 5);
                String name = addresses.get(0).getFeatureName();

                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getAddressLine(1);
                String country = addresses.get(0).getAddressLine(2);
                Show.showLog("Name" + name + "\naddress" + address + "\ncity" + city + "\ncountry" + country, "");
                return address + ", " + city;
            } else {
//                Toast.makeText(context, "latitude and longitude are null",
//                        Toast.LENGTH_LONG).show();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();


            return null;
        }
    }

}
