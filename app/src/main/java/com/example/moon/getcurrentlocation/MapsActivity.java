package com.example.moon.getcurrentlocation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private String TAG = "MyTag";
    String typeOfLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent intent = getIntent();
        typeOfLocation = intent.getStringExtra("type");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(TextUtils.equals(typeOfLocation,"f"))
            getLocationUsingFusedLocation(googleMap);
        else if(TextUtils.equals(typeOfLocation,"l"))
            getLocationUsingLocationManager(googleMap);
//
//
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void getLocationUsingLocationManager(GoogleMap googleMap) {
        if((ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},101);
        }else{
            LocationManager locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            Location bestLocation = null;
            float accurecy_max = 0.0f;

            List<String> allProviders = locationManager.getAllProviders();
            for(String provider:allProviders){
                Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
                if(lastKnownLocation!=null){
                    float accuracy = lastKnownLocation.getAccuracy();
                    if(accuracy>accurecy_max){
                        bestLocation = lastKnownLocation;
                        accurecy_max = accuracy;
                    }
                }
            }

            if(bestLocation!=null) {
                double latitude = bestLocation.getLatitude();
                double longitude = bestLocation.getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);
                googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
            }else{
                Toast.makeText(getApplicationContext(),"LOCATION NOT FOUND",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getLocationUsingFusedLocation(final GoogleMap googleMap) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
      if((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)||(
              ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)){
          ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},100);
      }else {
          Log.i(TAG, "fusedLocation: "+"OKK");
          mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
              @Override
              public void onSuccess(Location location) {
                  if (location == null) {
                      Toast.makeText(getApplicationContext(), "LOCATION NOT FOUND", Toast.LENGTH_SHORT).show();
                  }else{
                      double longitude = location.getLongitude();
                      double latitude = location.getLatitude();
                      MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(latitude,longitude)).draggable(false)
                              .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                      googleMap.addMarker(markerOptions);
                      googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),16.0f));

                      Log.i(TAG, "onSuccess: "+location.getLongitude()+","+location.getLatitude());
                      Toast.makeText(getApplicationContext(),"Lon = " + longitude + " Lat = "+latitude,Toast.LENGTH_SHORT).show();


                  }
              }
          }).addOnFailureListener(this, new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                  Log.i(TAG, "onFailure: ");
              }
          });
      }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==100){
             getLocationUsingFusedLocation(mMap);
        }else if(requestCode==101){
            getLocationUsingLocationManager(mMap);
        }
    }
}
