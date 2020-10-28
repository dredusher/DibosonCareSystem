package com.usher.diboson;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

// =================================================================================
public class ArithmeticActivity extends DibosonActivity
{
    // =============================================================================
    private static final int    MESSAGE_START           =   0;
    private static final int    MESSAGE_RESULT          =   1;
    private static final int    MESSAGE_WAIT_FOR_ANSWER =   2;
    private static final int    ONE_SECOND              =   1000;
    // =============================================================================

    // =============================================================================
    private static int   []     keys                    = {
                                                            R.drawable.button_0,
                                                            R.drawable.button_1,
                                                            R.drawable.button_2,
                                                            R.drawable.button_3,
                                                            R.drawable.button_4,
                                                            R.drawable.button_5,
                                                            R.drawable.button_6,
                                                            R.drawable.button_7,
                                                            R.drawable.button_8,
                                                            R.drawable.button_9,
                                                            R.drawable.button_minus,
                                                            R.drawable.button_dot,
                                                            R.drawable.button_equals
                                                          };
    // =============================================================================

    // =============================================================================
    double          answerActual;
    double          answerEntered;
    AnswerHandler   answerHandler;
    TextView        answersTitleView;
    TextView        answersView;
    int             answerTimeOut;
    TextView        answerToProblem;
    Arithmetic      arithmetic;
    EditText        enteredAnswer;
    Button          newProblem;
    int             numberOfCorrectAnswers;
    int             numberOfQuestions;
    TextView        problemToSolve;
    TextView        questionsTitleView;
    TextView        questionsView;
    Button          settingsButton;
    boolean         sound;
    TextView        timer;
    ToneGenerator   toneGenerator;
    // =============================================================================

