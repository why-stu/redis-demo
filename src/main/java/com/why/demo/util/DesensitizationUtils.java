package com.why.demo.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 脱敏工具类
 */
public class DesensitizationUtils {
    /**
     * 脱敏时替换的字符
     */
    private static final String MASK_CHAR = "*";

    /**
     * 对字符串进行脱敏操作
     *
     * @param sourceStr             原始字符串
     * @param prefixPlaintextLength 左侧需要保留几位明文
     * @param suffixPlaintextLength 右侧需要保留几位明文
     * @return 脱敏后的字符串
     */
    private static String desensitize(String sourceStr, int prefixPlaintextLength, int suffixPlaintextLength) {
        if (sourceStr == null || sourceStr.length() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = sourceStr.length(); i < n; i++) {
            if (i < prefixPlaintextLength) {
                sb.append(sourceStr.charAt(i));
                continue;
            }
            if (i > (n - suffixPlaintextLength - 1)) {
                sb.append(sourceStr.charAt(i));
                continue;
            }
            sb.append(MASK_CHAR);
        }
        return sb.toString();
    }

    /**
     * 【中文姓名】只保留第一位，其他隐藏为星号，比如：张*
     *
     * @param username 姓名
     * @return 结果
     */
    public static String username(String username) {
        if (username == null || username.length() == 0) {
            return "";
        }
        return desensitize(username, 1, 0);
    }

    /**
     * 【身份证号】显示前六位, 四位，其他隐藏。共计18位或者15位，比如：340304*******1234
     *
     * @param idCardNo 身份证号码
     * @return 结果
     */
    public static String idCardNo(String idCardNo) {
        if (idCardNo == null || idCardNo.length() == 0) {
            return "";
        }
        return desensitize(idCardNo, 6, 4);
    }

    /**
     * 【固定电话号码】后四位，其他隐藏，比如 ****1234
     *
     * @param phone 固定电话
     * @return 结果
     */
    public static String fixedPhone(String phone) {
        if (phone == null || phone.length() == 0) {
            return "";
        }
        return desensitize(phone, 0, 4);
    }

    /**
     * 【手机号码】后四位，其他隐藏，比如 *******6810
     *
     * @param phone 手机号码
     * @return 结果
     */
    public static String mobilePhone(String phone) {
        if (phone == null || phone.length() == 0) {
            return "";
        }
        return desensitize(phone, 0, 4);
    }

    /**
     * 【地址】只显示到地区，不显示详细地址，比如：北京市海淀区****
     *
     * @param address 地址
     * @return 结果
     */
    public static String address(String address) {
        if (address == null || address.length() == 0) {
            return "";
        }
        return desensitize(address, 6, 0);
    }

    /**
     * 【电子邮箱 邮箱前缀仅显示第一个字母，前缀其他隐藏，用星号代替，@及后面的地址显示，比如：d**@163.com
     *      如果 @符号之前只有一位，则直接隐藏，比如：*@163.com
     *
     * @param email 电子邮箱
     * @return 结果
     */
    public static String email(String email) {
        if (email == null || email.length() == 0) {
            return "";
        }
        int index = StringUtils.indexOf(email, '@');
        if (index <= 1) {
            return "*" + email.substring(index);
        }
        String preEmail = desensitize(email.substring(0, index), 1, 0);
        return preEmail + email.substring(index);
    }

    /**
     * 【金额】 金额只显示小数点'.'，其他的金额数字全部隐藏，不展示具体数位，
     *      小数点前用三位星号，小数点后用两位星号代替，比如：***.**
     *
     * @param money 金额
     * @return 结果
     */
    public static String money(String money) {
        if (money == null || money.length() == 0) {
            return "";
        }
        return "***.**";
    }

    /**
     * 【银行卡号】前六位，后四位，其他用星号隐藏每位1个星号，比如：622260**********1234
     *
     * @param bankAccount 银行卡号
     * @return 结果
     */
    public static String bankAccount(String bankAccount) {
        if (bankAccount == null || bankAccount.length() == 0) {
            return "";
        }
        return desensitize(bankAccount, 6, 4);
    }

    /**
     * 【密码】密码的全部字符都用*代替，比如：******
     *
     * @param password 密码
     * @return 结果
     */
    public static String password(String password) {
        if (password == null || password.length() == 0) {
            return "";
        }
        return "******";
    }

    /**
     * 【密钥】密钥除了最后三位，全部都用*代替，比如：***xdS 脱敏后长度为6，如果明文长度不足三位，则按实际长度显示，剩余位置补*
     *
     * @param secretKey 密钥
     * @return 结果
     */
    public static String secretKey(String secretKey) {
        if (secretKey == null || secretKey.length() == 0) {
            return "";
        }
        int viewLength = 6;
        StringBuilder tmpKey = new StringBuilder(desensitize(secretKey, 0, 3));
        if (tmpKey.length() > viewLength) {
            return tmpKey.substring(tmpKey.length() - viewLength);
        } else if (tmpKey.length() < viewLength) {
            int buffLength = viewLength - tmpKey.length();
            for (int i = 0; i < buffLength; i++) {
                tmpKey.insert(0, "*");
            }
            return tmpKey.toString();
        } else {
            return tmpKey.toString();
        }
    }
}
