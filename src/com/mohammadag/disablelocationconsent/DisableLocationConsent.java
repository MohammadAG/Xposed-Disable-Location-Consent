package com.mohammadag.disablelocationconsent;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class DisableLocationConsent implements IXposedHookLoadPackage {
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		// Android 4.4.x (KitKat) or higher
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			if (lpparam.packageName.equals("com.google.android.gms")) {
				final Class<?> NetworkLocationService = XposedHelpers.findClass("com.google.android.location.network.NetworkLocationService", lpparam.classLoader);
				XposedHelpers.findAndHookMethod("com.google.android.location.network.NetworkLocationService", lpparam.classLoader, "a", Context.class, new XC_MethodReplacement() {
					@Override
					protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
						Context context = (Context)param.args[0];
						Cursor cursor = null;
						int networkLocationOptIn = 0;
						try {
							cursor = context.getContentResolver().query(Uri.parse("content://com.google.settings/partner"), new String[] { "value" }, "name=?", new String[] { "network_location_opt_in" }, null);
							if (cursor != null && cursor.moveToNext()) {
								networkLocationOptIn = cursor.getInt(0);
							}
						}
						catch (Exception e) {
							return null;
						}
						finally {
							if (cursor != null) {
								cursor.close();
							}
							if (networkLocationOptIn == 0) {
								XposedHelpers.callStaticMethod(NetworkLocationService, "a", context, true);
							}
							
						}
						return null;
					}
				});
			}
		}
		// Android 4.3.x (Jelly Bean) or lower
		else if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			if (lpparam.packageName.equals("com.google.android.location")) {
				final Class<?> NetworkLocationProvider = XposedHelpers.findClass("com.google.android.location.NetworkLocationProvider", lpparam.classLoader);
				XposedHelpers.findAndHookMethod("com.google.android.location.NetworkLocationProvider", lpparam.classLoader, "applySettings", new XC_MethodReplacement() {
					@Override
					protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
						Context context = (Context)XposedHelpers.getStaticObjectField(param.thisObject.getClass(), "context");
						Cursor cursor = null;
						int networkLocationOptIn = 0;
						try {
							cursor = context.getContentResolver().query(Uri.parse("content://com.google.settings/partner"), new String[] { "value" }, "name=?", new String[] { "network_location_opt_in" }, null);
							if (cursor != null && cursor.moveToNext()) {
								networkLocationOptIn = cursor.getInt(0);
							}
						}
						catch (Exception e) {
							return null;
						}
						finally {
							if (cursor != null) {
								cursor.close();
							}
							if (networkLocationOptIn == 0) {
								Object NetworkLocationProviderInstance = XposedHelpers.callStaticMethod(NetworkLocationProvider, "getInstance");
								XposedHelpers.callMethod(NetworkLocationProviderInstance, "userConfirmedEnable", true);
							}
							
						}
						return null;
					}
				});
			}
		}

	}
}
