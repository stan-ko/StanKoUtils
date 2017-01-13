package com.stanko.tools;

/**
 * by Devlight
 *
 * Authors:
 * Stan Koshutsky <Stan.Koshutsky@gmail.com>
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
