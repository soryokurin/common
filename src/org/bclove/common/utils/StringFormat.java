package org.bclove.common.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * This class contains a number of static methods that can be used to
 * validate the format of Strings, typically received as input from
 * a user, and to format values as Strings that can be used in
 * HTML output without causing interpretation problems.
 *
 * @author Hans Bergsten, Gefion software <hans@gefionsoftware.com>
 * @version 2.0
 */
public class StringFormat {
    // Static format objects
    private static SimpleDateFormat dateFormat = new SimpleDateFormat();
    private static DecimalFormat numberFormat = new DecimalFormat();
    
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
    
    /**
     * Returns true if the specified number string represents a valid
     * integer in the specified range, using the default Locale.
     *
     * @param numberString a String representing an integer
     * @param min the minimal value in the valid range
     * @param min the maximal value in the valid range
     * @return true if valid, false otherwise
     */
    public static boolean isValidInteger(String numberString, int min, 
        int max) {
        Integer validInteger = null;
        try {
            Number aNumber = numberFormat.parse(numberString);
            int anInt = aNumber.intValue();
            if (anInt >= min && anInt <= max) {
                validInteger = new Integer(anInt);
            }
        }
        catch (ParseException e) {
            // Ignore and return null
        }
        return validInteger != null;
    }
    

    /**
     * Returns true if the specified number string represents a valid
     * cellphone number, using the default Locale.
     *
     * @param cellPhone a String representing an cellphone number
     * @return true if valid, false otherwise
     */
    public static boolean isValidCellPhone(String cellPhone) {
    	Integer validInteger = null;
    	try {
    		Number aNumber = numberFormat.parse(cellPhone);
    		int anInt = aNumber.intValue();
    		//加入更多的判断以确定是否是一个合法的移动电话号码.
    		validInteger = new Integer(anInt);
    	}
    	catch (ParseException e) {
    		// Ignore and return null
    	}
    	return validInteger != null;
    }
    
    /**
     * Returns true if the specified url represents a valid url
     * @param url
     * @return
     */
    public static boolean isValidUrl(String url) {
        int c;
        if (url == null || url.length() < 1 || url.length() > 30) {
            return false;
        }
        if (url.charAt(0) == 45) { //减号	
            return false;
        }
        if ("img".equalsIgnoreCase(url)) {
            return false;
        }
        if ("www".equalsIgnoreCase(url)) {
            return false;
        }
        if (url.startsWith("www.")) {
            return false;
        }
        for (int i = 0; i < url.length(); i++) {
            c = url.charAt(i);
            if (!(c >= 65 && c <= 90) && !(c >= 97 && c <= 122) && c != 45 && !(c >= 48 && c <= 57)) {
                return false;
            }
         }
        return true;
    }
    
    /**
     * Validate the password.
     * @param passwd
     * @return
     */
    public static boolean isValidPassword(String passwd) {
        if (passwd == null || passwd.length() < 1 || passwd.length() > 20) {
            return false;
        }
        return true;
    }
    
    /**
     * Returns true if the specified number string represents a valid
     * phone number, using the default Locale.
     *
     * @param cellPhone a String representing an phone number
     * @return true if valid, false otherwise
     */
    public static boolean isValidOfficePhone(String cellPhone) {
    	Integer validInteger = null;
    	try {
    		Number aNumber = numberFormat.parse(cellPhone);
    		int anInt = aNumber.intValue();
    		//加入更多的判断以确定是否是一个合法的座机号码.
    		validInteger = new Integer(anInt);
    	}
    	catch (ParseException e) {
    		// Ignore and return null
    	}
    	return validInteger != null;
    }
    
