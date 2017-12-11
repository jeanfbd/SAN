package desenvolvimentoads.san.Helper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * Created by jeanf on 17/07/2017.
 */

public class DateHelper {

    public static String dateToString (Date date){
        if (date == null)
            return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-mm-dd HH:MM:SS");
        String dateString = dateFormat.format(date);
        return dateString;
    }

    public static Date stringToDate(String dateString){
        if (dateString == null || dateString.equals(""))
            return null;
        Date date = null;
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = (Date)formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String dateBRFormat(String stringDate){
        Date date = stringToDate(stringDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
        String dateBR = dateFormat.format(date);
        return dateBR;
    }

    public static String getSystemDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getTimestamp(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss", Locale.getDefault());
        return formatter.format(timestamp);
    }

}
