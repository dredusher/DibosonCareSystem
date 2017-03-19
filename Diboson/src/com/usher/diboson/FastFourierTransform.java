package com.usher.diboson;

public class FastFourierTransform 
{
	/* ============================================================================= */
	// 17/10/2013 ECU implementation of Cooley-Tukey algorithm (see Wiki)
	/* ============================================================================= */
	
	// =============================================================================

	// =============================================================================
	
	/* ============================================================================= */
	public FastFourierTransform () 
	{
		
	}
	/* ============================================================================= */
	public Complex [] fft (Complex [] theComplexNumbers) 
	{
		// -------------------------------------------------------------------------
		// 18/10/2013 ECU return the transform of theComplexNumbers [] whose length 
		//                must be a power of 2
		// 18/11/2016 ECU changed from static
		// -------------------------------------------------------------------------
		int localNumberOfPoints = theComplexNumbers.length;
		// -------------------------------------------------------------------------
		// 18/10/2013 ECU check for the base case of a single element
		// -------------------------------------------------------------------------
		if (localNumberOfPoints == 1)
				return new Complex[] { theComplexNumbers [0] };
		// -------------------------------------------------------------------------
		// 18/10/2013 ECU check for radix-2 Cooley-Tukey algorithm
		// -------------------------------------------------------------------------
		if ((localNumberOfPoints % 2) != 0) 
		{ 
			// ---------------------------------------------------------------------
			// 18/10/2013 ECU cannot use the algorithm
			// ---------------------------------------------------------------------
			throw new RuntimeException ("numberOfPoints is not a power of 2"); 
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 18/10/2013 ECU do the transformation of the even terms
		// -------------------------------------------------------------------------
		Complex [] evenTerms = new Complex [localNumberOfPoints/2];
		
		for (int k = 0; k < localNumberOfPoints/2; k++) 
		{
			evenTerms[k] = theComplexNumbers [2*k];
		}
		// -------------------------------------------------------------------------
		// 18/10/2013 ECU now do the actual transformation
		// -------------------------------------------------------------------------
		Complex [] evenTransformation = fft (evenTerms);
		// -------------------------------------------------------------------------
		// 18/10/2013 ECU do the transformation of the odd terms
		// -------------------------------------------------------------------------
		Complex [] oddTerms  = evenTerms;  // reuse the array
		
		for (int k = 0; k < localNumberOfPoints/2; k++) 
		{
			oddTerms[k] = theComplexNumbers [2*k + 1];
		}
		// -------------------------------------------------------------------------
		// 18/10/2013 ECU now do the actual transformation
		// -------------------------------------------------------------------------
		Complex[] oddTransformation = fft (oddTerms);
		// -------------------------------------------------------------------------
      	// 18/10/2013 ECU now combine the odd and even terms
		// -------------------------------------------------------------------------
		Complex[] totalTransformation = new Complex [localNumberOfPoints];
		double kth;
		Complex wk;
		
     	for (int k = 0; k < localNumberOfPoints/2; k++) 
     	{
     		// ---------------------------------------------------------------------
     		// 18/10/2013 ECU get the power of the 'twiddle factor'
     		// ---------------------------------------------------------------------
     		kth = -2 * k * Math.PI / localNumberOfPoints;
          
     		wk = new Complex (Math.cos(kth), Math.sin(kth));
          
     		totalTransformation [k]         		   		= evenTransformation[k].plus(wk.times(oddTransformation[k]));
     		totalTransformation [k + localNumberOfPoints/2] = evenTransformation[k].minus(wk.times(oddTransformation[k]));
     	}
     	// -------------------------------------------------------------------------
     	// 20/10/2013 ECU return the final transformation
     	// -------------------------------------------------------------------------
     	return totalTransformation;
	}
	/* ============================================================================== */
}

 


