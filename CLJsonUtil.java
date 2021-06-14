package com.example.util.json;


import com.example.util.json.data.model.app.IJsonArrayObject;
import com.example.util.json.data.model.app.IJsonObject;
import com.example.util.json.data.model.app.IJsonParentProperty;
import com.example.util.json.data.model.app.IJsonTarget;
import com.example.util.json.CLCustomJsonConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.ArrayList;


public class CLJsonUtil
{

    public static final byte TYPE_GSON = 1;
    public static final byte TYPE_CUSTOM = 2;
    private static final byte DEFAULT_JSON_CONVERTER = TYPE_CUSTOM;

    //    public static final byte TYPE_JACKSON = 2;



    public static IJsonConverter getJsonConverter()
    {
        return getConverter(DEFAULT_JSON_CONVERTER, null);
    }

    public static IJsonConverter getJsonConverter(byte byConverterType,Object objTypeAdapter)
    {
        return getConverter(byConverterType, objTypeAdapter);
    }

    private static IJsonConverter getConverter(byte byConverterType,Object objTypeAdapter)
    {
        switch (byConverterType)
        {
            case TYPE_GSON:
                return new CLGsonConverter(objTypeAdapter);
            case TYPE_CUSTOM:
                return new CLCustomJsonConverter();
        }
        return null;
    }

   /* public static <T> T convertJsonArrayToObject(JSONArray clValuesArray, Class clTargetClass)
    {
       return convertJsonArrayToObject(clValuesArray,clTargetClass,null,DEFAULT_JSON_CONVERTER);
    }*/
/*
* jcson converter example
* */

    /*else
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(
                DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return (T) mapper.readValue(clValuesArray.toString(),clTargetClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/


}
