package com.tongtech.common.utils;

import com.tongtech.common.utils.operation.ByteArrayOutputStream;
import org.apache.ibatis.cache.CacheException;
import org.apache.ibatis.cache.decorators.SerializedCache;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectSerializeUtils {


    public static byte[] serialize(Serializable value) {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bos);) {
            oos.writeObject(value);
            oos.flush();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new CacheException("Error serializing object.  Cause: " + e, e);
        }
    }


    public static Serializable deserialize(byte[] value) {
        try(ObjectInputStream ois = new SerializedCache
                .CustomObjectInputStream(new ByteArrayInputStream(value))) {
            return (Serializable) ois.readObject();
        } catch (Exception e) {
            throw new CacheException("Error deserializing object.  Cause: " + e, e);
        }
    }


}
