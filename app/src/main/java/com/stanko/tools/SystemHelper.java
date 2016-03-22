package com.stanko.tools;

/**
 * Created by stan on 17.03.16.
 */
public class SystemHelper {

    /**
     * Requests Garbage Collector to execute
     */
    public static void cleanUpMemory(){
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }
}
