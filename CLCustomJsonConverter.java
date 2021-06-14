package com.example.util.json;

import com.example.util.json.data.model.app.IJsonArrayObject;
import com.example.util.json.data.model.app.IJsonObject;
import com.example.util.json.data.model.app.IJsonParentProperty;
import com.example.util.json.data.model.app.IJsonTarget;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.*;
import java.util.ArrayList;


public class CLCustomJsonConverter implements IJsonConverter
{
    public <T> T convertJsonArrayToObject(JSONArray clValuesArray, Class clTargetClass)
    {
        try
        {
            Object objDTOInstance = clTargetClass.newInstance();
            JSONArray clNamesArray = ((IJsonArrayObject) objDTOInstance).getMethodsArray();

            return convertJsonToObject(clNamesArray, clValuesArray, clTargetClass, objDTOInstance);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public <T> T convertJsonToObject(String sJsonString, Class clTargetClass)
    {
        if (sJsonString != null)
        {
            try
            {
                  JSONObject clJsonObject = new JSONObject(sJsonString);
                JSONArray clNamesArray = clJsonObject.names();
                JSONArray clValuesArray = clJsonObject.toJSONArray(clNamesArray);
                return convertJsonToObject(clNamesArray, clValuesArray, clTargetClass, null);
            } catch (JSONException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
            return null;
    }

    @Override
    public <T> T convertJsonToObject(JSONArray clNamesArray, JSONArray clValuesArray, Class clTargetClass, Object objDTOInstance)
    {
        try
        {
            Method clMethod;
            String sJsonProperty;

            if (objDTOInstance == null)
                objDTOInstance = clTargetClass.newInstance();

            for (int i = 0; i < clNamesArray.length(); i++)
            {
                sJsonProperty = (String) clNamesArray.get(i);
//                Object objValue=clJsonObject.get(sJsonProperty);
                if (!clValuesArray.isNull(i))
                {
                    Object objValue = clValuesArray.get(i);

                    if (objDTOInstance instanceof IJsonObject)
                        sJsonProperty = ((IJsonObject) objDTOInstance).getMethodName(sJsonProperty);
                    else
                        sJsonProperty = (String) clNamesArray.get(i);

                    if (sJsonProperty == null)
                        continue;

                    sJsonProperty = sJsonProperty.trim();

                    clMethod = getDeclaredMethod(clTargetClass, getMethodName(sJsonProperty, "set"));

                    if (clMethod != null)
                    {
                        if (objValue instanceof JSONArray)
                        {
                            Type clParameterType = clMethod.getGenericParameterTypes()[0];
                            Object objParam = null;
                            Class clParamClass = null;
                            JSONArray clJsonArray = (JSONArray) objValue;

                            if (clParameterType instanceof ParameterizedType)
                            {
                                Type[] clParamTypes = ((ParameterizedType) clMethod.getGenericParameterTypes()[0]).getActualTypeArguments();
                                ArrayList clListDTOs = new ArrayList();

                                if (objDTOInstance instanceof IJsonTarget)
                                {
                                    clParamClass = ((IJsonTarget) objDTOInstance).getTargetDTO(clJsonArray);
                                    if (clParamClass != null) //converted to Arraylist if null is returned else converted to DTO
                                        objParam = convertJsonArrayToObject(clJsonArray, clParamClass);
                                }


                                for (int j = 0; j < clJsonArray.length(); j++)
                                {
                                    if (clJsonArray.get(j) instanceof JSONArray)
                                    {
                                        clParamClass = null;

                                        if (objDTOInstance instanceof IJsonTarget)
                                            clParamClass = ((IJsonTarget) objDTOInstance).getTargetDTO((JSONArray) clJsonArray.get(j));

                                        if (clParamClass == null)
                                            clParamClass = (Class) clParamTypes[0];

                                        clListDTOs.add(convertJsonArrayToObject((JSONArray) clJsonArray.get(j), clParamClass));
                                    }
                                    else
                                        clListDTOs.add(invokeMethod(objDTOInstance, clTargetClass, clMethod, objValue));


                                /*else if (clJsonArray.get(j) instanceof Number)
                                {
                                    clMethod = getDeclaredMethod(clTargetClass, getMethodName(sJsonProperty, "set"));
                                    if (clMethod != null)
                                    {
                                        Class clParamType = clMethod.getParameterTypes()[0];
                                        if (clJsonArray.get(j) instanceof String)
                                            clMethod.invoke(objDTOInstance, clJsonArray.get(j));
                                        else
                                        {
                                            Number numValue = (Number) clJsonArray.get(j);
                                            if (clParamType.equals(Byte.TYPE))
                                                clMethod.invoke(objDTOInstance, numValue.byteValue());
                                            else if (clParamType.equals(Short.TYPE))
                                                clMethod.invoke(objDTOInstance, numValue.shortValue());
                                            else if (clParamType.equals(Integer.TYPE))
                                                clMethod.invoke(objDTOInstance, numValue.intValue());
                                            else if (clParamType.equals(Long.TYPE))
                                                clMethod.invoke(objDTOInstance, numValue.longValue());
                                            else if (clParamType.equals(Float.TYPE))x
                                                clMethod.invoke(objDTOInstance, numValue.floatValue());
                                            else if (clParamType.equals(Double.TYPE))
                                                clMethod.invoke(objDTOInstance, numValue.doubleValue());
                                        }

                                    }
                                }
                                else if (clJsonArray.get(j) instanceof JSONObject)
                                    clListDTOs.add(convertJsonToObject(clJsonArray.get(j).toString(), (Class) clParamTypes[0]));
                                else if (clJsonArray.get(j) != null)
                                    clListDTOs.add(clJsonArray.get(j).toString());*/

                                    objParam = clListDTOs;

                                }
                            }
                            else if (clParameterType == Object.class)
                                objParam = convertJsonToObjectArray(objValue.toString(), ((Class) clParameterType).getComponentType(), objDTOInstance);
                            else if (clParameterType instanceof ArrayList)
                            {
                                objParam = convertJsonToArrayList(objValue.toString());
                            }
                            else if (clParameterType instanceof Class && ((Class) clParameterType).isArray())
                            {
                                if (((Class) clParameterType).getSimpleName().equals("Object[][]"))
                                    objParam = convertJsonToObjectArray(objValue.toString(), Object[].class, objDTOInstance);
                                else if (((Class) clParameterType).getSimpleName().contains("[]"))
                                    objParam = convertJsonToObjectArray(objValue.toString(), ((Class) clParameterType).getComponentType(), objDTOInstance);
                            }
                            else if (clParameterType instanceof TypeVariable)
                                objParam = objValue.toString();
                            else if(clMethod.getGenericParameterTypes()[0] instanceof Class)
                            {
                                Type[] clParamTypes = ((Class)clMethod.getGenericParameterTypes()[0]).getInterfaces();

                                if (isImplementsJsonArrayObject(((Class)clMethod.getGenericParameterTypes()[0])))
                                    objParam = convertJsonArrayToObject((JSONArray) objValue, (Class)clMethod.getGenericParameterTypes()[0]);

                            }

                            clMethod.invoke(objDTOInstance, objParam);
                        }
                        else
                        {
                            invokeMethod(objDTOInstance, clTargetClass, clMethod, objValue);
                        }
                    }
                }
            }

            return (T) objDTOInstance;

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    private <T> Object invokeMethod(Object objDTOInstance, Class clTargetClass, Method clMethod, Object objValue) throws InvocationTargetException, IllegalAccessException
    {
        if (objValue instanceof String || objValue instanceof Number)
        {
            Class clParamType = clMethod.getParameterTypes()[0];
            if(objValue instanceof Number)
            {
                Number numValue = (Number) objValue;
                if (objValue instanceof Byte || clParamType.equals(Byte.TYPE))
                    clMethod.invoke(objDTOInstance, numValue.byteValue());
                else if (objValue instanceof Short || clParamType.equals(Short.TYPE))
                    clMethod.invoke(objDTOInstance, numValue.shortValue());
                else if (objValue instanceof Integer || clParamType.equals(Integer.TYPE))
                    clMethod.invoke(objDTOInstance, numValue.intValue());
                else if (objValue instanceof Long || clParamType.equals(Long.TYPE))
                    clMethod.invoke(objDTOInstance, numValue.longValue());
                else if (objValue instanceof Float || clParamType.equals(Float.TYPE))
                    clMethod.invoke(objDTOInstance, numValue.floatValue());
                else if (objValue instanceof Double || clParamType.equals(Double.TYPE))
                    clMethod.invoke(objDTOInstance, numValue.doubleValue());
            }
            if (objValue instanceof String)
                clMethod.invoke(objDTOInstance, objValue);
        }
        else if (objValue instanceof Boolean)
        {
            clMethod.invoke(objDTOInstance, objValue);
        }
        else if (objValue instanceof JSONObject)
        {
            Class objInnerClass = clMethod.getParameterTypes()[0];
//            Class objInnerClass = clMethod.getReturnType();
            if(objInnerClass.getSuperclass()==null)//java.lang.object class
            {
                clMethod.invoke(objDTOInstance, objValue.toString());
            }
            else
            {
                T objInnerDTO = convertJsonToObject(objValue.toString(), objInnerClass);
                clMethod.invoke(objDTOInstance, objInnerDTO);
            }
        }
        return clMethod.getDefaultValue();
    }

    @Override
    public <T> T convertJsonToArrayList(String sJsonString, Class[] clTypes, boolean isSingleTargetClass)
    {
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
    public <T> T convertJsonToArrayList(JSONArray clJsonArray, Class[] clTypes)
    {
        return convertJsonToArrayList(clJsonArray, clTypes, false);
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

    @Override
    public <T> T convertJsonToArrayList(JSONArray clJsonArray, Class[] clTypes, boolean isSingleTargetClass)
    {
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
                        boolean isConvertToJsonObject = isImplementsJsonArrayObject(clTargetClass);

                        if (isConvertToJsonObject)
                            alArrayData.add(convertJsonArrayToObject((JSONArray) clJsonArray.get(j), clTargetClass));
                        else
                            alArrayData.add(convertJsonToArrayList(clJsonArray.get(j).toString()));
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
    public <T> T convertJsonToArrayList(String sJsonString)
    {
        return convertJsonToArrayList(sJsonString, null, false);
    }

    @Override
    public <T> T convertJsonToObjectArray(String sJsonString, Class<T> clTargetClass, Object objDTOInstance) throws JSONException
    {
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
    public <T> T convertJsonToObjectArray(JSONArray clJsonArray, Class<T> clTargetClass, Object objDTOInstance) throws JSONException
    {
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

                        if (clTargetInstance instanceof IJsonParentProperty)
                        {
                            String[] sArrMethods = ((IJsonParentProperty) clTargetInstance).getParentMethods();
                            for (int j = 0; j < sArrMethods.length; j++)
                            {
                                Method clSrcMethod = getDeclaredMethod(objDTOInstance.getClass(), getMethodName(sArrMethods[j], "get"));
                                Object objValue = clSrcMethod.invoke(objDTOInstance, null);

                                Method clTargetMethod = getDeclaredMethod(clTargetClass, getMethodName(sArrMethods[j], "set"));
                                clTargetMethod.invoke(clTargetInstance, objValue);
                            }
                        }

                        isConvertToJsonObject = isImplementsJsonArrayObject(clTargetClass);
                        if (isConvertToJsonObject)
                        {
                            //Array.set(objArray, i, convertJsonArrayToObject((JSONArray) clJsonArray.get(i), clTargetClass));
                            JSONArray clNamesArray = ((IJsonArrayObject) clTargetInstance).getMethodsArray();
                            Array.set(objArray, i, convertJsonToObject(clNamesArray, (JSONArray) clJsonArray.get(i), clTargetClass, clTargetInstance));
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

                    if (clTargetInstance instanceof IJsonParentProperty)
                    {
                        String[] sArrMethods = ((IJsonParentProperty) clTargetInstance).getParentMethods();
                        for (int j = 0; j < sArrMethods.length; j++)
                        {
                            Method clSrcMethod = getDeclaredMethod(clTargetClass, getMethodName(sArrMethods[j], "get"));
                            Object objValue = clSrcMethod.invoke(objDTOInstance, null);

                            Method clTargetMethod = getDeclaredMethod(clTargetClass, getMethodName(sArrMethods[j], "set"));
                            clTargetMethod.invoke(clTargetInstance, objValue);
                        }
                    }

                    JSONObject clJsonObject = new JSONObject(clJsonArray.get(i).toString());
                    JSONArray clNamesArray = clJsonObject.names();
                    JSONArray clValuesArray = clJsonObject.toJSONArray(clNamesArray);

                    convertJsonToObject(clNamesArray, clValuesArray, clTargetClass, clTargetInstance);
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
        catch (InvocationTargetException ie)
        {
            throw new RuntimeException(ie);
        }
        catch (InstantiationException ie)
        {
            throw new RuntimeException(ie);
        }
    }

    private Method getDeclaredMethod(Class clTargetClass, String sMethod)
    {
        Method[] clDeclaredMethods = clTargetClass.getDeclaredMethods();
        for (Method clDeclaredMethod1 : clDeclaredMethods)
        {
            if (clDeclaredMethod1.getName().equals(sMethod))
                return clDeclaredMethod1;
        }

        byte byCount = 0;
        byte byMaxCount = 2;
        while (byCount < byMaxCount)
        {
            Class clSuperClass = clTargetClass.getSuperclass();
            if (clSuperClass==null || clSuperClass == Object.class)
                break;

            clDeclaredMethods = clSuperClass.getDeclaredMethods();

            for (Method clDeclaredMethod : clDeclaredMethods)
            {
                if (clDeclaredMethod.getName().equals(sMethod))
                    return clDeclaredMethod;
            }

            clTargetClass = clSuperClass;
            byCount++;
        }


        return null;
    }

    private String getMethodName(String propertyName, String prefix)
    {
        return prefix + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }


    private <T> T convertJsonToObject_old(JSONArray clNamesArray, JSONArray clValuesArray, Class clTargetClass, Object objDTOInstance)
    {
        try
        {
            Method clMethod;
            String sJsonProperty;

            if (objDTOInstance == null)
                objDTOInstance = clTargetClass.newInstance();

            for (int i = 0; i < clNamesArray.length(); i++)
            {
                sJsonProperty = (String) clNamesArray.get(i);
//                Object objValue=clJsonObject.get(sJsonProperty);
                Object objValue = clValuesArray.get(i);

                if (objDTOInstance instanceof IJsonObject)
                    sJsonProperty = ((IJsonObject) objDTOInstance).getMethodName(sJsonProperty);
                else
                    sJsonProperty = (String) clNamesArray.get(i);

                if (sJsonProperty == null)
                    continue;

                sJsonProperty = sJsonProperty.trim();

                clMethod = getDeclaredMethod(clTargetClass, getMethodName(sJsonProperty, "set"));
                if (clMethod != null)
                {

                    if (objValue instanceof String || objValue instanceof Number)
                    {
                        Class clParamType = clMethod.getParameterTypes()[0];
                        if (objValue instanceof String)
                            clMethod.invoke(objDTOInstance, objValue);
                        else
                        {
                            Number numValue = (Number) objValue;
                            if (clParamType.equals(Byte.TYPE))
                                clMethod.invoke(objDTOInstance, numValue.byteValue());
                            else if (clParamType.equals(Short.TYPE))
                                clMethod.invoke(objDTOInstance, numValue.shortValue());
                            else if (clParamType.equals(Integer.TYPE))
                                clMethod.invoke(objDTOInstance, numValue.intValue());
                            else if (clParamType.equals(Long.TYPE))
                                clMethod.invoke(objDTOInstance, numValue.longValue());
                            else if (clParamType.equals(Float.TYPE))
                                clMethod.invoke(objDTOInstance, numValue.floatValue());
                            else if (clParamType.equals(Double.TYPE))
                                clMethod.invoke(objDTOInstance, numValue.doubleValue());
                        }
                    }
                    else if (objValue instanceof JSONObject)
                    {
                        Class objInnerClass = clMethod.getReturnType();
                        T objInnerDTO = convertJsonToObject(objValue.toString(), objInnerClass);
                        clMethod = getDeclaredMethod(clTargetClass, getMethodName(sJsonProperty, "set"));
                        if (clMethod != null)
                            clMethod.invoke(objDTOInstance, objInnerDTO);
                    }
                    else if (objValue instanceof JSONArray)
                    {
                        Type clParameterType = clMethod.getGenericParameterTypes()[0];
                        Object objParam = null;
                        Class clParamClass = null;
                        JSONArray clJsonArray = (JSONArray) objValue;

                        if (clParameterType instanceof ParameterizedType)
                        {
                            Type[] clParamTypes = ((ParameterizedType) clMethod.getGenericParameterTypes()[0]).getActualTypeArguments();
                            ArrayList clListDTOs = new ArrayList();

                            if (objDTOInstance instanceof IJsonTarget)
                            {
                                clParamClass = ((IJsonTarget) objDTOInstance).getTargetDTO(clJsonArray);
                                if (clParamClass != null) //converted to Arraylist if null is returned else converted to DTO
                                    objParam = convertJsonArrayToObject(clJsonArray, clParamClass);
                            }


                            for (int j = 0; j < clJsonArray.length(); j++)
                            {
                                if (clJsonArray.get(j) instanceof JSONArray)
                                {
                                    clParamClass = null;

                                    if (objDTOInstance instanceof IJsonTarget)
                                        clParamClass = ((IJsonTarget) objDTOInstance).getTargetDTO((JSONArray) clJsonArray.get(j));

                                    if (clParamClass == null)
                                        clParamClass = (Class) clParamTypes[0];

                                    clListDTOs.add(convertJsonArrayToObject((JSONArray) clJsonArray.get(j), clParamClass));
                                }
                                else if (clJsonArray.get(j) instanceof Number)
                                {
                                    clMethod = getDeclaredMethod(clTargetClass, getMethodName(sJsonProperty, "set"));
                                    if (clMethod != null)
                                    {
                                        Class clParamType = clMethod.getParameterTypes()[0];
                                        if (clJsonArray.get(j) instanceof String)
                                            clMethod.invoke(objDTOInstance, clJsonArray.get(j));
                                        else
                                        {
                                            Number numValue = (Number) clJsonArray.get(j);
                                            if (clParamType.equals(Byte.TYPE))
                                                clMethod.invoke(objDTOInstance, numValue.byteValue());
                                            else if (clParamType.equals(Short.TYPE))
                                                clMethod.invoke(objDTOInstance, numValue.shortValue());
                                            else if (clParamType.equals(Integer.TYPE))
                                                clMethod.invoke(objDTOInstance, numValue.intValue());
                                            else if (clParamType.equals(Long.TYPE))
                                                clMethod.invoke(objDTOInstance, numValue.longValue());
                                            else if (clParamType.equals(Float.TYPE))
                                                clMethod.invoke(objDTOInstance, numValue.floatValue());
                                            else if (clParamType.equals(Double.TYPE))
                                                clMethod.invoke(objDTOInstance, numValue.doubleValue());

                                        }

                                    }
                                }
                                else if (clJsonArray.get(j) instanceof JSONObject)
                                    clListDTOs.add(convertJsonToObject(clJsonArray.get(j).toString(), (Class) clParamTypes[0]));
                                else if (clJsonArray.get(j) != null)
                                    clListDTOs.add(clJsonArray.get(j).toString());

                                objParam = clListDTOs;

                            }
                        }
                        else if (clParameterType == Object.class)
                            objParam = convertJsonToObjectArray(objValue.toString(), ((Class) clParameterType).getComponentType(), objDTOInstance);
                        else if (clParameterType instanceof ArrayList)
                        {
                            objParam = convertJsonToArrayList(objValue.toString());
                        }
                        else if (clParameterType instanceof Class && ((Class) clParameterType).isArray())
                        {
                            //if(((Class)clParameterType).getSimpleName().equals("Object[][]"))
                            if (((Class) clParameterType).getSimpleName().contains("[]"))
                                objParam = convertJsonToObjectArray(objValue.toString(), ((Class) clParameterType).getComponentType(), objDTOInstance);
                        }
                        else if (clParameterType instanceof TypeVariable)
                            objParam = objValue.toString();

                        clMethod.invoke(objDTOInstance, objParam);
                    }

                }
            }

            return (T) objDTOInstance;

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
