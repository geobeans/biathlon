package cn.geobeans.biathlon.utils;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

/**
 * @Author: baixm
 * @Date: 2020/1/2
 */
public class MyClickListener implements View.OnTouchListener {
    private long firstClickTime;
    private long secondClickTime;
    private long stillTime;
    private boolean isUp = false;
    private boolean isDoubleClick = false;
    private MyClickCallBack myClickCallBack;

    public interface MyClickCallBack {
        void oneClick();//点击一次的回调

        void doubleClick();//连续点击两次的回调

        void longClick();//长按

    }


    public MyClickListener(MyClickCallBack myClickCallBack) {
        this.myClickCallBack = myClickCallBack;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isUp = false;
                if (firstClickTime == 0 & secondClickTime == 0) {//第一次点击
                    firstClickTime = System.currentTimeMillis();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!isUp) {
                                myClickCallBack.longClick();
                                firstClickTime = 0;
                                secondClickTime = 0;
                                isDoubleClick = false;
                            } else {
                                if (!isDoubleClick) {
                                    myClickCallBack.oneClick();
                                }
                                isDoubleClick = false;
                                firstClickTime = 0;
                                secondClickTime = 0;
                            }
                        }
                    }, 300);

                } else {
                    secondClickTime = System.currentTimeMillis();
                    stillTime = secondClickTime - firstClickTime;
                    if (stillTime < 400) {//两次点击小于0.5秒
                        myClickCallBack.doubleClick();
                        isDoubleClick = true;
                        firstClickTime = 0;
                        secondClickTime = 0;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isUp = true;
                break;
        }
        return true;
    }
}
