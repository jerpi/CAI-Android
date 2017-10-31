package com.som.sombrero.utils;

import android.support.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class ByteArrayConverter {
    private ByteArrayConverter() { }

    @NonNull
    public static byte[] float2ByteArray(float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public static float byteArray2Float(byte[] value) {
        return ByteBuffer.wrap(value).getFloat();
    }

    @NonNull
    public static byte[] floatArray2ByteArray(float[] values) {
        ByteBuffer buffer = ByteBuffer.allocate(4 * values.length);
        for (float value : values) {
            buffer.putFloat(value);
        }
        return buffer.array();
    }

    @NonNull
    public static float[] byteArray2FloatArray(byte[] values, int bytes) {
        FloatBuffer buffer = FloatBuffer.allocate(values.length/4);
        ByteBuffer byteBuffer = ByteBuffer.wrap(values);

        for (int i = 0; i < Math.min(values.length, bytes)/4; i++) {
            float f = byteBuffer.getFloat(i*4);
            buffer.put(f);
        }
        return buffer.array();
    }
}
