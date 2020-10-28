package com.usher.diboson;

import android.content.Context;
import android.media.MediaPlayer;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class DialogueUtilities
{
	// =============================================================================
	// =============================================================================
	// I M P O R T A N T   I M P O R T A N T   I M P O R T A N T
	// =================   =================   =================
	//
	// 22/03/2018 ECU This class contains the code for the user dialogue when the
	//                resultant methods to be invoked are static (i.e. the argument 
	//                that specifies the underlying object is set to null). The code
	//                that was here is now in DialogueUtilitiesNonStatic where it
	//                has been generalised, by the passing of the underlying object 
	//                argument, the invocation of both static and non-static
	//                methods.
	//
	//				  Methods in this class are for 'static methods' - so it passes
	//                'StaticData.STATIC_METHOD' (i.e. null) as the underlying
	//                object.
	// =============================================================================
	// =============================================================================
	
	
	// =============================================================================
	static void adapterListChoice (Context theContext,
								   int theLayoutID,
								   String theTitle,
								   ArrayList<ListItem> 	theListItems,
								   final Method theSelectMethod,
								   String theCancelLegend,
								   final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.adapterListChoice (theContext,
													  StaticData.STATIC_METHOD,
													  theLayoutID,
													  theTitle,
													  theListItems,
													  theSelectMethod,
													  theCancelLegend,
													  theCancelMethod);	
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void getDate (final Context 	theContext,
						 			  String	theTitle,
						 			  String	theSubTitle,
						 			  String 	theConfirmLegend,
						 		final Method 	theConfirmMethod,
						 			  String    theCancelLegend,
						 		final Method    theCancelMethod)
	{
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.getDate (theContext,
											StaticData.STATIC_METHOD,
											theTitle,
											theSubTitle,
											theConfirmLegend,
											theConfirmMethod,
											theCancelLegend,
											theCancelMethod);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void IPAddressInput (Context theContext,
								String  theTitle,
								String 	theSubTitle,
								String 	theDefaultAddress,
								final Method theConfirmMethod,
								final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.IPAddressInput (theContext,
												   StaticData.STATIC_METHOD,
												   theTitle,
												   theSubTitle,
												   theDefaultAddress,
												   theConfirmMethod,
												   theCancelMethod);
		// -------------------------------------------------------------------------	
	}
	// =============================================================================
	static void listChoice (Context theContext,
							String theTitle,
							String [] theItems,
							final Method theSelectMethod,
							String theCancelLegend,
							final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 07/04/2015 ECU change for those dialogues which are recursive - see
		//                MusicPlayer
		//            ECU changed to use the master method but with no positive button
		// -------------------------------------------------------------------------
		listChoice (theContext,theTitle,theItems,theSelectMethod,null,null,theCancelLegend,theCancelMethod);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void listChoice (Context theContext,
							String theTitle,
							String [] theItems,
							final Method theSelectMethod,
							String theConfirmLegend,
							final Method theConfirmMethod,
							String theCancelLegend,
							final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 13/11/2017 ECU until 4.02.57 this used to be the master method until the
		//                option to dismiss the dialogue before the associated method
		//                is called
		// -------------------------------------------------------------------------
		listChoice (theContext,
					theTitle,
					theItems,
					theSelectMethod,
					theConfirmLegend,theConfirmMethod,
					theCancelLegend,theCancelMethod,
					false);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void listChoice (Context 		theContext,
							String 			theTitle,
							String [] 		theItems,
							final Method 	theSelectMethod,
							String 			theConfirmLegend,
							final Method 	theConfirmMethod,
							String 			theCancelLegend,
							final Method 	theCancelMethod,
							final boolean 	theDismissDialogueFlag)
	{
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.listChoice (theContext,
											   StaticData.STATIC_METHOD,
											   theTitle,
											   theItems,
											   theSelectMethod,
											   theConfirmLegend,
											   theConfirmMethod,
											   theCancelLegend,
											   theCancelMethod,
											   theDismissDialogueFlag);	
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void multilineTextInput (		Context 	theContext,
											String  	theTitle,
											String 		theSubTitle,
											int 		theNumberOfLines,
											String 		theDefaultText,
									final 	Method 		theConfirmMethod,
									final 	Method 		theCancelMethod,
											int			theInputType,
											String  	theHelpText,
									final	boolean 	theMandatoryFlag)
	{
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.multilineTextInput (theContext,
													   StaticData.STATIC_METHOD,
													   theTitle,
													   theSubTitle,
													   theNumberOfLines,
													   theDefaultText,
													   theConfirmMethod,
													   theCancelMethod,
													   theInputType,
													   theHelpText,
													   theMandatoryFlag);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	static void multilineTextInput (Context 	theContext,
									String  	theTitle,
									String 		theSubTitle,
									int 		theNumberOfLines,
									String 		theDefaultText,
									final Method theConfirmMethod,
									final Method theCancelMethod,
									int			theInputType,
									String  	theHelpText)
	{
		// -------------------------------------------------------------------------
		// 14/07/2017 ECU this used to be the master method until the mandatory
		//                flag was added
		//            ECU call the new master method but with 'false' for the
		//                mandatory flag
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,
							theTitle,
							theSubTitle,
							theNumberOfLines,
							theDefaultText,
							theConfirmMethod,
							theCancelMethod,
							theInputType,
							theHelpText,
							false);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	static void multilineTextInput (Context theContext,
									String  theTitle,
									String 	theSubTitle,
									int 	theNumberOfLines,
									String 	theDefaultText,
									final Method theConfirmMethod,
									final Method theCancelMethod,
									int		theInputType)		
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU created to make use of the new master method
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,
							theTitle,
							theSubTitle,
							theNumberOfLines,
							theDefaultText,
							theConfirmMethod,
							theCancelMethod,
							theInputType,
							null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	static void multilineTextInput (Context theContext,
								    String  theTitle,
								    String 	theSubTitle,
								    int 	theNumberOfLines,
								    String 	theDefaultText,
								    final Method theConfirmMethod,
								    final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 22/01/2016 ECU created to call the new master method which has the input
		//                type
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,
							theTitle,
							theSubTitle,
							theNumberOfLines,
							theDefaultText,
							theConfirmMethod,
							theCancelMethod,
							StaticData.NO_RESULT,
							null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	static void multilineTextInput (Context theContext,
									String  theTitle,
									String 	theSubTitle,
									int 	theNumberOfLines,
									final Method theConfirmMethod,
									final Method theCancelMethod,
									int 	theInputType)
	{
		// -------------------------------------------------------------------------
		// 12/07/2015 ECU created to call the generic method with no preset text
		// 22/01/2016 ECU added the input type as an argument
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,
							theTitle,
							theSubTitle,
							theNumberOfLines,
							null,
							theConfirmMethod,
							theCancelMethod,
							theInputType,
							null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	static void multilineTextInput (Context theContext,
									String  theTitle,
									String 	theSubTitle,
									int 	theNunberOfLines,
									final Method theConfirmMethod,
									final Method theCancelMethod,
									int 	theInputType,
									String  theHelpText)
	{
		// -------------------------------------------------------------------------
		// 12/07/2015 ECU created to call the generic method with no preset text
		// 22/01/2016 ECU added the input type as an argument
		//            ECU and the click method
		//            ECU changed to the Help flag
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,
							theTitle,
							theSubTitle,
							theNunberOfLines,
							null,
							theConfirmMethod,
							theCancelMethod,
							theInputType,
							theHelpText);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	static void multilineTextInput (Context theContext,
									String  theTitle,
									String 	theSubTitle,
									int 	theNunberOfLines,
									final Method theConfirmMethod,
									final Method theCancelMethod)
		{
			// ---------------------------------------------------------------------
			// 12/07/2015 ECU created to call the generic method with no preset text
			// 22/01/2016 ECU added the input type as an argument
			// ---------------------------------------------------------------------
			multilineTextInput (theContext,
								theTitle,
								theSubTitle,
								theNunberOfLines,
								null,
								theConfirmMethod,
								theCancelMethod,
								StaticData.NO_RESULT,
								null);
			// ---------------------------------------------------------------------
		}
	// =============================================================================
	public static void multipleChoice (Context theContext,
				                String theTitle,
				                String [] theOptions,
				                final boolean [] theInitialOptions,
				                final Method theConfirmMethod,
				                final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 25/07/2020 ECU made public
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.multipleChoice (theContext,
												   StaticData.STATIC_METHOD,
												   theTitle,
												   theOptions,
												   theInitialOptions,
												   theConfirmMethod,
												   theCancelMethod);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void prompt (Context theContext,
						String  theTitle,
						String  theSummary,
						String  thePrompt,
						String  theAcknowledgeLegend)
	{
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.prompt (theContext,
										   StaticData.STATIC_METHOD,
										   theTitle,
										   theSummary,
										   thePrompt,
										   theAcknowledgeLegend);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	static void prompt (Context theContext,
						String  theTitle,
						String  thePrompt,
						String  theAcknowledgeLegend)
	{
		// -------------------------------------------------------------------------
		// 08/05/2016 ECU created to use the new method where the layout is specified
		// -------------------------------------------------------------------------
		prompt (theContext,theTitle,null,thePrompt,theAcknowledgeLegend);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void searchChoice (Context theContext,
							  String theTitle,
							  String theConfirmLegend,
							  final Method theConfirmMethod,
							  String theCancelLegend,
							  final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.searchChoice (theContext,
												 StaticData.STATIC_METHOD,
												 theTitle,
												 theConfirmLegend,
												 theConfirmMethod,
												 theCancelLegend,
												 theCancelMethod);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void securityInput (Context theContext,
							   String theTitle,
							   String theExistingCode,
							   String theConfirmLegend,
							   final Method theConfirmMethod,
							   String theCancelLegend,
							   final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.securityInput (theContext,
												  StaticData.STATIC_METHOD,
												  theTitle,
												  theExistingCode,
												  theConfirmLegend,
												  theConfirmMethod,
												  theCancelLegend,
												  theCancelMethod);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void singleChoice (Context 	theContext,
							  String 	theTitle,
							  String [] theOptions,
							  int theInitialOption,
							  String theConfirmLegend,
							  final Method theConfirmMethod,
							  String theCancelLegend,
							  final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.singleChoice (theContext,
											     StaticData.STATIC_METHOD,
											     theTitle,
											     theOptions,
											     theInitialOption,
											     theConfirmLegend,
											     theConfirmMethod,
											     theCancelLegend,
											     theCancelMethod);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void singleChoice (Context 	theContext,
			  				  		 String 	theTitle,
			  				  		 String [] theOptions,
			  				  		 int 		theInitialOption,
			  				  		 final Method theConfirmMethod,
			  				  		 final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 15/06/2016 ECU created to call the main method with no changes to the
		//                legends on the button
		// 24/07/2020 ECU made public
		// -------------------------------------------------------------------------
		singleChoice (theContext,
				      theTitle,
				      theOptions,
				      theInitialOption,
				      null,theConfirmMethod,
				      null,theCancelMethod);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void sliderChoice (final Context theContext,
							  String theTitle,
							  final String theSubTitle,
							  int theIconId,
							  final MediaPlayer theMediaPlayer,
							  int theInitialValue,
							  final int theMinimumValue,
							  final int theMaximumValue,
							  String theConfirmLegend,
							  final Method theConfirmMethod,
							  String theCancelLegend,
							  final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.sliderChoice (theContext,
											     StaticData.STATIC_METHOD,
											     theTitle,
											     theSubTitle,
											     theIconId,
											     theMediaPlayer,
											     theInitialValue,
											     theMinimumValue,
											     theMaximumValue,
											     theConfirmLegend,
											     theConfirmMethod,
											     theCancelLegend,
											     theCancelMethod);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	// =============================================================================
	static void sliderChoice (Context theContext,
								  String theTitle,
								  String theSubTitle,
								  final MediaPlayer theMediaPlayer,
								  int theInitialValue,
								  final int theMaximumValue,
								  String theConfirmLegend,
								  final Method theConfirmMethod)
	{
		// -------------------------------------------------------------------------
		// 15/08/2015 ECU created to call the new master method but indicating that
		//                the default icon should be used
		// 18/08/2015 ECU added the final 'null' to indicate that no 'cancel' option
		//                is required
		// 06/03/2016 ECU added the '0' to be the minimum value
		// -------------------------------------------------------------------------
		sliderChoice (theContext,
								  theTitle,
								  theSubTitle,
								  StaticData.NO_RESULT,	// use default icon
								  theMediaPlayer,
								  theInitialValue,
								  0,
								  theMaximumValue,
								  theConfirmLegend,
								  theConfirmMethod,
								  null,
								  null);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	static void sliderChoice (Context theContext,
			  				  String theTitle,
			  				  String theSubTitle,
			  				  int theIconId,
			  				  final MediaPlayer theMediaPlayer,
			  				  int theInitialValue,
			  				  final int theMinimumValue,
			  				  final int theMaximumValue,
			  				  String theConfirmLegend,
			  				  final Method theConfirmMethod,
			  				  String theCancelLegend)
	{
		// -------------------------------------------------------------------------
		// 17/01/2016 ECU created to call the master method
		// 06/03/2016 ECU added the minimum value
		// -------------------------------------------------------------------------
		sliderChoice (theContext,
					  theTitle,
					  theSubTitle,
					  theIconId,
					  theMediaPlayer,
					  theInitialValue,
					  theMinimumValue,
					  theMaximumValue,
					  theConfirmLegend,
					  theConfirmMethod,
					  theCancelLegend,
					  null);
		// -------------------------------------------------------------------------
	}
	// =============================================================================

	// =============================================================================
	static void textInput (Context theContext,
						   String theTitle,
						   String theSubTitle,
						   final Method theConfirmMethod,
						   final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 11/07/2015 ECU changed to use the new multiline method but just specify a
		//                single line
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,theTitle,theSubTitle,1,theConfirmMethod,theCancelMethod);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	static void textInput (Context theContext,
			   			   String theTitle,
			   			   String theSubTitle,
			   			   final Method theConfirmMethod,
			   			   final Method theCancelMethod,
			   			   int	theInputType)
	{
		// -------------------------------------------------------------------------
		// 11/07/2015 ECU changed to use the new multiline method but just specify a
		//                single line
		// 22/01/2016 ECU added the input type
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,theTitle,theSubTitle,1,theConfirmMethod,theCancelMethod,theInputType);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	static void textInput (Context theContext,
			   			   String 	theTitle,
			   			   String 	theSubTitle,
			   			   final 	Method theConfirmMethod,
			   			   final	Method theCancelMethod,
			   			   int		theInputType,
			   			   String 	theHelpText)
	{
		// -------------------------------------------------------------------------
		// 11/07/2015 ECU changed to use the new multiline method but just specify a
		//                single line
		// 22/01/2016 ECU added the input type and the click method
		//            ECU changed to use theHelpFlag
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,theTitle,theSubTitle,1,theConfirmMethod,theCancelMethod,theInputType,theHelpText);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void textInput (Context theContext,
						   String theTitle,
						   String theSubTitle,
						   String theDefaultText,
						   final Method theConfirmMethod,
						   final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 12/07/2015 ECU created to set the default text and then use the generic
		//                method
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,theTitle,theSubTitle,1,theDefaultText,theConfirmMethod,theCancelMethod);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	static void textInput (Context theContext,
			   			   String theTitle,
			   			   String theSubTitle,
			   			   String theDefaultText,
			   			   final Method theConfirmMethod,
			   			   final Method theCancelMethod,
			   			   int theInputType)
	{
		// -------------------------------------------------------------------------
		// 12/07/2015 ECU created to set the default text and then use the generic
		//                method
		// -------------------------------------------------------------------------
		multilineTextInput (theContext,theTitle,theSubTitle,1,theDefaultText,theConfirmMethod,theCancelMethod,theInputType);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	static void yesNo (Context theContext,
			   		   String theTitle,
			   		   String theMessage,
			   		   final Object theChosenObject,
			   		   final boolean theConfirmState,
			   		   final String theConfirmLegend,
			   		   final Method theConfirmMethod,
			   		   final boolean theCancelState,
			   		   final String theCancelLegend,
			   		   final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		DialogueUtilitiesNonStatic.yesNo (theContext,
										  StaticData.STATIC_METHOD,
										  theTitle,
										  theMessage,
										  theChosenObject,
										  theConfirmState,
										  theConfirmLegend,
										  theConfirmMethod,
										  theCancelState,
										  theCancelLegend,
										  theCancelMethod);
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	static void yesNo (Context theContext,
			   		   String theTitle,
			   		   String theMessage,
			   		   final Object theChosenObject,
			   		   final Method theConfirmMethod,
			   		   final Method theCancelMethod)
	{
		// -------------------------------------------------------------------------
		// 02/01/2016 ECU changed to call the master method which allows the button
		//                legends to be changed
		// -------------------------------------------------------------------------
		yesNo (theContext,
			   theTitle,
			   theMessage,
			   theChosenObject,
			   true,theContext.getString(R.string.yes),theConfirmMethod,
			   true,theContext.getString(R.string.no),theCancelMethod);
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}
