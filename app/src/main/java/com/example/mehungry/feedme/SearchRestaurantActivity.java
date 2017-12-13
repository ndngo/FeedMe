package com.example.mehungry.feedme;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionApi;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class SearchRestaurantActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    public static final int PLACE_AUTOCOMPLETE_REQUEST = 1;
    private TextView searchTextView;
    private TextView name;
    private TextView address;
    private RatingBar ratingbar;

    // can;'t filter results with place picker
    // use autocomplete instead to search nearby
    public void launchPlacePicker() throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        /*LatLng northEast = new LatLng(43.972598, -79.118649);
        LatLng southWest = new LatLng(43.681140, -79.671073);
        LatLngBounds BOUNDS_SENECA_YORK_VIEW = new LatLngBounds(
                southWest,
                northEast
        );
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        builder.setLatLngBounds(BOUNDS_SENECA_YORK_VIEW);

        startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);*/
    }

    public void launchPlaceAutoComplete() {
        try {
            PlaceAutocomplete.IntentBuilder builder = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY);

            // change this to use current location of user using
            LatLng northEast = new LatLng(43.972598, -79.118649);
            LatLng southWest = new LatLng(43.681140, -79.671073);
            builder.setBoundsBias(new LatLngBounds(southWest, northEast));
            startActivityForResult(builder.build(this), PLACE_AUTOCOMPLETE_REQUEST);
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_restaurant);
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        searchTextView = findViewById(R.id.searchrestaurant_searchtext);
        name = findViewById(R.id.searchrestaurant_name);
        address = findViewById(R.id.searchrestaurant_address);
        ratingbar = findViewById(R.id.searchrestaurant_rating);
        Button searchButton = findViewById(R.id.searchrestaurant_searchbutton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
 //                   launchPlacePicker();
                launchPlaceAutoComplete();

            }
        });
        launchPlaceAutoComplete();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("CONNECTION FAILED");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PLACE_AUTOCOMPLETE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                searchTextView.setText(place.getName());
                name.setText(place.getName());
                address.setText(place.getAddress());
                ratingbar.setNumStars((int)place.getRating());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i("SEARCH_RESTAURANT", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // operation cancelled
            }
        }
    }
}
