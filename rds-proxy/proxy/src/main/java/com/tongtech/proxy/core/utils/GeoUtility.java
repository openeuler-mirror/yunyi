package com.tongtech.proxy.core.utils;

public class GeoUtility {
    private static final double LATITUDE_STDMAX = 90;
    private static final double LATITUDE_STDMIN = -90;
    private static final double LATITUDE_MAX = 85.05112878;
    private static final double LATITUDE_MIN = -85.05112878;
    private static final double LONGITUDE_MAX = 180;
    private static final double LONGITUDE_MIN = -180;

    private static final int MAX_VALUE = 1 << 26;

    private static final long B[] = {0x5555555555555555L, 0x3333333333333333L,
            0x0F0F0F0F0F0F0F0FL, 0x00FF00FF00FF00FFL,
            0x0000FFFF0000FFFFL};
    private static final int S[] = {1, 2, 4, 8, 16};

    private static final char[] geoalphabet = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
            , 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm'
            , 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x'
            , 'y', 'z'};

    /**
     * 用内部可支持纬度范围(-85, +85)计算内部保存的hash值
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return hash值
     */
    public static long getHashInternal(double longitude, double latitude) {
        if (latitude > LATITUDE_MAX || latitude < LATITUDE_MIN
                || longitude > LONGITUDE_MAX || longitude < LONGITUDE_MIN) {
            throw new IllegalArgumentException("ERR invalid longitude,latitude pair " + longitude + "," + latitude);
        }

        latitude = (latitude - LATITUDE_MIN) / (LATITUDE_MAX - LATITUDE_MIN);
        longitude = (longitude - LONGITUDE_MIN) / (LONGITUDE_MAX - LONGITUDE_MIN);

        int lat = (int) (latitude * MAX_VALUE);
        int lon = (int) (longitude * MAX_VALUE);

        return interleave64(lat, lon);
    }

    public static double getLatitue(long hash) {
        long xy = deinterleave64(hash);
        double x = xy >>> 32;

        x = (x / MAX_VALUE) * (LATITUDE_MAX - LATITUDE_MIN) + LATITUDE_MIN;

        return x;
    }

    public static double getLongitude(long hash) {
        long xy = deinterleave64(hash);
        double y = xy & 0xffffffffl;

        y = (y / MAX_VALUE) * (LONGITUDE_MAX - LONGITUDE_MIN) + LONGITUDE_MIN;

        return y;
    }


    public static String getHashString(long hash) {
        long xy = deinterleave64(hash);

        double lat = xy >>> 32;
        double lon = (xy) & 0xffffffffl;

        lat = (lat / MAX_VALUE) * (LATITUDE_MAX - LATITUDE_MIN) + LATITUDE_MIN;
        lon = (lon / MAX_VALUE) * (LONGITUDE_MAX - LONGITUDE_MIN) + LONGITUDE_MIN;

        xy = getHashStd(lon, lat);
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            int idx = (int) ((xy >> (52 - ((i + 1) * 5))) & 0x1f);
            buf.append(geoalphabet[idx]);
        }
        return buf.toString();
    }

    /**
     * 用标砖的纬度范围（-90，+90）计算公开的hash值
     *
     * @param longitude
     * @param latitude
     * @return
     */
    private static long getHashStd(double longitude, double latitude) {
        if (latitude > LATITUDE_STDMAX || latitude < LATITUDE_STDMIN
                || longitude > LONGITUDE_MAX || longitude < LONGITUDE_MIN) {
            throw new IllegalArgumentException("ERR invalid longitude,latitude pair " + longitude + "," + latitude);
        }

        latitude = (latitude - LATITUDE_STDMIN) / (LATITUDE_STDMAX - LATITUDE_STDMIN);
        longitude = (longitude - LONGITUDE_MIN) / (LONGITUDE_MAX - LONGITUDE_MIN);

        int lat = (int) (latitude * MAX_VALUE);
        int lon = (int) (longitude * MAX_VALUE);

        return interleave64(lat, lon);
    }

    //    private static long interleave64(int xlo, int ylo) {
    private static long interleave64(long x, long y) {

        x = (x | (x << S[4])) & B[4];
        y = (y | (y << S[4])) & B[4];

        x = (x | (x << S[3])) & B[3];
        y = (y | (y << S[3])) & B[3];

        x = (x | (x << S[2])) & B[2];
        y = (y | (y << S[2])) & B[2];

        x = (x | (x << S[1])) & B[1];
        y = (y | (y << S[1])) & B[1];

        x = (x | (x << S[0])) & B[0];
        y = (y | (y << S[0])) & B[0];

        return x | (y << 1);
    }

    /* reverse the interleave process
     * derived from http://stackoverflow.com/questions/4909263
     */
    public static long deinterleave64(long interleaved) {

        long x = interleaved;
        long y = interleaved >>> 1;

        x = (x & B[0]) | ((x & B[0]) >>> S[0]);
        y = (y & B[0]) | ((y & B[0]) >>> S[0]);

        x = (x & B[1]) | ((x & B[1]) >>> S[1]);
        y = (y & B[1]) | ((y & B[1]) >>> S[1]);

        x = (x & B[2]) | ((x & B[2]) >>> S[2]);
        y = (y & B[2]) | ((y & B[2]) >>> S[2]);

        x = (x & B[3]) | ((x & B[3]) >>> S[3]);
        y = (y & B[3]) | ((y & B[3]) >>> S[3]);

        x = ((x & B[4]) | ((x & B[4]) >>> S[4])) & 0xffffffffl;
        y = ((y & B[4]) | ((y & B[4]) >>> S[4])) & 0xffffffffl;

        return (x << 32) | y;
    }

    /**
     * 计算两个经纬度之间的距离  结果单位：英尺
     *
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public static double getDistanceFeet(double lng1, double lat1, double lng2, double lat2) {
        return Math.round(getDistance(lng1, lat1, lng2, lat2) * 32808.3989501312335958d) / 10000d;
    }

    /**
     * 计算两个经纬度之间的距离  结果单位：英里
     *
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public static double getDistanceMile(double lng1, double lat1, double lng2, double lat2) {
        return getDistance(lng1, lat1, lng2, lat2) * 6.2137119223733396961742d / 10000.0d;
    }

    /**
     * 计算两个经纬度之间的距离  结果单位：英里
     *
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public static double getDistanceKelometer(double lng1, double lat1, double lng2, double lat2) {
        return getDistance(lng1, lat1, lng2, lat2) / 1000.0d;
    }

    /**
     * 计算两个经纬度之间的距离  结果单位：米
     *
     * @param lng1 第一个点的经度
     * @param lat1 第一个点的纬度
     * @param lng2 第二个点的经度
     * @param lat2 第二个点的纬度
     * @return 两个点的距离
     */
    public static double getDistance(double lng1, double lat1, double lng2, double lat2) {
        double lat1r = rad(lat1);
        double lat2r = rad(lat2);
        double a = lat1r - lat2r;
        double b = rad(lng1) - rad(lng2);
        double s = 2.0 * EARTH_RADIUS * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(lat1r) * Math.cos(lat2r) * Math.pow(Math.sin(b / 2), 2)));
//        s = Math.round(s * 10000) / 10000.0;
        return s;
    }

    private static double EARTH_RADIUS = 6372797.560856;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    public static void main(String[] arg) {
//        System.out.println(getDistance(13, 38, 15, 37));

//        deinterleave64(interleave64(123456, 345678));

        System.out.println(getLatitue(3479099956230698l));
        System.out.println(getLongitude(3479099956230698l));

        System.out.println(getLatitue(3479099956230698l));
        System.out.println(getLongitude(3479099956230698l));

        int[] a = new int[1000];
        for (int i = 0; i < a.length; ++i) {
            long l = System.nanoTime();
            a[i] = ((int) ( (l >> 6) ^ (l >> 13) ^ (l >> 20))) & 0x3;
        }
        for (int i = 0; i < a.length; ++i) {
            System.out.println(a[i]);
        }
    }

}
