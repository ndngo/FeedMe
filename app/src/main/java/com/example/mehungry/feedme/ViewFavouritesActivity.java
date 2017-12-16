package com.example.mehungry.feedme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class ViewFavouritesActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private ArrayList<String> placesList = new ArrayList<>();
    private ArrayAdapter<String> stringAdapter;
    private ListView favouritesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_favourites);
        favouritesListView = (ListView) findViewById(R.id.viewfavourites_favouriteslist);
        mDatabase = FirebaseDatabase.getInstance().getReference("places");
        stringAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                placesList
        );
        favouritesListView.setAdapter(stringAdapter);

        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                /*FavouritePlace place = dataSnapshot.getValue(FavouritePlace.class);
                System.out.println("name" + place.name);
                System.out.println("name" + place.address);
                */
                FavouritePlace place = (FavouritePlace) dataSnapshot.getValue(FavouritePlace.class);
                String placeString = String.valueOf(place);
                stringAdapter.add(placeString);
                System.out.println(place);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Read in failed: " + databaseError.getCode());
            }
        });


    }

    public static class FavouritePlace {
        public String id;
        public String name;
        public String address;

        public FavouritePlace() {}

        public FavouritePlace(String id, String name, String address) {
            this.id = id;
            this.name = name;
            this.address = address;
        }

        public String toString() {
            return "id: " + this.id + "\nname: " + this.name + "\naddr: " + this.address;
        }
    }
}
