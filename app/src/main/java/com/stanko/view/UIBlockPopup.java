package com.stanko.view;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import com.stanko.R;
import com.stanko.tools.Log;

/*
 * используется для временного блокирования UI от действий юзера, 
 * но не блокирования main UI thread. Просто напяливает поверх 
 * всего экрана невидимый layout. Возможно ActionBar останется при делах...
 */

public class UIBlockPopup {

	private PopupWindow popupWindow;
	//private View popupView; 
	
	private boolean isVisible;
	public boolean isVisible(){
		return isVisible;
	}
	
	private Context context;

	protected LayoutInflater inflater;
	private LinearLayout contentLayout;
	
	private View rootView;
	private ProgressBar pbar;
	
	public UIBlockPopup(Activity activity){
		this.context = activity;
		this.rootView = ((ViewGroup)activity.findViewById(android.R.id.content)).getChildAt(0);
		this.inflater = activity.getLayoutInflater();
		pbar =  new ProgressBar(activity, null, android.R.attr.progressBarStyleLargeInverse);
	}

	public void show() {
		if (popupWindow!=null && popupWindow.isShowing())
			return;
			
		contentLayout = new LinearLayout(context);
		contentLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		contentLayout.setOrientation(LinearLayout.VERTICAL);
		contentLayout.setGravity(Gravity.CENTER);
//		contentLayout.setBackgroundResource(R.drawable.popup_outer);
		
		// клик мимо кликабельного объекта - выход
//		contentLayout.setClickable(true);
//		contentLayout.setOnClickListener(this);
//		//contentLayout.setOnTouchListener(menuSwipeListener);
		contentLayout.addView(pbar, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		//pbar.showContextMenu()
		//contentLayout.addView(inflater.inflate(R.layout.calendar_popup, null, false), LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		popupWindow = new PopupWindow(contentLayout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
		
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

//		popupWindow.showAtLocation(context.findViewById(contextRootViewId), Gravity.CENTER, 0, 0);
		popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
		
		//popupView = popupWindow.getContentView();
		
	
		contentLayout.setBackgroundResource(R.drawable.popup_outer);
		
		popupWindow.setFocusable(true);

		isVisible = true;
	}
	
	
	public void dismissPopup(){
		
		if (!isVisible)
			return;
		
		isVisible = false;
		
		pbar.setVisibility(View.GONE);
		
		popupWindow.dismiss();
		
		contentLayout.removeAllViews();

//		this.context = null;
//		this.listener = null;
//		this.rootView = null;
//		this.inflater = null;
	}


	protected void processCalendarPopupChoice(final Date date) {
		dismissPopup();
		
		Log.i(this,"Choice: "+date);

	}


	@Override
	protected void finalize() throws Throwable {
		this.context = null;
		this.rootView = null;
		this.inflater = null;		
		super.finalize();
	}

}
