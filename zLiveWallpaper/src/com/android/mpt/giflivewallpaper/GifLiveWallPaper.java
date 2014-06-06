package com.android.mpt.giflivewallpaper;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public class GifLiveWallPaper extends WallpaperService {

	static final String TAG = "LIVE_WALLPAPER";
	static final Handler liveHandler = new Handler();

	@Override
	public Engine onCreateEngine() {
		try {
			return new WallPaperEngine();
		} catch (IOException e) {
			Log.w(TAG, "Error creating WallPaperEngine", e);
			stopSelf();
			return null;
		}
	}

	class WallPaperEngine extends Engine {

		private Movie liveMovie;
		private int duration;
		private Runnable runnable;
		float mScaleX;
		float mScaleY;
		int mWhen;
		long mStart;

		public WallPaperEngine() throws IOException {

			InputStream is = getResources().openRawResource(R.raw.sam);

			if (is != null) {

				try {
					liveMovie = Movie.decodeStream(is);
					duration = liveMovie.duration();

				} finally {
					is.close();
				}
			} else {
				throw new IOException("Unable to open R.raw.hand");
			}
			mWhen = -1;
			runnable = new Runnable() {
				public void run() {
					nyan();
				}
			};
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			liveHandler.removeCallbacks(runnable);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			super.onVisibilityChanged(visible);
			if (visible) {
				nyan();
			} else {
				liveHandler.removeCallbacks(runnable);
			}
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
			mScaleX = width / (1f * liveMovie.width());
			mScaleY = height / (1f * liveMovie.height());
			nyan();
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
				float xOffsetStep, float yOffsetStep, int xPixelOffset,
				int yPixelOffset) {
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep,
					xPixelOffset, yPixelOffset);
			nyan();
		}

		void nyan() {
			tick();
			SurfaceHolder surfaceHolder = getSurfaceHolder();
			Canvas canvas = null;
			try {
				canvas = surfaceHolder.lockCanvas();
				if (canvas != null) {
					drawGif(canvas);
				}
			} finally {
				if (canvas != null) {
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
			liveHandler.removeCallbacks(runnable);
			if (isVisible()) {
				liveHandler.postDelayed(runnable, 1000L / 25L);
			}
		}

		void tick() {
			if (mWhen == -1L) {
				mWhen = 0;
				mStart = SystemClock.uptimeMillis();
			} else {
				long mDiff = SystemClock.uptimeMillis() - mStart;
				mWhen = (int) (mDiff % duration);
			}
		}

		void drawGif(Canvas canvas) {
			canvas.save();
			canvas.scale(mScaleX, mScaleY);
			liveMovie.setTime(mWhen);
			liveMovie.draw(canvas, 0, 0);
			canvas.restore();
		}
	}
}
