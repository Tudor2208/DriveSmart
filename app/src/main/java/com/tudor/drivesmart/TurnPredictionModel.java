package com.tudor.drivesmart;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;
import android.util.Pair;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TurnPredictionModel {
    private Interpreter interpreter;
    private int[] outputShape;
    private static String modelFileName;

    public TurnPredictionModel(Context context, String model) {
        try {
            modelFileName = model;
            interpreter = new Interpreter(loadModelFile(context));
            outputShape = interpreter.getOutputTensor(0).shape();
        } catch (IOException ex) {
            Log.w("prediction", "Error occurred with TurnPredictionModel");
        }
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        try (AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelFileName);
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
             FileChannel fileChannel = inputStream.getChannel()) {
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    public Pair<String, Float> predict(float[][][][] input) {
        float[][] output = new float[outputShape[0]][outputShape[1]];
        interpreter.run(input, output);

        String[] classes = {"LEFT", "RIGHT", "STRAIGHT"};
        int predictedIndex = argmax(output[0]);
        return Pair.create(classes[predictedIndex], output[0][predictedIndex]);
    }

    private int argmax(float[] array) {
        int bestIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[bestIndex]) {
                bestIndex = i;
            }
        }
        return bestIndex;
    }

}
