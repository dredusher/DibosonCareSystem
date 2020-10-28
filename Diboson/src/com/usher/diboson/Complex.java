package com.usher.diboson;

public class Complex 
{
    // =============================================================================
    // 18/10/2013 ECU created to handle the manipulation of complex numbers
    // 24/03/2020 ECU general tidy up of the layout
	/* ============================================================================= */
    private final double realPart;   
    private final double imaginaryPart;  
    /* ============================================================================= */
    public Complex (double theRealPart, double theImaginaryPart)
    {
        // --------------------------------------------------------------------------
    	// 18/10/2013 ECU construct the complex number 
    	// -------------------------------------------------------------------------
        realPart		= theRealPart;
        imaginaryPart 	= theImaginaryPart;
        // -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public String toString() 
    {
    	// ------------------------------------------------------------------------
    	// 18/10/2013 ECU put the complex number into a printable form
    	// 20/03/2017 ECU changed to use BLANK....
    	// ------------------------------------------------------------------------
        if (imaginaryPart == 0) return realPart + StaticData.BLANK_STRING;
        if (realPart == 0) return imaginaryPart + "i";
        if (imaginaryPart <  0) return realPart + " - " + (-imaginaryPart) + "i";
        // -------------------------------------------------------------------------
        return realPart + " + " + imaginaryPart + "i";
        // -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public double abs ()
    {
        // -------------------------------------------------------------------------
    	// 18/10/2013 ECU return Math.sqrt(realPart*realPart + imaginaryPart*imaginaryPart)
    	// -------------------------------------------------------------------------
    	return Math.hypot (realPart, imaginaryPart);
    	// -------------------------------------------------------------------------
    }  
    /* ============================================================================= */
    public double phase ()
    {
        // -------------------------------------------------------------------------
    	// 18/10/2013 ECU the angle (-pi to pi) of the complex number relative to real
    	//                axis
    	// -------------------------------------------------------------------------
    	return Math.atan2 (imaginaryPart, realPart);
    	// -------------------------------------------------------------------------
    }  
    /* ============================================================================= */
    public Complex plus (Complex aComplexNumber)
    {
        // -------------------------------------------------------------------------
    	// 18/10/2013 ECU returns the current complex number plus aComplexNumber
    	// -------------------------------------------------------------------------
        Complex a = this;             // invoking object
        double realPart		 = a.realPart      + aComplexNumber.realPart;
        double imaginaryPart = a.imaginaryPart + aComplexNumber.imaginaryPart;
        // -------------------------------------------------------------------------
        return new Complex (realPart, imaginaryPart);
        // -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public Complex minus (Complex aComplexNumber)
    {
        // -------------------------------------------------------------------------
    	// 18/10/2013 ECU returns the current complex number minus aComplexNumber
    	// -------------------------------------------------------------------------
        Complex a = this;
        double realPart 		= a.realPart      - aComplexNumber.realPart;
        double imaginaryPart 	= a.imaginaryPart - aComplexNumber.imaginaryPart;
        // -------------------------------------------------------------------------
        return new Complex (realPart, imaginaryPart);
        // -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public Complex times(Complex aComplexNumber) 
    {
        // ------------------------------------------------------------------------
    	// 18/10/2013 ECU performs a multiplication of current complex number with
    	//                aComplexNumber
    	// ------------------------------------------------------------------------
        Complex a = this;
        // ------------------------------------------------------------------------
        double realPart 		= a.realPart * aComplexNumber.realPart
                                    - a.imaginaryPart * aComplexNumber.imaginaryPart;
        double imaginaryPart 	= a.realPart * aComplexNumber.imaginaryPart
                                    + a.imaginaryPart * aComplexNumber.realPart;
        // -------------------------------------------------------------------------
        return new Complex (realPart, imaginaryPart);
        // -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public Complex times (double theMultiplier)
    {
        // -------------------------------------------------------------------------
    	// 18/10/2013 ECU performs a scalar multiplication 
    	// -------------------------------------------------------------------------
        return new Complex (theMultiplier * realPart,
                            theMultiplier * imaginaryPart);
        // -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public Complex conjugate ()
    {
        // -------------------------------------------------------------------------
    	// 18/10/2013 ECU returns the conjugate of current complex number
    	// -------------------------------------------------------------------------
    	return new Complex (realPart, -imaginaryPart);
    	// -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public Complex reciprocal ()
    {
        // -------------------------------------------------------------------------
    	// 18/10/2013 ECU returns the reciprocal of this complex number
    	// -------------------------------------------------------------------------
        double scale = realPart*realPart + imaginaryPart*imaginaryPart;
        // -------------------------------------------------------------------------
        return new Complex (realPart / scale, -imaginaryPart / scale);
        // -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public double realPart ()
    {
        // -------------------------------------------------------------------------
    	// 18/10/2013 ECU returns real part of this complex number
    	// -------------------------------------------------------------------------
    	return realPart;
    	// -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public double imaginaryPart ()
    {
        // -------------------------------------------------------------------------
    	// 18/10/2013 ECU returns imaginary part of this complex number
    	// -------------------------------------------------------------------------
    	return imaginaryPart;
    	// -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public Complex divides (Complex aComplexNumber)
    {
        // -------------------------------------------------------------------------
    	// 18/10/2013 ECU divides this complex number by the argument
    	// -------------------------------------------------------------------------
        Complex a = this;
        // -------------------------------------------------------------------------
        return a.times (aComplexNumber.reciprocal());
        // -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public Complex exp ()
    {
        // -------------------------------------------------------------------------
    	// 18/10/2013 ECU returns the exponential of this complex number
    	// -------------------------------------------------------------------------
        return new Complex (Math.exp (realPart) * Math.cos (imaginaryPart),
                            Math.exp (realPart) * Math.sin (imaginaryPart));
        // -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public Complex sin ()
    {
        // -------------------------------------------------------------------------
    	// 18/10/2013 ECU returns the complex sine of this complex number
    	// -------------------------------------------------------------------------
        return new Complex (Math.sin (realPart) * Math.cosh (imaginaryPart()),
                                Math.cos (realPart) * Math.sinh (imaginaryPart));
        // -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public Complex cos ()
    {
        // -------------------------------------------------------------------------
    	// 18/10/2103 ECU returns the complex cosine of this complex number
    	// -------------------------------------------------------------------------
        return new Complex (Math.cos (realPart) * Math.cosh (imaginaryPart),
                                -Math.sin (realPart) * Math.sinh (imaginaryPart));
        // -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public Complex tan ()
    {
        // -------------------------------------------------------------------------
    	// 18/10/2013 ECU returns the complex tangent of this complex number
    	// -------------------------------------------------------------------------
        return sin ().divides (cos ());
        // -------------------------------------------------------------------------
    }
    /* ============================================================================= */
    public static Complex plus (Complex complexNumber1, Complex complexNumber2)
    {
        // -------------------------------------------------------------------------
    	// 18/10/2013 ECU return the addition of two complex numbers
    	// -------------------------------------------------------------------------
        double realPart		 = complexNumber1.realPart      + complexNumber2.realPart;
        double imaginaryPart = complexNumber1.imaginaryPart + complexNumber2.imaginaryPart;
        // -------------------------------------------------------------------------
        Complex sum = new Complex (realPart, imaginaryPart);
        // -------------------------------------------------------------------------
        return sum;
        // -------------------------------------------------------------------------
    }
    /* ============================================================================= */
}
