package com.usher.diboson;

import android.content.Context;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;

import java.io.Serializable;

public class CameraSettings implements Serializable
{
	// =============================================================================
	// 16/06/2017 ECU created to hold settings that relate to the camera / camcorder
	// 22/06/2017 ECU added 'emailVideo'
	// =============================================================================

	// =============================================================================
	private static final long serialVersionUID = 1L;
	// =============================================================================
	
	// =============================================================================
	final static CameraQuality [] CAMERA_QUALITIES =  
										{
											// -------------------------------------
											new CameraQuality (CamcorderProfile.QUALITY_HIGH,"highest"),
											new CameraQuality (CamcorderProfile.QUALITY_LOW,"lowest"),
											new CameraQuality (CamcorderProfile.QUALITY_1080P,"1080P (1920 x 1080)"),
											new CameraQuality (CamcorderProfile.QUALITY_480P,"480P (720 x 480)"),
											new CameraQuality (CamcorderProfile.QUALITY_720P,"720P (1280 x 720)"),
											new CameraQuality (CamcorderProfile.QUALITY_CIF,"cif (352 x 288)"),
											new CameraQuality (CamcorderProfile.QUALITY_QCIF,"qcif (176 x 144)"),
											// -------------------------------------
										};
	// -----------------------------------------------------------------------------
	final static String []	CAMERA_POSITION =   
										{
											// -------------------------------------
											"Backward Facing Camera",
											"Forward Facing Camera"
											// -------------------------------------
										};
	// =============================================================================
	
	// =============================================================================
	int		camera 			= CameraInfo.CAMERA_FACING_BACK;
	int		duration		= StaticData.VIDEO_MAX_DURATION_MINUTES;	// minutes
	boolean elapsedTimer	= true;
	boolean emailVideo		= false;	
	long	fileSize		= StaticData.VIDEO_MAX_FILE_SIZE;			// bytes
	boolean hideView		= false;
	int		quality			= 0;
	// =============================================================================