    /**
     * Returns true if the string is in the format of a valid SMTP
     * mail address: only one at-sign, except as the first or last
     * character, no white-space and at least one dot after the
     * at-sign, except as the first or last character.
     * <p>
     * Note! This rule is not always correct (e.g. on an intranet it may 
     * be okay with just a name) and it does not guarantee a valid Internet 
     * email address but it takes care of the most obvious SMTP mail 
     * address format errors.
     *
     * @param mailAddr a String representing an email address
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmailAddr(String mailAddr) {
        return StringFormat.isValidEmailAddr(mailAddr, true);
    }

    /**
     * Returns true if the string is in the format of a valid SMTP
     * mail address: only one at-sign, except as the first or last
     * character, no white-space and at least one dot after the
     * at-sign, except as the first or last character.
     * <p>
     * Note! This rule is not always correct (e.g. on an intranet it may 
     * be okay with just a name) and it does not guarantee a valid Internet 
     * email address but it takes care of the most obvious SMTP mail 
     * address format errors.
     *
     * @param mailAddr
     * @param strict
     * @return
     */
    public static boolean isValidEmailAddr(String mailAddr, boolean strict) {
    	if (mailAddr == null) {
    	    return false;
    	}
    	
        if (!strict) {
            if  (mailAddr.length() < 1) {
                return true;   
            }
        } else {
            if (mailAddr.length() < 1) {
                return false;
            }
        }
        
    	if (mailAddr.length() > 50) {
    	    return false;
    	}
        
    	boolean isValid = true;
    	mailAddr = mailAddr.trim();

    	// Check at-sign and white-space usage
    	int atSign = mailAddr.indexOf('@');
    	if (atSign == -1 || 
    	    atSign == 0 ||
    	    atSign == mailAddr.length() -1 ||
    	    mailAddr.indexOf('@', atSign + 1) != -1 ||
    	    mailAddr.indexOf(' ') != -1 ||
    	    mailAddr.indexOf('\t') != -1 ||
    	    mailAddr.indexOf('\n') != -1 ||
    	    mailAddr.indexOf('\r') != -1) {
    	    isValid = false;
    	}
    	// Check dot usage
    	if (isValid) {
    	    mailAddr = mailAddr.substring(atSign + 1);
    	    int dot = mailAddr.indexOf('.');
    	    if (dot == -1 || 
    		dot == 0 ||
    		dot == mailAddr.length() -1) {
    		isValid = false;
    	    }
    	}
            return isValid;
    }
    
    /**
     * Returns true if the specified string matches a string in the set
     * of provided valid strings, ignoring case if specified.
     *
     * @param value the String validate
     * @param validStrings an array of valid Strings
     * @param ignoreCase if true, case is ignored when comparing the value
     *  to the set of validStrings
     * @return true if valid, false otherwise
     */
    public static boolean isValidString(String value, String[] validStrings, 
            boolean ignoreCase) {
        boolean isValid = false;
        for (int i = 0; validStrings != null && i < validStrings.length; i++) {
            if (ignoreCase) {
                if (validStrings[i].equalsIgnoreCase(value)) {
                    isValid = true;
                    break;
                }
            }
            else {
                if (validStrings[i].equals(value)) {
                    isValid = true;
                    break;
                }
            }
        }
        return isValid;
    }

    /**
     * Returns true if the strings in the specified array all match a string 
     * in the set of provided valid strings, ignoring case if specified.
     *
     * @param values the String[] validate
     * @param validStrings an array of valid Strings
     * @param ignoreCase if true, case is ignored when comparing the value
     *  to the set of validStrings
     * @return true if valid, false otherwise
     */
    public static boolean isValidString(String[] values, 
	    String[] validStrings, boolean ignoreCase) {

	if (values == null) {
	    return false;
	}
        boolean isValid = true;
        for (int i = 0; values != null && i < values.length; i++) {
	    if (!isValidString(values[i], validStrings, ignoreCase)) {
		isValid = false;
		break;
	    }
	}
	return isValid;
    }

