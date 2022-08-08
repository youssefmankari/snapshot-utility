package org.fajr.snapshot_utility;

import com.google.gson.Gson;

public class ScheduledJob {
	
	protected String clazz = getClass().getName(); 

	private int id;
	
	private String startAt;
	private String endAt;
	/*
	 * interval in mm:ss between two consecutives screenshot
	 */
	private String periodicity;
	private ScheduledJobStatus status; 
	private SelectedRectangle selectedRectangle;
	private boolean runPeriodically;
	
	/**
	 * Interval in hours between two consecutives tasks batch
	 */
	private int interval;

	public ScheduledJob(int id, String startAt, String endAt, String periodicity,SelectedRectangle selectedRectangle, boolean runTaskPeriodically, int interval) {
		setId(id);
		setStartAt(startAt);
		setEndAt(endAt);
		setPeriodicity(periodicity);
		setSelectedRectangle(selectedRectangle);
		setRunPeriodically(runTaskPeriodically);
		setInterval(interval);
	}

	public ScheduledJob() {
		
	}

	public ScheduledJob(int id, String startAt, String endAt, String periodicity, boolean runTaskPeriodically,
			int interval) {
		setId(id);
		setStartAt(startAt);
		setEndAt(endAt);
		setPeriodicity(periodicity);
		setRunPeriodically(runTaskPeriodically);
		setInterval(interval);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getStartAt() {
		return startAt;
	}

	public void setStartAt(String startAt) {
		this.startAt = startAt;
	}

	public String getEndAt() {
		return endAt;
	}

	public void setEndAt(String endAt) {
		this.endAt = endAt;
	}

	public String getPeriodicity() {
		return periodicity;
	}

	public void setPeriodicity(String periodicity) {
		this.periodicity = periodicity;
	}

	public ScheduledJobStatus getStatus() {
		return status;
	}

	public void setStatus(ScheduledJobStatus status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
		//return "Job_"+getId();
		Gson gson = new Gson();
		return gson.toJson(this);//"{\"id\":\"+getId()};
	}
	
	
	public String description() {
		return "ScheduledJob [id=" + id + ", startAt=" + startAt + ", endAt=" + endAt + ", periodicity=" + periodicity
				+ ", status=" + status + ", selectedRectangle=" + selectedRectangle + ", runPeriodically="
				+ runPeriodically + ", interval=" + interval + "]";
	}

	public SelectedRectangle getSelectedRectangle() {
		return selectedRectangle;
	}

	public void setSelectedRectangle(SelectedRectangle selectedRectangle) {
		this.selectedRectangle = selectedRectangle;
	}

	public boolean isRunPeriodically() {
		return runPeriodically;
	}

	public void setRunPeriodically(boolean runPeriodically) {
		this.runPeriodically = runPeriodically;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}
	
	public String getClazz() {
		return clazz;
	}
	
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
	
	
}
