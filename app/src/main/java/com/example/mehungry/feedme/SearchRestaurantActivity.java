package com.example.mehungry.feedme;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlaceDetectionApi;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

public class SearchRestaurantActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, OnRestaurantRequestCompleted {
    private GoogleApiClient mGoogleApiClient;
    public static final int PLACE_AUTOCOMPLETE_REQUEST = 1;
    protected static GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;
    private ImageView searchRandomButton;
//    private TextView searchTextView;
    private Place place;
    private TextView name;
    private TextView address;
    private RatingBar ratingbar;
    private static LatLng northEast, southWest;
    private ArrayList<String> favourites;
    private FirebaseDatabase db;
    private DatabaseReference mDatabase;
    private String currentPlaceId;
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

        mDatabase = FirebaseDatabase.getInstance().getReference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_restaurant);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        mGeoDataClient = Places.getGeoDataClient(this, null);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        northEast = new LatLng(43.972598, -79.118649);
        southWest = new LatLng(43.681140, -79.671073);
        searchRandomButton = findViewById(R.id.searchrestaurant_searchrandom);
//        searchTextView = findViewById(R.id.searchrestaurant_searchtext);
        name = findViewById(R.id.searchrestaurant_name);
        address = findViewById(R.id.searchrestaurant_address);
        ratingbar = findViewById(R.id.searchrestaurant_rating);
        Button searchButton = findViewById(R.id.searchrestaurant_searchbutton);
        favourites = new ArrayList<>();
        ImageButton addFavourite= findViewById(R.id.searchrestaurant_addfavourite);
        addFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favourites.add(place.getId());
                Toast.makeText(getApplicationContext(), "Added to favourites", Toast.LENGTH_LONG).show();
                mDatabase.child("places").child(place.getId()).child("id").setValue(place.getId());
                mDatabase.child("places").child(place.getId()).child("name").setValue(place.getName());
                mDatabase.child("places").child(place.getId()).child("address").setValue(place.getAddress());
                System.out.println("displaying favourites: " + favourites.size());
                for(int i = 0; i < favourites.size(); i++) {
                    System.out.println(favourites.get(i));
                }
            }
        });
        searchRandomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRandomRestaurantRequest();
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //                   launchPlacePicker();
                launchPlaceAutoComplete();

            }
        });



        Intent intent = getIntent();
        int x = intent.getIntExtra("random",0);
        if (x == 1) {
            sendRandomRestaurantRequest();
            // launch async task here
        } else {
            launchPlaceAutoComplete();
        }
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
                setPlaceDetails(place);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i("SEARCH_RESTAURANT", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // operation cancelled
            }
        }
    }

    public void setPlaceDetails(Place place) {
//        searchTextView.setText(place.getName());
        name.setText(place.getName());
        address.setText(place.getAddress());
        System.out.println("RATING:" + (int)place.getRating());
        ratingbar.setRating((int)place.getRating());
    }

    @Override
    public void OnTaskCompleted(AutocompletePredictionBufferResponse response) {
        // get results
        // pick from results a random restaurant
        // display to screen this selection
        int size = response.getCount();

        if (size > 0) {
            String description, id;

            for (int i = 0; i < size; i++) {
                description = response.get(i).getFullText(null).toString();
                id = response.get(i).getPlaceId();

                System.out.println("description:" + description + "\nid: " + id);
            }

            Random random = new Random();
            System.out.println(size);
            int r = random.nextInt(size);

            String restaurantName = response.get(r).getFullText(null).toString().split(",")[0];
            Task<PlaceBufferResponse> q = mGeoDataClient.getPlaceById(response.get(r).getPlaceId());

            currentPlaceId = response.get(r).getPlaceId();

            while (!q.isComplete()) {
                System.out.println("finding place...");
            }
            System.out.println("FOUND A PLACE");
            PlaceBufferResponse rPlace = q.getResult();
            place = rPlace.get(0);
            System.out.println(restaurantName + "\n" +
                    place.getAddress() + "\n" +
                    place.getPhoneNumber() + "\n" +
                    place.getWebsiteUri() + "\n" +
                    place.getRating() + "\n" +
                    place.getLatLng() + "\n"
            );
            setPlaceDetails(place);
        } else {
            sendRandomRestaurantRequest();
        }

    }

    public void sendRandomRestaurantRequest() {
        RestaurantRequest task = new RestaurantRequest(this);
        String [] searchString = getResources().getStringArray(R.array.food_search_terms);
        Random random = new Random();
        int selection = random.nextInt(searchString.length - 1);
        //task.execute((searchString[selection] + " food"));
        task.execute((searchString[selection]));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // resolve which menu item was selected
        switch (item.getItemId()) {
            case R.id.favourites:
                startActivity(new Intent(this, ViewFavouritesActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class RestaurantRequest extends AsyncTask<String, Void, AutocompletePredictionBufferResponse> {

        private String searchTerms;
        private OnRestaurantRequestCompleted listener;

        public RestaurantRequest(OnRestaurantRequestCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected AutocompletePredictionBufferResponse doInBackground(String... strings) {
            searchTerms = strings[0];
            AutocompletePredictionBufferResponse response;
            AutocompleteFilter.Builder builder = new AutocompleteFilter.Builder();
            builder.setCountry("CA");
            builder.setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE);
            AutocompleteFilter filter = builder.build();
            Task<AutocompletePredictionBufferResponse> task = mGeoDataClient.getAutocompletePredictions(
                    searchTerms,
                    new LatLngBounds(southWest, northEast),
                    filter);
            while(!task.isComplete()) {
                System.out.println("task is not complete");
            }
            // task not yet complete, wait for task to finish
            response = task.getResult();
            return response;
        }

        @Override
        protected void onPostExecute(AutocompletePredictionBufferResponse response) {
            super.onPostExecute(response);
            listener.OnTaskCompleted(response);
        }
    }
}