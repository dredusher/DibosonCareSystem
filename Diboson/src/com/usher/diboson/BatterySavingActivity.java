package com.usher.diboson;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

// =================================================================================
// BatterySavingActivity
// =====================
// 27/12/2019 ECU created to try save the battery while keeping the screen on
//
//                The biggest users of the battery are :-
//                   1) the display
//                   2) bluetooth (if enabled)
//
// ---------------------------------------------------------------------------------
// 29/12/2019 ECU changed from 'click' to 'long click' to 'finish' this activity
// =================================================================================

// =================================================================================
public class BatterySavingActivity extends DibosonActivity
{
    // =============================================================================
    private boolean bluetoothState;
    private float   brightness;
    // =============================================================================

    // =============================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // -------------------------------------------------------------------------
        super.onCreate(savedInstanceState);
        // -------------------------------------------------------------------------
        if (savedInstanceState == null)
        {
            // ---------------------------------------------------------------------
            // 27/12/2019 ECU set to 'full screen' and get the screen to be left on
            // ---------------------------------------------------------------------
            Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN,false,true);
            // ---------------------------------------------------------------------
            // Screen
            // ======
            // This activity has a layout which uses a 'black' background which
            // should reduce the drain on the battery
            // ---------------------------------------------------------------------
            // 27/12/2019 ECU now display the appropriate layout
            // ---------------------------------------------------------------------
            setContentView (R.layout.activity_battery_saving);
            // ---------------------------------------------------------------------
            // 27/12/2019 ECU set the click listener for the current layout
            // ---------------------------------------------------------------------
            RelativeLayout relativeLayout = (RelativeLayout) this.findViewById (R.id.battery_saving_layout);
            // ---------------------------------------------------------------------
            // 27/12/2019 ECU just want to 'finish' this activity
            // 29/12/2019 ECU changed from click to long click
            // ---------------------------------------------------------------------
            relativeLayout.setOnLongClickListener (new View.OnLongClickListener()
            {
                // -----------------------------------------------------------------
                @Override
                public boolean onLongClick (View theView)
                {
                    // -------------------------------------------------------------
                    // 27/12/2019 ECU 'finish' this activity
                    // -------------------------------------------------------------
                    finish ();
                    // -------------------------------------------------------------
                    // 29/12/2019 ECU indicate processed
                    // -------------------------------------------------------------
                    return true;
                    // -------------------------------------------------------------
                }
                // -----------------------------------------------------------------
            });
            // ---------------------------------------------------------------------
            // 27/12/2017 ECU reduce the brightness after remembering the current
            //                brightness
            // ---------------------------------------------------------------------
            brightness = Utilities.setScreenBrightness (this,0.0f);
            // ---------------------------------------------------------------------
            // bluetooth
            // =========
            // If this device currently has bluetooth enabled then it will be
            // disabled while this activity is running and then re-enabled when
            // this activity finishes. If bluetooth is not enabled then nothing is
            // done
            // ---------------------------------------------------------------------
            // 27/12/2019 ECU check if bluetooth is enabled
            // ---------------------------------------------------------------------
            if (bluetoothState = Utilities.bluetoothEnablement (false))
            {
                // -----------------------------------------------------------------
                // 27/12/2019 ECU bluetooth is currently enabled - so ask the user
                //                if it is to be disabled
                // -----------------------------------------------------------------
                Utilities.popToastAndSpeak (getString (R.string.bluetooth_being_disabled),true);
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
            // 27/12/2019 ECU tell the user how to close this activity
            // ---------------------------------------------------------------------
            Utilities.popToastAndSpeak (getString (R.string.long_click_anywhere),true);
            // ---------------------------------------------------------------------
        }
        else
        {
            // ---------------------------------------------------------------------
            // 27/12/2019 ECU the activity has been recreated after having been
            //                destroyed by the Android OS
            // ---------------------------------------------------------------------
            finish ();
            // ---------------------------------------------------------------------
        }
    }
    // =============================================================================
    @Override
    protected void onDestroy()
    {
        // -------------------------------------------------------------------------
        // 27/12/2019 ECU called when this activity is being 'finish-ed'
        // -------------------------------------------------------------------------
        if (bluetoothState)
        {
            // ---------------------------------------------------------------------
            // 27/12/2019 ECU tell the user what is happening
            // ---------------------------------------------------------------------
            Utilities.popToastAndSpeak (getString (R.string.bluetooth_being_enabled),true);
            // ---------------------------------------------------------------------
            // 27/12/2019 ECU re-enable bluetooth
            // ---------------------------------------------------------------------
            Utilities.bluetoothEnablement (true);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 27/12/2019 ECU restore the brightness before the activity started
        // -------------------------------------------------------------------------
        Utilities.setScreenBrightness (this,brightness);
        // -------------------------------------------------------------------------
        // 27/12/2019 ECU now let the OS do the erst of it's work
        // -------------------------------------------------------------------------
        super.onDestroy();
        // -------------------------------------------------------------------------
    }
    // =============================================================================
}
// =================================================================================
