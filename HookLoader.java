import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.io.File;

import dalvik.system.PathClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookHuaweiLoader implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    public final static String TAG = "antiy: ";
    private final static String modulePackageName = HookLoader.class.getPackage().getName();
    private final String handleHookClass = Hook.class.getName();
    private final String testMethod = "aaa";


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {


        if (!loadPackageParam.packageName.equals("android")) {
            return;
        }
        XposedBridge.log(TAG + "start hook");
        XposedHelpers.findAndHookMethod("com.android.server.am.ActivityManagerService", loadPackageParam.classLoader,
                "broadcastIntentLocked",
                "com.android.server.am.ProcessRecord", "java.lang.String", "android.content.Intent",
                "java.lang.String", "android.content.IIntentReceiver", int.class, "java.lang.String", "android.os.Bundle",
                "java.lang.String[]", int.class, "android.os.Bundle", boolean.class, boolean.class, int.class, int.class,
                int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        if (param.args[2].toString().contains("android.intent.action.PACKAGE_ADDED") && param.args[2].toString().contains("你应用的包名") &&
                        !param.args[2].toString().contains("com.android.vending")) {

                            XposedBridge.log(TAG + "检测到 " + this.getClass().getPackage().getName() + " 安装，正在免重启加载模块。 ");

                            Context context = AndroidAppHelper.currentApplication().getApplicationContext();
                            Class<?> cls = getApkClass(context, handleHookClass);
                            Object instance = cls.newInstance();
                            PackageManager packageManager = context.getPackageManager();
                            PackageInfo packageInfo = packageManager.getPackageInfo("你应用的包名", 0);

                            cls.getDeclaredMethod(testMethod, loadPackageParam.getClass(), int.class)
                                    .invoke(instance, loadPackageParam, packageInfo.versionCode);

                            XposedBridge.log(TAG + "==== 免重启加载完成 =================");
                        }

                    }
                });
    }


    /**
     * @param context
     * @param handleHookClass
     * @return HookTest.class
     * @throws Throwable
     */
    private Class<?> getApkClass(Context context, String handleHookClass) throws Throwable {
        File apkFile = null;
        if (context == null) {
            return null;
        }
        try {
            Context moudleContext = context.createPackageContext(HookHuaweiLoader.modulePackageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            String apkPath = moudleContext.getPackageCodePath();
            apkFile = new File(apkPath);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        PathClassLoader pathClassLoader = new PathClassLoader(apkFile.getAbsolutePath(), XposedBridge.BOOTCLASSLOADER);
        return Class.forName(handleHookClass, true, pathClassLoader);
    }
}
