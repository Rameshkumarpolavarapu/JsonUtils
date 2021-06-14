package com.example.util.json;

import org.json.JSONArray;
import org.json.JSONException;


public interface IJsonConverter
{
    public <T> T convertJsonArrayToObject(JSONArray clValuesArray, Class clTargetClass);

    public <T> T convertJsonToObject(String sJsonString, Class clTargetClass);

    public <T> T convertJsonToObject(JSONArray clNamesArray, JSONArray clValuesArray, Class clTargetClass, Object objDTOInstance);

    public <T> T convertJsonToArrayList(String sJsonString, Class[] clTypes, boolean isSingleTargetClass);

    public <T> T convertJsonToArrayList(JSONArray clJsonArray, Class[] clTypes);

    public <T> T convertJsonToArrayList(JSONArray clJsonArray, Class[] clTypes, boolean isSingleTargetClass);

    public <T> T convertJsonToArrayList(String sJsonString);

    public <T> T convertJsonToObjectArray(String sJsonString, Class<T> clTargetClass, Object objDTOInstance) throws JSONException;

    public <T> T convertJsonToObjectArray(JSONArray clJsonArray, Class<T> clTargetClass, Object objDTOInstance) throws JSONException;
}
