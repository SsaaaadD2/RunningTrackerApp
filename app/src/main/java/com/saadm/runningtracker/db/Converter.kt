package com.saadm.runningtracker.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converter {

    @TypeConverter
    fun bmapToByteArray(bmp: Bitmap) : ByteArray{
        var outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun byteArrayToBMap(bArray: ByteArray) : Bitmap{
        return BitmapFactory.decodeByteArray(bArray, 0, bArray.size)
    }
}