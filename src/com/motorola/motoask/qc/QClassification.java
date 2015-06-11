package com.motorola.motoask.qc;

public class QClassification {
	private String mClassification;
	private double mConfidence;
	public QClassification(String classification, double confidence) {
		mClassification = classification;
		mConfidence = confidence;
	}
	
	public String getClassification() {
		return mClassification;
	}
	
	public double getConfidence() {
		return mConfidence;
	}
}
