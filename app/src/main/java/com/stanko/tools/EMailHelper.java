package com.stanko.tools;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EMailHelper {
	
	public static boolean isValidEmail(String email) {
	    String expression = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
	    
	    Pattern p = Pattern.compile(expression);
	    Matcher m = p.matcher(email);
	    
	    return m.matches();
	}

	private final static String[] knownPackages = new String[]{
			"com.google.android.gm",
			"com.google.android.apps.inbox",
			"com.appple.app.email",
			"ru.mail.mailapp",
			"com.microsoft.office.outlook",
			"com.my.mail",
			"com.yahoo.mobile.client.android.mail",
			"com.cloudmagic.mail",
			"cz.seznam.email",
			"com.email.email",
			"com.trtf.blue",
			"me.bluemail.mail",
			"com.fsck.k9",
			"com.mailboxapp",
			"com.syntomo.email",
			"org.kman.aquamail",
			"com.mobincube.android.sc_15ibz",
			"ru.yandex.mail",
			"net.daum.android.solmail",
			"com.boxer.email",
			"com.aol.mobile.aolapp",
			"com.mobincube.android.sc_gaz7l",
			"com.xiaomistudio.tools.finalmail",
			"com.mail.emails",
			"co.itspace.emailproviders",
			"de.gmx.mobile.android.mail",
			"com.yahoo.mobile.client.android.im",
			"com.mail.mobile.android.mail",
			"com.asus.email",
			"com.maildroid",
			"com.wemail",
			"de.web.mobile.android.mail",
			"com.onegravity.k10.free",
			"com.dicklucifer.email",
			"de.freenet.mail",
			"com.qs.enhancedemail",
			"com.feistapps.anonymousemail",
			"com.onegravity.k10.pro2",
			"com.gloxandro.birdmail",
			"com.kaitenmail",
			"com.android.email",
			"com.sec.android.email",
			"com.htc.android.mail"};

	private static boolean applyKnownPackage(final Context context, final Intent emailIntent) {
		final List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(emailIntent, 0);
		if (resInfo.size() == 0)
			return false;
		if (resInfo.size() == 1) {
			emailIntent.setPackage(resInfo.get(0).activityInfo.packageName);
			return true;
		}
		for (ResolveInfo info : resInfo) {
			for (String sharePackage : knownPackages) {
				if (info.activityInfo.packageName.toLowerCase(Locale.US).contains(sharePackage)
						|| info.activityInfo.name.toLowerCase(Locale.US).contains(sharePackage)) {
					emailIntent.setPackage(info.activityInfo.packageName);
					return true;
				}
			}
		}
		return true;
	}




	private static String EMH_DEFAULT_PICKER_TITLE = "Send EMail using:";
	private static String EMH_DEFAULT_SECURITY_EXCEPTION_ERROR_MESSAGE = "Sending EMail has been forbidden by permissions";
	private static String EMH_DEFAULT_NO_ASSOCIATED_APP_ERROR_MESSAGE = "No EMail app has been found";

	/**
	 * Starts EMail intent with filled by given receiver, subject and text
	 *
	 * @param context
	 * @param receiver
	 * @param subject
	 * @param text
	 */
	public static void sendEmail(Context context,
								 final String receiver, //TODO: String[] of receivers
								 final String subject,
								 final String text,
								 String pickerTitle,
								 String securityErrorMessage,
								 String noAssociatedAppErrorMessage
								 )
	{
		if (TextUtils.isEmpty(pickerTitle))
			pickerTitle = EMH_DEFAULT_PICKER_TITLE;
		if (TextUtils.isEmpty(securityErrorMessage))
			securityErrorMessage = EMH_DEFAULT_SECURITY_EXCEPTION_ERROR_MESSAGE;
		if (TextUtils.isEmpty(noAssociatedAppErrorMessage))
			noAssociatedAppErrorMessage = EMH_DEFAULT_NO_ASSOCIATED_APP_ERROR_MESSAGE;


		final Intent emailIntent = new Intent(Intent.ACTION_SEND);
		if (!TextUtils.isEmpty(receiver))
			emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{receiver});
		if (!TextUtils.isEmpty(subject))
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		if (!TextUtils.isEmpty(text))
			emailIntent.putExtra(Intent.EXTRA_TEXT, text);
		emailIntent.setData(Uri.parse("mailto:"));
//        emailIntent.setType("text/plain");
		emailIntent.setType("message/rfc822");

		if (!applyKnownPackage(context, emailIntent)) {
			Toast.makeText(context, noAssociatedAppErrorMessage, Toast.LENGTH_LONG).show();
			return;
		}

		try {
			context.startActivity(Intent.createChooser(emailIntent, pickerTitle));
		} catch (ActivityNotFoundException e) {
			Toast.makeText(context, noAssociatedAppErrorMessage, Toast.LENGTH_LONG).show();
		} catch (SecurityException e) {
			Toast.makeText(context, securityErrorMessage, Toast.LENGTH_LONG).show();
		}
	}
