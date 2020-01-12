package com.example.e_rikshaw;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

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
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.markushi.ui.CircleButton;
import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerMapActivityFinal<place1> extends AppCompatActivity  implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Spinner mNoOfSeats;
    FirebaseAuth mAuth;
    String userId,mName,mProfileImageUrl,mPhone;


    private int flag=0;
    FirebaseUser currentUser;
    DatabaseReference mRef;
    StorageReference mStorage;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private Button  mRequest,mConfirm,mGotIt;
    private ImageButton mFareInfo;
    private CircleButton mSearchButton;
    private LatLng pickUpLocation;
    private int searchLayoutStatus=0;

    private LinearLayout mDriverInfo;
    private CardView mSearchNearby,mFinalConfirmationLayout;
    private Boolean requestBol = false;
    private Marker pickUpMarker;
    private String destination;
    //private LatLng destinationLatLng;
    private ImageView mDriverProfileImage;
    private TextView mDriverName, mDriverPhone,mPrice,mTotalFare,mDiscount,mPayableAmount;
    int discount=1;

    Dialog mFareInfoDialog;
    private   double latitude,longitude;
    private  int ProximityRadius =10000,status=0;
    ;
    private AppBarConfiguration mAppBarConfiguration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map_activity_final);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSearchButton=findViewById(R.id.search);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userId = mAuth.getCurrentUser().getUid();
        mFareInfo=findViewById(R.id.fareInfo);
        mRef = FirebaseDatabase.getInstance().getReference().child("users").child("customers").child(userId);
        mStorage = FirebaseStorage.getInstance().getReference().child("customer_profile_images").child(userId);
        mSearchNearby=findViewById(R.id.searchLayout);
        mFinalConfirmationLayout=findViewById(R.id.FinalConfirmationLayout);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapActivityFinal.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {

            mapFragment.getMapAsync(this);


        }
        mDriverInfo = (LinearLayout) findViewById(R.id.driverInfo);
        mDriverProfileImage = (ImageView) findViewById(R.id.driverProfileImage);
        mDriverName = (TextView) findViewById(R.id.driverName);
        mDriverPhone = (TextView) findViewById(R.id.driverPhone);
//        mDriverRikshaw =(TextView) findViewById(R.id.rikshaw);

        mNoOfSeats=findViewById(R.id.no_of_seats);
        mPrice=findViewById(R.id.priceText);




        mConfirm = (Button) findViewById(R.id.confirm);
        mRequest=(Button)findViewById(R.id.request);
        //  PlacesClient placesClient = Places.createClient(this);

        mFareInfoDialog = new Dialog(CustomerMapActivityFinal.this);

        List<String> list1 =new ArrayList<String>();
        list1.add("1");
        list1.add("2");
        list1.add("3");

        ArrayAdapter<String> arrayAdapter =new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list1);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mNoOfSeats.setAdapter(arrayAdapter);
        mNoOfSeats.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mNoOfSeats.setSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


mNoOfSeats.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        mPrice.setText(Integer.toString(Integer.parseInt(mNoOfSeats.getSelectedItem().toString())*7-discount));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
});

        mRequest.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        mFinalConfirmationLayout.setVisibility(View.VISIBLE);

        mFareInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFareInfoDialog.setContentView(R.layout.pricechart);

                mFareInfoDialog.show();
                mGotIt=mFareInfoDialog.findViewById(R.id.gotIt);
                mDiscount=mFareInfoDialog.findViewById(R.id.Discount);
                mTotalFare=mFareInfoDialog.findViewById(R.id.totalFare);
                mPayableAmount=mFareInfoDialog.findViewById(R.id.payableAmount);
                mTotalFare.setText(Integer.toString(Integer.parseInt(mNoOfSeats.getSelectedItem().toString())*7));
                mDiscount.setText(Integer.toString(discount));
                mPayableAmount.setText(Integer.toString(Integer.parseInt(mNoOfSeats.getSelectedItem().toString())*7-discount));

                mGotIt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFareInfoDialog.dismiss();

                    }
                });
            }
        });
        status=1;
        if(status==1&& requestBol && driverFound){
            endRide();
        }
    }
});



        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(searchLayoutStatus==0) {
                mSearchNearby.setVisibility(View.VISIBLE);
                searchLayoutStatus=1;
            }
            else if(searchLayoutStatus==1){
                mSearchNearby.setVisibility(View.GONE);
                searchLayoutStatus=0;
            }
            }
        });







        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (requestBol && driverFound) {
                        endRide();

                }
                else {
                    mFinalConfirmationLayout.setVisibility(View.INVISIBLE);
                    requestBol = true;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    Map map=new HashMap();
                    map.put("seats",mNoOfSeats.getSelectedItem().toString());

                     GeoFire geoFire = new GeoFire(ref);
                    try {
                        geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                Log.e("mytag", "geofire complete");

                            }
                        });

                        pickUpLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        pickUpMarker = mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("pickup here"));
                        mRequest.setText("Getting your Driver.....");
                        flag=1;

                        getClosestDriver();
                    } catch (NullPointerException e) {
                        Log.d("Null pointer exception", "nullpointerexception");
                    }
