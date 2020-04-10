package com.picovr.piconativeplayerdemo.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.io.IOException;
import java.io.InputStream;

public class TextureUtil {

    public static int initTexture(Resources resources, int drawableId) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        Bitmap bitmapTmp = BitmapFactory.decodeResource(resources, drawableId);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapTmp, 0);
        bitmapTmp.recycle();
        return textureId;
    }

    public static int initTexture(Context ctx, int drawableId) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        InputStream is = ctx.getResources().openRawResource(drawableId);
        Bitmap bitmapTmp;
        try {
            bitmapTmp = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapTmp, 0);
        bitmapTmp.recycle();
        return textureId;
    }

    public static int initTextureA(Resources resources, int drawableId) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        Bitmap bitmapTmp = null;
        bitmapTmp = BitmapFactory.decodeResource(resources, drawableId);
        if (bitmapTmp == null) {
            return 0;
        }
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapTmp, 0);
        bitmapTmp.recycle();
        return textureId;
    }

    public static int initTextureFile(Context context, String filePath) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        Bitmap bitmapTmp = null;
        Uri uri = Uri.parse(filePath);
        String scheme = uri.getScheme();
        if (scheme == null) {
            bitmapTmp = BitmapFactory.decodeFile(filePath);
        } else if (scheme.equals("file")) {
            bitmapTmp = BitmapFactory.decodeFile(uri.getPath());
        } else if (scheme.equals("content")) {

            AssetFileDescriptor fd = null;
            try {
                ContentResolver resolver = context.getContentResolver();
                fd = resolver.openAssetFileDescriptor(uri, "r");
                if (fd == null) {
                    return 0;
                }
                bitmapTmp = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor());
                if (fd != null) {
                    fd.close();
                }
            } catch (IOException ex) {
            }
        } else {
            return 0;
        }

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapTmp, 0);
        bitmapTmp.recycle();
        return textureId;
    }

    public static int initTexture(Bitmap bitmap) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        return textureId;
    }

    public static int initTextureWithNoRecycleBitmap(Bitmap bitmap) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        return textureId;
    }

    public static void changeTexture(int textureId, Bitmap bitmap) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
    }

    public static Bitmap makeTextBitmap(String text, int color, int width, int height) {
        if (null == text) {
            throw new IllegalArgumentException();
        }
        TextPaint mTextPaint = new TextPaint();
        String familyName = "";
        mTextPaint.setAntiAlias(true);
        Typeface font = Typeface.create(familyName, Typeface.BOLD);
        mTextPaint.setTypeface(font);
        mTextPaint.setAlpha(255);
        mTextPaint.setTextSize(24);
        mTextPaint.setColor(color);
        Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(mBitmap);
        mCanvas.drawBitmap(mBitmap, 0, 0, mTextPaint);
        StaticLayout sl = new StaticLayout(text, mTextPaint, mBitmap.getWidth() - 3, Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        mCanvas.translate(0, 0);
        sl.draw(mCanvas);
        return mBitmap;
    }

    public static Bitmap makeBGTextBitmap(String text, int color, int width, int height, Context mcontext, int resourceid, boolean isright) {
        if (null == text) {
            throw new IllegalArgumentException();
        }
        TextPaint mTextPaint = new TextPaint();
        String familyName = "";
        mTextPaint.setAntiAlias(true);
        Typeface font = Typeface.create(familyName, Typeface.BOLD);
        mTextPaint.setTypeface(font);
        mTextPaint.setAlpha(255);
        mTextPaint.setTextSize(34);
        mTextPaint.setColor(color);
        Resources res = mcontext.getResources();

        Bitmap bmp = BitmapFactory.decodeResource(res, resourceid);
        Bitmap mBitmap = ThumbnailUtils.extractThumbnail(bmp, width + 300, height + 30);
        Canvas mCanvas = new Canvas(mBitmap);
        mCanvas.drawBitmap(mBitmap, 0, 0, mTextPaint);

        StaticLayout sl = new StaticLayout(text, mTextPaint, mBitmap.getWidth() - 10, Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        if (isright) {
            mCanvas.translate(50, 0);
        } else {
            mCanvas.translate(0, 0);
        }
        sl.draw(mCanvas);
        if (!bmp.isRecycled()) {
            bmp.recycle();
        }
        return mBitmap;
    }

    public static Bitmap getTextBitmap(String text, int color, int width, int height) {

        if (null == text) {
            throw new IllegalArgumentException();
        }

        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        Typeface font = Typeface.defaultFromStyle(Typeface.NORMAL);
        textPaint.setTypeface(font);
        textPaint.setAlpha(255);
        textPaint.setTextSize(24);
        textPaint.setColor(color);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, 0, 0, textPaint);

        StaticLayout sl = new StaticLayout(text, textPaint, bitmap.getWidth() - 3, Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        canvas.translate(0, 0);
        sl.draw(canvas);

        return bitmap;
    }

    public static Bitmap makeTextBitmap(String text, int textSize, int color, int bgColor, int width, int height) {

        if (null == text) {
            throw new IllegalArgumentException();
        }

        TextPaint mTextPaint = new TextPaint();
        String familyName = "";
        mTextPaint.setAntiAlias(true);
        Typeface font = Typeface.create(familyName, Typeface.BOLD);
        mTextPaint.setTypeface(font);
        mTextPaint.setAlpha(255);
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(color);

        Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(bgColor);
        mCanvas.drawBitmap(mBitmap, 0, 0, mTextPaint);

        StaticLayout sl = new StaticLayout(text, mTextPaint, mBitmap.getWidth() - 3, Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        mCanvas.translate(0, 0);
        sl.draw(mCanvas);
        return mBitmap;
    }

    public static int createTexture3D() {
        final int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        if (textures[0] == 0) {
            return 0;
        }
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        return textures[0];
    }
}
