package com.bridge4biz.laundry.io.model.map;

import java.util.ArrayList;
import java.util.List;

public class GeocodeResponse {
    private String status;
    private List<Geocode> results = new ArrayList<Geocode>();

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setResults(List<Geocode> results) {
        this.results = results;
    }

    public List<Geocode> getResults() {
        return results;
    }
}


