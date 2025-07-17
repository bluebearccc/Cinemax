package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.LocationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class MapService {
    @Value("${mapbox.api.token}")
    private String mapboxToken;

    private static final String MAPBOX_API_URL_V6 = "https://api.mapbox.com/search/geocode/v6/forward";
    private static final String MAPBOX_REVERSE_URL = "https://api.mapbox.com/search/geocode/v6/reverse";

    public LocationResponse geocode(String address) {
        String proximity = "105.834160,21.027763"; // Tọa độ Hà Nội

        String url = UriComponentsBuilder
                .fromUriString(MAPBOX_API_URL_V6)
                .queryParam("access_token", mapboxToken)
                .queryParam("q", address)
                .queryParam("country", "VN")
                .queryParam("proximity", proximity)
                .build()
                .toUriString();

        System.out.println("Geocoding URL: " + url);
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        System.out.println("API Response: " + response);
        if (response == null || response.get("features") == null) {
            return null;
        }
        List<Map<String, Object>> features = (List<Map<String, Object>>) response.get("features");

        if (features.isEmpty()) {
            return null;
        }

        Map<String, Object> first = features.get(0);
        Map<String, Object> properties = (Map<String, Object>) first.get("properties");
        Map<String, Object> geometry = (Map<String, Object>) first.get("geometry");

        String placeName = (String) properties.get("full_address");
        if (placeName == null) {
            placeName = (String) properties.get("name");
        }

        // Lấy tọa độ từ geometry
        List<Double> coordinates = (List<Double>) geometry.get("coordinates");
        double lng = coordinates.get(0); // longitude là index 0
        double lat = coordinates.get(1); // latitude là index 1

        return new LocationResponse(lat, lng, placeName);
    }

    // Phương thức mới cho reverse geocoding
    public LocationResponse reverseGeocode(double lat, double lng) {
        String url = UriComponentsBuilder
                .fromUriString(MAPBOX_REVERSE_URL)
                .queryParam("access_token", mapboxToken)
                .queryParam("longitude", lng)
                .queryParam("latitude", lat)
                .queryParam("country", "VN")
                .queryParam("language", "vi")
                .build()
                .toUriString();

        System.out.println("Reverse Geocoding URL: " + url); // Debug log

        RestTemplate restTemplate = new RestTemplate();
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            System.out.println("Reverse API Response: " + response); // Debug log

            if (response == null || response.get("features") == null) {
                return null;
            }

            List<Map<String, Object>> features = (List<Map<String, Object>>) response.get("features");

            if (features.isEmpty()) {
                return null;
            }

            Map<String, Object> first = features.get(0);
            Map<String, Object> properties = (Map<String, Object>) first.get("properties");

            String placeName = (String) properties.get("full_address");
            if (placeName == null) {
                placeName = (String) properties.get("name");
            }
            if (placeName == null) {
                placeName = "Vị trí không xác định";
            }

            return new LocationResponse(lat, lng, placeName);

        } catch (Exception e) {
            System.err.println("Error in reverse geocoding: " + e.getMessage());
            return new LocationResponse(lat, lng, "Không thể xác định địa chỉ");
        }
    }

}
