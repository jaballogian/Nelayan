package jaballogian.nelayan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import me.anwarshahriar.calligrapher.Calligrapher;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiAclient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    private Marker currentMarker;

    private FirebaseAuth mAuth;
    private DatabaseReference mLocationReference, mLatitudeReference, mLongitudeReference;

    private ImageButton settingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Calligrapher calligrapher = new Calligrapher(this);
        calligrapher.setFont(this, "PRODUCT_SANS.ttf", true);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){

            Intent toLogInActivity = new Intent(this, LogInActivity.class);
            startActivity(toLogInActivity);
            finish();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mGoogleApiAclient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiAclient.connect();

        mMap.setMyLocationEnabled(true);

        //Toast.makeText(this, "Mohon tunggu hingga peta selesai dimuat", Toast.LENGTH_LONG).show();

        settingButton = (ImageButton) findViewById(R.id.settingsButtonMapsAcitivity);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent  toSettingsActivity = new Intent(MapsActivity.this, SettingsActivity.class);
                startActivity(toSettingsActivity);
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiAclient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;

        final LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){

            String uID = currentUser.getUid();

            mLatitudeReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uID);
            mLatitudeReference.child("Latitude").setValue(location.getLatitude());

            mLongitudeReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uID);
            mLongitudeReference.child("Longitude").setValue(location.getLongitude());

//            mLocationReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uID);
//
//            HashMap<String, Double> lokasiUser = new HashMap<>();
//            lokasiUser.put("Latitude", location.getLatitude());
//            lokasiUser.put("Lonigtude", location.getLongitude());
//
//            mLocationReference.setValue(lokasiUser);

        }

        if(currentMarker != null){
            currentMarker.remove();
            currentMarker = null;
        }
        else {
            currentMarker = mMap.addMarker(new MarkerOptions().position(coordinate).title("Anda di sini").icon(BitmapDescriptorFactory.fromResource(R.drawable.sailboat_32px)));
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
