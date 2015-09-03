package com.stanko.tools;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.SyncFailedException;

public class FileUtils {

    /**
     * Copies an existing file to another destination
     * @param src - String path of a file to copy from
     * @param dst - String path of a file to copy to
     * @throws IOException
     */
    public static void copy(final String src, final String dst) throws IOException {
        if (TextUtils.isEmpty(src) || TextUtils.isEmpty(dst))
            throw new IOException("IOException (copy file): source file does not exists or not readable");
        copy (new File(src), new File(dst));
    }

    /**
     * Copies an existing file to another destination
     * @param src - File to copy from
     * @param dst - File to copy to
     * @throws IOException
     */
    public static void copy(final File src, final File dst) throws IOException {
        if (src==null || !src.exists() || !src.canRead())
            throw new IOException("IOException (copy file): source file does not exists or not readable");

//		if ( !makeDirsForFile(dst) )
//			throw new IOException("IOException (copy file): cant create dirs for destination file");

        if ( !isWritable(dst,true) )
            throw new IOException("IOException (copy file): destination file not writable");

        Log.w("FileUtils","copying: src: "+src+" to dst: "+dst);

        final FileInputStream fis = new FileInputStream(src);
        final FileOutputStream fos = new FileOutputStream(dst);
        final InputStream in = new BufferedInputStream( fis );
        final BufferedOutputStream out = new BufferedOutputStream( fos );

        // Transfer bytes from in to out
        final byte[] buf = new byte[1024];
        int len;
        IOException e = null;

        try {
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);
        } catch (IOException e1){
            e = e1;
        }

        // close InputStream in
        try {
            in.close();
        } catch (IOException ignored) {}
        // close FileInputStream fis
        try {
            fis.close();
        } catch (IOException ignored) {}
        // close BufferedOutputStream out
        try {
            out.flush();
            out.close();
        } catch (IOException ignored) {}
        // close FileOutputStream fos
        try {
            fos.flush();
            fos.close();
        } catch (IOException ignored) {}

        sync(fos);

