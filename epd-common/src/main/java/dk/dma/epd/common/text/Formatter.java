/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.epd.common.text;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import dk.dma.epd.common.Heading;
import it.toscana.rete.lamma.prototype.model.FuelConsumption;
import it.toscana.rete.lamma.prototype.model.ThetaUDimension;
import it.toscana.rete.lamma.prototype.model.Wave;
import it.toscana.rete.lamma.utils.FuelConsumptionCalculator;
import it.toscana.rete.lamma.utils.Utils;

/**
 * Utility class for doing different formatting
 */
public class Formatter {

    private static SimpleDateFormat tzConvert = new SimpleDateFormat("Z");
    private static SimpleDateFormat shortDateTime = new SimpleDateFormat("MM/dd HH:mm:ss");
    private static SimpleDateFormat longDateTime = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private static SimpleDateFormat shortDateTimeNoS = new SimpleDateFormat("MM/dd HH:mm");

    private static SimpleDateFormat onlyTime = new SimpleDateFormat("HH:mm");

    public static String formateTimeFromDate(long date) {
        return onlyTime.format(date);
    }

    public static String formateTimeFromDate(Date date) {
        return onlyTime.format(date);
    }

    public static String formatShortDateTime(Date date) {
        if (date == null) {
            return "N/A";
        }
        return shortDateTime.format(date) + "(" + getTzNumber(date) + ")";
    }

    public static String formatShortDateTimeNoTz(Date date) {
        if (date == null) {
            return "N/A";
        }
        return shortDateTimeNoS.format(date);
    }

    public static String formatLongDateTime(Date date) {
        if (date == null) {
            return "N/A";
        }
        return longDateTime.format(date) + "(" + getTzNumber(date) + ")";
    }
    
    public static String formatLongDateTimeNoTz(Date date) {
        if (date == null) {
            return "N/A";
        }
        return longDateTime.format(date);
    }

    
    public static String formatYodaTime(DateTime time) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("dd MMMM, HH:mm");

