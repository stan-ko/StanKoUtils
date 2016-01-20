package com.stanko.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by Stan
 */
public class ZipHlper {

    private final static String LOG_TAG = ZipHlper.class.getSimpleName();

    public static final int BUFFER = 1024;

    public static void zip(File[] files, File zipFileName) {
        FileOutputStream dest = null;
        ZipOutputStream zipOutputStream = null;
        try {
            dest = new FileOutputStream(zipFileName);
            zipOutputStream = new ZipOutputStream(new BufferedOutputStream(dest));
            final byte data[] = new byte[BUFFER];
            for (File file : files) {
                add(file,zipOutputStream,data);
            }
            FileUtils.sync(dest);
            dest.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zipOutputStream!=null)
                try {
                    zipOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (dest!=null)
                try {
                    dest.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public static void zip(String[] files, String zipFileName) {
        FileOutputStream dest = null;
        ZipOutputStream zipOutputStream = null;
        try {
            dest = new FileOutputStream(zipFileName);
            zipOutputStream = new ZipOutputStream(new BufferedOutputStream(dest));
            final byte data[] = new byte[BUFFER];
            for (String file : files) {
                Log.d(LOG_TAG, "Adding: " + file);
                add(new File(file),zipOutputStream,data);
            }

            FileUtils.sync(dest);
            dest.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zipOutputStream!=null)
                try {
                    zipOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (dest!=null)
                try {
                    dest.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private static void add(final File file, final ZipOutputStream zipOutputStream, final byte[] data){
        Log.d(LOG_TAG, "Adding: " + file);
        BufferedInputStream origin = null;
        try {
            FileInputStream fi = new FileInputStream(file);
            origin = new BufferedInputStream(fi, BUFFER);

            ZipEntry zipEntry = new ZipEntry(file.getName());
            zipOutputStream.putNextEntry(zipEntry);
            int count;

            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                zipOutputStream.write(data, 0, count);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (origin!=null)
                try {
                    origin.close();
                } catch (IOException ignored){}

        }
    }

    public static void zip(final String fileToAddToZip, String targetZipFile) {
        zip(new File(fileToAddToZip),new File(targetZipFile));
    }

    public static void zip(final File fileToAddToZip, File targetZipFile) {
        try {
            Log.d(LOG_TAG, "Adding: " + fileToAddToZip);
            final FileOutputStream dest = new FileOutputStream(targetZipFile);
            final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            final byte data[] = new byte[BUFFER];
            final FileInputStream fi = new FileInputStream(fileToAddToZip);
            final BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);

            final ZipEntry entry = new ZipEntry(fileToAddToZip.getName());
            out.putNextEntry(entry);
            int count;

            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
            FileUtils.sync(dest);
            out.close();
            dest.flush();
            dest.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void unzip(final String zipFile, final String targetLocation) {
        unzip(new File(zipFile),new File(targetLocation));
    }

    public void unzip(final File zipFile, final File targetLocation) {
        if (!FileUtils.isReadable(zipFile)) {
            new Exception("unzip(): Invalid zipFile: "+zipFile).printStackTrace();
            return;
        }
        //create target location folder if not exist
        targetLocation.mkdirs();
        if (!targetLocation.exists()){
            new Exception("unzip(): Can't create path (targetLocation): "+targetLocation).printStackTrace();
            return;
        }
        try {
            final FileInputStream fileInputStream = new FileInputStream(zipFile);
            final ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                //create dir if required while unzipping
                if (zipEntry.isDirectory()) {
                    new File(targetLocation,zipEntry.getName()).mkdirs();//dirChecker(ze.getName());
                }
                else {
                    final FileOutputStream fileOutputStream = new FileOutputStream(targetLocation + zipEntry.getName());
                    for (int c = zipInputStream.read(); c != -1; c = zipInputStream.read()) {
                        fileOutputStream.write(c);
                    }
                    FileUtils.sync(fileOutputStream);
                    zipInputStream.closeEntry();
                    fileOutputStream.close();
                }
            }
            zipInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}