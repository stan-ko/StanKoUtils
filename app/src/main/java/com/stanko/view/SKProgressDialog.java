package com.stanko.view;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Window;


/**
 * custom ProgressDialog based class with interface
 *  
 * @author Stan Koshutsky
 *
 */
public class SKProgressDialog extends ProgressDialog {

	private ISKProgressDialog callBack;
	
	public <T extends Context & ISKProgressDialog> SKProgressDialog(T context) {
		super(context);
		callBack = context;
		setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	requestWindowFeature(Window.FEATURE_PROGRESS);
    	setOnCancelListener(new Dialog.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (callBack!=null)
					callBack.onProgressCancelled();
			}
		});
	}

	public <T extends Context & ISKProgressDialog> SKProgressDialog(final T context, final String title) {
		this(context);
		super.setTitle(title);
	}
	
	public <T extends Context & ISKProgressDialog> SKProgressDialog(final T context, final String title, final String message) {
		this(context);
		setTitle(title);
		super.setMessage(message);
	}

	@Override
	public void setMessage(CharSequence message) {
		if(isShowing()){
			hide();
			super.setMessage(message);
			show();
		}
		else
			super.setMessage(message);
	}
	

	// callback interface
	public interface ISKProgressDialog {
		void onProgressCancelled();
	}
	
	public void releaseCallBack()
	{
		callBack = null;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		callBack = null;
	}
}

