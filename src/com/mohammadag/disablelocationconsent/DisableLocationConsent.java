package com.mohammadag.disablelocationconsent;

import android.content.Context;
import android.location.LocationManager;
import android.os.Build;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class DisableLocationConsent implements IXposedHookLoadPackage {
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			if (lpparam.packageName.equals("com.google.android.gms")) {
				final Class<?> NetworkLocationService = XposedHelpers.findClass("com.google.android.location.network.NetworkLocationService", lpparam.classLoader);

				XposedHelpers.findAndHookMethod("com.google.android.location.network.NetworkLocationService", lpparam.classLoader, "a", Context.class, new XC_MethodReplacement() {
					@Override
					protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
						Context context = (Context) param.args[0];

						LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
						boolean networkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

						XposedHelpers.callStaticMethod(NetworkLocationService, "b", context, networkProviderEnabled);

						return null;
					}
				});
			}
		} else if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			if (lpparam.packageName.equals("com.google.android.location")) {
				final Class<?> NetworkLocationProvider = XposedHelpers.findClass("com.google.android.location.NetworkLocationProvider", lpparam.classLoader);
				String hookedMethodName;

				if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
					hookedMethodName = "applySettings";
				} else {
					hookedMethodName = "handleEnable";
				}

				XposedHelpers.findAndHookMethod("com.google.android.location.NetworkLocationProvider", lpparam.classLoader, hookedMethodName, new XC_MethodReplacement() {
					@Override
					protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
						Context context = (Context) XposedHelpers.getStaticObjectField(param.thisObject.getClass(), "context");

						if (context == null) {
							return null;
						}

						Object NetworkLocationProviderInstance = XposedHelpers.callStaticMethod(NetworkLocationProvider, "getInstance");

						if (NetworkLocationProviderInstance == null) {
							return null;
						}

						LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
						boolean networkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

						XposedHelpers.callMethod(NetworkLocationProviderInstance, "setUserConfirmedPreference", networkProviderEnabled);

						return null;
					}
				});
			}
		}

	}
}