    // =============================================================================
    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        // -------------------------------------------------------------------------
        super.onCreate (savedInstanceState);
        // -------------------------------------------------------------------------
        if (savedInstanceState == null)
        {
            // ---------------------------------------------------------------------
            // 12/02/2020 ECU the activity has been created anew
            // ---------------------------------------------------------------------
            Utilities.SetUpActivity (this,StaticData.ACTIVITY_FULL_SCREEN);
            // ---------------------------------------------------------------------
            // 12/02/2020 ECU just display a dummy 'cracked screen'
            // ---------------------------------------------------------------------
            setContentView (R.layout.activity_arithmetic);
            // ---------------------------------------------------------------------
            // ---------------------------------------------------------------------
            // 05/03/2020 ECU set up the handler to handle the timer
            // ---------------------------------------------------------------------
            answerHandler = new AnswerHandler ();
            // ---------------------------------------------------------------------
            // 05/03/2020 ECU set up aspects of the form
            // ---------------------------------------------------------------------
            answersTitleView    = (TextView) findViewById (R.id.answers_title);
            answersView         = (TextView) findViewById (R.id.no_of_correct_answers);
            answerToProblem     = (TextView) findViewById (R.id.answer);
            enteredAnswer       = (EditText) findViewById (R.id.input_answer);
            newProblem          = (Button)   findViewById (R.id.new_problem);
            problemToSolve      = (TextView) findViewById (R.id.problem);
            questionsTitleView  = (TextView) findViewById (R.id.questions_title);
            questionsView       = (TextView) findViewById (R.id.no_of_questions);
            settingsButton      = (Button)   findViewById (R.id.arithmetic_settings);
            timer               = (TextView) findViewById (R.id.timer);
            // ---------------------------------------------------------------------
            // 12/03/2020 ECU adjust the width of some fields
            // ---------------------------------------------------------------------
            int fieldWidth = PublicData.screenWidth / 4;
            answersTitleView.setWidth   (fieldWidth);
            answersView.setWidth        (fieldWidth);
            questionsTitleView.setWidth (fieldWidth);
            questionsView.setWidth      (fieldWidth);
            // ---------------------------------------------------------------------
            answerTimeOut       = 60;
            sound               = true;
            // ---------------------------------------------------------------------
            toneGenerator = new ToneGenerator (AudioManager.STREAM_MUSIC, 100);
            // ---------------------------------------------------------------------
            // 05/03/2020 ECU initialise the arithmetic system
            // ---------------------------------------------------------------------
            // 14/03/2020 ECU check if any data has been stored
            // ---------------------------------------------------------------------
            if (PublicData.storedData.arithmeticData == null)
            {
                // -----------------------------------------------------------------
                // 14/03/2020 ECU create the data object to store
                //            ECU added the number of operands
                //                   0 ............. low number
                //                   100 ........... the range
                //                   0 ............. the input precision
                //                   1 ............. the result precision
                //                   2 ............. the number of operands
                // -----------------------------------------------------------------
                PublicData.storedData.arithmeticData = new ArithmeticData (0,100,0,1,2);
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
            // 14/03/2020 ECU create the arithmetic object using the stored data
            // ---------------------------------------------------------------------
            arithmetic = new Arithmetic (PublicData.storedData.arithmeticData);
            // ---------------------------------------------------------------------
            // 05/03/2020 ECU initialise the running totals
            // ---------------------------------------------------------------------
            numberOfCorrectAnswers  =   0;
            numberOfQuestions       =   0;
            // ---------------------------------------------------------------------
            // 05/03/2020 ECU set the button handler
            // ---------------------------------------------------------------------
            newProblem.setOnClickListener (new View.OnClickListener()
            {
                @Override
                public void onClick (View theView)
                {
                    // -------------------------------------------------------------
                    poseAProblem ();
                    // -------------------------------------------------------------
                }
            });
            // ---------------------------------------------------------------------
            enteredAnswer.setOnEditorActionListener (new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    if (actionId == EditorInfo.IME_ACTION_DONE)
                    {
                        String value = (enteredAnswer.getText().toString()).trim();
                        // ---------------------------------------------------------
                        if (!Utilities.isStringBlank(value))
                        {
                            try
                            {
                                answerEntered = Double.parseDouble(value);
                                // -----------------------------------------------------
                                // 05/03/2020 ECU check if the entered answer is correct
                                // -----------------------------------------------------
                                checkAnswer (answerEntered,answerActual);
                                // -----------------------------------------------------
                                answerHandler.removeMessages (MESSAGE_WAIT_FOR_ANSWER);
                                // -----------------------------------------------------
                                answerHandler.sendEmptyMessage (MESSAGE_RESULT);
                                // -----------------------------------------------------
                            }
                            catch (NumberFormatException theException)
                            {
                                Utilities.popToastAndSpeak  (getString (R.string.arithmetic_enter_number),true);
                            }
                            // ----------------------------------------------------------
                        }
                        else
                        {
                            Utilities.popToastAndSpeak  (getString (R.string.arithmetic_enter_number),true);
                        }
                        // ---------------------------------------------------------
                    }
                    return false;
                }
            });
            // ---------------------------------------------------------------------
            settingsButton.setOnClickListener (new View.OnClickListener()
            {
                    // -------------------------------------------------------------
                    @Override
                    public void onClick (View view)
                    {
                        // ---------------------------------------------------------
                        // 14/03/2020 ECU enter settings
                        // ---------------------------------------------------------
                        answerHandler.removeMessages (MESSAGE_WAIT_FOR_ANSWER);
                        // ---------------------------------------------------------
                        settings ();
                        // ---------------------------------------------------------
                    }
                    // -----------------------------------------------------------------
            });
            // ---------------------------------------------------------------------
            // 05/03/2020 ECU now start the system
            // ---------------------------------------------------------------------
            poseAProblem ();
            // ---------------------------------------------------------------------
        }
        else
        {
            // ---------------------------------------------------------------------
            // 12/02/2020 ECU the activity has been recreated after having been
            //                destroyed by the Android OS
            // ---------------------------------------------------------------------
            finish ();
            // ---------------------------------------------------------------------
        }
    }
    // =============================================================================
    @Override
    public void onBackPressed ()
    {
        // -------------------------------------------------------------------------
        double localResult = (numberOfCorrectAnswers * 100.0) / numberOfQuestions;
        Utilities.popToastAndSpeak (String.format (getString (R.string.arithmetic_result_format),localResult),true);
        // -------------------------------------------------------------------------
        answerHandler.removeMessages (MESSAGE_WAIT_FOR_ANSWER);
        // -------------------------------------------------------------------------
        // 06/03/2020 ECU try and hide the soft keyboard
        // -------------------------------------------------------------------------
        Utilities.softKeyboard (this,enteredAnswer,false);
        // -------------------------------------------------------------------------
        // 05/03/2020 ECU terminate this activity
        // -------------------------------------------------------------------------
        finish ();
        // -------------------------------------------------------------------------
        // 12/02/2020 ECU now call the super for this method
        // -------------------------------------------------------------------------
        super.onBackPressed ();
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public void onDestroy()
    {
        // -------------------------------------------------------------------------
        // 14/02/2012 ECU added
        // -------------------------------------------------------------------------
        super.onDestroy();
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void beep ()
    {
        // -------------------------------------------------------------------------
        // 05/03/2020 ECU created to sound a short beep
        // 06/03/2020 ECU add the enablement check
        // -------------------------------------------------------------------------
        if (sound)
        {
            // ---------------------------------------------------------------------
            try
            {
                // -----------------------------------------------------------------
                // 05/03/2020 ECU sound a short beep
                // -----------------------------------------------------------------
                toneGenerator.startTone (ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_SP_PRI,20);
                // -----------------------------------------------------------------
            }
            catch (Exception theException)
            {

            }
            // ---------------------------------------------------------------------
        }
    }
    // =============================================================================
    void checkAnswer (double theEnteredAnswer,double theActualAnswer)
    {
        // -------------------------------------------------------------------------
        if (theEnteredAnswer == theActualAnswer)
        {
            Utilities.popToastAndSpeak (getString (R.string.arithmetic_right),true);
            // ---------------------------------------------------------------------
            numberOfCorrectAnswers++;
            answersView.setText   (StaticData.BLANK_STRING + numberOfCorrectAnswers);
            // ---------------------------------------------------------------------
        }
        else
        {
            // ---------------------------------------------------------------------
            // 05/03/2020 ECU display the actual answer
            // ---------------------------------------------------------------------
            answerToProblem.setText (getString (R.string.arithmetic_answer) + arithmetic.answer.Print());
            // ---------------------------------------------------------------------
            Utilities.popToastAndSpeak (getString (R.string.arithmetic_wrong),true);
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    int getNumberFromView (EditText theView)
    {
        // -------------------------------------------------------------------------
        // 14/03/2020 ECU read the string from the view and return as a number
        // -------------------------------------------------------------------------
        return Integer.parseInt (theView.getText().toString());
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void poseAProblem ()
    {
        // -------------------------------------------------------------------------
        // 05/03/2020 ECU created to generate an 'arithmetic question'
        // 14/03/2020 ECU change to use more than 1 operand
        // -------------------------------------------------------------------------
        arithmetic.setAProblem ();
        problemToSolve.setText (arithmetic.printTheProblem ());
        answerActual = arithmetic.solveTheProblem();
        // -------------------------------------------------------------------------
        // 05/03/2020 ECU clear any associated fields
        //--------------------------------------------------------------------------
        answerToProblem.setText (StaticData.BLANK_STRING);
        enteredAnswer.setText (StaticData.BLANK_STRING);
        // -------------------------------------------------------------------------
        // 05/03/2020 ECU delete any queued messages for the handler
        // -------------------------------------------------------------------------
        answerHandler.removeMessages (MESSAGE_WAIT_FOR_ANSWER);
        answerHandler.removeMessages (MESSAGE_START);
        // -------------------------------------------------------------------------
        // 05/03/2020 ECU start up the 'answering' process
        // -------------------------------------------------------------------------
        Message localMessage = answerHandler.obtainMessage (MESSAGE_START,answerTimeOut,0);
        answerHandler.sendMessage (localMessage);
        // -------------------------------------------------------------------------
        // 06/03/2020 ECU try and display the soft keyboard
        // -------------------------------------------------------------------------
        Utilities.softKeyboard (this,enteredAnswer,true);
        // -------------------------------------------------------------------------
        numberOfQuestions++;
        // -------------------------------------------------------------------------
        questionsView.setText (StaticData.BLANK_STRING + numberOfQuestions);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void restartThisActivity ()
    {
        // -------------------------------------------------------------------------
        // 18/12/2016 ECU 'finish' this activity
        // 28/06/2019 ECU changed from 'static'
        //            ECU changed from 'GameTwo.context' to 'context'
        // -------------------------------------------------------------------------
        finish ();
        // -------------------------------------------------------------------------
        // 18/12/2016 ECU restart this activity
        // -------------------------------------------------------------------------
        Intent localIntent = new Intent (this,ArithmeticActivity.class);
        localIntent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
        // -------------------------------------------------------------------------
        startActivity (localIntent);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    void settings ()
    {
        // -------------------------------------------------------------------------
        // 14/03/2020 ECU handle the settings for the arithmetic activity
        // -------------------------------------------------------------------------
        setContentView (R.layout.arithmetic_settings);

        // -------------------------------------------------------------------------
        final EditText lowNumberView          = (EditText)findViewById (R.id.arithmetic_low_number);
        final EditText rangeView              = (EditText)findViewById (R.id.arithmetic_range);
        final EditText inputPrecisionView     = (EditText)findViewById (R.id.arithmetic_input_precision);
        final EditText operandsView           = (EditText)findViewById (R.id.arithmetic_operands);
        final EditText resultPrecisionView    = (EditText)findViewById (R.id.arithmetic_result_precision);
        // -------------------------------------------------------------------------
        // 14/03/2020 ECU display the current settings
        // -------------------------------------------------------------------------
        inputPrecisionView.setText      (Integer.toString (arithmetic.lowNumber.precision));
        lowNumberView.setText           (arithmetic.lowNumber.Print ());
        operandsView.setText            (Integer.toString (arithmetic.operands.length));
        rangeView.setText               (arithmetic.printNumber (arithmetic.range));
        resultPrecisionView.setText     (Integer.toString (arithmetic.answer.precision));
        // -------------------------------------------------------------------------

        // -------------------------------------------------------------------------
        ((Button) findViewById (R.id.arithmetic_settings_done)).setOnClickListener (new View.OnClickListener()
        {
            // ---------------------------------------------------------------------
            @Override
            public void onClick (View view)
            {
                // -----------------------------------------------------------------
                // 14/03/2020 ECU enter settings
                // -----------------------------------------------------------------
                int inputPrecision  = getNumberFromView (inputPrecisionView);
                int lowNumber       = getNumberFromView (lowNumberView);
                int operands        = getNumberFromView (operandsView);
                int range           = getNumberFromView (rangeView);
                int resultPrecision = getNumberFromView (resultPrecisionView);
                // -----------------------------------------------------------------
                // 14/03/2020 ECU store the input data
                // -----------------------------------------------------------------
                PublicData.storedData.arithmeticData
                    = new ArithmeticData (lowNumber,range,inputPrecision,resultPrecision,operands);
                // -----------------------------------------------------------------
                // 14/03/2020 ECU restart this activity to take advantage of the
                //                new data
                // -----------------------------------------------------------------
                restartThisActivity ();
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
        });
    }
    // =============================================================================

    // =============================================================================
    class AnswerHandler extends Handler
    {
        // -------------------------------------------------------------------------
        int     timeAllowed = StaticData.NOT_SET;
        int     timeToAnswer;
        // -------------------------------------------------------------------------
        @Override
        public void handleMessage (Message theMessage)
        {
            // ---------------------------------------------------------------------
            switch (theMessage.what)
            {
                // -----------------------------------------------------------------
                case MESSAGE_START:
                    // -------------------------------------------------------------
                    timeAllowed = theMessage.arg1;
                    sendEmptyMessage (MESSAGE_WAIT_FOR_ANSWER);
                    timer.setTextColor (getResources().getColor(R.color.gray));
                    // -------------------------------------------------------------
                    timeToAnswer = 0;
                    // -------------------------------------------------------------
                    break;
                // -----------------------------------------------------------------
                case MESSAGE_RESULT:
                    // -------------------------------------------------------------
                    timer.setTextColor (getResources().getColor( R.color.black));
                    timer.setText (StaticData.BLANK_STRING + timeToAnswer);
                    // -------------------------------------------------------------
                    break;
                // -----------------------------------------------------------------
                case MESSAGE_WAIT_FOR_ANSWER:
                    // -------------------------------------------------------------
                    // 06/03/2020 ECU waiting for the user to enter an answer
                    // -------------------------------------------------------------
                    if (timeToAnswer == timeAllowed)
                    {
                        // ---------------------------------------------------------
                        timer.setTextColor (getResources().getColor(R.color.red));
                        // ---------------------------------------------------------
                        // 05/03/2020 ECU indicate that the timer has expired
                        // ---------------------------------------------------------
                        Utilities.popToastAndSpeak (getString (R.string.arithmetic_timeout),true);
                        // ---------------------------------------------------------
                    }
                    // -------------------------------------------------------------
                    // 06/03/2020 ECU display the current time taken to answer -
                    //                changing from a 'count down' to an 'actual
                    //                time'
                    // --------------------------------------------------------------
                    timer.setText (StaticData.BLANK_STRING + ((timeToAnswer < timeAllowed) ? (timeAllowed - timeToAnswer)
                                                                                           : timeToAnswer));
                    // -------------------------------------------------------------
                    // 06/03/2020 ECU increment the 'time to answer' timer
                    // -------------------------------------------------------------
                    timeToAnswer++;
                    // -------------------------------------------------------------
                    beep ();
                    // -------------------------------------------------------------
                    // 06/03/2020 ECU queue a message for next prompt
                    // -------------------------------------------------------------
                    sendEmptyMessageDelayed (MESSAGE_WAIT_FOR_ANSWER,ONE_SECOND);
                    // -------------------------------------------------------------
                    break;
                // -----------------------------------------------------------------
            }
        }
    };
    // =============================================================================

    // =============================================================================
    public static class ImageAdapter extends BaseAdapter
    {
        // -------------------------------------------------------------------------
        // 11/05/2015 ECU changed to 'static'
        // 12/05/2015 ECU added the text flag
        // -------------------------------------------------------------------------
        private Context mContext;
        // =========================================================================
        public ImageAdapter(Context theContext)
        {
            mContext 			= theContext;
        }
        // =========================================================================
        @Override
        public int getCount()
        {
            // ---------------------------------------------------------------------
            return keys.length;
            // ---------------------------------------------------------------------
        }
        // =========================================================================
        @Override
        public Object getItem (int position)
        {
            return keys [position];
        }
        /* ========================================================================= */
        @Override
        public long getItemId (int position)
        {
            return 0;
        }
        /* ------------------------------------------------------------------------- */
        @Override
        public View getView (int position, View convertView, ViewGroup parent)
        {
            ImageView imageView = new ImageView (mContext);
            imageView.setImageResource (keys [position]);
            imageView.setScaleType (ImageView.ScaleType.FIT_CENTER);
            // ---------------------------------------------------------------------
            // 15/02/2014 ECU added - try to scale image to fit
            // 16/02/2014 ECU changed to use 'keySize'
            // ---------------------------------------------------------------------
            imageView.setLayoutParams(new GridView.LayoutParams (PublicData.screenWidth / 12,PublicData.screenWidth / 12));
            // ---------------------------------------------------------------------
            return imageView;
            // ---------------------------------------------------------------------
        }
        /* ------------------------------------------------------------------------- */
    }
    // =============================================================================

}
// =================================================================================
