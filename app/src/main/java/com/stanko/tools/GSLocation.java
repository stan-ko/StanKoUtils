package com.stanko.tools;

//
//import android.content.Context;
//import android.location.Location;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.util.Log;
//
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationServices;
//import com.onebit.nimbusnote.application.App;
//
///**
// * Created by scijoker on 12.03.14.
// */
public class GSLocation {}
//        implements
//        GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener,
//        android.location.LocationListener
//{
//
//    private static final String TAG = GSLocation.class.getSimpleName();
//
//    private LocationChangeStateListener locationChangeStateListener;
//    private GoogleApiClient mGoogleApiClient;
//    private Context context;
//
//    public GSLocation(Context context) {
//        this.context = context;
//        buildGoogleApiClient(context);
//    }
//
//    protected synchronized void buildGoogleApiClient(Context context) {
//        mGoogleApiClient = new GoogleApiClient.Builder(context)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//        makeGetLocationRequest();
//    }
//
//    public void makeGetLocationRequest() {
//        if (GeoUtils.isGooglePlayServicesAvailable(context)) {
//            mGoogleApiClient.connect();
//        } else {
//            if (locationChangeStateListener != null) {
//                locationChangeStateListener.onCurrentLocationChanged(null);
//            }
//        }
//    }
//
//    public void setLocationChangeStateListener(LocationChangeStateListener locationChangeStateListener) {
//        this.locationChangeStateListener = locationChangeStateListener;
//    }
//
//    @Override
//    public void onConnected(Bundle bundle) {
//        if (GeoUtils.isGPSTurned(App.getGlobalAppContext())) {
//            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, getRequestRequest(), location -> {
//                if (locationChangeStateListener != null) {
//                    locationChangeStateListener.onCurrentLocationChanged(location);
//                }
//                Log.d(TAG, "requestLocationUpdates()::" + location.getLatitude() + " " + location.getLongitude());
//                if (mGoogleApiClient.isConnected()) {
//                    mGoogleApiClient.disconnect();
//                }
//            });
//        }
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        mGoogleApiClient.connect();
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
//    }
//
//    private LocationRequest getRequestRequest() {
//        LocationRequest request = new LocationRequest();
//        request.setFastestInterval(5000);
//        request.setInterval(10000);
//        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        return request;
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//
//    }
//}