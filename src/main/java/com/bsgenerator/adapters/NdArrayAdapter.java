package com.bsgenerator.adapters;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class NdArrayAdapter {
    public static double getDouble(INDArray array, long i, long j) {
        return array.getDouble(i, j);
    }

    public static INDArray zeros(long rows, long columns) {
        return Nd4j.zeros(rows, columns);
    }
}
