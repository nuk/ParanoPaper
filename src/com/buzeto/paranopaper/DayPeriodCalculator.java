package com.buzeto.paranopaper;

import java.util.Calendar;
import java.util.TimeZone;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

public class DayPeriodCalculator {
	
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
	
	public String period(){
		long diffSunrise = now.getTimeInMillis() - sunrise.getTimeInMillis();
		diffSunrise = diffSunrise / 1000;
		if (Math.abs(diffSunrise) < 15*60){
			return "SUNRISE";
		}
		
		long diffSunset = now.getTimeInMillis() - sunset.getTimeInMillis();
		diffSunset = diffSunset / 1000;
		if (Math.abs(diffSunset) < 15*60){
			return "SUNSET";
		}
		
		if (diffSunrise > 0 && diffSunset < 0){
			return "DAY";
		}
		
		return "NIGHT";
	}
}
