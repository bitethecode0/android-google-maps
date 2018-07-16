package com.example.joon.googlemaps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joon.googlemaps.Model.PlaceInfo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private static final String TAG = "MapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40,-168), new LatLng(71,136));
    private static final int PLACE_PICKER_REQUEST =1;
    private String place_id="";

    //widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGpsOff,mPlacePicker;
    //private boolean isGpsOff= true;
    //vars
    private boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private PlaceInfo mPlaceInfo;
    private Marker mMarker;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();



        mSearchText = findViewById(R.id.input_search);
        mGpsOff = findViewById(R.id.ic_gps_off);
        mGpsOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mGpsOff.setImageResource(R.drawable.ic_gps_on);
                getDeviceLocation();
            }
        });

        mPlacePicker = findViewById(R.id.place_picker);
        mPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try{
                    startActivityForResult(builder.build(MapActivity.this), PLACE_PICKER_REQUEST);
                }catch (GooglePlayServicesRepairableException e){
                    Log.e(TAG, "onClick: GooglePlayServicesRepairableException"+e.getMessage() );
                }
                catch(GooglePlayServicesNotAvailableException e){
                    Log.e(TAG, "onClick: GooglePlayServicesRepairableException"+e.getMessage() );
                }
            }
        });
        getLocationPermissions();


    }

    /**
     * result for the google place picker
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PLACE_PICKER_REQUEST){
            if(resultCode == RESULT_OK){
                Place place = PlacePicker.getPlace(this, data);

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, place.getId());
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            }
        }

    }

    private void getLocationPermissions() {
        Log.d(TAG, "getLocationPermissions: gettiong the location permissions.");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[1]) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                // if permissions were already granted, execute initMap()
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }

        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }

                    mLocationPermissionGranted = true;
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    initMap();
                }
            }
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                if (mLocationPermissionGranted){
                    getDeviceLocation();
                    if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                }
            }
        });
    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the device location.");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(!task.isSuccessful()){
                            Log.d(TAG, "onComplete: failed to detect current location");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        } else{
                            Log.d(TAG, "onComplete: found current location");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "my location");
                            initSearch();
                        }
                    }
                });
            }

        } catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException"+e.getMessage() );
        }
    }


    /**
     * override moveCamera with different arguement
     * @param latLng
     * @param defaultZoom
     * @param placeInfo
     */
    private void moveCamera(LatLng latLng, float defaultZoom, PlaceInfo placeInfo) {
        Log.d(TAG, "moveCamera: moving the camera to the current location. lat : "+latLng.latitude+", lng : "+latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, defaultZoom));

        mMap.clear();

        if(placeInfo!=null){
            try{
                String snippet = "Address" + placeInfo.getAddress()+"\n"+
                        "Phone number" + placeInfo.getPhone_number()+"\n"+
                        "Website" + placeInfo.getWebsiteUri()+"\n"+
                        "Price rating" + placeInfo.getRating()+"\n";
                MarkerOptions options = new MarkerOptions().position(latLng)
                        .title(placeInfo.getName())
                        .snippet(snippet);


                mMarker = mMap.addMarker(options);


            } catch(NullPointerException e){
                Log.e(TAG, "moveCamera: NullPointerException"+e.getMessage() );
            }



        } else{
            mMap.addMarker(new MarkerOptions().position(latLng));

        }


        hideSoftKeyboard();


    }

    private void moveCamera(LatLng latLng, float defaultZoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to the current location. lat : "+latLng.latitude+", lng : "+latLng.longitude);


        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, defaultZoom));


        if(!title.equals("my location")){
            MarkerOptions options = new MarkerOptions().position(latLng).title(title);
            mMap.addMarker(options);
        }

        hideSoftKeyboard();


    }

    private void initSearch() {
        Log.d(TAG, "initSearch: initiate search.");

        mSearchText.setOnItemClickListener(mAutocompleteListener);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this,mGoogleApiClient ,LAT_LNG_BOUNDS ,null);
        mSearchText.setAdapter(mPlaceAutocompleteAdapter);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event.getAction() == KeyEvent.ACTION_DOWN||
                        event.getAction() == KeyEvent.KEYCODE_ENTER){

                    geoLocate();
                }

                return false;
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d(TAG, "onMarkerClick: click and navigate to the information page.");
//                Fragment fragment = new ShowPlaceInfo();
//                Bundle args = new Bundle();
//                args.putParcelable("Place info", mPlaceInfo);
//                fragment.setArguments(args);
//
//                Log.d(TAG, "onMarkerClick: transaction starts.");
//                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                transaction.replace( R.id.container,fragment);
//                transaction.addToBackStack(getString(R.string.show_placeinfo));
//                transaction.commit();



                Bundle bundle = new Bundle();
                bundle.putParcelable(getString(R.string.place_info), mPlaceInfo);
                Intent intent = new Intent(MapActivity.this, ShowPlaceInfoActivity.class);
                intent.putExtra(getString(R.string.place_id), place_id);
                intent.putExtras(bundle);
                startActivity(intent);


                return true;
            }
        });






    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: start.");
        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString,1);
        } catch(IOException e){
            Log.e(TAG, "geoLocate: IOException"+e.getMessage() );
        }

        if(list.size()>0){
            Address address = list.get(0);
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
        }

    }

    private void hideSoftKeyboard(){
        View view = this.getCurrentFocus();
        if(view!=null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Google Places API auto complete suggettions ------------------------------------------------------
     */


    private AdapterView.OnItemClickListener mAutocompleteListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(position);
            place_id = item.getPlaceId();
            Log.d(TAG, "onItemClick: place_ id : "+place_id);

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, place_id);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.d(TAG, "onResult: didn't complete successfully");
                places.release();
                return;
            }
            final Place place = places.get(0);

            try {
                mPlaceInfo = new PlaceInfo();
                mPlaceInfo.setName(place.getName().toString());
                Log.d(TAG, "onResult: name: " + place.getName());
                mPlaceInfo.setAddress(place.getAddress().toString());
                Log.d(TAG, "onResult: address: " + place.getAddress());
                //                mPlace.setAttributions(place.getAttributions().toString());
                //                Log.d(TAG, "onResult: attributions: " + place.getAttributions());
                mPlaceInfo.setId(place.getId());
                Log.d(TAG, "onResult: id:" + place.getId());
                mPlaceInfo.setLatlng(place.getLatLng());
                Log.d(TAG, "onResult: latlng: " + place.getLatLng());
                mPlaceInfo.setRating(place.getRating());
                Log.d(TAG, "onResult: rating: " + place.getRating());
                mPlaceInfo.setPhone_number(place.getPhoneNumber().toString());
                Log.d(TAG, "onResult: phone number: " + place.getPhoneNumber());
                mPlaceInfo.setWebsiteUri(place.getWebsiteUri());
                Log.d(TAG, "onResult: website uri: " + place.getWebsiteUri());

                Log.d(TAG, "onResult: place: " + mPlaceInfo.toString());

            } catch (NullPointerException e) {
                Log.e(TAG, "onResult: NullPointerException" + e.getMessage());
            }

            moveCamera(new LatLng(place.getViewport().getCenter().latitude,place.getViewport().getCenter().longitude )
                    ,DEFAULT_ZOOM,mPlaceInfo);
            places.release();
        }



    };

    @Override
    public void onPause(){

        if(mGoogleApiClient!=null && mGoogleApiClient.isConnected()){
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }
        super.onPause();

    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

}
