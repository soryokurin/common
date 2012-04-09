package org.bclove.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 * @author soryokurin
 *
 */
public class DateFormat {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat();
   
    
    /**
     * Returns true if the specified date string represents a valid
     * date in the specified format, using the default Locale.
     *
     * @param dateString a String representing a date/time.
     * @param dateFormatPattern a String specifying the format to be used
     *   when parsing the dateString. The pattern is expressed with the
     *   pattern letters defined for the java.text.SimpleDateFormat class.
     * @return true if valid, false otherwise
     */
    public static boolean isValidDate(String dateString, 
        String dateFormatPattern) {
        Date validDate = null;
        synchronized (dateFormat) { 
            try {
                dateFormat.applyPattern(dateFormatPattern);
                dateFormat.setLenient(false);
                validDate = dateFormat.parse(dateString);
            }
            catch (ParseException e) {
                // Ignore and return null
            }
        }
        return validDate != null;
    }
    

    public static String toString(Date date, String dateFormatPattern){
        String time="";
    	try {
            if (dateFormatPattern == null) {
                dateFormatPattern = "yyyy-MM-dd";
            }
            synchronized (dateFormat) { 
                dateFormat.applyPattern(dateFormatPattern);
                dateFormat.setLenient(false);
                time = dateFormat.format(date);
            }
		} catch (Exception e) {
			// TODO: handle exception
		}
        return time;
    }
    
    public static void main(String[] args){
    	System.out.println(toString(new Date(), "yyyy-MM-dd hh:mm:ss"));
    }
}
