package com.black.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片相关工具
 */

public class ImageUtil {
    private final static MemoryCache iconImageCache = new MemoryCache();
    private static int MAX_WIDTH;
    private static int MAX_HEIGHT;

    public static int getMaxWidth(Context context) {
        if (MAX_WIDTH == 0) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            MAX_WIDTH = (int) (90 * dm.density);
        }
        return MAX_WIDTH;
    }

    public static int getMaxHeight(Context context) {
        if (MAX_HEIGHT == 0) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            MAX_HEIGHT = (int) (160 * dm.density);
        }
        return MAX_HEIGHT;
    }

    public static Bitmap loadImgThumbnail(String filePath, int w, int h) {
        return getBitmapByPath(filePath, w, h);
    }

    public static void putCacheIcon(String key, Bitmap icon) {
        iconImageCache.put(key, icon);
    }

    public static Bitmap getCacheIcon(String key) {
        return iconImageCache.get(key);
    }

    /**
     * 获取bitmap
     *
     * @param filePath
     * @return
     */
    public static Bitmap getBitmapByPath(String filePath) {
        return getBitmapByPath(filePath, null);
    }

    public static Bitmap getBitmapByPath(String filePath, BitmapFactory.Options opts) {
        return getBitmapByPath(filePath, opts, 0, 0);
    }

    public static Bitmap getBitmapByPath(String filePath, int width, int height) {
        return getBitmapByPath(filePath, null, width, height);
    }

    public static Bitmap getBitmapByPath(String filePath, BitmapFactory.Options opts, int width, int height) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                BitmapFactory.Options op = new BitmapFactory.Options();
                int sampleSize = 1;
                if (width > 0 && height > 0) {
                    op.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(filePath, op);
                    if (width < op.outWidth || height < op.outHeight) {
                        final int halfW = op.outWidth / 2;
                        final int halfH = op.outHeight / 2;
                        while (halfH / sampleSize > height || halfW / sampleSize > width) {
                            sampleSize *= 2;
                        }
                    }
                }
                if (opts != null) {
                    op = opts;
                }
                op.inJustDecodeBounds = false;
                op.inSampleSize = sampleSize;
                if (sampleSize == 1) {
                    return BitmapFactory.decodeFile(filePath);
                } else {
                    return BitmapFactory.decodeFile(filePath, op);
                }
            }
            return null;
        } catch (OutOfMemoryError e) {
        }
        return null;
    }

    public static Bitmap getBitmapBySteam(Context context, InputStream inputStream) {
        try {
            byte[] bytes = CommonUtil.inputStreamTOByte(inputStream);
            ByteArrayInputStream stream1 = new ByteArrayInputStream(bytes);
            ByteArrayInputStream stream2 = new ByteArrayInputStream(bytes);
//            ByteArrayInputStream byteArrayInputStream = CommonUtil.getByteArrayInputStream(inputStream);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(stream1, null, options);
            // 调用上面定义的方法计算inSampleSize值
            options.inSampleSize = calculateInSampleSize(options, getMaxWidth(context), getMaxHeight(context));
            // 使用获取到的inSampleSize值再次解析图片
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(stream2, null, options);
        } catch (Exception e) {
            CommonUtil.printError(context, e);
        }
        return null;
    }

    public static Bitmap getBitmapBySteam(InputStream inputStream, int maxWidth, int maxHeight) {
        try {
            byte[] bytes = CommonUtil.inputStreamTOByte(inputStream);
            ByteArrayInputStream stream1 = new ByteArrayInputStream(bytes);
            ByteArrayInputStream stream2 = new ByteArrayInputStream(bytes);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(stream1, null, options);
            // 调用上面定义的方法计算inSampleSize值
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
            // 使用获取到的inSampleSize值再次解析图片
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(stream2, null, options);
        } catch (Exception e) {
        }
        return null;
    }

    public static Bitmap getBitmapBySteam(InputStream inputStream, long maxLength) {
        try {
            byte[] bytes = CommonUtil.inputStreamTOByte(inputStream);
            ByteArrayInputStream stream1 = new ByteArrayInputStream(bytes);
            ByteArrayInputStream stream2 = new ByteArrayInputStream(bytes);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(stream1, null, options);
            // 调用上面定义的方法计算inSampleSize值
            options.inSampleSize = calculateInSampleSize(options, maxLength);
            // 使用获取到的inSampleSize值再次解析图片
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(stream2, null, options);
        } catch (Exception e) {
        }
        return null;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        return calculateInSampleSize(width, height, reqWidth, reqHeight);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, long maxLength) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        long length = width * height;
        return calculateInSampleSize(width, height, ((int) (width * maxLength / length)), (int) (height * maxLength / length));
    }

    public static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static void saveImage(String filePath, String fileName, Bitmap bitmap) throws IOException {
        File imageFile = new File(filePath);
        if (!imageFile.exists()) {
            imageFile.mkdirs();
        }
        saveImage(new File(filePath, fileName), bitmap, 100);
    }

    public static void saveImage(File file, Bitmap bitmap, int quality) throws IOException {
        if (bitmap == null || file == null) {
            return;
        }
        FileOutputStream fos = new FileOutputStream(file);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        byte[] bytes = stream.toByteArray();
        fos.write(bytes);
        fos.close();
    }

    /**
     * 创建圆图
     *
     * @param resources
     * @param resId
     * @return
     */
    public Bitmap createCircleBitmap(Resources resources, int resId) {
        Bitmap resBm = createCircleBitmap(BitmapFactory.decodeResource(resources, resId));
        if (resBm != null) {
            Bitmap bitmap = createCircleBitmap(resBm);
            resBm.recycle();
            resBm = null;
            return bitmap;
        }
        return null;
    }

    /**
     * 创建圆图
     *
     * @param resBm
     * @return
     */
    public Bitmap createCircleBitmap(Bitmap resBm) {
        if (resBm == null) {
            return null;
        }
        BitmapShader mBitmapShader = new BitmapShader(resBm, android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT);
        int width = resBm.getWidth();
        int height = resBm.getHeight();
        int size = Math.min(width, height);
        float mRadius = size / 2;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(mBitmapShader);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(mRadius, mRadius, mRadius, paint);
        return bitmap;
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
            }
        }
        return result;
    }

    /**
     * base64转为bitmap
     *
     * @param base64Data
     * @return
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static Bitmap getImageViewBitmap(ImageView imageView) {
        if (imageView == null) {
            return null;
        }
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        if (drawable == null) {
            return null;
        }
        return drawable.getBitmap();
    }

    public static long getBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        Bitmap newBmp = null;
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Matrix matrix = new Matrix();
            float scaleWidth = ((float) w / width);
            float scaleHeight = ((float) h / height);
            matrix.postScale(scaleWidth, scaleHeight);
            newBmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        }
        return newBmp;
    }

    public static void saveImageToSysGallery(Context context, String picName, Bitmap bitmap) throws IOException {
        String fileName = null;
        //系统相册目录
        String galleryPath = Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator + "Camera" + File.separator;


        // 声明文件对象
        File file = null;
        // 声明输出流
        FileOutputStream outStream = null;

        try {
            // 如果有目标文件，直接获得文件对象，否则创建一个以filename为名称的文件
            file = new File(galleryPath, picName + ".jpg");

            // 获得文件相对路径
            fileName = file.toString();
            // 获得输出流，如果文件中有内容，追加内容
            outStream = new FileOutputStream(fileName);
            if (null != outStream) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
            }

        } catch (Exception e) {
            CommonUtil.printError(context, e);
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                CommonUtil.printError(context, e);
            }

            //通知相册更新
            MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, fileName, null);
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            context.sendBroadcast(intent);
        }

    }

    public static byte[] getBitmapBytes(Bitmap bitmap) {
        if (bitmap != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                return baos.toByteArray();
            } catch (Exception e) {

            }
        }
        return null;
    }

    public static Size getMaxSize(Bitmap bitmap, long maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        return getMaxSize(maxSize, (float) width / height);
    }

    public static Size getMaxSize(long maxLength, float ratio) {
        if (maxLength <= 0 || ratio <= 0) {
            return null;
        }
        int scale = (int) Math.sqrt((double) maxLength / ratio);
        return new Size((int) (scale * ratio), scale);
    }

    public static Bitmap getBitmapFromRes(Context context, int resId) {
        try {
            return BitmapFactory.decodeResource(context.getResources(), resId);
        } catch (Exception e) {
            return null;
        }
    }
}