package com.buzeto.paranopaper;

import java.util.Calendar;
import java.util.TimeZone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

public class ParanopaperService extends WallpaperService {

	private WallpaperEngine wallpaperEngine = new WallpaperEngine();;

	@Override
	public Engine onCreateEngine() {
		instalLocationListener();
		return wallpaperEngine;
	}

	void instalLocationListener(){
		LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_COARSE);
		long minTime = 10*60*1000; // millis
		long minDistance = 500*1000; // meters 
		manager.requestLocationUpdates(minTime, minDistance, c, 
				new LocationListener() {
			public void onStatusChanged(String provider, int status, Bundle extras) {
				System.out.println("Status: "+provider+", "+status);
			}
			public void onProviderEnabled(String provider) {
				System.out.println("Provider: "+provider);
			}
			public void onProviderDisabled(String provider) {
				System.out.println("Disabled: "+provider);
			}
			public void onLocationChanged(Location location) {
				wallpaperEngine.location = location;
				System.out.println("Changed location");
			}
		}, null);
		
	}
	
	class WallpaperEngine extends Engine {
		private final Handler handler = new Handler();
		private final Runnable drawRunner = new Runnable() {
			public void run() {
				draw();
			}

		};

		Location location;
		private int width;
		private int height;
		private boolean visible = true;

		public WallpaperEngine() {
			handler.post(drawRunner);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			this.visible = visible;
			if (visible) {
				handler.post(drawRunner);
			} else {
				handler.removeCallbacks(drawRunner);
			}
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			this.visible = false;
			handler.removeCallbacks(drawRunner);
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			this.width = width;
			this.height = height;
			super.onSurfaceChanged(holder, format, width, height);
		}

		int count = 0 ;
		private void draw() {
			SurfaceHolder holder = getSurfaceHolder();
			Canvas canvas = null;
			try {
				canvas = holder.lockCanvas();
				if (canvas != null){
					new BackgroundPainter(width, height).paint(location, canvas);
				}
			} finally {
				if (canvas != null)
					holder.unlockCanvasAndPost(canvas);
			}
			handler.removeCallbacks(drawRunner);
			if (visible) {
				handler.postDelayed(drawRunner, 5000);
			}
		}

		
	}
}

class BackgroundPainter {
	int width;
	int height;
	
	public BackgroundPainter(int width, int height) {
		super();
		this.width = width;
		this.height = height;
	}

	enum MyColor {
		POMEGRANATE(Color.rgb(211, 84, 0)),
		PUMPKIN(Color.rgb(211, 84, 0)),
		BELIZE_HOLE(Color.rgb(41, 128, 185)),
		PETER_RIVER(Color.rgb(52, 152, 219)),
		WET_ASPHALT(Color.rgb(52, 73, 94)),
		MIDNIGHT_BLUE(Color.rgb(44, 62, 80)),
		;
		
		private int intColor;
		MyColor(int intColor){
			this.intColor = intColor;
		}
	}
	
	void paint(Location location, Canvas canvas) {
		DayPeriodCalculator c = new DayPeriodCalculator(location);
		System.out.println(c.period());
		if (c.period() == "SUNRISE"){
			drawBackGround(canvas, MyColor.PUMPKIN.intColor);
		}else if (c.period() == "DAY"){
			drawBackGround(canvas, MyColor.PETER_RIVER.intColor);
		}else if (c.period() == "SUNSET"){
			drawBackGround(canvas, MyColor.POMEGRANATE.intColor);
		}else if (c.period() == "NIGHT"){
			drawBackGround(canvas, MyColor.MIDNIGHT_BLUE.intColor);
		}
	}

	private void drawBackGround(Canvas canvas, int color) {
		Paint paint = createPaint(color);
		canvas.drawRect(0, 0, width, height, paint);
	}

	private Paint createPaint(int color) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(color);
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeWidth(10f);
		return paint;
	}
}


