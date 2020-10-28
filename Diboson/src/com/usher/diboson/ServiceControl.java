package com.usher.diboson;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;

import java.lang.reflect.Method;

public class ServiceControl
{
    // -----------------------------------------------------------------------------
    // 12/08/2020 ECU added 'startImmediately' to indicate whether the service is
    //                to be started automatically when the app begins
    //
    //                intentMethod ............... the name of the method in the
    //                                             serviceClass which will be called
    //                                             to add information into the intent
    //                                             which will be passed to the
    //                                             'onCreate' of the service. For
    //                                             example 'updateIntent' would require
    //
    //                                              public static Intent updateIntent (Object theArgument)
    //	                                            {
    //                                                  return <modified intent>
    //	                                            }
    //
    //                serviceClass  .............. the class which constitutes the
    //                                             service
    //                startImmediately ........... indicates whether the service is
    //                                             to be started automatically when
    //                                             the app begins
    //                validationMethod ........... the name of the method in the
    //                                             serviceClass which will be called
    //                                             to provide additional validation
    //                                             as to whether the service should
    //                                             be started or not. For example
    //                                             'validation' would require
    //
    //                                               public static boolean validation (int theArgument)
    //                                               {
    //                                                      return 'true' if service can be started
    //                                                             'false' if service cannot be started
    //                                               }
    // 13/08/2020 ECU provide the methods to display the status of the services
    // -----------------------------------------------------------------------------
    String      intentMethod;
    Class<?>    serviceClass;
    boolean     startImmediately;
    String      validationMethod;
    // -----------------------------------------------------------------------------

