package com.tudor.drivesmart;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ImageUtils {
    private static final int WIDTH = 256;
    private static final int HEIGHT = 256;

    public static float[][][][] preprocessImage(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, true);
        float[][][][] input = new float[1][WIDTH][HEIGHT][3];

        for (int i = 0; i < WIDTH; i ++) {
            for (int j = 0; j < HEIGHT; j ++) {
                int pixel = resizedBitmap.getPixel(i, j);
                input[0][i][j][0] = Color.red(pixel);
                input[0][i][j][1] = Color.green(pixel);
                input[0][i][j][2] = Color.blue(pixel);
            }
        }

        return input;
    }
}
