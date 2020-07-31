package cn.geobeans.biathlon.utils;

import android.widget.Toast;

import cn.geobeans.biathlon.App;

/**
 * Toast 封装
 *
 * @author
 */
public class ToastUtils {

    public static Toast mToast;

    public static void showToast(String msg) {
        try {
            if (mToast == null) {
                mToast = Toast.makeText(App.mContext, "", Toast.LENGTH_SHORT);
            }
            mToast.setText(msg);
            mToast.show();
        } catch (Exception e) {
        }
    }
}
