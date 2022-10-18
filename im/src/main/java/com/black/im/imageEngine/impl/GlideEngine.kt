package com.black.im.imageEngine.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.TextUtils
import android.widget.ImageView
import com.black.im.R
import com.black.im.imageEngine.ImageEngine
import com.black.im.util.TUIKit.appContext
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import skin.support.content.res.SkinCompatResources
import java.io.File
import java.util.concurrent.ExecutionException

class GlideEngine : ImageEngine {
    override fun loadThumbnail(context: Context, resize: Int, placeholder: Drawable?, imageView: ImageView?, uri: Uri?) {
        Glide.with(context)
                .asBitmap() // some .jpeg files are actually gif
                .load(uri)
                .apply(RequestOptions()
                        .override(resize, resize)
                        .placeholder(placeholder)
                        .centerCrop())
                .into(imageView!!)
    }

    override fun loadGifThumbnail(context: Context, resize: Int, placeholder: Drawable?, imageView: ImageView?,
                                  uri: Uri?) {
        Glide.with(context)
                .asBitmap() // some .jpeg files are actually gif
                .load(uri)
                .apply(RequestOptions()
                        .override(resize, resize)
                        .placeholder(placeholder)
                        .centerCrop())
                .into(imageView!!)
    }

    override fun loadImage(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView?, uri: Uri?) {
        Glide.with(context)
                .load(uri)
                .apply(RequestOptions()
                        .override(resizeX, resizeY)
                        .priority(Priority.HIGH)
                        .fitCenter())
                .into(imageView!!)
    }

    override fun loadGifImage(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView?, uri: Uri?) {
        Glide.with(context)
                .asGif()
                .load(uri)
                .apply(RequestOptions()
                        .override(resizeX, resizeY)
                        .priority(Priority.HIGH)
                        .fitCenter())
                .into(imageView!!)
    }

    override fun supportAnimatedGif(): Boolean {
        return true
    }

    companion object {
        fun loadCornerImage(imageView: ImageView?, filePath: String?, listener: RequestListener<Drawable>?, radius: Float) {
            val transform = CornerTransform(appContext, radius)
            val drawable = ColorDrawable(Color.GRAY)
            val options = RequestOptions()
                    .centerCrop()
                    .placeholder(drawable)
                    .transform(transform)
            Glide.with(appContext)
                    .load(filePath)
                    .apply(options)
                    .listener(listener)
                    .into(imageView!!)
        }

        fun loadImage(imageView: ImageView?, filePath: String?, listener: RequestListener<Drawable>?) {
            Glide.with(appContext)
                    .load(filePath)
                    .listener(listener)
                    .into(imageView!!)
        }

        fun loadProfileImage(imageView: ImageView?, filePath: String?, listener: RequestListener<Drawable>?) {
            Glide.with(appContext)
                    .load(filePath)
                    .listener(listener)
                    .apply(RequestOptions().error(R.drawable.default_user_icon))
                    .into(imageView!!)
        }

        fun clear(imageView: ImageView?) {
            Glide.with(appContext).clear(imageView!!)
        }

        fun loadImage(imageView: ImageView?, uri: Uri?) {
            if (uri == null) {
                return
            }
            Glide.with(appContext)
                    .load(uri)
                    .apply(RequestOptions().error(R.drawable.icon_coin_default))
                    .into(imageView!!)
        }

        fun loadImage(imageView: ImageView?, defaultBitmap: Bitmap?, uri: Uri?) {
            if (uri == null) {
                return
            }
            val options = RequestOptions()
            if (defaultBitmap != null) {
                options.error(BitmapDrawable(SkinCompatResources.getInstance().skinResources, defaultBitmap))
            } else {
                options.error(R.drawable.default_user_icon)
            }
            Glide.with(appContext)
                    .load(uri)
                    .apply(options)
                    .into(imageView!!)
        }

        fun loadImage(filePath: String?, url: String?) {
            try {
                val file = Glide.with(appContext).asFile().load(url).submit().get()
                val destFile = File(filePath)
                file.renameTo(destFile)
            } catch (e: InterruptedException) {
            } catch (e: ExecutionException) {
            }
        }

        @Throws(InterruptedException::class, ExecutionException::class)
        fun loadBitmap(imageUrl: String?, targetImageSize: Int): Bitmap? {
            return if (TextUtils.isEmpty(imageUrl)) {
                null
            } else Glide.with(appContext).asBitmap()
                    .load(imageUrl)
                    .apply(RequestOptions().error(R.drawable.default_user_icon))
                    .into(targetImageSize, targetImageSize)
                    .get()
        }

        @Throws(InterruptedException::class, ExecutionException::class)
        fun loadBitmap(imageUrl: String?, defaultBitmap: Bitmap?, targetImageSize: Int): Bitmap? {
            if (TextUtils.isEmpty(imageUrl)) {
                return null
            }
            val options = RequestOptions()
            if (defaultBitmap != null) {
                options.error(BitmapDrawable(SkinCompatResources.getInstance().skinResources, defaultBitmap))
            } else {
                options.error(R.drawable.default_user_icon)
            }
            options.override(targetImageSize)
            return Glide.with(appContext).asBitmap()
                    .load(imageUrl)
                    .apply(options)
                    .into(targetImageSize, targetImageSize)
                    .get()
        }
    }
}