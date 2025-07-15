package com.bluebear.cinemax.dto;

public class LocationResponse {
    private double lat;
    private double lng;
    private String placeName;

    public LocationResponse() {}

    public LocationResponse(double lat, double lng, String placeName) {
        this.lat = lat;
        this.lng = lng;
        this.placeName = placeName;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }
}