	// =============================================================================
	public String CameraLegend ()
	{
		// -------------------------------------------------------------------------
		// 31/08/2020 ECU return the legend for the currently selected camera
		// -------------------------------------------------------------------------
		return CAMERA_POSITION [camera];
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public long FileSize (boolean theWarningFlag)
	{
		// -------------------------------------------------------------------------
		// 22/06/2017 ECU created to return the file size that should be used when
		//                setting the maximum file size to be used by the media
		//                recorder
		// 21/09/2017 ECU added the warning flag so that if the size is changed
		//                to that of the email attachment size then a warning can
		//                be optionally displayed
		// -------------------------------------------------------------------------
		if (emailVideo)
		{
			// ---------------------------------------------------------------------
			// 22/06/2017 ECU the resultant email will be emailed so check if the
			//                maximum size of an attachment has to be taken into
			//                account
			// ---------------------------------------------------------------------
			if (PublicData.storedData.emailAttachmentMaxSize > 0l)
			{
				// -----------------------------------------------------------------
				// 22/06/2017 ECU check if the current file size is bigger than the
				//                maximum attachment size
				// -----------------------------------------------------------------
				if (fileSize > PublicData.storedData.emailAttachmentMaxSize)
				{
					// -------------------------------------------------------------
					// 21/09/2017 ECU will be using the attachment size so optionally
					//                display a warning
					// -------------------------------------------------------------
					if (theWarningFlag)
					{
						Utilities.popToast (String.format (MainActivity.activity.getString(R.string.using_attachment),
																PublicData.storedData.emailAttachmentMaxSize),true);
					}
					// -------------------------------------------------------------
					// 22/06/2017 ECU truncate to the maximum attachment size
					// -------------------------------------------------------------
					return PublicData.storedData.emailAttachmentMaxSize;
					// -------------------------------------------------------------
				}
				else
				{
					// -------------------------------------------------------------
					// 22/06/2017 ECU the stored file size is smaller than the max
					//                attachment size so it can be used
					// -------------------------------------------------------------
					return fileSize;
					// -------------------------------------------------------------
				}
			}
			else
			{
				// -----------------------------------------------------------------
				// 22/06/2017 ECU the attachment file size is '0' so no checking is
				//                to take place
				// -----------------------------------------------------------------
				return fileSize;
				// -----------------------------------------------------------------
			}
		}
		else
		{
			// ---------------------------------------------------------------------
			// 22/06/2017 ECU will not be emailing the resulting video so just
			//                accept the set file size
			// ---------------------------------------------------------------------
			return fileSize;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static void setPosition (String thePosition)
	{
		// -------------------------------------------------------------------------
		// 16/06/2017 ECU created to find the entry in the array which corresponds
		//                to the specified legend
		// -------------------------------------------------------------------------
		for (int index = 0; index < CAMERA_POSITION.length; index++)
		{
			// ---------------------------------------------------------------------
			// 16/06/2017 ECU check if legends match - if so then set the quality to
			//                the index
			// ---------------------------------------------------------------------
			if (CAMERA_POSITION[index].equals(thePosition))
				PublicData.storedData.cameraSettings.camera = index;
			// ---------------------------------------------------------------------
		}
	}
	// =============================================================================
	public static int setQuality (String theQuality)
	{
		// -------------------------------------------------------------------------
		// 16/06/2017 ECU created to find the entry in the array which corresponds
		//                to the specified legend
		// -------------------------------------------------------------------------
		for (int index = 0; index < CAMERA_QUALITIES.length; index++)
		{
			// ---------------------------------------------------------------------
			// 16/06/2017 ECU check if legends match - if so then set the quality to
			//                the index
			// ---------------------------------------------------------------------
			if (CAMERA_QUALITIES[index].qualityLegend.equals(theQuality))
				return index;
			// ---------------------------------------------------------------------
		}
		// -------------------------------------------------------------------------
		// 16/06/2017 ECU no match found so return the fact
		// -------------------------------------------------------------------------
		return StaticData.NO_RESULT;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static String [] getQualityList (Context theContext)
	{
		// -------------------------------------------------------------------------
		// 16/06/2017 ECU created to return a list of legends for the available
		//                qualities
		// -------------------------------------------------------------------------
		String [] localQualities = new String [CAMERA_QUALITIES.length];
		
		for (int index = 0; index < CAMERA_QUALITIES.length; index++)
			localQualities [index] = CAMERA_QUALITIES[index].qualityLegend;
		// -------------------------------------------------------------------------
		// 16/06/2017 ECU return the generated list of legends
		// -------------------------------------------------------------------------
		return localQualities;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	
	// =============================================================================
	public int Quality ()
	{
		// -------------------------------------------------------------------------
		// 16/06/2017 ECU return the value of the quality which corresponds to the
		//                stored 'quality index'
		// -------------------------------------------------------------------------
		return CAMERA_QUALITIES [quality].qualityValue;
		// -------------------------------------------------------------------------
	}
	// -----------------------------------------------------------------------------
	public int Quality (int theQuality)
	{
		// -------------------------------------------------------------------------
		// 16/06/2017 ECU return the value of the quality which corresponds to the
		//                'theQuality'
		// -------------------------------------------------------------------------
		return CAMERA_QUALITIES [theQuality].qualityValue;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public String qualityLegend ()
	{
		// -------------------------------------------------------------------------
		// 16/06/2017 ECU return the legend of the stored quality
		// -------------------------------------------------------------------------
		return CAMERA_QUALITIES [quality].qualityLegend;
		// -------------------------------------------------------------------------
	}
	// =============================================================================
	public static boolean validateProfile (int theCameraId,int theQuality)
	{
		// -------------------------------------------------------------------------
		// 16/06/2017 ECU created to check if the quality is valid for the camera
		//                returns	true ...... the camera supports the quality
		//    					    false ..... the camera does NOT support the quality
		// -------------------------------------------------------------------------
		return CamcorderProfile.hasProfile (theCameraId,theQuality);
		// -------------------------------------------------------------------------
	}
	// ----------------------------------------------------------------------------
	public boolean validateProfile ()
	{
		// -------------------------------------------------------------------------
		// 16/06/2017 ECU created to check if the quality is valid for the camera
		//                returns	true ...... the camera supports the quality
		//    					    false ..... the camera does NOT support the quality
		// -------------------------------------------------------------------------
		return CamcorderProfile.hasProfile (camera,Quality ());
		// -------------------------------------------------------------------------
	}
	// =============================================================================
}