        return time.toString(fmt);
    }

    public static String formatTime(Long time) {
        if (time == null) {
            return "N/A";
        }
        long secondInMillis = 1000;
        long minuteInMillis = secondInMillis * 60;
        long hourInMillis = minuteInMillis * 60;
        long dayInMillis = hourInMillis * 24;

        long elapsedDays = time / dayInMillis;
        time = time % dayInMillis;
        long elapsedHours = time / hourInMillis;
        time = time % hourInMillis;
        long elapsedMinutes = time / minuteInMillis;
        time = time % minuteInMillis;
        long elapsedSeconds = time / secondInMillis;

        if (elapsedDays > 0) {
            return String.format("%02d:%02d:%02d:%02d", elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);
        } else {
            return String.format("%02d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
        }
    }

    public static String formatTimeShort(Long time) {
        String elapsed = formatTime(time);
        if (elapsed.length() < 4) {
            return elapsed;
        }
        return elapsed.substring(elapsed.length() - 5, elapsed.length());
    }

    public static String getTzNumber(Date date) {
        String tz = tzConvert.format(date);
        double tzVal = Double.parseDouble(tz) / 100;
        // TOOD Should be able to handle decimal zones
        return Long.toString((long) tzVal);
    }

    public static String latToPrintable(double lat) {
        String ns = "N";
        if (lat < 0) {
            ns = "S";
            lat *= -1;
        }
        int hours = (int) lat;
        lat -= hours;
        lat *= 60;
        String latStr = String.format(Locale.US, "%3.3f", lat);
        while (latStr.indexOf('.') < 2) {
            latStr = "0" + latStr;
        }
        return String.format(Locale.US, "%02d %s%s", hours, latStr, ns);
    }

    public static String lonToPrintable(double lon) {
        String ns = "E";
        if (lon < 0) {
            ns = "W";
            lon *= -1;
        }
        int hours = (int) lon;
        lon -= hours;
        lon *= 60;
        String lonStr = String.format(Locale.US, "%3.3f", lon);
        while (lonStr.indexOf('.') < 2) {
            lonStr = "0" + lonStr;
        }
        return String.format(Locale.US, "%03d %s%s", hours, lonStr, ns);
    }

    public static String formatString(String str, String defaultStr) {
        if (str == null) {
            return defaultStr;
        }
        return str;
    }

    public static String formatString(String str) {
        return formatString(str, "");
    }

    public static String formatSpeed(Double s) {
        if (s == null) {
            return "N/A";
        }
        return formatDouble(s, 2) + " kn";
    }

    public static String formatWindSpeed(Double s) {
        if (s == null) {
            return "N/A";
        }
        return formatDouble(s, 2) + " m/s";
    }

    public static String formatWindSpeed(Double s, int decimals) {
        if (s == null) {
            return "N/A";
        }
        return formatDouble(s, decimals) + " m/s";
    }

    public static String formatCurrentSpeed(Double s) {
        if (s == null) {
            return "N/A";
        }
        return formatDouble(s, 2) + " kn";
    }

    public static String formatCurrentSpeed(Double s, int decimals) {
        if (s == null) {
            return "N/A";
        }
        return formatDouble(s, decimals) + " kn";
    }

    public static String formatMeters(Double dist) {
        return formatMeters(dist, 0);
    }

    public static String formatMeters(Double dist, int decimals) {
        if (dist == null) {
            return "N/A";
        }
        return formatDouble(dist, decimals) + " m";
    }

    public static String formatDistNM(Double dist, int decimals) {
        if (dist == null) {
            return "N/A";
        }
        return formatDouble(dist, decimals) + " NM";
    }

    public static String formatDistNM(Double dist) {
    	if (dist == null) {
            return "N/A";
        }
        return formatDistNM(dist, 2);
    }

    public static String formatDegrees(Double dgs, int decimals) {
        if (dgs == null) {
            return "N/A";
        }
        return addZeroesToDecimalNumber(formatDouble(dgs, decimals), 3) + "°";
    }

    public static String formatHeading(Heading heading) {
        if (heading == null) {
            return "N/A";
        }
        return heading.name();
    }

    public static String formatRot(Double rot) {
        if (rot == null) {
            return "N/A";
        }
        return String.format(Locale.US, "%.1f", rot) + "°/min";
    }

    public static String formatLong(Long l) {
        if (l == null) {
            return "N/A";
        }
        return Long.toString(l);
    }
    public static String formatDouble(double d) {
        return formatDouble(d, 2);
    }

    public static String formatDouble(Double d, int decimals) {
        if (d == null) {
            return "N/A";
        }
        if (decimals == 0) {
            return String.format(Locale.US, "%d", Math.round(d));
        }
        String format = "%." + decimals + "f";
        return String.format(Locale.US, format, d);
    }

    public static String formatAisRouteType(int type) {
        switch (type) {
        case 1:
            return "Mandatory";
        case 2:
            return "Recommended";
        case 3:
            return "Alternative";
        case 4:
            return "Recommended through ice";
        case 5:
            return "Ship route";
        default:
            return "Unknown";
        }
    }

    private static String addZeroesToDecimalNumber(String str, int num) {
        int lengthBeforeDecimal = str.indexOf('.');
        if (lengthBeforeDecimal < 0) {
            lengthBeforeDecimal = str.length();
        }
        for (int i = 0; i < num - lengthBeforeDecimal; i++) {
            str = "0" + str;
        }
        return str;
    }

    public static String formatHours(double hours) {
        if (hours < 1) {
            return ((int) (hours * 60)) + " minutes";
        } else {

            if (((int) hours) == 1) {
                return ((int) hours) + " hour, " + ((int) ((hours - ((int) hours)) * 60)) + " minutes";
            } else {
                return ((int) hours) + " hours, " + ((int) ((hours - ((int) hours)) * 60)) + " minutes";
            }

        }

    }
    public static String formatPropulsion(String p) {
    	if(p == null) {
    		return null;
    	}
    	return Utils.stripFileExt(Paths.get(p).toFile());
    }
    /**
     * A very simple and crude conversion of plain text into HTML.
     * 
     * @param str
     *            the string to format
     * @return the resulting HTML
     */
    public static String formatHtml(String str) {
        // Sanity checks
        if (str == null) {
            return str;
        }

        str = StringEscapeUtils.escapeHtml(str);
        str = str.replaceAll("\n", "<br/>");
        return str;
    }
    public static String formatCurrent( ThetaUDimension cur) {
        if (cur == null) {
            return "N/A";
        }
        String sign = cur.getTheta() < 0 ? " -" : " ";
        return formatCurrentSpeed(cur.getU(), 1);
    }
    public static String formatWind( ThetaUDimension wind) {
        if (wind == null) {
            return "N/A";
        }
        String sign = wind.getTheta() < 0 ? " -" : " ";
        return formatSpeed(wind.getU())  + sign + formatDegrees(Math.abs(wind.getTheta()), 0);
    }
    public static String formatWind( ThetaUDimension wind, double heading) {
        if (wind == null) {
            return "N/A";
        }
        String sign = wind.getTheta() < 0 ? " -" : " ";
        double nWind = FuelConsumptionCalculator.wrapTo360(heading + wind.getTheta());
        return formatSpeed(wind.getU())  + sign + formatDegrees(Math.abs(wind.getTheta()), 0) + " " + formatDegrees(nWind, 0);
    }
    public static String formatWave( FuelConsumption fc) {
        if (fc == null || fc.getMetoc() == null) {
            return "N/A";
        }
        Wave w = fc.getMetoc().getMeanWave();
        double nWave = FuelConsumptionCalculator.wrapTo360(fc.getHeading() + fc.getWave_polar());
        return formatDouble(w.getHeight(),2) + "m " + formatDouble(w.getPeriod(), 0) + "s " + formatDegrees(nWave, 0);
    }
}
