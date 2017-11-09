package com.simoneluconi.projectmerende.API;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;


public interface APIInterface {
    // Request method and URL specified in the annotation
    // Callback for the parsed response is the last parameter

    @GET("?lista")
    Call<List<Merenda>> getLista();
}