        if (e != null)
            throw e;

    }

    /**
     * Copies a file to another destination
     * @param src - File to copy from
     * @param dst - File to copy to
     * @throws IOException
     */
    public static synchronized void copySynchronized(final File src, final File dst) throws IOException {
        if (src==null || !src.exists() || !src.canRead())
            throw new IOException("IOException (copy file): source file does not exists or not readable");

//		if (!makeDirsForFile(dst) )
//			throw new IOException("IOException (copy file): cant create dirs for destination file");

        if (!isWritable(dst, true) /*dst==null || dst.exists() && !dst.canWrite()*/)
            throw new IOException("IOException (copy file): destination file not writable");

        final FileInputStream fis = new FileInputStream(src);
        final FileOutputStream fos = new FileOutputStream(dst);
        final InputStream in = new BufferedInputStream( fis );
        final BufferedOutputStream out = new BufferedOutputStream( fos );

        // Transfer bytes from in to out
        final byte[] buf = new byte[1024];
        int len;
        IOException e = null;
        try {
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);
        } catch (IOException e1){
            e = e1;
        }
        // close InputStream in
        try {
            in.close();
        } catch (IOException ignored) {}
        // close FileInputStream fis
        try {
            fis.close();
        } catch (IOException ignored) {}
        // close BufferedOutputStream out
        try {
            out.flush();
            out.close();
        } catch (IOException ignored) {}
        // close FileOutputStream fos
        try {
            fos.flush();
            fos.close();
        } catch (IOException ignored) {}

        sync(fos);

        if (e != null)
            throw e;
    }

    /**
     * Writes a String to a File.
     * @param data - String to write to a file
     * @param targetFile - target File
     * @return true if all OK or false otherwise
     */
    public static boolean stringToFile(final String data, final File targetFile) {
        if (targetFile==null || !isWritable(targetFile, true)){
            Log.e(FileUtils.class,"stringToFile(): File is null or cant make path dirs");
            //new IOException("File is null or cant make path dirs").printStackTrace();
            return false;
        }

        boolean isSucceed = true;
        try {
            final FileWriter out = new FileWriter(targetFile);
            out.write(data);
            out.flush();
            out.close();
        } catch (IOException e) {
            //Logger.logError(TAG, e);
            isSucceed = false;
        }

        return isSucceed;
    }

    /**
     * Writes a stream to a file
     * @param inputStream
     * @param targetFile - File to create
     * @return true if all OK or false otherwise
     */
    public static boolean streamToFile(final InputStream inputStream, final File targetFile)
    {
        if (inputStream == null || !isWritable(targetFile, true) ){
            Log.e(FileUtils.class,"streamToFile(): Null parameter or can't make path dirs");
            //new IOException("Null parameter or can't make path dirs").printStackTrace();
            return false;
        }

        boolean isSucceed = false;
        final int buffer_size = 1024;
        OutputStream outputStream = null;
        try
        {
            outputStream = new FileOutputStream(targetFile);
            byte[] bytes=new byte[buffer_size];
            int count = 0;
            while((count=inputStream.read(bytes, 0, buffer_size))>0)
                outputStream.write(bytes, 0, count);

            isSucceed = true;
            outputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(IndexOutOfBoundsException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        finally {
            if (outputStream!=null)
                try{
                    sync(outputStream);
                    outputStream.close();
                } catch (IOException e) {}
            //inputStream wasn't opened here so it would not be closed here
        }
        return isSucceed;
    }

    /**
     * Writes a byte array to a file.
     * @param bos - ByteArrayOutputStream
     * @param targetFile - File to create
     * @return true if all OK or false otherwise
     */
    public static boolean byteArrayOutputStreamToFile(final ByteArrayOutputStream bos, final File targetFile) {
        if (bos==null || targetFile==null) {
            Log.e(FileUtils.class,"byteArrayOutputStreamToFile(): Null parameters given");
            //new IOException("Null parameters given").printStackTrace();
            return false;
        }
        return byteArrayToFile(bos.toByteArray(), targetFile);
    }
    /**
     * Writes a byte array to a file.
     * @param array - byte[]
     * @param targetFile - File to create
     * @return true if all OK or false otherwise
     */
    public static boolean byteArrayToFile(final byte[] array, final File targetFile) {

        if (array == null || array.length==0 || !isWritable(targetFile, true) ){
            Log.e(FileUtils.class,"byteArrayToFile(): Null parameter or can't make path dirs");
            //new IOException("Null parameter or can't make path dirs").printStackTrace();
            return false;
        }

        if (targetFile.exists() &&  !targetFile.delete())
            return false;

//		if (targetFile==null || !makeDirsForFile(targetFile)){
//			new IOException("File is null or cant make path dirs").printStackTrace();
//			return false;
//		}

        boolean isSucceed = false;
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(targetFile);
            outputStream.write(array);
            isSucceed = true;
            outputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (outputStream!=null)
                try{
                    sync(outputStream);
                    outputStream.close();
                }
                catch (IOException e) {}
        }

        return isSucceed;
    }


    /**
     * Method creates a File from intent's data
     * @param context - Context
     * @param uri - Uri taken from Intent.detData()
     * @param targetFile - File to create
     * @return true if all OK or false otherwise
     */
    public static boolean intentDataToFile(final Context context, final Uri uri, final String targetFile) {
        return intentDataToFile(context, uri, new File(targetFile));
    }

    /**
     * Method creates a File from intent's data
     * @param context - Context
     * @param uri - Uri taken from Intent.detData()
     * @param targetFile - File to create
     * @return true if all OK or false otherwise
     */
    public static boolean intentDataToFile(final Context context, final Uri uri, final File targetFile) {

        if (context==null || uri == null || uri.toString().length()==0 || !isWritable(targetFile, true)) {
            Log.e(FileUtils.class,"intentDataToFile(): Null parameter or can't make path dirs");
            //new IOException("Null parameter or can't make path dirs").printStackTrace();
            return false;
        }
//    	if (targetFile.exists() &&  !targetFile.delete())
//    		return false;
//		if (targetFile==null || !makeDirsForFile(targetFile)){
//			new IOException("File is null or cant make path dirs").printStackTrace();
//			return false;
//		}

        boolean isSucceed = false;
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            isSucceed = streamToFile(inputStream, targetFile);
        } catch (IOException e) {
            Log.e("FleUtils.intentDataToFile()", e);
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {}
        }

        return isSucceed;
    }


    /**
     * Method creates the FILE's path dirs and returns true if succeed. The difference
     *  from File.mkdirs() is that mkdirs() returns false in both cases:
     *  it can't create path OR path already exists. This method will return
     *  false if path could not be created only.
     * @param file - File only the not a Directory!
     * @return  true if file's path created/exists or false if path could not be created
     */
    public static boolean makeDirsForFile(final String file){
        if (TextUtils.isEmpty(file)) {
            Log.e(FileUtils.class,"makeDirsForFile(): Null or Empty parameter given");
            //new Exception("Empty parameter").printStackTrace();
            return false;
        }
        return makeDirsForFile(new File(file));
    }
    /**
     * Method creates the FILE's path dirs and returns true if succeed. The difference
     *  from File.mkdirs() is that mkdirs() returns false in both cases:
     *  it can't create path OR path already exists. This method will return
     *  false if path could not be created only.
     * @param file - File only the not a Directory!
     * @return  true if file's path created/exists or false if path could not be created
     */
    public static boolean makeDirsForFile(final File file){

        if (file==null)
            return false;

        Log.d("FileUtils","file: "+file+" isDirectory(): "+file.isDirectory());

        // для файла надо брать parent, папки в этот метод ходить не должны!
        // кроме того, несуществующий файл вернет false в isDirectory() и в isFile()
        //final File fileDir = file.isDirectory() ? file : new File(file.getParent());

        final String fileParentDir = file.getParent();
        final File fileDir = TextUtils.isEmpty(fileParentDir) ? null : new File(file.getParent());

        boolean isSucceed = fileDir==null || fileDir.exists();
        if (isSucceed)
            Log.d("FileUtils","fileDir: "+fileDir+" already exists(): "+isSucceed);

        if (!isSucceed && fileDir!=null){
            isSucceed = fileDir.mkdirs();
            Log.d("FileUtils","fileDir: "+fileDir+" mkdirs(): "+isSucceed);
        }

        return isSucceed;
    }

    /**
     * Method checks if file could be written/deleted or created. Does not create path/mkdirs so if path doesn't exists returns false.
     * @param file - File only not a Directory!
     * @return  true if file's is writable or false otherwise
     */
    public static boolean isWritable(final String file){
        if (TextUtils.isEmpty(file)) {
            Log.e(FileUtils.class,"isWritable(): Null or Empty parameter given");
            //new Exception("Empty parameter").printStackTrace();
            return false;
        }
        return isWritable(new File(file));
    }
    /**
     * Method checks if file could be written/deleted or created. Does not create path/mkdirs so if path doesn't exists returns false.
     * @param file - File only not a Directory!
     * @return  true if file's is writable or false otherwise
     */
    public static boolean isWritable(final File file){

        if (file==null){
            Log.e(FileUtils.class,"isWritable(): Null or Empty parameter given");
            return false;
        }

        return isWritable(file, false);
//		final File fileDir = new File(file.getParent());
//		
//		if (!fileDir.exists())
//			return false;
//
//		boolean isWritable = true;
//    	
//		if (file.exists())
//			isWritable = file.canWrite();
//		else{
//			try{
//				isWritable = file.createNewFile();
//			} catch (IOException e) {}
//			
//			if (isWritable)
//				isWritable = file.delete();
//		}	
//		
//		return isWritable;
    }

    /**
     * Method checks if file could be written/deleted or created. Method will create path/mkdirs if makeDirs set to true and file's path doesn't exists.
     * @param file - File only not a Directory!
     * @return  true if file's is writable or false otherwise
     */
    public static boolean isWritable(final String file, final boolean makeDirs){
        if (TextUtils.isEmpty(file)) {
            Log.e(FileUtils.class,"isWritable(): Null or Empty parameter given");
            //new Exception("Empty parameter").printStackTrace();
            return false;
        }
        return isWritable(new File(file), makeDirs);
    }

    /**
     * Method checks if file could be written/deleted or created. Method will create path/mkdirs if makeDirs set to true and file's path doesn't exists.
     * @param file - File only not a Directory!
     * @return  true if file's is writable or false otherwise
     */
    public static boolean isWritable(final File file, final boolean makeDirs){

        if (file==null){
            Log.e(FileUtils.class,"isWritable(): Null or Empty parameter given");
            return false;
        }

        final String fileParentDir = file.getParent();
        if (!TextUtils.isEmpty(fileParentDir)){
            final File fileDir = new File(fileParentDir);
            if (!fileDir.exists()){
                if (makeDirs)
                    return makeDirsForFile(file);
                else
                    return false;
            }
        }

        boolean isWritable = true;

        if (file.exists())
            isWritable = file.canWrite();
        else{
            try{
                isWritable = file.createNewFile();
            } catch (IOException e) {}

            if (isWritable)
                isWritable = file.delete();
        }

        return isWritable;
    }

    /**
     * Method checks if file could be read or created
     * @param file - File only not a Directory!
     * @return  true if file's is writable or false otherwise
     */
    public static boolean isReadable(final String file){
        if (TextUtils.isEmpty(file)) {
            Log.e(FileUtils.class,"isReadable(): Null or Empty parameter given");
            //new Exception("Empty or null parameter").printStackTrace();
            return false;
        }
        return isReadable(new File(file));
    }

    /**
     * Method checks if file could be read or created
     * @param file - File only not a Directory!
     * @return  true if file's is writable or false otherwise
     */
    public static boolean isReadable(final File file){

        if (file==null || !file.isFile()) {
            Log.e(FileUtils.class,"isReadable(): Null parameter given or not a File");
            return false;
        }

//		final File fileDir = new File(file.getParent());
//		if (!fileDir.exists())
//			return false;

        boolean isReadable = file.exists() && file.canRead();

        return isReadable;
    }

    /**
     * Method ensures about file creation from stream. For Samsung like devices
     * @param stream - OutputStream
     * @return  true if all OK or false otherwise
     */
    public static boolean sync(final OutputStream stream) {
        if (stream==null){
            Log.e(FileUtils.class,"sync(): Null parameter given");
            return false;
        }

        if (!(stream instanceof FileOutputStream))
            return false;
        else
            return sync((FileOutputStream)stream);
    }

    /**
     * Method ensures about file creation from stream. For Samsung like devices
     * @param stream - FileOutputStream
     * @return  true if all OK or false otherwise
     */
    public static boolean sync(final FileOutputStream stream) {
        if (stream==null){
            Log.e(FileUtils.class,"sync(): Null parameter given");
            return false;
        }
        try {
            stream.getFD().sync();
            return true;
        } catch (SyncFailedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean syncAndClose(final FileOutputStream stream) {
        if (stream==null){
            Log.e(FileUtils.class,"sync(): Null parameter given");
            return false;
        }
        boolean result = false;
        try {
            stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            stream.getFD().sync();
            result = true;
        } catch (SyncFailedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }


    /**
     * Method deletes all files and subdirectories recursively from given directory.
     * @param file - File which represents a directory where to delete all files and dirs
     * @return  true if all OK or false otherwise
     */
    public static void deleteFilesAndDirsRecursive(final String file) {
        if (TextUtils.isEmpty(file)){
            Log.e(FileUtils.class,"deleteFilesAndDirsRecursive(): Null or Empty parameter given");
            return;
        }
        deleteFilesAndDirsRecursive(new File(file));
    }
    /**
     * Method deletes all files and subdirectories recursively from given directory.
     * @param directory - File which represents a directory where to delete all files and dirs
     * @return  true if all OK or false otherwise
     */
    public static void deleteFilesAndDirsRecursive(final File directory) {
        if (directory==null || !directory.isDirectory()){
            Log.e(FileUtils.class,"deleteFilesAndDirsRecursive(): Null parameter given or not a Directory");
            return;
        }

        deleteFilesAndDirs(directory);
    }
    // recursively called method
    private static void deleteFilesAndDirs(final File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()){
            final File [] filesList = fileOrDirectory.listFiles();
            for (File child : filesList)
                deleteFilesAndDirs(child);
        }

        fileOrDirectory.delete();
    }

    /**
     * Method deletes all files only (but NOT subdirectories) from given directory.
     * @param targetDir - File with represents a Directory where to delete all files
     * @return  true if all OK or false otherwise
     */
    public static void deleteFiles(final File targetDir) {
        if (targetDir==null || !targetDir.isDirectory()){
            Log.e(FileUtils.class,"deleteFiles(): Null parameter given or not a Directory");
            return;
        }
        if (!targetDir.isDirectory())
            return;

        final File [] filesList = targetDir.listFiles();
        for (File file2Delete : filesList)
            if (file2Delete.isFile()) // excluding dirs!
                file2Delete.delete();
    }

    /**
     * Check if given path is a File and if it exists
     * @param path
     * @return boolean
     */
    public static boolean isFileExists(final String path) {
        if (TextUtils.isEmpty(path)){
            Log.e(FileUtils.class,"isFileExists(): Null or Empty parameter given");
            return false;
        }
        final File fileToCheck = new File(path);
        return fileToCheck.exists() && !fileToCheck.isDirectory();
    }

    /**
     * Returns the free space in bytes available at the given file's path
     * or -1 if storage is not available (UNMOUNTED, etc)
     * @return long
     */
    public static long getAvailableSpace(final File file){
        final String mFileRootPath = file.getAbsolutePath();
        return getAvailableSpace(mFileRootPath);
    }

    /**
     * Returns the free space in bytes available at the given file's path
     * or -1 if storage is not available (UNMOUNTED, etc)
     * @return long
     */
    public static long getAvailableSpace(final Uri uri){
        final String mFileRootPath = uri.getPath();
        return getAvailableSpace(mFileRootPath);
    }

    /**
     * Returns the free space in bytes available at the given file's path
     * or -1 if storage is not available (UNMOUNTED, etc)
     * @return long
     */
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static long getAvailableSpace(String mFileRootPath){
        final File file = new File(mFileRootPath);
        if (!file.exists())
            mFileRootPath = file.getParent();

        long availableBytez = 0;
        try {
            final StatFs stat = new StatFs(mFileRootPath);
            stat.restat(mFileRootPath);
            if (DeviceInfo.hasAPI18())
                availableBytez = (stat.getAvailableBlocksLong() * stat.getBlockSizeLong());
            else
                availableBytez = ((long) stat.getAvailableBlocks() * (long) stat.getBlockSize());

//				File path = Environment.getExternalStorageDirectory();
//	            StatFs stat = new StatFs(path.getPath());
//	            long blockSize = stat.getBlockSize();
//	            long totalBlocks = stat.getBlockCount();
//	            long availableBlocks = stat.getAvailableBlocks();
//
//	            mSdSize.setSummary(formatSize(totalBlocks * blockSize));
//	            mSdAvail.setSummary(formatSize(availableBlocks * blockSize) + readOnly);
//
//	            mSdMountToggle.setEnabled(true);
//	            mSdMountToggle.setTitle(mRes.getString(R.string.sd_eject));
//	            mSdMountToggle.setSummary(mRes.getString(R.string.sd_eject_summary));

        } catch (Exception e) {//IllegalArgumentException, NPE
            e.printStackTrace();
            // this can occur if the SD card is removed, but we haven't received the 
            // ACTION_MEDIA_REMOVED Intent yet.
            // status = Environment.MEDIA_REMOVED;
            availableBytez = -1;
        }

        return availableBytez;
    }


    /**
     * returns list of given directory files as String[] 
     * @param directory - File representing directory
     * @param extension - String representing file's extension, e.g. "png" or ".jpeg" (including or excluding starting dot)
     * @return
     */
    public static String[] getFilenamesByExtension(final File directory, final String extension){
        if (directory==null || !directory.isDirectory() || TextUtils.isEmpty(extension)) {
            Log.e(FileUtils.class,"getFilenamesByExtension(): Null, not a directory or empty extension");
            //new IOException("Null not a directory or empty extension").printStackTrace();
            return null;
        }

        final ExtensionFilter filter = new ExtensionFilter(extension.startsWith(".") ? extension : "."+extension);

        return directory.list(filter);
        //return null;
    }

    /**
     * creates File[] containing files located in given directory
     * @param directory - File representing directory
     * @param extension - String representing file's extension, e.g. "png" or ".jpeg" (including or excluding starting dot)
     * @return
     */
    public static File[] getFilesByExtension(final File directory, final String extension){
        if (directory==null || !directory.isDirectory() || TextUtils.isEmpty(extension)){
            Log.e(FileUtils.class,"getFilesByExtension(): Null not a directory or empty extension");
            //new IOException("Null not a directory or empty extension").printStackTrace();
            return null;
        }

//    	String [] listOfFileNames = getFilenamesByExtension(directory, extension);
//    	if (listOfFileNames == null)
//    		return null;

        final ExtensionFilter filter = new ExtensionFilter(extension.startsWith(".") ? extension : "."+extension);
        return  directory.listFiles(filter);
//    	
//    	if (filesList.length==0 )
//    		return new File[]{};
//
////    	File [] filesList = new File[listOfFileNames.length];
//    	for (int i = 0; i < filesList.length; i++) {
//    		filesList[i] = new File(directory.getAbsolutePath()+File.separator+listOfFileNames[i]);
//		}
//    	
//    	return filesList;
    }

//    public static String[] getFilesByExtension(File directory, String ... extensions){
//    	if (directory==null || !directory.isDirectory() || extensions==null || extensions.length==0)
//    		return null;
//    	
//    	
//    	for (String ext : extensions){
//    		ExtensionFilter filter = new ExtensionFilter(".png");
//    	}
//
//    	return directory.list(filter);
//    	//return null;
//    }

    /**
     * Represents extension for filtering for File.list()
     * @author Stan Koshutsky
     *
     */
    public static class ExtensionFilter implements FilenameFilter {

        private final String ext;

        public ExtensionFilter(String ext) {
            this.ext = ext;
        }

        @Override
        public boolean accept(File dir, String filename) {
            return (filename.endsWith(ext));
        }
    }


    public static File getFileFromUri(final Context context, final Uri uri) {
        final String path = getPathFromUri(context, uri);
        if (TextUtils.isEmpty(path))
            return null;
        else
            return new File(path);
    }

    @SuppressLint("NewApi")
    public static String getPathFromUri(final Context context, final Uri uri) {
//    	if (ctx==null || uri==null)
//    		return null;
//    	
//    	final Context context = ctx.getApplicationContext();

        //final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (DeviceInfo.hasAPI(19) && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context
     *            The context.
     * @param uri
     *            The Uri to query.
     * @param selection
     *            (Optional) Filter used in the query.
     * @param selectionArgs
     *            (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(final Context context, final Uri uri, final String selection, final String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(final Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(final Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(final Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static void mergeFiles(final File file1, final File file2, final File outputFile) {
        if (!isReadable(file1) || !isReadable(file2) || !outputFile.exists() && !makeDirsForFile(outputFile)) {
            return;
        }
        try {
            FileInputStream fis1 = new FileInputStream(file1);
            FileInputStream fis2 = new FileInputStream(file2);
            SequenceInputStream sis = new SequenceInputStream(fis1, fis2);
            FileOutputStream fos = new FileOutputStream(outputFile);
            int count;
            byte[] temp = new byte[4096];
            while ((count = sis.read(temp)) != -1) {
                fos.write(temp, 0, count);
            }
            FileUtils.sync(fos);
            fos.close();
            sis.close();
            fis1.close();
            fis2.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}