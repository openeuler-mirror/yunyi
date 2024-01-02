package com.tongtech.probe.pojo;


import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * 节点监控信息（node)
 */
public abstract class Statistics implements Serializable
{
    private static final long serialVersionUID = 1L;

    //protected static final Log LOGGER = StaticContext.SERVER_LOGGER;

    private String type;
    private String name;
    private boolean alive;

    public final synchronized String getType() {
        return this.type;
    }

    public final synchronized void setType(String type) {
        this.type = type;
    }

    public final synchronized String getName() {
        return this.name;
    }

    public final synchronized void setName(String name) {
        this.name = name;
    }

    public final synchronized boolean isAlive() {
        return this.alive;
    }

    public final synchronized void setAlive(boolean alive) {
        this.alive = alive;
        if (!alive) {
            cleanFields();
        }
    }

    private void cleanFields() {
        try {
            Class clazz = this.getClass();
            Field[] fields = clazz.getDeclaredFields();
            if (fields != null) {
                for (Field field : fields) {
                    try {
                        String name = field.getName();
                        if ("name".equals(name) || "type".equals(name) || "alive".equals(name)) {
                            continue;
                        }
                        field.setAccessible(true);
                        Type type = field.getType();
                        if (Long.class.equals(type)
                                || Integer.class.equals(type)
                                || String.class.equals(type)
                                || Float.class.equals(type)
                                || Double.class.equals(type)
                                || Boolean.class.equals(type)
                                || Map.class.equals(type)
                                || List.class.equals(type)) {
                            field.set(this, null);
                            //LOGGER.debugLog("Statistics::cleanFields() set {} to null", field);
                        } else {
                            //LOGGER.debugLog("Statistics::cleanFields() find filed '{}'", field);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                } // for (Field field : fields) {
            } // if (fields != null) {
        } catch (Throwable t2) {
            t2.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Statistics{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", alive=" + alive +
                '}';
    }
}
