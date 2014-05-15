package com.buzeto.paranopaper;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class ParanopaperService extends WallpaperService {

	private WallpaperEngine wallpaperEngine = new WallpaperEngine();
	private Intent batteryStatus;;

	@Override
	public Engine onCreateEngine() {
		instalLocationListener();
		installBatteryMonitor();
		return wallpaperEngine;
	}

	private void installBatteryMonitor() {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		batteryStatus = this.registerReceiver(null, ifilter);
	}
	
	private float batteryLevel(){
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		return level / (float)scale;
	}

	void instalLocationListener(){
		LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_COARSE);
		long minTime = 10*60*1000; // millis
		long minDistance = 500*1000; // meters 
		manager.requestLocationUpdates(minTime, minDistance, c, 
				new LocationListener() {
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			public void onProviderEnabled(String provider) {}
			public void onProviderDisabled(String provider) {}
			public void onLocationChanged(Location location) {
				wallpaperEngine.location = location;
			}
		}, null);
		
	}
	
	class WallpaperEngine extends Engine {
		private final Handler handler = new Handler();
		private final Runnable drawRunner = new Runnable() {
			public void run() {
				draw(40);
			}

		};

		Location location;
		private int width;
		private int height;
		private boolean visible = true;
		
		int offset = 0 ;
		private BackgroundPainter backgroundPainter;
		
		Bitmap background ;
		private Bitmap battery_low_image;
		private Bitmap battery_medium_image;
		private Bitmap battery_full_image;
		private Bitmap cloud_img;

		public WallpaperEngine() {
			handler.post(drawRunner);
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			background = BitmapFactory.decodeResource(getResources(), R.drawable.bkg_pre_v3);
			
			battery_low_image = BitmapFactory.decodeResource(getResources(), R.drawable.small_twig_v2);
			battery_low_image = Bitmap.createScaledBitmap(battery_low_image, battery_low_image.getWidth()/3, battery_low_image.getHeight()/3, false);
			battery_medium_image = BitmapFactory.decodeResource(getResources(), R.drawable.medium_tree_v2);
			battery_medium_image = Bitmap.createScaledBitmap(battery_medium_image, battery_medium_image.getWidth()/3, battery_medium_image.getHeight()/3, false);
			battery_full_image = BitmapFactory.decodeResource(getResources(), R.drawable.big_tree_v3);
			battery_full_image = Bitmap.createScaledBitmap(battery_full_image, battery_full_image.getWidth()/3, battery_full_image.getHeight()/3, false);
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
			backgroundPainter = new BackgroundPainter(width, height, getResources());
			super.onSurfaceChanged(holder, format, width, height);
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
				float xOffsetStep, float yOffsetStep, int xPixelOffset,
				int yPixelOffset) {
			offset = xPixelOffset;
		}

		private void draw(long delayInMillis) {
			SurfaceHolder holder = getSurfaceHolder();
			Canvas canvas = null;
			try {
				canvas = holder.lockCanvas();
				if (canvas != null){
					canvas.drawColor(Color.WHITE, Mode.CLEAR);
					backgroundPainter.paint(location, offset, canvas);
					drawLandscapeImage(canvas);
					
					Bitmap img = battery_medium_image;
					if (batteryLevel() <= 0.30){
						img = battery_low_image;
					}else if (batteryLevel() >= 0.80){
						img = battery_full_image;
					}
					
					canvas.drawBitmap(img, 
										offset+width/2+img.getWidth()/2, 
										height/2-img.getHeight()/2+35, null);
					
				}
			} catch(Exception e){
				Log.e("error", "",e);
			}
			finally {
				if (canvas != null)
					holder.unlockCanvasAndPost(canvas);
			}
			handler.removeCallbacks(drawRunner);
			if (visible) {
				handler.postDelayed(drawRunner,delayInMillis); //TODO: parametrize
			}
		}

		private void drawLandscapeImage(Canvas canvas) {
			canvas.drawBitmap(background, offset-(background.getWidth()/5)+width/5, 100, null);
		}
	}
}


