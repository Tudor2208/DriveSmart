package com.tudor.drivesmart;

import android.annotation.SuppressLint;
import android.graphics.RectF;

import androidx.annotation.NonNull;

public class Recognition {

    private final Integer labelId;
    private String labelName;
    private final Float confidence;
    private final RectF location;

    public Recognition(
            final int labelId, final String labelName,  final Float confidence, final RectF location) {
        this.labelId = labelId;
        this.labelName = labelName;
        this.confidence = confidence;
        this.location = location;
    }

    public Integer getLabelId() {
        return labelId;
    }

    public String getLabelName() {
        return labelName;
    }

    public Float getConfidence() {
        return confidence;
    }

    public RectF getLocation() {
        return new RectF(location);
    }

    public void setLabelName(String labelName) {this.labelName = labelName;}


    @NonNull
    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        String resultString = "";

        resultString += labelId + " ";

        if (labelName != null) {
            resultString += labelName + " ";
        }

        if (confidence != null) {
            resultString += String.format("(%.1f%%) ", confidence * 100.0f);
        }

        if (location != null) {
            resultString += location + " ";
        }

        return resultString.trim();
    }
}
