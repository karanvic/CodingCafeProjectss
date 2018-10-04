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
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mlastlocation;
    private LocationRequest mLocationRequest;
    private LatLng customerpickuplocation;
    private int radius=1;
    private Boolean driverfound=false;
    private String driverfoundID;

    private Button LogoutCustomerButton;
    private Button SettingsCustomerButton;
    private FirebaseAuth mAuth;
    private FirebaseUser currentuser;
    private Button CustomerRequestBtn;
    private String customerID;
    private DatabaseReference customerdbref;
    private DatabaseReference driveravailableref;
    private DatabaseReference driversref;
    private DatabaseReference driverlocationref;
    Marker drivermarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        mAuth=FirebaseAuth.getInstance();
        currentuser=mAuth.getCurrentUser();
        customerID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        customerdbref=FirebaseDatabase.getInstance().getReference().child("Customers Request");
        driveravailableref=FirebaseDatabase.getInstance().getReference().child("DriversAv");
        driverlocationref=FirebaseDatabase.getInstance().getReference().child("Drivers Working");


        LogoutCustomerButton=findViewById(R.id.customer_logout_btn);
        CustomerRequestBtn=findViewById(R.id.customerRequest);



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        LogoutCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                mAuth.signOut();
                LogoutCustomer();

            }
        });
        CustomerRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GeoFire geoFire=new GeoFire(customerdbref);
                geoFire.setLocation(customerID,new GeoLocation(mlastlocation.getLatitude(),mlastlocation.getLongitude()));

                customerpickuplocation=new LatLng(mlastlocation.getLatitude(),mlastlocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(customerpickuplocation).title("Pick Me From Here!!"));

                CustomerRequestBtn.setText("Car is Coming....Please Wait!!!");
                GetClosestDriverCab();
            }
        });
    }

    private void GetClosestDriverCab()
    {
        GeoFire geoFire=new GeoFire(driveravailableref);
        GeoQuery geoQuery=geoFire.queryAtLocation(new GeoLocation(customerpickuplocation.latitude,customerpickuplocation.longitude),1);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location)
            {
                if (!driverfound){
                    driverfound=true;
                    driverfoundID=key;


                    driversref=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverfoundID);
                    HashMap drivermap=new HashMap();
                    drivermap.put("CustomerRideID",customerID);
                    driversref.updateChildren(drivermap);
                    GettingDriverLocation();
                    CustomerRequestBtn.setText("Looking For a Driver Location!!!");
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady()
            {
                if (!driverfound){
                    radius=radius+1;
                    GetClosestDriverCab();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void GettingDriverLocation()
    {
       driverlocationref.child(driverfoundID).child("I").addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot)
           {
               List<Object> driverlocationmap=(List<Object>) dataSnapshot.getValue();
               double locationlat=0;
               double locationlng=0;
               CustomerRequestBtn.setText("Driver Found!!!");


               if(driverlocationmap.get(0)!=null)
               {
                   locationlat=Double.parseDouble(driverlocationmap.get(0).toString());
               }
               if(driverlocationmap.get(1)!=null)
               {
                   locationlng=Double.parseDouble(driverlocationmap.get(1).toString());
               }

               LatLng driverlatlng=new LatLng(locationlat,locationlng);

               if (drivermarker!=null){
                   drivermarker.remove();
               }

               Location location1=new Location("");
               location1.setLatitude(customerpickuplocation.latitude);
               location1.setLongitude(customerpickuplocation.longitude);

               Location location2=new Location("");
               location2.setLatitude(driverlatlng.latitude);
               location2.setLongitude(driverlatlng.longitude);

               float Distance=location1.distanceTo(location2);
               CustomerRequestBtn.setText("Driver Found" + String.valueOf(Distance));

               drivermarker=mMap.addMarker(new MarkerOptions().position(driverlatlng).title("Your Driver is Here!!"));
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
  return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
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

        mlastlocation=location;

        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));



    }
    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }
    private void LogoutCustomer()
    {
        Intent mainIntent=new Intent(CustomerMapActivity.this,WelcomeActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    }

