package org.fajr.snapshot_utility;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

public class Utilities {

	static Logger log = Logger.getLogger(Utilities.class.getName());


	public static synchronized void readSettingsWithCallback1(Callback callback) {
		//log.info("Reading Fajr App settings...");
		String applicationSettingsFolder = Preferences.APPLICATION_SETTINGS_FOLDER;
		String settingsFile = Preferences.SETTINGS_FILE;

		File applicationSettingsDirectory = new File(applicationSettingsFolder);

		if (!applicationSettingsDirectory.exists())
			applicationSettingsDirectory.mkdirs();

		File applicationSettingsFile = new File(applicationSettingsFolder, settingsFile);

		Settings settings = null;
		if (!applicationSettingsFile.exists()) {

			try {
				applicationSettingsFile.createNewFile();
				settings = new Settings();
				settings.setRunMode("REAL");
				persistSettings(settings);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			Gson gson = new Gson();
			try (Reader reader = new FileReader(applicationSettingsFile)) {
				settings = gson.fromJson(reader, Settings.class);
				if (settings == null) {
					settings = new Settings();
					settings.setRunMode("REAL");
					persistSettings(settings);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		callback.taskTerminated(true, "setting initialized", settings);

	}

	public static synchronized void updateStatusForScheduleId(final int id, final ScheduledJobStatus scheduledJobStatus) {
		readSettingsWithCallback(new Callback() {

			@Override
			public void taskTerminated(ScheduledJob scheduledJob, Settings settings) {
				// TODO Auto-generated method stub

			}

			@Override
			public void taskTerminated(boolean success, String message, Settings settings) {
				List<ScheduledJob> schedules = settings.getSchedules();
				for (Iterator<ScheduledJob> iterator = schedules.iterator(); iterator.hasNext();) {
					ScheduledJob scheduledJob = iterator.next();
					if (scheduledJob.getId() == id) {
						scheduledJob.setStatus(scheduledJobStatus);
					}
				}

				persistSettings(settings);

			}
		});

	}

	protected static synchronized void persistSettings(Settings settings) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String settingsJson = gson.toJson(settings, Settings.class);

		try {
			File jsonSettingFile = new File(Preferences.APPLICATION_SETTINGS_FOLDER, Preferences.SETTINGS_FILE);
			FileWriter file = new FileWriter(jsonSettingFile);
			file.write(settingsJson);
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void removeScheduleById(int id) {

	}

	public static void persistSettings(ScheduledJobsList scheduledJobsList) {
		// TODO Auto-generated method stub

	}

	public static void startKeepAwakeThread() {
		KeepAwakeThread t = new KeepAwakeThread();
		Thread t2 = new Thread(t);
		t2.start();

	}

	public static synchronized void updateRunModeStatus(final boolean runMode) {

		readSettingsWithCallback(new Callback() {

			@Override
			public void taskTerminated(ScheduledJob scheduledJob, Settings settings) {
				// TODO Auto-generated method stub

			}

			@Override
			public void taskTerminated(boolean success, String message, Settings settings) {
				String rm = runMode ? "TEST" : "REAL";
				settings.setRunMode(rm);
				persistSettings(settings);

			}
		});
	}
	
	public static synchronized void readSettingsWithCallback(Callback callback) {
		//log.info("Reading Fajr App settings...");
		String applicationSettingsFolder = Preferences.APPLICATION_SETTINGS_FOLDER;
		String settingsFile = Preferences.SETTINGS_FILE;

		File applicationSettingsDirectory = new File(applicationSettingsFolder);

		if (!applicationSettingsDirectory.exists())
			applicationSettingsDirectory.mkdirs();

		File applicationSettingsFile = new File(applicationSettingsFolder, settingsFile);

		Settings settings = null;
		if (!applicationSettingsFile.exists()) {

			try {
				applicationSettingsFile.createNewFile();
				settings = new Settings();
				settings.setRunMode("REAL");
				persistSettings(settings);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			//Gson gson = new Gson();
			try (Reader reader = new FileReader(applicationSettingsFile)) {
				
				final TypeToken<Settings> requestListTypeToken = new TypeToken<Settings>() {};
				 final RuntimeTypeAdapterFactory<ScheduledJob> typeFactory = RuntimeTypeAdapterFactory
			                .of(ScheduledJob.class, "clazz")
			                .registerSubtype(IpCameraScheduledJob.class,IpCameraScheduledJob.class.getName())
			                .registerSubtype(ScreenGrabScheduledJob.class,ScreenGrabScheduledJob.class.getName());
				 
				 final Gson gson = new GsonBuilder().registerTypeAdapterFactory(
			                typeFactory).create();
				 
				 
				
				settings = gson.fromJson(reader, requestListTypeToken.getType());
				if (settings == null) {
					settings = new Settings();
					settings.setRunMode("REAL");
					persistSettings(settings);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		callback.taskTerminated(true, "setting initialized", settings);

	}
	
	public static void main(String[] args) {
		readSettingsWithCallback(new Callback() {
			
			@Override
			public void taskTerminated(ScheduledJob scheduledJob, Settings settings) {
				// TODO Auto-generated method stub
				log.info("1");
				
			}
			
			@Override
			public void taskTerminated(boolean success, String message, Settings settings) {
				// TODO Auto-generated method stub
				log.info("setting = "+ settings);

			}
		});
	}
	
}
