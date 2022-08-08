package org.fajr.snapshot_utility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DateTimeUtils {

	public static Date convertTimeStringToDate(String timeAsString) {
		LocalDate localDate = LocalDate.now();
		LocalTime startLocalTime = LocalTime.parse(timeAsString);
		LocalDateTime startLocalDateTime = LocalDateTime.of(localDate, startLocalTime);
		Date startAt = Date.from(startLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
		return startAt;
	}

	public static Date addHoursToJavaUtilDate(Date date, int hours) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.HOUR_OF_DAY, hours);
		return calendar.getTime();
	}

	public static Date addSecondsToDate(Date date, int seconds) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.SECOND, seconds);
		return calendar.getTime();
	}

	public static String currentDateTime() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}

	public static void main(String[] args) {
		Date date = new Date();
		Date date2 = addSecondsToDate(date, 60);
		System.out.println("date = " + date + " . date2 = " + date2);
		
		System.out.println(currentDateTime());
	}
}
