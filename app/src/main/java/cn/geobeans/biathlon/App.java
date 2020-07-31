package cn.geobeans.biathlon;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.Display;

import com.android.tony.defenselib.handler.IExceptionHandler;

import cn.geobeans.biathlon.utils.CrashHandler;
import cn.geobeans.biathlon.utils.PreferencesUtil;

/**
 * @Author: baixm
 * @Date: 2019/11/25
 */
public class App extends Application implements IExceptionHandler {
    public static final String DD_KEY = "DD_KEY";
    public static Context mContext;
    public static final String TIME_KEY = "TIME_KEY";
    public static final String SX_KEY = "SX_KEY";
    public static final String XX_KEY = "XX_KEY";
    public static final String ANGLE_KEY = "ANGLE_KEY";
    public static final int[] angle = {189, 90, 40};
    public static final int[] time = {60, 120, 180};
//    public static final String[] ddItems = {"内蒙古", "长春", "崇礼"};
    public static final String[] ddItems = {"崇礼比赛场", "崇礼训练场", "阿尔山训练场"};
    public static final String[] timeItems = {"1分钟", "2分钟", "3分钟"};

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        CrashHandler.getInstance().init(this);
        PreferencesUtil.getInstance().init(this);
    }

    public static int getScreenInch(Activity context) {

        Display display = context.getWindowManager().getDefaultDisplay();
        return display.getHeight();
    }


    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // step1: Initialize the lib.
//        DefenseCrash.initialize();
//        // setp2: Install the fire wall defense.
//        DefenseCrash.install(this);
    }

    @Override
    public void onCaughtException(Thread thread, Throwable throwable, boolean isSafeMode) {
        // step3: Print the error when crashed during runtime.
        Log.e("onCaughtException: ", "onCaughtException");
        throwable.printStackTrace();
        // step4: Upload this throwable to your crash collection sdk.
    }

    @Override
    public void onMayBeBlackScreen(Throwable throwable) {
        Log.e("onMayBeBlackScreen: ", "onMayBeBlackScreen");
        throwable.printStackTrace();
        // onLayout(),onMeasure() or onDraw() has breaks down,
        // it causes the drawing to be abnormal and the choreographer to break down.
        // We will notify you on this method,you’d better finish this activity or restart the application.
    }

    @Override
    public void onEnterSafeMode() {
        // We enter the safe mode to keep the main looper loop after crashed.You’d better do nothing here,we just notify you.
    }
}
