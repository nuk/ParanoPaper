package com.buzeto.paranopaper;

import java.util.Calendar;
import java.util.TimeZone;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

public class DayPeriodCalculator {
	
	enum Phase { SUNRISE, DAY, SUNSET, NIGHT} 
	
	private Calendar sunrise;
	private Calendar sunset;
	private Calendar now;

	public DayPeriodCalculator(android.location.Location location) {
		now = Calendar.getInstance();
		if (location != null){
			com.luckycatlabs.sunrisesunset.dto.Location loc = new Location(location.getLatitude(), location.getLongitude());
			SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(loc,TimeZone.getDefault().getID());
			
			sunrise = calculator.getOfficialSunriseCalendarForDate(now);
			sunset = calculator.getOfficialSunsetCalendarForDate(now);
		}else{
			sunrise = (Calendar) now.clone();
			sunrise.set(Calendar.MINUTE,0);
			sunrise.set(Calendar.HOUR_OF_DAY,6);
			
			sunset = (Calendar) now.clone();
			sunset.set(Calendar.MINUTE,0);
			sunset.set(Calendar.HOUR_OF_DAY,18);
		}
		
	}
	
	static class Status {
		Phase phase;
		int periodLenghInMinutes;
		int periodPositionInMinutes;
	}
	
	public Status period(){
		long diffSunriseInSeconds = diffInSeconds(now, sunrise);
		long diffSunsetInSeconds = diffInSeconds(now, sunset);
		
		Status status = new Status();
		
		if (Math.abs(diffSunriseInSeconds) < 15*60){
			status.phase = Phase.SUNRISE;
			status.periodLenghInMinutes = 30;
			status.periodPositionInMinutes = (int)(diffSunriseInSeconds + 15*60)/60;
		}else if (Math.abs(diffSunsetInSeconds) < 15*60){
			status.phase = Phase.SUNSET;
			status.periodLenghInMinutes = 30;
			status.periodPositionInMinutes = (int)(diffSunsetInSeconds + 15*60)/60;
		}else if (diffSunriseInSeconds > 0 && diffSunsetInSeconds < 0){
			status.phase = Phase.DAY;
			status.periodLenghInMinutes = (int)(diffSunriseInSeconds - diffSunsetInSeconds)/60;
			status.periodPositionInMinutes = (int)(diffSunriseInSeconds)/60;
		}else{
			status.phase = Phase.NIGHT;
			status.periodLenghInMinutes = (int)(diffSunsetInSeconds - diffSunriseInSeconds)/60;
			status.periodPositionInMinutes = (int)(diffSunsetInSeconds)/60;
		}
		
		return status;
	}
	
	private long diffInSeconds(Calendar c1, Calendar c2){
		long diffSunrise = c1.getTimeInMillis() - c2.getTimeInMillis();
		return diffSunrise / 1000;
	}
}
