package com.shane;

import com.shane.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameLoop extends SurfaceView implements Runnable,
		SurfaceHolder.Callback {

	public static final double INITIAL_TIME = 3;
	static final int REFRESH_RATE = 20;
	static final int GRAVITY = 1;
	static final int INITIAL_FUEL = 100;

	Thread main;

	Paint paint = new Paint();

	Bitmap background;

	int xcor[] = { 0, 200, 190, 218, 260, 275, 298, 309, 327, 336, 368, 382,
			448, 462, 476, 498, 527, 600, 600, 0, 0 };
	int ycor[] = { 616, 540, 550, 605, 605, 594, 530, 520, 520, 527, 626, 636,
			636, 623, 535, 504, 481, 481, 750, 750, 616 };

	Canvas offscreen;

	boolean downPressed = false;
	boolean leftPressed = false;
	boolean rightPressed = false;
	Boolean gameover = false;

	float x, y;
	int width = 0;
	int height = 0;

	double t = INITIAL_TIME;

	public GameLoop(Context context) {
		super(context);
		SoundContext = context;
		init();
	}

	public GameLoop(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		SoundContext = context;
		init();
	}

	public GameLoop(Context context, AttributeSet attrs) {
		super(context, attrs);
		SoundContext = context;
		init();
	}

	public void init() {
		getHolder().addCallback(this);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		width = w;
		height = h;

		x = width / 2;
	}

	// 0: freely, 1: left thruster, 2: main flame; 3: right thruster
	public int State = 0;
	Paint sPaint;
	Path path = null;

	private MediaPlayer mp;
	public Context SoundContext;

	private void drawTerrain(Canvas canvas, Paint paint) {
		if (path == null) {
			path = new Path();

			for (int i = 0; i < xcor.length; i++) {
				path.lineTo(xcor[i], ycor[i]);
			}
		}
		canvas.drawPath(path, paint);
		paint.setTextSize(20);
		canvas.drawText("Fuel: " + String.valueOf(Fuel), width / 2 - 60, 20,
				paint);
	}

	int StateCount = 0;
	int Fuel = 100;

	public void run() {
		while (true) {
			while (!gameover) {
				Canvas canvas = null;
				SurfaceHolder holder = getHolder();
				synchronized (holder) {
					canvas = holder.lockCanvas();

					canvas.drawColor(Color.BLACK);

					if (sPaint == null) {
						sPaint = new Paint();
						sPaint.setDither(true);
						sPaint.setColor(0xFFFFFF00);
						sPaint.setStyle(Paint.Style.STROKE);
						sPaint.setAntiAlias(true);
						sPaint.setStrokeWidth(3);

						Bitmap bmpShade = BitmapFactory.decodeResource(
								getResources(), R.drawable.mars);
						BitmapShader shader = new BitmapShader(bmpShade,
								Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
						sPaint.setColor(0xFFFFFFFF);
						sPaint.setStyle(Paint.Style.FILL);
						sPaint.setShader(shader);
					}
					drawTerrain(canvas, sPaint);

					// s = ut + 0.5 gt^2

					// not that the initial velocity (u) is zero so I have not
					// put ut into the code below
					if (State != 0) {
						StateCount++;
						if (StateCount > 3 || Fuel == 0) {
							StateCount = 0;
							State = 0;
						}
					}

					switch (State) {
					case 0:
						y = (int) y + (int) ((0.5 * (GRAVITY * t * t)));
						break;
					case 1:
						Fuel -= 4;
						x += 10;
						mp = MediaPlayer.create(SoundContext, R.drawable.sound);
						mp.start();
						break;
					case 2:
						Fuel -= 4;
						y -= 10;
						mp = MediaPlayer.create(SoundContext, R.drawable.sound);
						mp.start();
						break;
					case 3:
						Fuel -= 4;
						x -= 10;
						mp = MediaPlayer.create(SoundContext, R.drawable.sound);
						mp.start();
						break;
					}
					if (Fuel < 0)
						Fuel = 0;

					if (x < 0)
						x += width;
					if (x > width)
						x -= width;

					t = t + 0.01; // increment the parameter for synthetic time
									// by a small amount

					int CollisionState = DetectCollision(); // 0: fly; 1: Safely
															// landing; 2:
															// Crashed

					switch (CollisionState) {
					case 0:
						break;
					case 1: {
						Bitmap bitmapLanded = BitmapFactory.decodeResource(
								getContext().getResources(),
								R.drawable.real_craft);
						canvas.drawBitmap(bitmapLanded, x - 25, y - 25, paint);
					}
						gameover = true;

						break;
					case 2: {
						Bitmap bitmapCrash = BitmapFactory.decodeResource(
								getContext().getResources(),
								R.drawable.craft_crashed);
						canvas.drawBitmap(bitmapCrash, x - 25, y, paint);
					}
					mp = MediaPlayer.create(SoundContext, R.drawable.crashedsound);
					mp.start();
						gameover = true;
						break;
					}

					if (!gameover) {
						Bitmap bitmap = BitmapFactory.decodeResource(
								getContext().getResources(),
								R.drawable.real_craft);
						canvas.drawBitmap(bitmap, x - 25, y - 25, paint);
						if (State == 1) {
							Bitmap bitmapThruster = BitmapFactory
									.decodeResource(
											getContext().getResources(),
											R.drawable.thruster);
							canvas.drawBitmap(bitmapThruster, x - 25, y + 20,
									paint);
						} else if (State == 2) {
							Bitmap bitmapThruster = BitmapFactory
									.decodeResource(
											getContext().getResources(),
											R.drawable.main_flame);
							canvas.drawBitmap(bitmapThruster, x - 10, y + 20,
									paint);
						} else if (State == 3) {
							Bitmap bitmapThruster = BitmapFactory
									.decodeResource(
											getContext().getResources(),
											R.drawable.thruster);
							canvas.drawBitmap(bitmapThruster, x + 15, y + 20,
									paint);
						}
					}

					try {
						Thread.sleep(REFRESH_RATE);
					} catch (Exception e) {
					}

					finally {
						if (canvas != null) {
							holder.unlockCanvasAndPost(canvas);
						}
					}
				}
			}
		}
	}

	Region reg = null;

	private int DetectCollision() {

		if (path == null)
			return 0;

		int Lx = (int) (x - 25);
		int Ly = (int) (y + 20);
		int Rx = (int) (x + 25);
		int Ry = (int) (y + 20);

		if (reg == null) {
			Rect rcFrame = new Rect(0, 0, width, height);
			Region clip = new Region(rcFrame);
			reg = new Region();
			reg.setPath(path, clip);
		}

		boolean rL = reg.contains(Lx, Ly);
		boolean rR = reg.contains(Rx, Ry);

		if (!rL && !rR)
			return 0;
		if (rL && rR)
			return 1;
		return 2;
	}

	public boolean contains(int[] xcor, int[] ycor, double x0, double y0) {
		int crossings = 0;

		for (int i = 0; i < xcor.length - 1; i++) {
			int x1 = xcor[i];
			int x2 = xcor[i + 1];

			int y1 = ycor[i];
			int y2 = ycor[i + 1];

			int dy = y2 - y1;
			int dx = x2 - x1;

			double slope = 0;
			if (dx != 0) {
				slope = (double) dy / dx;
			}

			boolean cond1 = (x1 <= x0) && (x0 < x2); // is it in the range?
			boolean cond2 = (x2 <= x0) && (x0 < x1); // is it in the reverse
														// range?
			boolean above = (y0 < slope * (x0 - x1) + y1); // point slope y - y1

			if ((cond1 || cond2) && above) {
				crossings++;
			}
		}
		return (crossings % 2 != 0); // even or odd
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	public void surfaceCreated(SurfaceHolder holder) {
		main = new Thread(this);
		if (main != null)
			main.start();

	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		while (retry) {
			try {
				main.join();
				retry = false;
			} catch (InterruptedException e) {
				// try again shutting down the thread
			}
		}
	}

	public void reset() {
		gameover = false;

		x = width / 2;
		y = 0;
		t = 3;
		Fuel = 100;
	}

	public void FireLeft() {
		// TODO Auto-generated method stub
		State = 1;
	}

	public void FireUp() {
		// TODO Auto-generated method stub
		State = 2;
	}

	public void FireRight() {
		// TODO Auto-generated method stub
		State = 3;
	}
}
