package com.shane;

import com.shane.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class MarsLander extends Activity {
	private GameLoop gameLoop;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main); // set the content view or our widget
										// lookups will fail

		gameLoop = (GameLoop) findViewById(R.id.gameLoop);

		final Button btnRestart = (Button) findViewById(R.id.btnRestart);
		btnRestart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gameLoop.reset();
				gameLoop.invalidate();
			}
		});

		final Button btnLeft = (Button) findViewById(R.id.btnLeft);
		btnLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gameLoop.FireLeft();
				gameLoop.invalidate();
			}
		});

		final Button btnUp = (Button) findViewById(R.id.btnUp);
		btnUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gameLoop.FireUp();
				gameLoop.invalidate();
			}
		});

		final Button btnRight = (Button) findViewById(R.id.btnRight);
		btnRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gameLoop.FireRight();
				gameLoop.invalidate();
			}
		});

	}
}