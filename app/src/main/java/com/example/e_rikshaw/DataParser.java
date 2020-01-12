package com.example.e_rikshaw;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {

    private HashMap<String,String> getSingleNearbyPlace(JSONObject googlePlaceJSON){
        HashMap<String ,String> googlePlaceMap =new HashMap<>();
        String NameOfPlace ="-NA-";
        String vicinity="-NA-";
        String Latitude="";
        String Longitude="";
        String reference="";

        try {
            if(!googlePlaceJSON.isNull("name")){

                NameOfPlace=googlePlaceJSON.getString("name");
            }
            if(!googlePlaceJSON.isNull("vicinity")) {
                vicinity=googlePlaceJSON.getString("name");

            }

                Latitude =googlePlaceJSON.getJSONObject("geometry").getJSONObject("location").getString("lat");
            Longitude =googlePlaceJSON.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference =googlePlaceJSON.getString("reference");
            googlePlaceMap.put("vicinity",vicinity);
            googlePlaceMap.put("lat",Latitude);
            googlePlaceMap.put("lng",Longitude);
            googlePlaceMap.put("reference",reference);





            } catch (JSONException e) {
            e.printStackTrace();

        }
        return googlePlaceMap;
    }

        private List<HashMap<String,String>>getAllNearbyPlaces(JSONArray jsonArray){
        int counter=jsonArray.length();
            List<HashMap<String,String>> nearbyPlaceList= new ArrayList<>();
        HashMap<String,String> nearbyPlaceMap=null;
        for(int i=0;i<counter;i++){
            try {
                    nearbyPlaceMap=getSingleNearbyPlace((JSONObject) jsonArray.get(i));
                    nearbyPlaceList.add(nearbyPlaceMap);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
return nearbyPlaceList;
     }
    List<HashMap<String,String>>    parse(String JSONdata){
        JSONArray jsonArray=null;
        JSONObject jsonObject;
        try {
            jsonObject=new JSONObject(JSONdata);

            jsonArray=jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    return getAllNearbyPlaces(jsonArray);
    }
}
