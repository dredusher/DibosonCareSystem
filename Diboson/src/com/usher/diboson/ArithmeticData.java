package com.usher.diboson;

import java.io.Serializable;

public class ArithmeticData implements Serializable
{
    // =============================================================================
    // 14/03/2020 ECU created to hold the data associated with the arithmetic activity
    // =============================================================================

    // =============================================================================
    private static final long serialVersionUID = 1L;
    // =============================================================================

    // =============================================================================
    int     lowNumber;
    int     inputPrecision;
    int     numberOfOperands;
    int     range;
    int     resultPrecision;
    // =============================================================================

    // =============================================================================
    public ArithmeticData (int theLowNumber,int theRange,int theInputPrecision,int theResultPrecision,int theNumberOfOperands)
    {
        // -------------------------------------------------------------------------
        lowNumber           = theLowNumber;
        inputPrecision      = theInputPrecision;
        numberOfOperands    = theNumberOfOperands;
        range               = theRange;
        resultPrecision     = theResultPrecision;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
}
