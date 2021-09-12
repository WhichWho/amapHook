package com.cnl.amaphook;

import android.os.SystemClock;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.cnl.amaphook.HookData.data;

public class XposedMain implements IXposedHookLoadPackage {

    private static final String TAG = "Hooklearnzz:   ";
    private float degree = 0;

    private void log(String s) {
        XposedBridge.log("======");
        XposedBridge.log("------");
        XposedBridge.log(TAG + s);
        XposedBridge.log("------");
        XposedBridge.log("======");
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if ("com.ledreamer.zz".equals(lpparam.packageName)) {
            log("hook成功");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //等待脱壳结束
                    SystemClock.sleep(2000);
                    start(lpparam);
                }
            }).start();
        }
    }


    private long startTime = 0;
    private long repeatTime = 0;
    private double lastX = 0;
    private double lastY = 0;

    public void start(XC_LoadPackage.LoadPackageParam lpparam) {
        startTime = System.currentTimeMillis();
        repeatTime = (long) data[data.length - 1][0];
        lastX = data[0][1];
        lastY = data[0][2];
        //经度
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation",
                lpparam.classLoader, "getLongitude", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        if(System.currentTimeMillis() - startTime < 30 * 1000){
//                            XposedBridge.log("经度: " + param.getResult());
//                            param.setResult(param.getResult());
//                            return;
//                        }
                        long t = (System.currentTimeMillis() - startTime) % repeatTime;
                        for (double[] datum : data) {
                            if (t <= datum[0]) {
                                lastX = datum[1] + getDynamic();
                                param.setResult(lastY);
                                break;
                            }
                        }
                        param.setResult(lastX);
                    }
                });
        //纬度
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation",
                lpparam.classLoader, "getLatitude", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//                        if(System.currentTimeMillis() - startTime < 30 * 1000){
//                            XposedBridge.log("纬度: " + param.getResult());
//                            param.setResult(param.getResult());
//                            return;
//                        }
                        long t = (System.currentTimeMillis() - startTime) % repeatTime;
                        for (double[] datum : data) {
                            if (t <= datum[0]) {
                                lastY = datum[2] + getDynamic();
                                param.setResult(lastY);
                                break;
                            }
                        }
                        param.setResult(lastY);
                    }
                });
        //速度 2m/s - 3m/s
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation",
                lpparam.classLoader, "getSpeed", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        param.setResult((float)(2f + Math.random()));
                    }
                });
        //角度 0-360
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation",
                lpparam.classLoader, "getBearing", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        degree += (Math.random() - 0.5f) * 10 + 360;
                        degree %= 360;
                        param.setResult(degree);
                    }
                });
    }

    //+- 0.000015;
    private double getDynamic() {
        return (Math.random() - 0.5f) * 0.00003f;
    }
}