    // =============================================================================
    public ServiceControl (Class<?> theServiceClass,boolean theStartImmediatelyFlag)
    {
        // -------------------------------------------------------------------------
        intentMethod        = null;
        serviceClass        = theServiceClass;
        startImmediately    = theStartImmediatelyFlag;
        validationMethod    = null;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public ServiceControl (Class<?> theServiceClass,boolean theStartImmediatelyFlag,String theValidationMethod)
    {
        // -------------------------------------------------------------------------
        intentMethod        = null;
        serviceClass        = theServiceClass;
        startImmediately    = theStartImmediatelyFlag;
        validationMethod    = theValidationMethod;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public ServiceControl (Class<?> theServiceClass,boolean theStartImmediatelyFlag,String theValidationMethod,String theIntentMethod)
    {
        // -------------------------------------------------------------------------
        intentMethod        = theIntentMethod;
        serviceClass        = theServiceClass;
        startImmediately    = theStartImmediatelyFlag;
        validationMethod    = theValidationMethod;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public Intent GetIntent (Context theContext)
    {
        // -------------------------------------------------------------------------
        // 11/08/2020 ECU want to return an intent for this class
        // -------------------------------------------------------------------------
        Intent localIntent = new Intent (theContext,serviceClass);
        // -------------------------------------------------------------------------
        // 11/08/2020 ECU it is possible that some data needs to be stored in the
        //                'extras' of the intent
        // -------------------------------------------------------------------------
        return UpdateIntent (localIntent);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    SpannableStringBuilder Print (Context theContext)
    {
        // -------------------------------------------------------------------------
        // 13/08/2020 ECU display information on the service
        // 16/08/2020 ECU changed to use service running
        //            ECU changed to use spannable so that colour can emphasise the
        //            state of the service
        // -------------------------------------------------------------------------
        SpannableStringBuilder printDetails = new SpannableStringBuilder ();
        // -------------------------------------------------------------------------
        // 16/08/2020 ECU get the 'running' state of this service
        // -------------------------------------------------------------------------
        boolean serviceRunning = Utilities.isServiceRunning (theContext,serviceClass);
        // --------------------------------------------------------------------------
        // 16/08/2020 ECU generate the 'printable' entry for this ervice
        // --------------------------------------------------------------------------
        String  printEntry = String.format (theContext.getString (R.string.service_status_format),
                                            serviceClass.getName(),
                                            (serviceRunning ? StaticData.BLANK_STRING
                                                            : "not "));
        // -------------------------------------------------------------------------
        // 16/08/2020 ECU add the string into the 'builder'
        // -------------------------------------------------------------------------
        printDetails.append (printEntry);
        // -------------------------------------------------------------------------
        // 16/08/2020 ECU set the colour of the entry depending on whether the service
        //                is running or not
        //            ECU also use 'monospace' to make the display easier to read
        // 05/10/2020 ECU change to use 'blue' instead of 'dark green'
        // -------------------------------------------------------------------------
       printDetails.setSpan (new ForegroundColorSpan (serviceRunning ? Color.BLUE
                                                                     : Color.RED),
                            0,
                            printEntry.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        printDetails.setSpan (new TypefaceSpan ("monospace"),
                              0,
                              printEntry.length(),
                              Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // -------------------------------------------------------------------------
        return printDetails;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static boolean ServicesRunning (Context theContext)
    {
        // -------------------------------------------------------------------------
        // 07/08/2020 ECU created to report if any of the services is running or
        //                whether they have all stopped
        // 11/08/2020 ECU changed to use ServiceControl
        // 12/08/2020 ECU moved here from Utilities
        // -------------------------------------------------------------------------
        for (ServiceControl serviceControl : StaticData.SERVICES_TO_HANDLE)
        {
            // ---------------------------------------------------------------------
            // 07/08/2020 ECU if any service is running then return true
            // ---------------------------------------------------------------------
            if (Utilities.isServiceRunning (theContext,serviceControl.serviceClass))
            {
                // -----------------------------------------------------------------
                // 07/08/2020 ECU the service is running so report this fact
                // -----------------------------------------------------------------
                return true;
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 07/08/2020 ECU none of the services are running so report this
        // -------------------------------------------------------------------------
        return false;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void ServicesRunningAndAction (final Context theContext,
                                                 final int theTimeToWaitInSecs,
                                                 final Method theMethodWhenStopped)
    {
        // -------------------------------------------------------------------------
        // 07/08/2020 ECU monitor the running services and when they have all stopped
        //                then 'invoke' the specified method
        // 12/08/2020 ECU moved here from Utilities
        // -------------------------------------------------------------------------
        Thread thread = new Thread ()
        {
            // ---------------------------------------------------------------------
            @Override
            public void run()
            {
                // -----------------------------------------------------------------
                int timeToWait = theTimeToWaitInSecs;
                // -----------------------------------------------------------------
                // 07/08/2020 ECU check if any of the services are still running
                // -----------------------------------------------------------------
                while (ServicesRunning (theContext) && (timeToWait-- > 0))
                {
                    // -------------------------------------------------------------
                    // 07/08/2020 ECU wait a bit
                    // -------------------------------------------------------------
                    try
                    {
                        sleep (StaticData.MILLISECONDS_PER_SECOND);
                    }
                    catch (InterruptedException theException)
                    {
                    }
                    // -------------------------------------------------------------
                }
                // -----------------------------------------------------------------
                // 07/08/2020 ECU all of the services have stopped so can 'invoke'
                //                the method
                // -----------------------------------------------------------------
                if (theMethodWhenStopped != null)
                {
                    try
                    {
                        // -----------------------------------------------------
                        // 07/08/2020 ECU invoke the method that has been passed
                        //                through
                        // -----------------------------------------------------
                        theMethodWhenStopped.invoke (null);
                        // -----------------------------------------------------
                    }
                    catch (Exception theException)
                    {
                    }
                }
                // -----------------------------------------------------------------
            }
        };
        // -------------------------------------------------------------------------
        // 21/11/2013 ECU start up the defined thread
        // -------------------------------------------------------------------------
        thread.start();
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void StartAllServices (Context theContext)
    {
        // -------------------------------------------------------------------------
        // 12/08/2020 ECU created to start all of the services that are needed by
        //                this app
        // -------------------------------------------------------------------------
        for (ServiceControl serviceControl : StaticData.SERVICES_TO_HANDLE)
        {
            // ---------------------------------------------------------------------
            // 11/08/2020 ECU check if it is OK to start this app
            // 12/08/2020 ECU check if the 'start immediately' flag is 'true'
            // ---------------------------------------------------------------------
            if (serviceControl.startImmediately &&
                serviceControl.Validate() &&
                !Utilities.isServiceRunning (theContext,serviceControl.serviceClass))
            {
                // -----------------------------------------------------------------
                // 11/08/2020 ECU it is OK to run this service and it isn't
                //                already running so can start it
                // -----------------------------------------------------------------
                theContext.startService (serviceControl.GetIntent (theContext));
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static SpannableStringBuilder Status (Context theContext)
    {
        // -------------------------------------------------------------------------
        // 13/08/2020 ECU created to return the status of all defined services
        // 16/08/2020 ECU changed to use 'spannable'
        // -------------------------------------------------------------------------
        SpannableStringBuilder statusDetails = new SpannableStringBuilder ();
        // -------------------------------------------------------------------------
        // 13/08/2020 ECU loop through each service - add status to the string
        // 16/08/2020 ECU use the colour to emphasise the state of the service
        // -------------------------------------------------------------------------
        for (ServiceControl serviceControl : StaticData.SERVICES_TO_HANDLE)
        {
            statusDetails.append (serviceControl.Print (theContext).append (StaticData.NEWLINE));
        }
        //--------------------------------------------------------------------------
        // 13/08/2020 ECU return the generated status string
        // -------------------------------------------------------------------------
        return statusDetails;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public static void StopAllServices (Context theContext)
    {
        // -------------------------------------------------------------------------
        // 12/08/2020 ECU created to stop all of the services that are used by this
        //                app
        // -------------------------------------------------------------------------
        for (ServiceControl serviceControl : StaticData.SERVICES_TO_HANDLE)
        {
            // ---------------------------------------------------------------------
            // 28/03/2020 ECU check if the service is running - if it is then stop
            //                it
            // ---------------------------------------------------------------------
            if (Utilities.isServiceRunning (theContext,serviceControl.serviceClass))
            {
                // -----------------------------------------------------------------
                // 28/03/2020 ECU this service is 'running' so stop it
                // -----------------------------------------------------------------
                theContext.stopService (new Intent (theContext,serviceControl.serviceClass));
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public Intent UpdateIntent (Intent theIntent)
    {
        // -------------------------------------------------------------------------
        // 11/08/2020 ECU called to use the method to determine if this entry is
        //                valid. If the method is null then return a true
        // -------------------------------------------------------------------------
        try
        {
            if (intentMethod != null)
            {
                // -----------------------------------------------------------------
                Method localIntentMethod = Utilities.createAMethod (serviceClass,intentMethod,theIntent);
                return ((Intent) localIntentMethod.invoke (null,new Object [] {theIntent}));
                // -----------------------------------------------------------------
            }
            else
            {
                return theIntent;
            }
        }
        catch (Exception theException)
        {
            // ---------------------------------------------------------------------
            // 11/08/2020 ECU error occurred so indicate 'valid'
            // ---------------------------------------------------------------------
            return theIntent;
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public boolean Validate ()
    {
        // -------------------------------------------------------------------------
        // 11/08/2020 ECU called to use the method to determine if this entry is
        //                valid. If the method is null then return a true
        // -------------------------------------------------------------------------
        try
        {
            if (validationMethod != null)
            {
                // -----------------------------------------------------------------
                Method localValidationMethod = Utilities.createAMethod (serviceClass,validationMethod,0);
                return ((Boolean) localValidationMethod.invoke (null,new Object [] {0}));
                // -----------------------------------------------------------------
            }
            else
            {
                return true;
            }
        }
        catch (Exception theException)
        {
            // ---------------------------------------------------------------------
            // 11/08/2020 ECU error occurred so indicate 'valid'
            // ---------------------------------------------------------------------
            return true;
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
}
