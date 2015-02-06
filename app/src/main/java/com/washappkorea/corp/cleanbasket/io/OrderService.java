package com.washappkorea.corp.cleanbasket.io;


import com.washappkorea.corp.cleanbasket.io.model.OrderItem;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.GET;

public interface OrderService {
    /** Item **/

    // Get Item Codes
    @GET("/item/code")
    ArrayList<OrderItem> getItemCodes(Callback<ArrayList<OrderItem>> callback);
}
