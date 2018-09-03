package com.geetapoojakaran.uberapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mlastlocation;
    private LocationRequest mLocationRequest;

    private Button LogoutDriverButton;
    private Button SettingsDriverButton;
    private FirebaseAuth mAuth;
    private FirebaseUser currentuser;
    private Boolean currentlogoutdriverstatus=false;
    private DatabaseReference AssignedcustomerRef,AssignedcustomerpickupRef;
    private String driverid,customerid="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);

        mAuth=FirebaseAuth.getInstance();
        currentuser=mAuth.getCurrentUser();
        driverid=mAuth.getCurrentUser().getUid();



        LogoutDriverButton=findViewById(R.id.driver_logout_btn);
        SettingsDriverButton=findViewById(R.id.driver_settings_btn);



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LogoutDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                currentlogoutdriverstatus=true;
                DisconnectTheDriver();
                mAuth.signOut();

                LogOutDriver();
            }
        });

        GetAssignedCustomerRequest();

    }

    private void GetAssignedCustomerRequest()
    {
         AssignedcustomerRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverid).child("CustomerRideID");

         AssignedcustomerRef.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot)
             {
                 if (dataSnapshot.exists()){
                     customerid=dataSnapshot.getValue().toString();

                     GetAssignedCustomerPickUpLocation();
                 }

             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });
    }

    private void GetAssignedCustomerPickUpLocation()
    {
        AssignedcustomerpickupRef=FirebaseDatabase.getInstance().getReference().child("Customers Requests").child(customerid).child("I");

        AssignedcustomerpickupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists()){
                    List<Object> customerlocationmap=(List<Object>) dataSnapshot.getValue();
                    double locationlat=0;
                    double locationlng=0;

                    if(customerlocationmap.get(0)!=null)
                    {
                        locationlat=Double.parseDouble(customerlocationmap.get(0).toString());
                    }
                    if(customerlocationmap.get(1)!=null)
                    {
                        locationlng=Double.parseDouble(customerlocationmap.get(1).toString());
                    }
                    LatLng driverlatlng=new LatLng(locationlat,locationlng);
                    mMap.addMarker(new MarkerOptions().position(driverlatlng).title("Pickup Location!!"));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        mMap.setMyLocationEnabled(true);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(mLocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location)
    {
       if (getApplicationContext()!=null){

           mlastlocation=location;

           LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
           mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
           mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
           mMap.addMarker(new MarkerOptions().position(latLng).title("Driver is Here!!"));

           String driverid= FirebaseAuth.getInstance().getCurrentUser().getUid();
           DatabaseReference refAv=FirebaseDatabase.getInstance().getReference("DriversAv");
           GeoFire geoFireAv=new GeoFire(refAv);

           DatabaseReference refworking=FirebaseDatabase.getInstance().getReference().child("Drivers Working");
           GeoFire geoFireworking=new GeoFire(refworking);


           switch (customerid){
               case "":
               geoFireworking.removeLocation(driverid);
                   geoFireAv.setLocation(driverid, new GeoLocation(location.getLatitude(),location.getLongitude()));
                   break;
                   default:
                       geoFireAv.removeLocation(driverid);
                       geoFireworking.setLocation(driverid, new GeoLocation(location.getLatitude(),location.getLongitude()));
                       break;
           }

       }
    }
    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!currentlogoutdriverstatus){
            DisconnectTheDriver();
        }
    }

    private void DisconnectTheDriver() {
        String driverId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driveravailabilityRef=FirebaseDatabase.getInstance().getReference("DriversAv");

        GeoFire geoFire=new GeoFire(driveravailabilityRef);
        geoFire.removeLocation(driverId);
    }
    private void LogOutDriver()
    {
        Intent mainIntent=new Intent(DriverMapActivity.this,WelcomeActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