//
//	public static void shareMessage(Context context, final String subject, final String text, final File... files) {
//		final Intent sharingIntent;
//		if (files != null && files.length > 0) {
//			sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
//			ArrayList<Uri> uris = new ArrayList<Uri>();
//			//convert from files to Android friendly Parcelable Uri's
//			for (File file : files)
//				uris.add(Uri.fromFile(file));
//			sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
//		} else
//			sharingIntent = new Intent(Intent.ACTION_SEND);
//		if (!TextUtils.isEmpty(subject))
//			sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
//		if (!TextUtils.isEmpty(text))
//			sharingIntent.putExtra(Intent.EXTRA_TEXT, text);
//		sharingIntent.setType("audio/*");
//
//		try {
//			context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.share_msg_chooser_label)));
//		} catch (ActivityNotFoundException e) {
//			Toast.makeText(context, R.string.toast_no_email_client_intent, Toast.LENGTH_LONG).show();
//		} catch (SecurityException e) {
//			Toast.makeText(context, R.string.toast_email_client_intent_not_allowed, Toast.LENGTH_LONG).show();
//			return;
//		}
//	}

	/**
	 * Starts EMail intent with filled by given receiver email and R.string.ids: subject and text
	 *
	 * @param context
	 * @param receiver - Receiver EMail address or addresses
	 * @param subjectResId - EMail subject string Resource Id (R.string.xxx)
	 * @param textResId - EMail text/body string Resource Id (R.string.xxx)
	 */
	public static void sendEmail(Context context,
								 final String receiver,
								 final int subjectResId,
								 final int textResId
	) {
		final String subject = subjectResId > 0 ? context.getString(subjectResId) : null;
		final String text = textResId > 0 ? context.getString(textResId) : null;
		sendEmail(context, receiver, subject, text, EMH_DEFAULT_PICKER_TITLE, EMH_DEFAULT_SECURITY_EXCEPTION_ERROR_MESSAGE, EMH_DEFAULT_NO_ASSOCIATED_APP_ERROR_MESSAGE);
	}

	/**
	 * Starts EMail intent with filled by given receiver email and R.string.ids: subject and text
	 *
	 * @param context
	 * @param receiver
	 * @param subjectResId
	 * @param textResId
	 */
	public static void sendEmail(Context context,
								 final String receiver,
								 final int subjectResId,
								 final int textResId,
								 final int pickerTitleResId,
								 final int securityErrorResId,
								 final int noAssociatedAppErrorResId
	) {
		final String subject = subjectResId > 0 ? context.getString(subjectResId) : null;
		final String text = textResId > 0 ? context.getString(textResId) : null;
		final String pickerTitle = pickerTitleResId > 0 ? context.getString(textResId) : EMH_DEFAULT_PICKER_TITLE;
		final String securityError = securityErrorResId > 0 ? context.getString(textResId) : EMH_DEFAULT_SECURITY_EXCEPTION_ERROR_MESSAGE;
		final String noAssociatedAppError = noAssociatedAppErrorResId > 0 ? context.getString(textResId) : EMH_DEFAULT_NO_ASSOCIATED_APP_ERROR_MESSAGE;
		sendEmail(context, receiver, subject, text, pickerTitle, securityError, noAssociatedAppError);
	}

	/**
	 * Starts EMail intent with filled by given R.string.ids: subject and text
	 *
	 * @param context
	 * @param subjectResId
	 * @param textResId
	 */
	public static void sendEmail(Context context, final int subjectResId, final int textResId) {
		final String subject = subjectResId > 0 ? context.getString(subjectResId) : null;
		final String text = textResId > 0 ? context.getString(textResId) : null;
		sendEmail(context, null, subject, text, EMH_DEFAULT_PICKER_TITLE, EMH_DEFAULT_SECURITY_EXCEPTION_ERROR_MESSAGE, EMH_DEFAULT_NO_ASSOCIATED_APP_ERROR_MESSAGE);
	}

	/**
	 * Starts EMail intent with filled by given R.string.ids: email address, subject and text
	 *
	 * @param context
	 * @param emailResId
	 * @param subjectResId
	 * @param textResId
	 */
	public static void sendEmail(Context context,
								 final int emailResId,
								 final int subjectResId,
								 final int textResId,
								 final int pickerTitleResId,
								 final int securityErrorResId,
								 final int noAssociatedAppErrorResId) {
		final String email = emailResId > 0 ? context.getString(emailResId) : null;
		final String subject = subjectResId > 0 ? context.getString(subjectResId) : null;
		final String text = textResId > 0 ? context.getString(textResId) : null;
		final String pickerTitle = pickerTitleResId > 0 ? context.getString(textResId) : EMH_DEFAULT_PICKER_TITLE;
		final String securityError = securityErrorResId > 0 ? context.getString(textResId) : EMH_DEFAULT_SECURITY_EXCEPTION_ERROR_MESSAGE;
		final String noAssociatedAppError = noAssociatedAppErrorResId > 0 ? context.getString(textResId) : EMH_DEFAULT_NO_ASSOCIATED_APP_ERROR_MESSAGE;
		sendEmail(context, email, subject, text, pickerTitle, securityError, noAssociatedAppError);
	}

	/**
	 * Starts EMail intent with filled by given R.string.ids: email address, subject and text
	 *
	 * @param context
	 * @param emailResId
	 * @param subjectResId
	 * @param textResId
	 */
	public static void sendEmail(Context context, final int emailResId, final int subjectResId, final int textResId) {
		final String email = emailResId > 0 ? context.getString(emailResId) : null;
		final String subject = subjectResId > 0 ? context.getString(subjectResId) : null;
		final String text = textResId > 0 ? context.getString(textResId) : null;
		sendEmail(context, email, subject, text, EMH_DEFAULT_PICKER_TITLE, EMH_DEFAULT_SECURITY_EXCEPTION_ERROR_MESSAGE, EMH_DEFAULT_NO_ASSOCIATED_APP_ERROR_MESSAGE);
	}






	// mail with attaches
	public static void sendEmail(Context context,
								 final String receiver,
								 final String subject,
								 final String text,
								 final File... files) {
		sendEmail(context,
				new String[]{receiver},
				subject,
				text,
				EMH_DEFAULT_PICKER_TITLE,
				EMH_DEFAULT_SECURITY_EXCEPTION_ERROR_MESSAGE,
				EMH_DEFAULT_NO_ASSOCIATED_APP_ERROR_MESSAGE,
				files);
	}

	public static void sendEmail(Context context,
								 final String receiver,
								 final String subject,
								 final String text,
								 final String pickerTitle,
								 final String securityErrorMessage,
								 final String noAssociatedAppErrorMessage,
								 final File... files) {
		sendEmail(context, new String[]{receiver}, subject, text, pickerTitle, securityErrorMessage, noAssociatedAppErrorMessage, files);
	}

	public static void sendEmail(Context context,
								 final String[] receivers,
								 final String subject,
								 final String text,
								 String pickerTitle,
								 String securityErrorMessage,
								 String noAssociatedAppErrorMessage,
								 final File... files)
	{
		if (TextUtils.isEmpty(pickerTitle))
			pickerTitle = EMH_DEFAULT_PICKER_TITLE;
		if (TextUtils.isEmpty(securityErrorMessage))
			securityErrorMessage = EMH_DEFAULT_SECURITY_EXCEPTION_ERROR_MESSAGE;
		if (TextUtils.isEmpty(noAssociatedAppErrorMessage))
			noAssociatedAppErrorMessage = EMH_DEFAULT_NO_ASSOCIATED_APP_ERROR_MESSAGE;

		final Intent emailIntent;
		if (files != null && files.length > 0) {
			emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			final ArrayList<Uri> uris = new ArrayList<Uri>();
			//convert from files to Android friendly Parcelable Uri's
			for (File file : files)
				uris.add(Uri.fromFile(file));
			emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		} else
			emailIntent = new Intent(Intent.ACTION_SEND);
		if (receivers != null && receivers.length > 0)
			emailIntent.putExtra(Intent.EXTRA_EMAIL, receivers);
		if (!TextUtils.isEmpty(subject))
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		if (!TextUtils.isEmpty(text))
			emailIntent.putExtra(Intent.EXTRA_TEXT, text);
		emailIntent.setData(Uri.parse("mailto:"));
//        emailIntent.setType("text/plain");
		emailIntent.setType("message/rfc822");

		if (!applyKnownPackage(context, emailIntent)) {
			Toast.makeText(context, noAssociatedAppErrorMessage, Toast.LENGTH_LONG).show();
			return;
		}

		try {
			context.startActivity(Intent.createChooser(emailIntent, pickerTitle));
		} catch (ActivityNotFoundException e) {
			Toast.makeText(context, noAssociatedAppErrorMessage, Toast.LENGTH_LONG).show();
		} catch (SecurityException e) {
			Toast.makeText(context, securityErrorMessage, Toast.LENGTH_LONG).show();
		}
	}




}
