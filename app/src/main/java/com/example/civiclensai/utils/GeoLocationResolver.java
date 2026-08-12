package com.example.civiclensai.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeoLocationResolver {

    private static final String TAG = "GeoLocationResolver";

    // Default Pune Coordinates
    public static final double PUNE_LAT = 18.5204;
    public static final double PUNE_LNG = 73.8567;

    /**
     * Resolves text address to exact Latitude and Longitude using Android Geocoder API with fallback keyword mapping.
     */
    public static double[] resolveCoordinates(Context context, String addressInput) {
        if (addressInput == null || addressInput.trim().isEmpty()) {
            return new double[]{PUNE_LAT, PUNE_LNG};
        }

        String query = addressInput.trim();

        // 1. Attempt System Geocoder Lookup
        try {
            if (context != null && Geocoder.isPresent()) {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(query, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    Log.d(TAG, "Geocoder successfully resolved: " + address.getLatitude() + ", " + address.getLongitude());
                    return new double[]{address.getLatitude(), address.getLongitude()};
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Geocoder lookup failed for '" + query + "', falling back to keyword mapper: " + t.getMessage());
        }

        // 2. Keyword Coordinates Resolver (Pune & Major Sectors)
        String lower = query.toLowerCase(Locale.ROOT);

        if (lower.contains("kothrud") || lower.contains("karve")) {
            return new double[]{18.5074 + randomOffset(), 73.8077 + randomOffset()};
        } else if (lower.contains("viman") || lower.contains("nagar road") || lower.contains("airport")) {
            return new double[]{18.5679 + randomOffset(), 73.9143 + randomOffset()};
        } else if (lower.contains("fc road") || lower.contains("fergusson") || lower.contains("shivajinagar") || lower.contains("jm road")) {
            return new double[]{18.5308 + randomOffset(), 73.8474 + randomOffset()};
        } else if (lower.contains("hadapsar") || lower.contains("magarpatta") || lower.contains("solapur")) {
            return new double[]{18.5089 + randomOffset(), 73.9260 + randomOffset()};
        } else if (lower.contains("pimpri") || lower.contains("chinchwad") || lower.contains("hinjewadi") || lower.contains("wakad")) {
            return new double[]{18.5987 + randomOffset(), 73.7707 + randomOffset()};
        } else if (lower.contains("baner") || lower.contains("balewadi") || lower.contains("pashan")) {
            return new double[]{18.5590 + randomOffset(), 73.7868 + randomOffset()};
        } else if (lower.contains("swargate") || lower.contains("katraj") || lower.contains("satara")) {
            return new double[]{18.4975 + randomOffset(), 73.8566 + randomOffset()};
        } else if (lower.contains("pune")) {
            return new double[]{PUNE_LAT + randomOffset(), PUNE_LNG + randomOffset()};
        } else if (lower.contains("mumbai")) {
            return new double[]{19.0760 + randomOffset(), 72.8777 + randomOffset()};
        } else if (lower.contains("delhi")) {
            return new double[]{28.6139 + randomOffset(), 77.2090 + randomOffset()};
        } else if (lower.contains("bangalore") || lower.contains("bengaluru")) {
            return new double[]{12.9716 + randomOffset(), 77.5946 + randomOffset()};
        }

        // Default to Pune Center with small randomized offset
        return new double[]{PUNE_LAT + randomOffset(), PUNE_LNG + randomOffset()};
    }

    private static double randomOffset() {
        return (Math.random() - 0.5) * 0.008;
    }
}