ref.child(userId).updateChildren(map);
                }
            }
        });


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyAH50yMhxkO7TUCU1Mx8VJciKIqj7a1Idg");
        }

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                destination = place.getName().toString();
    //            destinationLatLng = place.getLatLng();

            }


            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });


}


    private int radius = 1;
    private boolean driverFound = false;
    private String driverFoundID;

    GeoQuery geoQuery;

    private void getClosestDriver() {

        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickUpLocation.latitude, pickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestBol) {
                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("users").child("drivers").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                                if (driverFound) {
                                    return;
                                }

                                driverFound = true;
                                driverFoundID = dataSnapshot.getKey();

                                DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("users").child("drivers").child(driverFoundID).child("customerRequest");

                                String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                HashMap map = new HashMap();
                                map.put("customerRideId", customerId);
                                map.put("destination", destination);
//                              map.put("destinationLat", destinationLatLng.latitude);
  //                              map.put("destinationLng", destinationLatLng.longitude);
                                driverRef.updateChildren(map);

                                getDriverLocation();
                                getDriverInfo();
                                getHasRideEnded();
                                mRequest.setText("Looking for Driver Location....");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
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
                if (!driverFound) {
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

    private void getDriverLocation() {
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundID).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (dataSnapshot.exists() && requestBol) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;

                    mRequest.setText("Driver Found");
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());

                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat, locationLng);
                    if (mDriverMarker != null) {
                        mDriverMarker.remove();
                    }


                    Location loc1 = new Location("");
                    loc1.setLatitude(pickUpLocation.latitude);
                    loc1.setLongitude(pickUpLocation.longitude);


                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);
                    if (distance < 1) {
                        mRequest.setText("Driver's here");
                    } else {

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
                    if (map.get("car") != null) {
                        //       mDriverRikshaw.setText(map.get("car").toString());

                    }
                    if (map.get("ProfileImageUrl") != null) {
                        Glide.with(getApplication()).load(map.get("ProfileImageUrl").toString()).into(mDriverProfileImage);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;
    private void getHasRideEnded(){
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("users").child("drivers").child(driverFoundID).child("customerRequest").child("customerRideId");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                }else{
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void endRide() {
        requestBol = false;

            geoQuery.removeAllListeners();
            driverLocationRef.removeEventListener(driverLocationRefListener);
            driveHasEndedRef.removeEventListener(driveHasEndedRefListener);

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
            //geoFire.removeLocation(userId);
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
            if (mDriverMarker != null) {
                mDriverMarker.remove();
            }
            mFinalConfirmationLayout.setVisibility(View.INVISIBLE);
            startActivity(new Intent(CustomerMapActivityFinal.this,RidecComplete.class));

            mRequest.setText("call E-Rikshaw");

            mDriverInfo.setVisibility(View.GONE);
            mDriverName.setText("");
            mDriverPhone.setText("");
            // mDriverCar.setText("Destination: --");
            mDriverProfileImage.setImageResource(R.mipmap.user_profile);
        }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapActivityFinal.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);


        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();


    }

    @Override
    public void onLocationChanged(Location location) {
        latitude=location.getLatitude();
        longitude=location.getLongitude();
        mLastLocation = location;
        LatLng initialLoc= mMap.getCameraPosition().target;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        if (!getDriversAroundStarted)
            getDriversAround();


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);


        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    final int LOCATION_REQUEST_CODE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                } else {
                    Toast.makeText(getApplicationContext(), "Please the permissions", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    Boolean getDriversAroundStarted = false;
    List<Marker> markerList = new ArrayList<Marker>();

    private void getDriversAround() {
        getDriversAroundStarted = true;
        DatabaseReference driversLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        GeoFire geoFire = new GeoFire(driversLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 3000);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
//this  function is used when geo Querry will we used and add the marker  in the marker list i.e  list of  available drivers marker list.
                for (Marker markerIt : markerList) {
                    if (markerIt.getTag().equals(key))
                        return;

                }

                LatLng driverLocation = new LatLng(location.latitude, location.longitude);
                Marker mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).title(key));
                mDriverMarker.setTag(key);
                markerList.add(mDriverMarker);


            }

            @Override
            public void onKeyExited(String key) {
                for (Marker markerIt : markerList)
                    if (markerIt.getTag().equals(key)) {
                        markerIt.remove();
                        markerList.remove(markerIt);
                        return;
                    }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for (Marker markerIt : markerList)
                    if (markerIt.getTag().equals(key)) {
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));

                    }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .

                        setDrawerLayout(drawer)
                .

                        build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        updateNavBar();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    case R.id.nav_logout: {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(CustomerMapActivityFinal.this, MainActivity.class));

                        finish();
                        break;


                    }
                    case R.id.nav_profile: {
                        startActivity(new Intent(CustomerMapActivityFinal.this, CustomerSettingsActivity.class));
                        Toast.makeText(CustomerMapActivityFinal.this, "Profile", Toast.LENGTH_SHORT).show();
                    break;
                    }
                    case R.id.nav_history: {


                        Intent i= new Intent(CustomerMapActivityFinal.this, HistoryActivity.class);
                        i.putExtra("customerOrDriver","customers");
                            startActivity(i);
                        Toast.makeText(CustomerMapActivityFinal.this, "history", Toast.LENGTH_SHORT).show();
                    break;
                    }
                }
                return false;
            }
        });
    }


