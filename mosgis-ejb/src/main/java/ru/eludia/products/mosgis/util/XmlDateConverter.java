package ru.eludia.products.mosgis.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Aleksei
 */
public class XmlDateConverter {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
	
	
	public static LocalDate parseDate(String xmlDate) {
		return LocalDate.parse(xmlDate, DATE_FORMAT);
	}
	
	public static String printDate(LocalDate value) {
		if (value == null) {
            return null;
        }
        return DATE_FORMAT.format(value);
	}
	
	public static LocalTime parseTime(String xmlDate) {
		return LocalTime.parse(xmlDate, TIME_FORMAT);
	}
	
	public static String printTime(LocalTime value) {
		if (value == null) {
            return null;
        }
        return TIME_FORMAT.format(value);
	}
	
	public static LocalDateTime parseDateTime(String xmlDate) {
		return LocalDateTime.parse(xmlDate, DATETIME_FORMAT);
	}
	
	public static String printDateTime(LocalDateTime value) {
		if (value == null) {
            return null;
        }
        return DATETIME_FORMAT.format(value);
	}
}