    /**
     * Returns the specified string converted to a format suitable for
     * HTML. All signle-quote, double-quote, greater-than, less-than and
     * ampersand characters are replaces with their corresponding HTML
     * Character Entity code.
     *
     * @param in the String to convert
     * @return the converted String
     */
    public static String toHTMLString(String in) {
        StringBuffer out = new StringBuffer();
        for (int i = 0; in != null && i < in.length(); i++) {
            char c = in.charAt(i);
            if (c == '\'') {
                out.append("&#039;");
            }
            else if (c == '\"') {
                out.append("&#034;");
            }
            else if (c == '<') {
                out.append("&lt;");
            }
            else if (c == '>') {
                out.append("&gt;");
            }
            else if (c == '&') {
                out.append("&amp;");
            }
            else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * Converts a String to a Date, using the specified pattern.
     * (see java.text.SimpleDateFormat for pattern description) and
     * the default Locale.
     *
     * @param dateString the String to convert
     * @param dateFormatPattern the pattern
     * @return the corresponding Date
     * @exception ParseException, if the String doesn't match the pattern
     */
    public static Date toDate(String dateString, String dateFormatPattern) 
        throws ParseException {
        Date date = null;
        if (dateFormatPattern == null) {
            dateFormatPattern = "yyyy-MM-dd";
        }
        synchronized (dateFormat) { 
            dateFormat.applyPattern(dateFormatPattern);
            dateFormat.setLenient(false);
            date = dateFormat.parse(dateString);
        }
        return date;
    }

    /**
     * Converts a String to a Number, using the specified pattern.
     * (see java.text.NumberFormat for pattern description) and the
     * default Locale.
     *
     * @param numString the String to convert
     * @param numFormatPattern the pattern
     * @return the corresponding Number
     * @exception ParseException, if the String doesn't match the pattern
     */
    public static Number toNumber(String numString, String numFormatPattern) 
        throws ParseException {
        Number number = null;
        if (numFormatPattern == null) {
            numFormatPattern = "######.##";
        }
        synchronized (numberFormat) { 
            numberFormat.applyPattern(numFormatPattern);
            number = numberFormat.parse(numString);
        }
        return number;
    }

    /**
     * Replaces one string with another throughout a source string.
     *
     * @param in the source String
     * @param from the sub String to replace
     * @param to the sub String to replace with
     * @return a new String with all occurences of from replaced by to
     */
    public static String replaceInString(String in, String from, String to) {
        if (in == null || from == null || to == null) {
            return in;
        }

        StringBuffer newValue = new StringBuffer();
        char[] inChars = in.toCharArray();
        int inLen = inChars.length;
        char[] fromChars = from.toCharArray();
        int fromLen = fromChars.length;

        for (int i = 0; i < inLen; i++) {
            if (inChars[i] == fromChars[0] && (i + fromLen) <= inLen) {
                boolean isEqual = true;
                for (int j = 1; j < fromLen; j++) {
                    if (inChars[i + j] != fromChars[j]) {
                        isEqual = false;
                        break;
                    }
                }
                if (isEqual) {
                    newValue.append(to);
                    i += fromLen - 1;
                }
                else {
                    newValue.append(inChars[i]);
                }
            }
            else {
                newValue.append(inChars[i]);
            }
        }
        return newValue.toString();
    }

    /**
     * Returns a page-relative or context-relative path URI as
     * a context-relative URI.
     *
     * @param relURI the page or context-relative URI
     * @param currURI the context-relative URI for the current request
     * @exception IllegalArgumentException if the relURI is invalid
     */
    public static String toContextRelativeURI(String relURI, String currURI) 
        throws IllegalArgumentException {

        if (relURI.startsWith("/")) {
            // Must already be context-relative
            return relURI;
        }
        
        String origRelURI = relURI;
        if (relURI.startsWith("./")) {
            // Remove current dir characters
            relURI = relURI.substring(2);
        }
        
        String currDir = currURI.substring(0, currURI.lastIndexOf("/") + 1);
        StringTokenizer currLevels = new StringTokenizer(currDir, "/");
        
        // Remove and count all parent dir characters
        int removeLevels = 0;
        while (relURI.startsWith("../")) {
            if (relURI.length() < 4) {
                throw new IllegalArgumentException("Invalid relative URI: " + 
		    origRelURI);
            }
            relURI = relURI.substring(3);
            removeLevels++;
        }
        
        if (removeLevels > currLevels.countTokens()) {
            throw new IllegalArgumentException("Invalid relative URI: " + 
		origRelURI + " points outside the context");
        }
        int keepLevels = currLevels.countTokens() - removeLevels;
        StringBuffer newURI = new StringBuffer("/");
        for (int j = 0; j < keepLevels; j++) {
            newURI.append(currLevels.nextToken()).append("/");
        }
        return newURI.append(relURI).toString();
    }
}
