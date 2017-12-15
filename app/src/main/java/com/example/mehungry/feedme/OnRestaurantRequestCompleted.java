package com.example.mehungry.feedme;

import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;

/**
 * Created by nngo1 on 15-Dec-17.
 */

public interface OnRestaurantRequestCompleted {
    void OnTaskCompleted(AutocompletePredictionBufferResponse response);
}
