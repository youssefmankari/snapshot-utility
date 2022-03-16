package org.fajr.snapshot_utility;

import com.google.gson.Gson;

public class ScheduledJob {

	private int id;
	private String startAt;
	private String endAt;
	private String periodicity;
	private ScheduledJobStatus status; 
	private SelectedRectangle selectedRectangle;

	public ScheduledJob(int id, String startAt, String endAt, String periodicity,SelectedRectangle selectedRectangle) {
		setId(id);
		setStartAt(startAt);
		setEndAt(endAt);
		setPeriodicity(periodicity);
		setSelectedRectangle(selectedRectangle);
	}

	public ScheduledJob() {
		
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
				+ ", status=" + status + "]";
	}

	public SelectedRectangle getSelectedRectangle() {
		return selectedRectangle;
	}

	public void setSelectedRectangle(SelectedRectangle selectedRectangle) {
		this.selectedRectangle = selectedRectangle;
	}
}
