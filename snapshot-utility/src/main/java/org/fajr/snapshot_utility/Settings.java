package org.fajr.snapshot_utility;

import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

public class Settings {
	
//	public  String settingsFile ;
	public  String screenshotsFolder;
	public List<ScheduledJob> schedules;
	
//	public String getSettingsFile() {
//		return settingsFile;
//	}
//	public void setSettingsFile(String settingsFile) {
//		this.settingsFile = settingsFile;
//	}
	public String getScreenshotsFolder() {
		return screenshotsFolder;
	}
	public void setScreenshotsFolder(String screenshotsFolder) {
		this.screenshotsFolder = screenshotsFolder;
	}
	public List<ScheduledJob> getSchedules() {
		return schedules;
	}
	public void setSchedules(List<ScheduledJob> scheduledJobList) {
		this.schedules = scheduledJobList;
	}

	@Override
	public String toString() {
//		return "Settings [settingsFolder=" + getSettingsFile() + ", screenshotsFolder=" + screenshotsFolder
//				+ ", schedules=" + Arrays.toString(schedules) + "]";
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	
	
	
//	public static void readSettingsWithCallback(Callback callback) {
//		String path = System.getProperty("user.home") + File.separator + "Fajr App";
//		path += File.separator + "Settings";
//		settingsFolder = new File(path);
//		JSONParser parser = new JSONParser();
//		JSONObject settings;
//		try {
//			String fileName = settingsFolder + File.separator + "settings.json";
//			File settingsFile = new File(fileName);
//			if (settingsFile.exists()) {
//				settings = (JSONObject) parser.parse(new FileReader(fileName));
//				screenshotsFolder = (String) settings.get("screenshot folder");
//				System.out.println(screenshotsFolder);
////				startTime = (String) settings.get("start time");
////				endTime = (String) settings.get("end time");
////				periodicity = (String) settings.get("periodicity");
//			
//				callback.taskTerminated(true,"Fajr settings initialized successfully!");
//			} else {
//				// setting file not there, return to create a new one
//				callback.taskTerminated(true,"setting file not found");
//			}
//		} catch (IOException | ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			callback.taskTerminated(false,e.getMessage());
//		}
//	}
	
//	public static Date convertTimeStringToDate(String timeAsString){
//		LocalDate localDate = LocalDate.now();
////		String startTimeString = FajrAppSettings.startTime;
//		
//		LocalTime startLocalTime = LocalTime.parse(timeAsString);
//		LocalDateTime startLocalDateTime = LocalDateTime.of(localDate, startLocalTime);
//		LocalDateTime plusDays = startLocalDateTime.plusDays(1);
//		
//		Date startAt = Date.from(startLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
//		return startAt;
//	}
	
	
	
//	public static LocalDateTime getStartTimeAsLocalDateTime(){
//		LocalDate localDate = LocalDate.now();
//		String startTimeString = FajrAppSettings.startTime;
//		
//		LocalTime startTime = LocalTime.parse(startTimeString);
//		LocalDateTime startLocalDateTime = LocalDateTime.of(localDate, startTime);
//		
//		return startLocalDateTime;
//	}
	
//	public static Date getEndTimeAsDate(){
//		LocalDate localDate = LocalDate.now();
//		String endTimeString = FajrAppSettings.endTime;
//		
//		LocalTime endTime = LocalTime.parse(endTimeString);
//		LocalDateTime endLocalDateTime = LocalDateTime.of(localDate, endTime);
//		
//		Date endAt = Date.from(endLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
//		return endAt;
//	}
	

	
/*	LocalDate localDate = LocalDate.now();
	String startTimeString = FajrAppSettings.startTime;
	
	LocalTime startTime = LocalTime.parse(startTimeString);
	LocalDateTime startLocalDateTime = LocalDateTime.of(localDate, startTime);
	
	Date startAt = Date.from(startLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
	
	String endTimeString = FajrAppSettings.endTime;
	LocalTime endTime = LocalTime.parse(endTimeString);
	LocalDateTime endLocalDateTime = LocalDateTime.of(localDate, endTime);
	Date endAt = Date.from(endLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());*/

}
