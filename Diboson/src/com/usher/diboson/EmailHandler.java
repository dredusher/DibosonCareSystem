package com.usher.diboson;

import android.os.Handler;
import android.os.Message;

// =================================================================================
// EmailHandler
// ============
//
//  This handler has been created to handle messages associated with the transmission
//  of emails
//
// =================================================================================

// =================================================================================
public class EmailHandler extends Handler
{
    // =============================================================================
    private static final String TAG = "EmailHandler";
    // =============================================================================

    // =============================================================================
    @Override
    public void handleMessage (Message theMessage)
    {
        // -------------------------------------------------------------------------
        // 06/12/2019 ECU log the information
        // -------------------------------------------------------------------------
        Utilities.LogToProjectFile(TAG,"Message type : " + theMessage.what);
        // -------------------------------------------------------------------------
        // 06/12/2019 ECU change to switch on the type of message received
        //                which is in '.what'
        // -------------------------------------------------------------------------
        switch (theMessage.what)
        {
            // =====================================================================
            case StaticData.MESSAGE_EMAIL_SEND:
                // -----------------------------------------------------------------
                // 06/12/2019 ECU called to transmit an email message whose details
                //                are contained in the object
                // -----------------------------------------------------------------
                EmailMessage emailMessage = (EmailMessage) theMessage.obj;
                // -----------------------------------------------------------------
                // 06/12/2019 ECU now send the message
                // -----------------------------------------------------------------
                emailMessage.Send ();
                // -----------------------------------------------------------------
                break;
            // =====================================================================
            case StaticData.MESSAGE_EMAIL_SENT:
                // -----------------------------------------------------------------
                // 04/12/2019 ECU called when an email has been sent
                // -----------------------------------------------------------------
                // 04/12/2019 ECU check if there are any queued messages to be sent
                // -----------------------------------------------------------------
                if ((PublicData.emailMessages != null) && (PublicData.emailMessages.size() > 0))
                {
                    // -------------------------------------------------------------
                    // 04/12/2019 ECU try and send the message which is at the top of
                    //                the list
                    // -------------------------------------------------------------
                    PublicData.emailMessages.get (0).Send ();
                    // -------------------------------------------------------------
                    // 04/12/2019 ECU delete the top entry
                    // -------------------------------------------------------------
                    PublicData.emailMessages.remove (0);
                    // -------------------------------------------------------------
                }
                // -----------------------------------------------------------------
                break;
            // =====================================================================
            default:
                // -----------------------------------------------------------------
                // 06/12/2019 ECU an 'unknown' message has been received
                // -----------------------------------------------------------------
                break;
             // ====================================================================
        }
    }
    // -----------------------------------------------------------------------------


    // =============================================================================
    public void SendEmailMessage (String theRecipients,String theSubject,String theMessage,String theExtras,String [] theAttachments)
    {
        // -------------------------------------------------------------------------
        // 06/12/2019 ECU created to send the defined email message
        // -------------------------------------------------------------------------
        Message localMessage = obtainMessage(StaticData.MESSAGE_EMAIL_SEND,
                                         new EmailMessage (theRecipients,
                                                           theSubject,
                                                           theMessage,
                                                           theExtras,
                                                           theAttachments));
        // -------------------------------------------------------------------------
        // 06/12/2019 ECU now trigger the transmission
        // -------------------------------------------------------------------------
        sendMessage (localMessage);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void SendEmailMessage (String theRecipients,String theSubject,String theMessage,String theExtras,String theAttachment)
    {
        // -------------------------------------------------------------------------
        // 06/12/2019 ECU created to send an email message which has a single
        //                attachment
        // -------------------------------------------------------------------------
        SendEmailMessage (theRecipients,theSubject,theMessage,theExtras,new String [] {theAttachment});
        // -------------------------------------------------------------------------
    }
    // =============================================================================
}
// =================================================================================

