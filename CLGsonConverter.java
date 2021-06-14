package com.example.util.json;

import com.focus.centra.crm.data.model.app.IJsonArrayObject;
import com.focus.centra.crm.data.model.app.IJsonParentProperty;
import com.focus.centra.crm.data.model.app.IJsonTarget;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;


public class CLGsonConverter implements IJsonConverter {
    private Object objTypeAdapter;

    public CLGsonConverter(Object objTypeAdapter) {
        this.objTypeAdapter = objTypeAdapter;
    }


    @Override
    public  <T> T convertJsonArrayToObject(JSONArray clValuesArray, Class clTargetClass) {


        if (objTypeAdapter != null)
        {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(clTargetClass, objTypeAdapter)
                    .create();
            return (T) gson.fromJson(clValuesArray.toString(), clTargetClass);
        }
        else
        {
            return (T) new Gson().fromJson(clValuesArray.toString(), clTargetClass);
        }

    }

    @Override
    public <T> T convertJsonToObject(String sJsonString, Class clTargetClass)
    {
        if (objTypeAdapter != null)
        {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(clTargetClass, objTypeAdapter)
                    .create();
            return (T) gson.fromJson(sJsonString, clTargetClass);
        }
        else
        return (T) new Gson().fromJson(sJsonString, clTargetClass);
    }

    // not used because in gson no need of using names
    @Override
    public <T> T convertJsonToObject(JSONArray clNamesArray, JSONArray clValuesArray, Class clTargetClass, Object objDTOInstance) {
        return null;
    }

