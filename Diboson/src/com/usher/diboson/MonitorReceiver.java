package com.usher.diboson;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

// =================================================================================
// MonitorReceiver
// ===============
// 21/12/2019 ECU created to handle alarms associated with the 'monitoring' process
// =================================================================================

// =================================================================================
public class MonitorReceiver extends BroadcastReceiver
{
    // =============================================================================
    private static final String TAG = "MonitorReceiver";
    // =============================================================================

    // -----------------------------------------------------------------------------
    // 21/12/2019 ECU declare the method that receives the relevant alarm
    // -----------------------------------------------------------------------------
    @Override
    public void onReceive (Context context,Intent intent)
    {
        // -------------------------------------------------------------------------
        // 21/12/2019 ECU set up variables that are passed through with the intent
        // -------------------------------------------------------------------------
        int 	alarmID 	= StaticData.NO_RESULT;
        int 	alarmType	= StaticData.NO_RESULT;
        // -------------------------------------------------------------------------
        Bundle localExtras = intent.getExtras();
        if (localExtras != null)
        {
            alarmID 	= intent.getIntExtra (StaticData.PARAMETER_ALARM_ID,StaticData.NO_RESULT);
            alarmType 	= intent.getIntExtra (StaticData.PARAMETER_ALARM_TYPE,StaticData.NO_RESULT);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 21/12/2019 ECU log the alarm details before processing
        // 24/12/2019 ECU display a more meaningful 'alarmType'
        // -------------------------------------------------------------------------
        Utilities.LogToProjectFile (TAG,"Received : " + alarmID + "   " + MonitorHandler.MessageName (alarmType));
        // -------------------------------------------------------------------------
        // 21/12/2019 ECU switch depending on the type of alarm received
        // -------------------------------------------------------------------------
        switch (alarmID)
        {
            // ====================================================================
            case StaticData.ALARM_ID_MONITOR:
                // ----------------------------------------------------------------
                MonitorHandler.AlarmReceived (alarmType);
                // ----------------------------------------------------------------
                break;
            // =====================================================================
            default:
                // -----------------------------------------------------------------
                // 21/12/2019 ECU received an alarm that cannot be handled by this
                //                class
                // -----------------------------------------------------------------
                break;
            // =====================================================================
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================

    // =============================================================================
    public static void SetAlarm (Context theContext,long theTime,int theAlarmID,int theAlarmType)
    {
        // -------------------------------------------------------------------------
        // 21/12/2019 ECU created to set an exact alarm
        // -------------------------------------------------------------------------
        // 21/12/2019 ECU set up the alarm manager and required intent
        // -------------------------------------------------------------------------
        AlarmManager    alarmManager   = (AlarmManager) theContext.getSystemService (Context.ALARM_SERVICE);
        Intent          alarmIntent    = new Intent (theContext, MonitorReceiver.class);
        // -------------------------------------------------------------------------
        alarmIntent.putExtra (StaticData.PARAMETER_ALARM_ID,theAlarmID);
        alarmIntent.putExtra (StaticData.PARAMETER_ALARM_TYPE,theAlarmType);
        // -------------------------------------------------------------------------
        // 21/12/2019 ECU use ...ONE_SHOT
        // 24/12/2019 ECU Note - the second argument of 'getBroadcast' is the
        //                       request code. PendingIntents are the same if
        //                       the request codes are equal - extras are not
        //                       taken into account.
        //
        //                       Generate a 'request code' from the ID and type
        // -------------------------------------------------------------------------
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast (theContext,
                (theAlarmID * 1000) + theAlarmType,
                alarmIntent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
        // -------------------------------------------------------------------------
        // 21/12/2019 ECU log details of alarm being set
        // 22/12/2019 ECU add a print of ID and type
        // -------------------------------------------------------------------------
        Utilities.LogToProjectFile (TAG,"SetAlarm : " +   MonitorHandler.dateFormat.format (theTime) + "  " + theAlarmID + "  " + theAlarmType);
        // -------------------------------------------------------------------------
        // 24/12/2015 ECU changed to use the new method
        // -------------------------------------------------------------------------
        Utilities.SetAnExactAlarm (alarmManager,theTime,alarmPendingIntent);
        // -------------------------------------------------------------------------
    }
    // =============================================================================

}
// =================================================================================
