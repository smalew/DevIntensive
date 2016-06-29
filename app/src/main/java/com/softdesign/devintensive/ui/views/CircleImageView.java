package com.softdesign.devintensive.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.RoundedAvatarDrawable;

/**
 * Created by koropenkods on 27.06.16.
 */
public class CircleImageView extends ImageView {

    private static final String TAG = ConstantManager.PREFIX_TAG + "CircleImageView";

    private float mXRadius;
    private float mYRadius;
    private float mRadius;

    public CircleImageView(Context context) {
        super(context);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setImageResource(int resId) {
        Bitmap avatar = BitmapFactory.decodeResource(getResources(), resId);
        this.setImageBitmap(avatar);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        RoundedAvatarDrawable drawableAvatar = new RoundedAvatarDrawable(bm);
        this.setImageDrawable(drawableAvatar);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
    }
}