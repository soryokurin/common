package org.bclove.common.store;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {

	static Pattern days = Pattern.compile("^([0-9]+)d$");
    static Pattern hours = Pattern.compile("^([0-9]+)h$");
    static Pattern minutes = Pattern.compile("^([0-9]+)mi?n$");
    static Pattern seconds = Pattern.compile("^([0-9]+)s$");
    
    static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Parse a duration
     * @param duration 3h, 2mn, 7s ,5d
     * @return The number of seconds
     */
    public static int parseDuration(String duration) {
        if (duration == null) {
            return 60 * 60 * 24 * 30;
        }
        int toAdd = -1;
        if (days.matcher(duration).matches()) {
            Matcher matcher = days.matcher(duration);
            matcher.matches();
            toAdd = Integer.parseInt(matcher.group(1)) * (60 * 60) * 24;
        } else if (hours.matcher(duration).matches()) {
            Matcher matcher = hours.matcher(duration);
            matcher.matches();
            toAdd = Integer.parseInt(matcher.group(1)) * (60 * 60);
        } else if (minutes.matcher(duration).matches()) {
            Matcher matcher = minutes.matcher(duration);
            matcher.matches();
            toAdd = Integer.parseInt(matcher.group(1)) * (60);
        } else if (seconds.matcher(duration).matches()) {
            Matcher matcher = seconds.matcher(duration);
            matcher.matches();
            toAdd = Integer.parseInt(matcher.group(1));
        }
        if (toAdd == -1) {
            throw new IllegalArgumentException("Invalid duration pattern : " + duration);
        }
        return toAdd;
    }
    
	/**
	 * 获取时间间隔
	 */
	public static String reckonTime(final Date time, Date now) {
		long t = (now.getTime() - time.getTime()) / 60000;

		if (t > 2880)
			return DATEFORMAT.format(time);
		else if (t > 1440)
			return "1天前";
		else if (t > 60)
			return t / 60 + "小时前";
		else if (t > 1)
			return t + "分种前";
		else
			return "刚刚";
	}
}
