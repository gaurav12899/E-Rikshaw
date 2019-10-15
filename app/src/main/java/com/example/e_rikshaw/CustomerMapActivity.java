package com.example.e_rikshaw;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
                                    GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;

    Location mLastLocation;
    LocationRequest mLocationRequest;
    private Button mLogout,mRequest,mSettings;
    private LatLng  pickUpLocation;

    private LinearLayout mDriverInfo;
    private Boolean requestBol =false;
    private Marker pickUpMarker;
    private String destination;

    private ImageView mDriverProfileImage;
    private TextView mDriverName, mDriverPhone,mDriverRikshaw ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        Places.initialize(getApplicationContext(), "AIzaSyAH50yMhxkO7TUCU1Mx8VJciKIqj7a1Idg");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(CustomerMapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }else{

            mapFragment.getMapAsync(this);

        }

        mDriverInfo= (LinearLayout) findViewById(R.id.driverInfo);
        mDriverProfileImage= (ImageView) findViewById(R.id.driverProfileImage);
        mDriverName= (TextView) findViewById(R.id.driverName);
        mDriverPhone= (TextView) findViewById(R.id.driverPhone);
        mDriverRikshaw =(TextView) findViewById(R.id.rikshaw);


        mLogout = (Button) findViewById(R.id.logout);
        mRequest = (Button) findViewById(R.id.request);
        mSettings = (Button) findViewById(R.id.settings);
        PlacesClient placesClient = Places.createClient(this);


        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(CustomerMapActivity.this, MainActivity.class));
                finish();
                return;
            }
        });
        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestBol) {
                    requestBol = false;
                    geoQuery.removeAllListeners();
                    driverLocationRef.removeEventListener(driverLocationRefListener);

                    if (driverFoundID != null) {
                        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("users").child("drivers").child(driverFoundID).child("customerRequest");
                        driverRef.removeValue();
                        driverFoundID = null;

                    }
                    driverFound = false;
                    radius = 1;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    try {
                        if (!(geoFire == null)) {
                            geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    if (error != null) {
                                        System.err.println("There was an error removing the location from GeoFire: " + error);

                                    } else {
                                        System.out.println("Location removed on server successfully!");

                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (pickUpMarker != null) {
                        pickUpMarker.remove();
                    }
          /*          if(mDriverMarker!=null){
                        mDriverMarker.remove();
                    }*/
                    mDriverInfo.setVisibility(View.GONE);
                    mDriverName.setText("");
                    mDriverPhone.setText("");
                    mDriverProfileImage.setImageResource(R.mipmap.user_profile);
                    mDriverRikshaw.setText("");

                } else {
                    requestBol = true;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            Log.e("mytag", "geofire complete");
                        }
                    });

                    pickUpLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    pickUpMarker = mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("pickup here"));

                    mRequest.setText("Getting your Driver.....");


                    getClosestDriver();

                }
            }
        });
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerMapActivity.this, CustomerSettingsActivity.class);
                startActivity(intent);
                return;
            }
        });

//try

//try

         // Initialize the AutocompleteSupportFragment.
         AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
         getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

         // Specify the types of place data to return.
         autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

         // Set up a PlaceSelectionListener to handle the response.
         autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
        @Override public void onPlaceSelected(Place place) {
        // TODO: Get info about the selected place.
        destination = place.getName().toString();
        }

        @Override public void onError(Status status) {
        // TODO: Handle the error.
        }
        });

//try


/***       Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                destination = place.getName().toString();

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.\
            }
        });
***/
//try
    }
private int radius=1;
    private boolean driverFound= false;
    private String driverFoundID;

    GeoQuery geoQuery;
private void getClosestDriver(){

    DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
    GeoFire geoFire = new GeoFire(driverLocation);
    GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(pickUpLocation.latitude,pickUpLocation.longitude),radius);
    geoQuery.removeAllListeners();


    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            if(!driverFound && requestBol){
                driverFound=true;
                driverFoundID=key;
                DatabaseReference driverRef =FirebaseDatabase.getInstance().getReference().child("users").child("drivers").child(driverFoundID).child("customerRequest");
                String customerId= FirebaseAuth.getInstance().getCurrentUser().getUid();
                HashMap map = new HashMap();
                map.put("customerRideId",customerId);

                map.put("destination",destination);
                driverRef.updateChildren(map);

                getDriverLocation();
                getDriverInfo();
                mRequest.setText("Looking for Drivers Location...");

            }
        }

        @Override
        public void onKeyExited(String key) {

        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {

        }

        @Override
        public void onGeoQueryReady() {
            if(!driverFound)
            {
            radius++;
            getClosestDriver();
            }
        }

        @Override
        public void onGeoQueryError(DatabaseError error) {

        }
    });
}
    private Marker mDriverMarker;
private DatabaseReference driverLocationRef;
private ValueEventListener driverLocationRefListener;
    private  void getDriverLocation() {
    driverLocationRef =FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundID).child("l");
        driverLocationRefListener =driverLocationRef.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {


            if(dataSnapshot.exists() && requestBol){
                List<Object> map =(List<Object> )dataSnapshot.getValue();
                double locationLat = 0;
                double locationLng = 0;
                System.out.println("system invoked");
                mRequest.setText("Driver Found");
                if(map.get(0)!=null){
                    locationLat=Double.parseDouble(map.get(0).toString());

                }
                if(map.get(1)!=null){
                     locationLng=Double.parseDouble(map.get(1).toString());
                }
                LatLng driverLatLng =new LatLng(locationLat,locationLng);
                    if(mDriverMarker!=null ){
                        mDriverMarker.remove();
                    }


                    Location loc1=new Location("");
                    loc1.setLatitude(pickUpLocation.latitude);
                    loc1.setLongitude(pickUpLocation.longitude);


                Location loc2=new Location("");
                loc2.setLatitude(driverLatLng.latitude);
                loc2.setLongitude(driverLatLng.longitude);

                float distance = loc1.distanceTo(loc2);
                if(distance<1) {
                    mRequest.setText("Driver's here" );
                }
                else{

                    mRequest.setText("Driver Found" + String.valueOf(distance));
                }
                mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("your driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.driver)));
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });

}

    private void getDriverInfo() {
        mDriverInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("users").child("drivers").child(driverFoundID);

        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                      if (map.get("name") != null) {

                        mDriverName.setText(map.get("name").toString());
                    }
                    if (map.get("phone") != null) {

                        mDriverPhone.setText(map.get("phone").toString());
                    }
                    if(map.get("rikshaw")!=null){
                        mDriverRikshaw.setText(map.get("rikshaw").toString());

                    }
                    if (map.get("profileImageUrl") != null) {
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mDriverProfileImage);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(CustomerMapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);


        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest =new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);

        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

final int LOCATION_REQUEST_CODE =1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                }else{
                    Toast.makeText(getApplicationContext(),"Please the permissions",Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