public void  updateNavBar(){
        NavigationView navigationView=(NavigationView) findViewById(R.id.nav_view);
        View headerView= navigationView.getHeaderView(0);
        final TextView navUsername = headerView.findViewById(R.id.nav_header_name);
    final CircleImageView navImage= headerView.findViewById(R.id.nav_header_image);

    final TextView navPhone = headerView.findViewById(R.id.nav_header_phone);

  //  Glide.with(this).load(currentUser.getPhotoUrl()).into(navImage);

    






    mRef.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                if(map.get("name")!=null){
                    mName = "Hi "+map.get("name").toString();
                    navUsername.setText(mName);
                }
                if(map.get("phone")!=null){
                    mPhone = map.get("phone").toString();
                    navPhone.setText(mPhone);
                }
                if(map.get("profileImageUrl")!=null){
                    mProfileImageUrl = map.get("profileImageUrl").toString();
                    Glide.with(getApplication()).load(mProfileImageUrl).into(navImage);

                }
            }
        }




        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    });
}

        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.main3, menu);
            return true;
        }

        @Override
        public boolean onSupportNavigateUp () {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                    || super.onSupportNavigateUp();
        }
public void onClick(View v){


String hospitals="hospital",schools="school",restaurants="restaurant",markets="market",Metros="metro";
Object transferData[]=new Object[2];
GetNearbyPlaces getNearbyPlaces= new GetNearbyPlaces();




 switch (v.getId()){
        case R.id.hospitals:
            mMap.clear();
            String url=getUrl(latitude,longitude,hospitals);
            transferData[0]=mMap;
            transferData[1]=url;
            getNearbyPlaces.execute(transferData);
            Toast.makeText(this,"searching for nearby hospitals...",Toast.LENGTH_LONG).show();

            Toast.makeText(this,"showing nearby hospitals...",Toast.LENGTH_LONG).show();
            break;

     case R.id.schools:
         mMap.clear();
         url=getUrl(latitude,longitude,schools);
         transferData[0]=mMap;
         transferData[1]=url;
         getNearbyPlaces.execute(transferData);
         Toast.makeText(this,"searching for nearby schools...",Toast.LENGTH_LONG).show();

         Toast.makeText(this,"showing nearby schools...",Toast.LENGTH_LONG).show();
         break;

     case R.id.restaurants:
         mMap.clear();
         url=getUrl(latitude,longitude,restaurants);
         transferData[0]=mMap;
         transferData[1]=url;
         getNearbyPlaces.execute(transferData);
         Toast.makeText(this,"searching for nearby restaurants...",Toast.LENGTH_LONG).show();
         Toast.makeText(this,"showing nearby restaurants...",Toast.LENGTH_LONG).show();
         break;

     case R.id.metros:
         mMap.clear();
         url=getUrl(latitude,longitude,Metros);
         transferData[0]=mMap;
         transferData[1]=url;
         getNearbyPlaces.execute(transferData);
         Toast.makeText(this,"searching for nearby metro...",Toast.LENGTH_LONG).show();

         Toast.makeText(this,"showing nearby metro...",Toast.LENGTH_LONG).show();
         break;


     case R.id.marketPlace:
         mMap.clear();
         url=getUrl(latitude,longitude,markets);
         transferData[0]=mMap;
         transferData[1]=url;
         getNearbyPlaces.execute(transferData);
         Toast.makeText(this,"searching for nearby marketPlaces...",Toast.LENGTH_LONG).show();

         Toast.makeText(this,"showing nearby marketPlaces...",Toast.LENGTH_LONG).show();
         break;


 }

}
    private String  getUrl(double latitude,double longitude,String nearbyPlace){
        StringBuilder googleURL=new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleURL.append("location="+latitude+","+longitude);
        googleURL.append("&radius="+ProximityRadius);
        googleURL.append("&type="+nearbyPlace);
        googleURL.append("&sensor=true");
        googleURL.append("&key="+"AIzaSyAH50yMhxkO7TUCU1Mx8VJciKIqj7a1Idg");

        Log.d("CustomerMapActivityFInal","url= "+googleURL.toString());
        return googleURL.toString();


    }




}
