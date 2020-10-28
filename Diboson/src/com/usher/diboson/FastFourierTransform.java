package com.usher.diboson;

public class FastFourierTransform 
{
	/* ============================================================================= */
	// 17/10/2013 ECU implementation of Cooley-Tukey algorithm (see Wiki)
	/* ============================================================================= */
	
	// =============================================================================
	private static double TWO_PI	=  (2 * Math.PI);
	// =============================================================================
	
	/* ============================================================================= */
	public FastFourierTransform ()
	{
		// -------------------------------------------------------------------------
		// 17/10/2013 ECU public constructor
		// -------------------------------------------------------------------------
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
		// 26/06/2020 ECU set up half of the number - used quite often.
		//                Changed where needed in the following code
		// -------------------------------------------------------------------------
		int localHalfOfNumberOfPoints = localNumberOfPoints / 2;
		// -------------------------------------------------------------------------
		// 18/10/2013 ECU do the transformation of the even terms
		// -------------------------------------------------------------------------
		Complex [] evenTerms = new Complex [localHalfOfNumberOfPoints];
		Complex [] oddTerms  = new Complex [localHalfOfNumberOfPoints];
		// -------------------------------------------------------------------------
		for (int k = 0; k < localHalfOfNumberOfPoints; k++)
		{
			evenTerms [k] = theComplexNumbers [(2 * k)];
			oddTerms  [k] = theComplexNumbers [(2 * k) + 1];
		}
		// -------------------------------------------------------------------------
		// 18/10/2013 ECU now do the actual transformation
		// -------------------------------------------------------------------------
		Complex [] evenTransformation = fft (evenTerms);
		Complex [] oddTransformation  = fft (oddTerms);
		// -------------------------------------------------------------------------
      	// 18/10/2013 ECU now combine the odd and even terms
		// -------------------------------------------------------------------------
		Complex [] totalTransformation = new Complex [localNumberOfPoints];
		double kth;
		Complex wk;
		double  twoPiFactor = -TWO_PI / localNumberOfPoints;
		// -------------------------------------------------------------------------
     	for (int k = 0; k < localHalfOfNumberOfPoints; k++)
     	{
     		// ---------------------------------------------------------------------
     		// 18/10/2013 ECU get the power of the 'twiddle factor'
     		// 26/06/2020 ECU changed to use the 'twoPiFactor' rather than doing it
     		//                each iteration
     		// ---------------------------------------------------------------------
     		kth = twoPiFactor * k;
          
     		wk = new Complex (Math.cos (kth), Math.sin (kth));
          
     		totalTransformation [k]         		   			= evenTransformation [k].plus  (wk.times (oddTransformation [k]));
     		totalTransformation [k + localHalfOfNumberOfPoints] = evenTransformation [k].minus (wk.times (oddTransformation [k]));
     		// ---------------------------------------------------------------------
     	}
     	// -------------------------------------------------------------------------
     	// 20/10/2013 ECU return the final transformation
     	// -------------------------------------------------------------------------
     	return totalTransformation;
     	// -------------------------------------------------------------------------
	}
	/* ============================================================================== */
}