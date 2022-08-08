package org.fajr.snapshot_utility;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class Settings {
	
//	@SerializedName("screenshotsFolder")
	public  String screenshotsFolder;
//	@SerializedName("runMode")
	public String runMode;// TEST or REAL
//	@SerializedName("schedules")
	public List<ScheduledJob> schedules;
	
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
	
	public String getRunMode() {
		return runMode;
	}
	public void setRunMode(String runMode) {
		this.runMode = runMode;
	}
	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
