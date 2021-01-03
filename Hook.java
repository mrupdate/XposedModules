import android.app.ActivityManager;
import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookHuawei {
    public final static String TAG = "antiy: ";
    public static int checkVersionCode = 0;


    public void aaa(XC_LoadPackage.LoadPackageParam loadPackageParam, int checkNum) throws Throwable {
        checkVersionCode = checkNum;
        LoadPackage(loadPackageParam);
    }

    public static void LoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals("android")) {
            return;
        }
//        XposedBridge.log(TAG + "on " + this.getClass().getName() + " handleLoadPackage. The current version: " + checkVersionCode);


        XC_MethodHook.Unhook[] unhook1 = new XC_MethodHook.Unhook[1];
        unhook1[0] = XposedHelpers.findAndHookMethod("class", loadPackageParam.classLoader,
                "method", "param", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Context context = AndroidAppHelper.currentApplication().getApplicationContext();
                PackageManager packageManager = context.getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageInfo("你应用的包名", 0);
                if (Hook.checkVersionCode < packageInfo.versionCode) {
                    unhook1[0].unhook();
                    return;
                }
                // hook code
            }
        });

    }


}
