package com.mohammadag.disablelocationconsent;

import android.app.Activity;
import android.os.Bundle;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class DisableLocationConsent implements IXposedHookLoadPackage {
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		// Google, I kindly ask you not to obfuscate this package.
		// I know your intentions of including this consent dialog is to make the
		// user aware that they're sharing their location with Google's servers.
		// However, any user installing this already knows that, and agrees to 
		// Google's consent, they just don't want to accept the dialog every time
		// they toggle a simple checkbox in settings.
		if (lpparam.packageName.equals("com.google.android.location")) {
			final Class<?> NetworkLocationProvider = XposedHelpers.findClass("com.google.android.location.NetworkLocationProvider", lpparam.classLoader);

			XposedHelpers.findAndHookMethod("com.google.android.location.ConfirmAlertActivity", lpparam.classLoader,
					"onCreate", Bundle.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Activity activity = (Activity) param.thisObject;
					XposedHelpers.setBooleanField(param.thisObject, "mAgreed", true);

					Object NetworkLocationProviderInstance = XposedHelpers.callStaticMethod(NetworkLocationProvider, "getInstance");
					XposedHelpers.callMethod(NetworkLocationProviderInstance, "userConfirmedEnable", true);

					activity.finish();
				}
			});
		}
	}
}
