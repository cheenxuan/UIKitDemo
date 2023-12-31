package com.tech.android.base.camerakit.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * @auther: xuan
 * @date  : 2023/10/23 .
 * <P>
 * Description:
 * <P>
 */
object ImageUtil {

    private const val TAG = "CameraExif"

    fun exifToDegrees(exifOrientation: Int): Int {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270
        }
        return 0
    }

    // Returns the degrees in clockwise. Values are 0, 90, 180, or 270.
    fun getOrientation(jpeg: ByteArray?): Int {
        if (jpeg == null) {
            return 0
        }
        var offset = 0
        var length = 0

        // ISO/IEC 10918-1:1993(E)
        while (offset + 3 < jpeg.size && jpeg[offset++].toInt() and 0xFF == 0xFF) {
            val marker = jpeg[offset].toInt() and 0xFF

            // Check if the marker is a padding.
            if (marker == 0xFF) {
                continue
            }
            offset++

            // Check if the marker is SOI or TEM.
            if (marker == 0xD8 || marker == 0x01) {
                continue
            }
            // Check if the marker is EOI or SOS.
            if (marker == 0xD9 || marker == 0xDA) {
                break
            }

            // Get the length and check if it is reasonable.
            length = pack(jpeg, offset, 2, false)
            if (length < 2 || offset + length > jpeg.size) {
                Log.e(TAG, "Invalid length")
                return 0
            }

            // Break if the marker is EXIF in APP1.
            if (marker == 0xE1
                && length >= 8
                && pack(jpeg, offset + 2, 4, false) == 0x45786966
                && pack(jpeg, offset + 6, 2, false) == 0
            ) {
                offset += 8
                length -= 8
                break
            }

            // Skip other markers.
            offset += length
            length = 0
        }

        // JEITA CP-3451 Exif Version 2.2
        if (length > 8) {
            // Identify the byte order.
            var tag = pack(jpeg, offset, 4, false)
            if (tag != 0x49492A00 && tag != 0x4D4D002A) {
                Log.e(TAG, "Invalid byte order")
                return 0
            }
            val littleEndian = tag == 0x49492A00

            // Get the offset and check if it is reasonable.
            var count = pack(jpeg, offset + 4, 4, littleEndian) + 2
            if (count < 10 || count > length) {
                Log.e(TAG, "Invalid offset")
                return 0
            }
            offset += count
            length -= count

            // Get the count and go through all the elements.
            count = pack(jpeg, offset - 2, 2, littleEndian)
            while (count-- > 0 && length >= 12) {
                // Get the tag and check if it is orientation.
                tag = pack(jpeg, offset, 2, littleEndian)
                if (tag == 0x0112) {
                    // We do not really care about type and count, do we?
                    val orientation = pack(jpeg, offset + 8, 2, littleEndian)
                    return when (orientation) {
                        1 -> 0
                        3 -> 180
                        6 -> 90
                        8 -> 270
                        else -> 0
                    }
                }
                offset += 12
                length -= 12
            }
        }
        Log.i(TAG, "Orientation not found")
        return 0
    }

    private fun pack(
        bytes: ByteArray, offset: Int, length: Int,
        littleEndian: Boolean,
    ): Int {
        var offset = offset
        var length = length
        var step = 1
        if (littleEndian) {
            offset += length - 1
            step = -1
        }
        var value = 0
        while (length-- > 0) {
            value = value shl 8 or (bytes[offset].toInt() and 0xFF)
            offset += step
        }
        return value
    }

    fun resize(
        inputPath: String?,
        outputPath: String?,
        dstWidth: Int,
        dstHeight: Int,
        quality: Int = 100,
        imgSize: Int = 300,
    ) {
        try {
            var tempQuality = quality
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(inputPath, options)
            val m = Matrix()
            val exif = ExifInterface(inputPath!!)
            val rotation = exif.getAttributeInt("Orientation", 1)
            if (rotation != 0) {
                m.preRotate(exifToDegrees(rotation).toFloat())
            }
            val maxPreviewImageSize = Math.max(dstWidth, dstHeight)
            var size = Math.min(options.outWidth, options.outHeight)
            size = Math.min(size, maxPreviewImageSize)
            options.inSampleSize = calculateInSampleSize(options, size, size)
            options.inScaled = true
            options.inDensity = options.outWidth
            options.inTargetDensity = size * options.inSampleSize
            options.inJustDecodeBounds = false
            var roughBitmap = BitmapFactory.decodeFile(inputPath, options)
            val out = FileOutputStream(outputPath)
            val baos = ByteArrayOutputStream()
            try {
                roughBitmap!!.compress(Bitmap.CompressFormat.JPEG, tempQuality, baos)
                while (baos.toByteArray().size / 1024 > imgSize) {
                    baos.reset()
                    roughBitmap.compress(Bitmap.CompressFormat.JPEG, tempQuality, baos)
                    tempQuality -= 5
                }
                baos.writeTo(out)
                out.flush()
                if (roughBitmap != null && !roughBitmap.isRecycled) {
                    roughBitmap.recycle()
                    roughBitmap = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    baos.close()
                    out.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun calculateInSampleSize(
        options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int,
    ): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight
                && halfWidth / inSampleSize >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun changeFileRotate(direction: Int, outputFile: File?): Boolean {
        return try {
            if (outputFile == null) return false
            var degress = 0
            degress = if (1 == direction) {
                90
            } else if (2 == direction) {
                180
            } else if (3 == direction) {
                270
            } else {
                return true
            }
            val matrix = Matrix()
            if (degress > 0) {
                matrix.postRotate(degress.toFloat())
            }
            var original = BitmapFactory.decodeFile(outputFile.absolutePath)
            var resizedBitmap = Bitmap.createBitmap(
                original!!, 0, 0,
                original.width, original.height, matrix, true
            )
            if (resizedBitmap != original && original != null && !original.isRecycled) {
                original.recycle()
                original = null
            }
            val fos = FileOutputStream(outputFile)
            resizedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            if (resizedBitmap != null && !resizedBitmap.isRecycled) {
                resizedBitmap.recycle()
                resizedBitmap = null
            }
            true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }

    fun cropImg(file: File?, top: Int, left: Int, width: Int, height: Int) {
        if (file == null) return
        if (top < 0
            || left < 0
            || width < 0
            || height < 0
        ) return
        try {
            var original = BitmapFactory.decodeFile(file.absolutePath)
            var cropBitmap = Bitmap.createBitmap(original!!, left, top, width, height)
            if (original != null && !original.isRecycled) {
                original.recycle()
                original = null
            }
            val fos = FileOutputStream(file)
            cropBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            if (cropBitmap != null && !cropBitmap.isRecycled) {
                cropBitmap.recycle()
                cropBitmap = null
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


}