    @Override
    public <T> T convertJsonToArrayList(String sJsonString, Class[] clTypes, boolean isSingleTargetClass) {
        JSONArray clJsonArray = null;
        try
        {
            clJsonArray = new JSONArray(sJsonString);
            return convertJsonToArrayList(clJsonArray, clTypes,isSingleTargetClass);
        } catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T convertJsonToArrayList(JSONArray clJsonArray, Class[] clTypes) {

        return convertJsonToArrayList(clJsonArray, clTypes, false);
    }

    @Override
    public <T> T convertJsonToArrayList(JSONArray clJsonArray, Class[] clTypes, boolean isSingleTargetClass) {
        ArrayList alArrayData = null;
        try
        {
            alArrayData = new ArrayList();
            Class clTargetClass;
            for (int j = 0; j < clJsonArray.length(); j++)
            { //todo
                if (clTypes != null)
                {
                    if (isSingleTargetClass)
                        clTargetClass = clTypes[0];
                    else
                        clTargetClass = clTypes[j];

                    if (clJsonArray.get(j) instanceof JSONArray)
                    {
                        alArrayData.add(convertJsonArrayToObject((JSONArray) clJsonArray.get(j), clTargetClass));
//                        boolean isConvertToJsonObject = isImplementsJsonArrayObject(clTargetClass);

                       /* if (isConvertToJsonObject)
                        else
                            alArrayData.add(convertJsonToArrayList(clJsonArray.get(j).toString()));*/
                    }
                    else
                    {
                        if(clJsonArray.isNull(j))
                            alArrayData.add(null);
                        else
                            alArrayData.add(convertJsonToObject(clJsonArray.get(j).toString(), clTargetClass));
                    }
                }
                else
                    alArrayData.add(clJsonArray.get(j));
            }
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return (T) alArrayData;
    }

    @Override
    public <T> T convertJsonToArrayList(String sJsonString) {
        return convertJsonToArrayList(sJsonString, null, false);
    }

    @Override
    public <T> T convertJsonToObjectArray(String sJsonString, Class<T> clTargetClass, Object objDTOInstance) throws JSONException {
        JSONArray clJsonArray = new JSONArray(sJsonString);

        if (clTargetClass != null)
        {
            if (clTargetClass.equals(Integer.TYPE))
            {
                int[] iArrValues = new int[clJsonArray.length()];
                for (int i = 0; i < clJsonArray.length(); i++)
                    iArrValues[i] = (int) clJsonArray.get(i);

                return (T) iArrValues;
            }
        }
        return convertJsonToObjectArray(clJsonArray, clTargetClass, objDTOInstance);
    }

    @Override
    public <T> T convertJsonToObjectArray(JSONArray clJsonArray, Class<T> clTargetClass, Object objDTOInstance) throws JSONException {
        T[] objArray;
        if (clTargetClass != null)
            objArray = (T[]) Array.newInstance(clTargetClass, clJsonArray.length());
        else
            objArray = (T[]) new Object[clJsonArray.length()];

        if (objArray instanceof Object[][])
        {
            Object[][] obj2DArray = (Object[][]) objArray;

            for (int i = 0; i < obj2DArray.length; i++)
            {
                int iLength2 = ((JSONArray) clJsonArray.get(i)).length();
//                int iLength2=((Object[])objArray[i]).length;
                Object[] objNestedArray = new Object[iLength2];
                Class clParamClass;
                if (objDTOInstance != null && objDTOInstance instanceof IJsonTarget)
                    clParamClass = ((IJsonTarget) objDTOInstance).getTargetDTO((JSONArray) clJsonArray.get(i));
                else
                    clParamClass = Object.class;
                fillDataFromJsonArray(objNestedArray, (JSONArray) clJsonArray.get(i), clParamClass, objDTOInstance);
                obj2DArray[i] = objNestedArray;
            }
        }
        else
            fillDataFromJsonArray(objArray, clJsonArray, clTargetClass, objDTOInstance);

        return (T) objArray;
    }

    private boolean isImplementsJsonArrayObject(Class clTargetClass)
    {
        boolean isConvertToJsonObject = false;
        Class[] clInterfaces = clTargetClass.getInterfaces();
        for (int i = 0; i < clInterfaces.length; i++)
        {
            if (clInterfaces[i] == IJsonArrayObject.class)
            {
                isConvertToJsonObject = true;
                break;
            }
        }
        return isConvertToJsonObject;
    }

    private <T> void fillDataFromJsonArray(T[] objArray, JSONArray clJsonArray, Class clTargetClass, Object objDTOInstance) throws JSONException
    {
        try
        {
            for (int i = 0; i < clJsonArray.length(); i++)
            {
                //if (clJsonArray.get(i) == null)
                if (clJsonArray.isNull(i))
                    Array.set(objArray, i, null);
                else if (clJsonArray.get(i) instanceof JSONArray)
                {
                    boolean isConvertToJsonObject = false;
                    if(clTargetClass!=null)
                    {
                        T clTargetInstance = (T) clTargetClass.newInstance();
                        isConvertToJsonObject = isImplementsJsonArrayObject(clTargetClass);
                        if (isConvertToJsonObject)
                        {
                            //Array.set(objArray, i, convertJsonArrayToObject((JSONArray) clJsonArray.get(i), clTargetClass));
                            JSONArray clNamesArray = ((IJsonArrayObject) clTargetInstance).getMethodsArray();
                            Array.set(objArray, i, convertJsonToObject(clJsonArray.get(i).toString(), clTargetClass));
                        }
                        else
                            Array.set(objArray, i, convertJsonToObjectArray((JSONArray) clJsonArray.get(i), clTargetClass, objDTOInstance));
                    }
                    else
                        Array.set(objArray, i, convertJsonToObjectArray((JSONArray) clJsonArray.get(i), clTargetClass, objDTOInstance));


                    //objArray[i]=convertJsonToObjectArray((JSONArray)clJsonArray.get(i),null);
                }
                else if (clJsonArray.get(i) instanceof JSONObject)
                {
                    T clTargetInstance = (T) clTargetClass.newInstance();
                    JSONObject clJsonObject = new JSONObject(clJsonArray.get(i).toString());
                    convertJsonToObject( clJsonObject.toString(), clTargetClass);
                    Array.set(objArray, i, clTargetInstance);
                }
                else
                {
                    boolean isConvertToJsonObject=false;
                    if(clTargetClass!=null)
                        isConvertToJsonObject = isImplementsJsonArrayObject(clTargetClass);
                    if (isConvertToJsonObject)
                    {
                        Array.set(objArray, i, convertJsonArrayToObject(clJsonArray,clTargetClass));
                        break;
                    }
                    else
                        Array.set(objArray, i, clJsonArray.get(i));
                }
                //objArray[i]=clJsonArray.get(i);
            }
        }
        catch (IllegalAccessException ie)
        {
            throw new RuntimeException(ie);
        }
        catch (InstantiationException ie)
        {
            throw new RuntimeException(ie);
        }
    }
}
