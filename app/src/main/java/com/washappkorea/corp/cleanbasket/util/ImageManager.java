package com.washappkorea.corp.cleanbasket.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

public class ImageManager {
	private static CircleTransformation sCircleTransformation;

	public static CircleTransformation getCircleTransformation() {
		if(sCircleTransformation == null) {
			sCircleTransformation = new CircleTransformation();
		}
		return sCircleTransformation;
	}

    public static class CircleTransformation implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, source.getWidth(), source.getHeight());
            final RectF rectF = new RectF(rect);
            final float roundPx = source.getScaledWidth(canvas)/2;

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(source, rect, rect, paint);

            if (output != source) {
                source.recycle();
            }
            return output;
        }

        @Override
        public String key() {
            return "square()";
        }
    }
}
