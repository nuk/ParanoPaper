package com.buzeto.paranopaper;

import java.util.HashMap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;

import com.buzeto.paranopaper.DayPeriodCalculator.Phase;
import com.buzeto.paranopaper.DayPeriodCalculator.Status;

class BackgroundPainter {
	int width;
	int height;
	private HashMap<Phase, PeriodPainter> painterMap;
	
	@SuppressWarnings("serial")
	public BackgroundPainter(final int width, final int height, final Resources r) {
		super();
		this.width = width;
		this.height = height;
		this.painterMap = new HashMap<Phase, PeriodPainter>(){{
			put(Phase.SUNRISE,new SunrisePainter(width, height, r));
			put(Phase.DAY,new DayPainter(width, height, r));
			put(Phase.SUNSET,new SunsetPainter(width, height, r));
			put(Phase.NIGHT,new NightPainter(width, height, r));
		}};
	}

	void paint(Location location, int xOffset, Canvas canvas) {
		DayPeriodCalculator c = new DayPeriodCalculator(location);
		Status period = c.period();
		PeriodPainter painter = painterMap.get(period.phase);
		painter.paint(canvas, xOffset, period);
	}

}

abstract class PeriodPainter {
	int width;
	int height;
	Resources r;

	public PeriodPainter(int width, int height, Resources r) {
		this.width = width;
		this.height = height;
		this.r = r;
	}
	
	public abstract void paint(Canvas canvas, int xOffset, Status status);
	
	int calculateAlpha(Status period) {
		int alpha = (int)(127 + period.periodHigh()*128);
		return alpha;
	}

	void drawBackGround(Canvas canvas, int color, int alpha) {
		Paint paint = createPaint(color,alpha);
		canvas.drawRect(0, 0, width, height, paint);
	}

	private Paint createPaint(int color, int alpha) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(color);
		paint.setAlpha(alpha);
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeWidth(10f);
		return paint;
	}
}

class SunrisePainter extends PeriodPainter{
	public SunrisePainter(int width, int height, Resources r) {
		super(width, height, r);
	}

	public void paint(Canvas canvas, int xOffset ,Status status) {
		drawBackGround(canvas, Color.WHITE, 128);
		int pumpkin = Color.rgb(211, 84, 0);
		drawBackGround(canvas, pumpkin, calculateAlpha(status));
	}
}

class DayPainter extends PeriodPainter{
	private Bitmap cloud_img;
	private Bitmap sun_img;

	public DayPainter(int width, int height, Resources r) {
		super(width, height, r);
		cloud_img = BitmapFactory.decodeResource(r, R.drawable.cloud);
		sun_img = BitmapFactory.decodeResource(r, R.drawable.sun);
	}

	public void paint(Canvas canvas, int xOffset, Status status) {
		drawBackGround(canvas, Color.WHITE, 128);
		int peterRiver = Color.rgb(52, 152, 219);
		drawBackGround(canvas, peterRiver, calculateAlpha(status));
		drawCloud(canvas, xOffset);
		drawSun(canvas, xOffset, status);
	}

	private void drawSun(Canvas canvas, int xOffset, Status status) {
		int x = xOffset+width/2+(int)(sun_img.getWidth()*1.5);
		int minY = height/2-sun_img.getHeight()/2;
		int maxY = height/2-sun_img.getHeight()*2;
		int y = (int)((minY+maxY)*status.periodHigh());
		canvas.drawBitmap(sun_img,x,y, null);
	}

	private void drawCloud(Canvas canvas, int xOffset) {
		int x = xOffset+width/2-cloud_img.getWidth()/2;
		int y = height/2-cloud_img.getHeight();
		canvas.drawBitmap(cloud_img,x,y, null);
	}
}

class SunsetPainter extends PeriodPainter{
	public SunsetPainter(int width, int height, Resources r) {
		super(width, height, r);
	}

	public void paint(Canvas canvas, int xOffset, Status status) {
		drawBackGround(canvas, Color.WHITE, 128);
		int pomegranate = Color.rgb(211, 84, 0);
		drawBackGround(canvas, pomegranate, calculateAlpha(status));
	}
}

class NightPainter extends PeriodPainter{
	private Bitmap star_img;
	private Bitmap moon_img;

	public NightPainter(int width, int height, Resources r) {
		super(width, height, r);
		star_img = BitmapFactory.decodeResource(r, R.drawable.star);
		star_img = Bitmap.createScaledBitmap(star_img, star_img.getWidth()/10, star_img.getHeight()/10, false);
		moon_img = BitmapFactory.decodeResource(r, R.drawable.moon);
	}

	public void paint(Canvas canvas, int xOffset, Status status) {
		drawBackGround(canvas, Color.WHITE, 128);
		int midnightBlue= Color.rgb(44, 62, 80);
		drawBackGround(canvas, midnightBlue, calculateAlpha(status));
		drawStars(canvas, xOffset);
		drawMoon(canvas, xOffset, status);
	}
	
	private void drawMoon(Canvas canvas, int xOffset, Status status) {
		int x = xOffset+width/2+(int)(moon_img.getWidth()*1.5);
		int minY = height/2-moon_img.getHeight()/2;
		int maxY = height/2-moon_img.getHeight()*2;
		int y = (int)((minY+maxY)*(1-status.periodHigh()));
		canvas.drawBitmap(moon_img,x,y, null);
	}

	private void drawStars(Canvas canvas, int xOffset) {
		int x = xOffset+width/2-star_img.getWidth()/2;
		int y = height/2-star_img.getHeight();
		canvas.drawBitmap(star_img,x,y, null);
		canvas.drawBitmap(star_img,x+100,y-100, null);
		canvas.drawBitmap(star_img,x-100,y-300, null);
		canvas.drawBitmap(star_img,x+width,y-300, null);
		canvas.drawBitmap(star_img,x+width+100,y-200, null);
	}
}