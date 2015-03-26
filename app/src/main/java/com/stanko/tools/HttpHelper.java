package com.stanko.tools;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpHelper {

	public static long getContentSize(final String sUrl) {
		long lContentSize = 0;
		try {
			final URL url = new URL(sUrl);
			HttpURLConnection ucon = (HttpURLConnection) url.openConnection();
			ucon.connect();
			final String sContentLength = ucon.getHeaderField("content-length");
			ucon.disconnect();
			lContentSize = Long.parseLong(sContentLength);
		} catch (MalformedURLException e){
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return lContentSize;
	}
}