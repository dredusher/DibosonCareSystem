package com.usher.diboson;

public class IRCode 
{
	/* ======================================================================= */
	public IRFunction		function;
	public long				code;
	/* ======================================================================= */
	public IRCode (IRFunction theFunction,long theCode)
	{
		function = theFunction;
		code     = theCode;
	}
	/* ======================================================================= */
}
