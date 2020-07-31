/*
 * Copyright 2017 linjiang.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.geobeans.biathlon.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.widget.EdgeEffect;

import java.lang.reflect.Field;

/**
 * https://github.com/whataa
 */
public class Util {
    /*16进制byte数组转String*/
    public static String bytes2HexString(byte[] b) {
        String r = "";

        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            r += hex.toUpperCase();
        }

        return r;
    }

    /*
     * 16进制字符串转字节数组
     */
    public static byte[] hexString2Bytes(String hex) {

        if ((hex == null) || (hex.equals(""))) {
            return null;
        } else if (hex.length() % 2 != 0) {
            return null;
        } else {
            hex = hex.toUpperCase();
            int len = hex.length() / 2;
            byte[] b = new byte[len];
            char[] hc = hex.toCharArray();
            for (int i = 0; i < len; i++) {
                int p = 2 * i;
                b[i] = (byte) (charToByte(hc[p]) << 4 | charToByte(hc[p + 1]));
            }
            return b;
        }

    }

    /*
     * 字符转换为字节
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    /**
     * 字符串转换成十六进制字符串
     *
     * @param String str 待转换的ASCII字符串
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    public static String str2HexStr(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
//            sb.append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * 十六进制转换字符串
     *
     * @param String str Byte字符串(Byte之间无分隔符 如:[616C6B])
     * @return String 对应的字符串
     */
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /**
     * bytes转换成十六进制字符串
     *
     * @param byte[] b byte数组
     * @return String 每个Byte值之间空格分隔
     */
    public static String byte2HexStr(byte[] b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            sb.append(" ");
        }

        //lijp
        try {
            CrashHandler.getInstance().writeFile(sb.toString().toUpperCase().trim()+"\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * bytes字符串转换为Byte值
     *
     * @param String src Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    public static byte[] hexStr2Bytes(String src) {
        int m = 0, n = 0;
        int l = src.length() / 2;
        System.out.println(l);
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            m = i * 2 + 1;
            n = m + 1;
            ret[i] = Byte.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
        }
        return ret;
    }

    /**
     * String的字符串转换成unicode的String
     *
     * @param String strText 全角字符串
     * @return String 每个unicode之间无分隔符
     * @throws Exception
     */
    public static String strToUnicode(String strText)
            throws Exception {
        char c;
        StringBuilder str = new StringBuilder();
        int intAsc;
        String strHex;
        for (int i = 0; i < strText.length(); i++) {
            c = strText.charAt(i);
            intAsc = (int) c;
            strHex = Integer.toHexString(intAsc);
            if (intAsc > 128)
                str.append("\\u" + strHex);
            else // 低位在前面补00
                str.append("\\u00" + strHex);
        }
        return str.toString();
    }

    /**
     * unicode的String转换成String的字符串
     *
     * @param String hex 16进制值字符串 （一个unicode为2byte）
     * @return String 全角字符串
     */
    public static String unicodeToString(String hex) {
        int t = hex.length() / 6;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < t; i++) {
            String s = hex.substring(i * 6, (i + 1) * 6);
            // 高位需要补上00再转
            String s1 = s.substring(2, 4) + "00";
            // 低位直接转
            String s2 = s.substring(4);
            // 将16进制的string转为int
            int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
            // 将int转换为字符
            char[] chars = Character.toChars(n);
            str.append(new String(chars));
        }
        return str.toString();
    }

    /**
     * 代码来自：java.lang.Long
     * 因为要跟踪看变量的值，所以要copy出来，或者是去附加源码，否则 eclipse 调试时查看变量的值会提示 xxx cannot be resolved to a variable
     *
     * @param s
     * @param radix
     * @return
     * @throws NumberFormatException
     * @author 微wx笑
     * @date 2017年12月6日下午5:19:40
     */
    public static long parseLong(String s, int radix) {
        try {
            if (s == null) {
                throw new NumberFormatException("null");
            }

            if (radix < Character.MIN_RADIX) {
                throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
            }
            if (radix > Character.MAX_RADIX) {
                throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
            }

            long result = 0;
            boolean negative = false;
            int i = 0, len = s.length();
            long limit = -Long.MAX_VALUE;
            long multmin;
            int digit;

            if (len > 0) {
                char firstChar = s.charAt(0);
                if (firstChar < '0') { // Possible leading "+" or "-"
                    if (firstChar == '-') {
                        negative = true;
                        limit = Long.MIN_VALUE;
                    } else if (firstChar != '+')

                        if (len == 1) // Cannot have lone "+" or "-"
                            i++;
                }
                multmin = limit / radix;
                while (i < len) {
                    // Accumulating negatively avoids surprises near MAX_VALUE
                    digit = Character.digit(s.charAt(i++), radix);
                    if (digit < 0) {
                    }
                    if (result < multmin) {
                    }
                    result *= radix;
                    if (result < limit + digit) {
                    }
                    result -= digit;
                }
            } else {
            }
            return negative ? result : -result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getIEEEFloatValue(String data) {

        StringBuffer buffer = new StringBuffer();
        StringBuilder endValue = new StringBuilder();

        char[] chars = data.toCharArray();

        for (int i = 0; i < data.length(); i++) {

            buffer.append(hexToBin(chars[i]));

        }


        if (buffer.substring(0, 1).equals("0")) {

//为正

        } else {

            //为负

            endValue.append("-");

        }

        String E = buffer.substring(1, 9);

        String M = buffer.substring(9, buffer.length());

        Integer EDec = Integer.parseInt(E, 2);

        System.out.println("E:" + EDec);

        Integer MDec = Integer.parseInt(M, 2);

        System.out.println("M:" + MDec);

//        指数

//        System.out.println("指数:" + (float)(2 >> (14)));

        System.out.println("指数:" + Math.pow(2, EDec - 127));

        System.out.println("尾数:" + (1.00 + (float) MDec / 8388608.00));

        System.out.println("结果:" + (Math.pow(2, EDec - 127)) * (1.00 + (float) MDec / 8388608.00));

        endValue.append(String.format("%.2f", (Math.pow(2, EDec - 127)) * (1.00 + (float) MDec / 8388608.00)));

        System.out.println("最终结果:" + endValue);


        return endValue.toString();

    }

    private static String hexToBin(char charAt) {

        // TODO Auto-generated method stub

        switch (charAt) {

            case '0':

                return "0000";

            case '1':

                return "0001";

            case '2':

                return "0010";

            case '3':

                return "0011";

            case '4':

                return "0100";

            case '5':

                return "0101";

            case '6':

                return "0110";

            case '7':

                return "0111";

            case '8':

                return "1000";

            case '9':

                return "1001";

            case 'A':

            case 'a':

                return "1010";

            case 'B':

            case 'b':

                return "1011";

            case 'C':

            case 'c':

                return "1100";

            case 'D':

            case 'd':

                return "1101";

            case 'E':

            case 'e':

                return "1110";

            case 'F':

            case 'f':

                return "1111";

        }

        return null;

    }


    public static int getCeil5(float num) {
        boolean isNegative = num < 0;
        return (((int) ((isNegative ? -num : num) + 2.9f)) / 3 * 3) * (isNegative ? -1 : 1);
    }

    public  static float calcTextSuitBaseY(RectF rectF, Paint paint) {
        return rectF.top + rectF.height() / 2 -
                (paint.getFontMetrics().ascent + paint.getFontMetrics().descent) / 2;
    }

    public  static float size2sp(float sp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                sp, context.getResources().getDisplayMetrics());
    }

    public  static int dip2px(float dipValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public  static float getTextHeight(Paint textPaint) {
        return -textPaint.ascent() - textPaint.descent();
    }

    public  static void trySetColorForEdgeEffect(EdgeEffect edgeEffect, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            edgeEffect.setColor(color);
            return;
        }
        try {
            Field edgeField = EdgeEffect.class.getDeclaredField("mEdge");
            edgeField.setAccessible(true);
            Drawable mEdge = (Drawable) edgeField.get(edgeEffect);
            mEdge.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            mEdge.setCallback(null);
            Field glowField = EdgeEffect.class.getDeclaredField("mGlow");
            glowField.setAccessible(true);
            Drawable mGlow = (Drawable) glowField.get(edgeEffect);
            mGlow.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            mGlow.setCallback(null);
        } catch (Exception ignored) {
        }
    }

    public static int tryGetStartColorOfLinearGradient(LinearGradient gradient) {
        try {
            Field field = LinearGradient.class.getDeclaredField("mColors");
            field.setAccessible(true);
            int[] colors = (int[]) field.get(gradient);
            return colors[0];
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Field field = LinearGradient.class.getDeclaredField("mColor0");
                field.setAccessible(true);
                return (int) field.get(gradient);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return Color.BLACK;
    }

    /**
     * 返回当前程序版本号
     */
    public static String getAppVersionCode(Context context) {
        int versioncode = 0;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            // versionName = pi.versionName;
            versioncode = pi.versionCode;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versioncode + "";
    }

    /**
     * 返回当前程序版本名
     */
    public static String getAppVersionName(Context context) {
        String versionName=null;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }
}
