package cn.geobeans.biathlon.utils;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;

/**
 * @Author: baixm
 * @Date: 2019/12/11
 */
public class NumRangeInputFilter implements InputFilter {

    // 只允许输入数字和小数点
//    private static final String REGEX = "([0-9]|\\.)*";
    public static final String REGEX = "([0-9]\\d*\\.?\\d*)|((-)?[0-9]\\d*\\.?\\d*)";
    // 输入的最大
    private int MAX_VALUE = 6;
    // 输入的最小
    private int MIN_VALUE = -6;
    // 小数点后的位数
    private static final int POINTER_LENGTH = 2;

    private static final String POINTER = ".";

    private static final String ZERO_ZERO = "00";

    public NumRangeInputFilter(int maxValue, int minValue) {
        MAX_VALUE = maxValue;
        MIN_VALUE = minValue;
    }

    public NumRangeInputFilter() {
    }

    /**
     * @param source 新输入的字符串
     * @param start  新输入的字符串起始下标，一般为0
     * @param end    新输入的字符串终点下标，一般为source长度-1
     * @param dest   输入之前文本框内容
     * @param dstart 原内容起始坐标，一般为0
     * @param dend   原内容终点坐标，一般为dest长度-1
     * @return 输入内容
     */

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String sourceText = source.toString();
        String destText = dest.toString();

        // 新输入的字符串为空（删除剪切等）
        if (TextUtils.isEmpty(sourceText)) {
            return "";
        }

        // 拼成字符串
        String temp = destText.substring(0, dstart)
                + sourceText.substring(start, end)
                + destText.substring(dend, dest.length());

        // 纯数字加小数点
        if (!temp.equals("-")) {
            if (!temp.matches(REGEX)) {
                ToastUtils.showToast("请输入" + MIN_VALUE + "到" + MAX_VALUE + "之间的正确数字");
                return dest.subSequence(dstart, dend);
            }
            // 小数点的情况
            if (temp.contains(POINTER)) {
                // 第一位就是小数点
                if (temp.startsWith(POINTER)) {
                    ToastUtils.showToast("第一位就是小数点");
                    return dest.subSequence(dstart, dend);
                }
                // 不止一个小数点
                if (temp.indexOf(POINTER) != temp.lastIndexOf(POINTER)) {
                    ToastUtils.showToast("不止一个小数点");
                    return dest.subSequence(dstart, dend);
                }
            }

            double sumText = Double.parseDouble(temp);
            if (sumText > MAX_VALUE) {
                // 超出最大值
                ToastUtils.showToast("最大值" + MAX_VALUE);
                return dest.subSequence(dstart, dend);
            }
            if (sumText > 0 && temp.length() > 3 && sumText < MIN_VALUE) {
                // 超出最大值
                ToastUtils.showToast("最小值" + MIN_VALUE);
                return dest.subSequence(dstart, dend);
            } else if (sumText < 0 && sumText < MIN_VALUE) {
                ToastUtils.showToast("最小值" + MIN_VALUE);
                return dest.subSequence(dstart, dend);
            }
            // 有小数点的情况下
            if (temp.contains(POINTER)) {
                //验证小数点精度，保证小数点后只能输入两位
                if (!temp.endsWith(POINTER) && temp.split("\\.")[1].length() > POINTER_LENGTH) {
                    ToastUtils.showToast("保证小数点后只能输入两位");
                    return dest.subSequence(dstart, dend);
                }
            } else if (temp.startsWith(POINTER) || temp.startsWith(ZERO_ZERO) | temp.startsWith("-00")) {
                // 首位只能有一个0
                ToastUtils.showToast("首位只能有一个0");
                return dest.subSequence(dstart, dend);
            }
        }
        return source;
    }
}
