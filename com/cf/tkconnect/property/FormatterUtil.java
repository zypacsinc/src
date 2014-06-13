
package com.cf.tkconnect.property;

import java.util.*;
import java.text.*;

public final class FormatterUtil{


    private static final int DEFAULT= DateFormat.DEFAULT;
    
     // currency formatter
    public static NumberFormat getCurrencyFormatter(Locale locale){
        return NumberFormat.getCurrencyInstance( locale );

    }
    // get the default date formatter for this locale.
    public static DateFormat getDateFormatter(Locale locale){
        return DateFormat.getDateInstance( DEFAULT,locale );

    }

    // date formatters based on style/pattern
    public static DateFormat getStyleDateFormatter(int style,Locale locale) {
        return DateFormat.getDateInstance( style, locale );
    }
    
    public static DateFormat getPatternDateFormatter(String pattern,Locale locale){
        return new SimpleDateFormat( pattern, locale );

    }


    // default datetime formatter

    public static DateFormat getDateTimeFormatter(Locale locale) {
        return DateFormat.getDateTimeInstance(DEFAULT, DEFAULT, locale);
    }   

    // time/date style based fomratter
    public static DateFormat getDateTimeFormatter(int dateStyle,int timeStyle,Locale locale) {
        return DateFormat.getDateTimeInstance( dateStyle, timeStyle, locale );
    } 

    // default time formatter

    public static DateFormat getTimeFormatter(Locale locale) {
        return DateFormat.getTimeInstance( DEFAULT, locale );
    }
    // time formatter with a specific style
    public static DateFormat getTimeFormatter(int style, Locale locale){
        return DateFormat.getTimeInstance( style , locale );
    }
    
    /** @return the default number formatter for the current Locale 
      */
    public static NumberFormat getNumberFormatter(Locale locale) {
        return NumberFormat.getInstance( locale );
    }

    /** @return the number formatter for the current Locale based on pattern
      */
    public static NumberFormat getPatternNumberFormatter( String pattern,Locale locale ) {
        return new DecimalFormat( pattern, new DecimalFormatSymbols( locale ) );
    }

    // percentage formatter
    public static NumberFormat getPercentFormatter(Locale locale) {
        return NumberFormat.getPercentInstance( locale );
    }
    

}
