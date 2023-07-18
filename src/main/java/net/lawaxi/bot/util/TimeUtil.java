package net.lawaxi.bot.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;

import java.text.DateFormat;
import java.util.TimeZone;

public class TimeUtil {
    public static final String PATTERN = "yyyy-MM-dd HH:mm";
    public static final DateFormat FORMAT = DateUtil.newSimpleFormat(PATTERN, null, TimeZone.getDefault());

    public static String time2String(long time) {
        return DateTime.of(time).toString(FORMAT);
    }
}
