package com.usher.diboson;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;


// =================================================================================
public class SwipeDismissListViewTouchListener implements View.OnTouchListener 
{
    // =============================================================================
    // 09/06/2015 ECU created to handle 'swiping' on a listview item which will be
    //                used to give the user the option of dismissing that item. In
    //                the context of the app 'dismissal' == 'deletion'
    // =============================================================================

    // =============================================================================
    private static final boolean VELOCITY_CHECK = false;        // 16/05/2020 ECU added
    // =============================================================================

    // =============================================================================
    private long                        animationTime;
    private int                         maximumFlingVelocity;
    private int                         minimumFlingVelocity;
    private int                         touchSlop;
    // =============================================================================
    // 09/06/2015 ECU Fixed properties
    // 17/05/2020 ECU changed the 'listViewWidth' preset from '1'
    // -----------------------------------------------------------------------------
    private OnDismissCallback           dismissalCallback;
    private ListView                    listView;
    private int                         listViewWidth = StaticData.NOT_SET;
    // =============================================================================
    // 09/06/2015 ECU Transient properties
    // -----------------------------------------------------------------------------
    private int                         dismissAnimationRefCount = 0;
    private boolean                     paused;
    private List<PendingDismissData>    pendingDismissals = new ArrayList<PendingDismissData>();
    private boolean                     swipingInProgress;
    private VelocityTracker             swipeVelocityTracker;
    private int                         touchedPosition;
    private float                       touchedXPosition;
    private View                        touchedView;
    // =============================================================================
    public interface OnDismissCallback
    {
        // -------------------------------------------------------------------------
        // 09/06/2015 ECU the 'callback' interface to inform the client about a
        //                successful dismissal of one or more list item positions
        // -------------------------------------------------------------------------
        void onDismiss (ListView listView,int [] reverseSortedPositions);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public SwipeDismissListViewTouchListener (ListView theListView, OnDismissCallback theCallback)
    {
        // -------------------------------------------------------------------------
        // 09/06/2015 ECU created as the 'swipe to dismiss' listener for the given
        //                list view
        //
        //                  theListView ..... the list view whose items should be
        //                                    dismissible
        //                  theCallback ..... the callback to trigger when the user
        //                                    has indicated that one or more list items
        //                                    are to be dismissed. It is not automatically
        //                                    dissmissed - the information is passed
        //                                    back to the listener, via the callback, so
        //                                    that the user has the option of confirming
        //                                    the dismissal or not
        // -------------------------------------------------------------------------
        dismissalCallback   = theCallback;
        listView            = theListView;
        // -------------------------------------------------------------------------
        // 17/05/2020 ECU if try and set 'listViewWidth' at this point using
        //                listView.getWidth () then this returns '0'. This is because
        //                although 'listView' has been defined it's characteristics
        //                have not been set up yet - see 'Selector.java' for the sequence
        //                of things. Perhaps, at some stage, it would be better to
        //                re-arrange the order in which the listener is set up in
        //                the calling class, e.g. Selector.
        // -------------------------------------------------------------------------
        // 09/06/2015 ECU get some configuration details from the list view
        // -------------------------------------------------------------------------
        ViewConfiguration viewConfiguration = ViewConfiguration.get (listView.getContext ());
        // -------------------------------------------------------------------------
        // 09/06/2015 ECU Maximum velocity to initiate a fling, as measured in dips per second.
        // -------------------------------------------------------------------------
        maximumFlingVelocity    = viewConfiguration.getScaledMaximumFlingVelocity ();
        // -------------------------------------------------------------------------
        // 09/06/2015 ECU Minimum velocity to initiate a fling, as measured in dips per second.
        // -------------------------------------------------------------------------
        minimumFlingVelocity    = viewConfiguration.getScaledMinimumFlingVelocity ();
        // -------------------------------------------------------------------------
        // 09/06/2015 ECU Distance in dips a touch can wander before we think the user is scrolling
        // -------------------------------------------------------------------------
        touchSlop               = viewConfiguration.getScaledTouchSlop ();
        // -------------------------------------------------------------------------
        // 09/06/2015 ECU the time used for any animation
        // -------------------------------------------------------------------------
        animationTime = listView.getContext().getResources().getInteger (android.R.integer.config_shortAnimTime);
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public void setEnabled (boolean enabled)
    {
        // -------------------------------------------------------------------------
        // 09/06/2015 ECU indicate whether watching for 'swipe to dismiss' gestures
        // -------------------------------------------------------------------------
        paused = !enabled;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    public AbsListView.OnScrollListener makeScrollListener()
     {
        // -------------------------------------------------------------------------
        // 09/06/2015 ECU declare the listener that will be called each time the
        //                list view is scrolled
        // -------------------------------------------------------------------------
        return new AbsListView.OnScrollListener ()
        {
            // ---------------------------------------------------------------------
            @Override
            public void onScrollStateChanged (AbsListView absListView, int scrollState)
            {
                // -----------------------------------------------------------------
                // 09/06/2015 ECU set the correct 'enablement'
                // -----------------------------------------------------------------
                setEnabled (scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
            @Override
            public void onScroll (AbsListView absListView, int i, int i1, int i2)
            {
                // -----------------------------------------------------------------
                // 09/06/2015 ECU nothing needs to be done on each scroll
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
        };
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    @Override
    public boolean onTouch (View theView, MotionEvent theMotionEvent)
    {
        // -------------------------------------------------------------------------
        // 09/06/2015 ECU created to handle a 'touch' event on the list view
        // -------------------------------------------------------------------------
        // 17/05/2020 ECU check if the width of the list view needs to be set
        // -------------------------------------------------------------------------
        if (listViewWidth == StaticData.NOT_SET)
        {
            // ---------------------------------------------------------------------
            // 09/06/2015 ECU have not set the width yet so do so
            // ---------------------------------------------------------------------
            listViewWidth = listView.getWidth ();
            // ---------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
        // 09/06/2015 ECU handle the action that was detected
        // -------------------------------------------------------------------------
        switch (theMotionEvent.getActionMasked())
        {
            // ---------------------------------------------------------------------
            // ---------------------------------------------------------------------
            case MotionEvent.ACTION_DOWN:
            {
                // -----------------------------------------------------------------
                // 09/06/2015 ECU a 'pressed gesture' has been started
                // -----------------------------------------------------------------
                // 09/06/2015 ECU do not do anything if in a 'paused' state
                // -----------------------------------------------------------------
                if (paused)
                {
                    return false;
                }
                // -----------------------------------------------------------------
                // 09/06/2015 ECU Find the child view that was touched (perform a hit test)
                // -----------------------------------------------------------------
                int[] listViewCoords = new int [2];
                listView.getLocationOnScreen (listViewCoords);
                // -----------------------------------------------------------------
                // 09/06/2015 ECU get the position for this gesture
                // -----------------------------------------------------------------
                int x = (int) theMotionEvent.getRawX () - listViewCoords [0];
                int y = (int) theMotionEvent.getRawY () - listViewCoords [1];
                // -----------------------------------------------------------------
                // 09/06/2015 ECU loop through the children looking for the one that
                //                was touched
                // -----------------------------------------------------------------
                View child;
                Rect rect = new Rect ();
                int childCount = listView.getChildCount();
                for (int index = 0; index < childCount; index++)
                {
                    child = listView.getChildAt (index);
                    // -------------------------------------------------------------
                    // 09/06/2015 ECU get the 'hit rectangle' for this child
                    // -------------------------------------------------------------
                    child.getHitRect (rect);
                    if (rect.contains (x, y))
                    {
                        // ---------------------------------------------------------
                        // 09/06/2015 ECU have found the child that was 'touched'
                        // ---------------------------------------------------------
                        touchedView = child;
                        break;
                        // ---------------------------------------------------------
                    }
                }
                // -----------------------------------------------------------------
                // 09/06/2015 ECU check if a view was found
                // -----------------------------------------------------------------
                if (touchedView != null)
                {
                    // -------------------------------------------------------------
                    // 09/06/2015 ECU child view found so remember some details
                    // --------------------------------------------------------------
                    touchedXPosition    = theMotionEvent.getRawX();
                    touchedPosition     = listView.getPositionForView (touchedView);
                    // --------------------------------------------------------------
                    // 09/06/2015 ECU create a helper for tracking the velocity of
                    //                the touch event
                    // --------------------------------------------------------------
                    swipeVelocityTracker = VelocityTracker.obtain ();
                    // --------------------------------------------------------------
                    // 09/06/2015 ECU add the user's movement event to the tracker
                    // --------------------------------------------------------------
                    swipeVelocityTracker.addMovement (theMotionEvent);
                    // --------------------------------------------------------------
                }
                // ------------------------------------------------------------------
                // 09/06/2015 ECU call up the listener for the 'touch' event
                // ------------------------------------------------------------------
                theView.onTouchEvent (theMotionEvent);
                // ------------------------------------------------------------------
                return true;
                // ------------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
            // ---------------------------------------------------------------------
            case MotionEvent.ACTION_UP:
            {
                // -----------------------------------------------------------------
                // 09/06/2015 ECU the user has 'lifted finger off' the screen
                // -----------------------------------------------------------------
                // 09/06/2015 ECU if there is no velocity tracker then ignore this
                //                event
                // -----------------------------------------------------------------
                if (swipeVelocityTracker == null)
                {
                     break;
                }
                // -----------------------------------------------------------------
                float deltaX = theMotionEvent.getRawX () - touchedXPosition;
                // -----------------------------------------------------------------
                // 09/06/2015 ECU add the current event into the tracker and then
                //                get the associated velocity
                //            ECU the argument with '...compute...' are the units that
                //                the velocity is to be provided in. A value of 1
                //                provides pixels per millisecond, 1000 provides pixels
                //                per second, etc.
                // -----------------------------------------------------------------
                swipeVelocityTracker.addMovement (theMotionEvent);
                swipeVelocityTracker.computeCurrentVelocity (1000);
                // -----------------------------------------------------------------
                // 09/06/2015 ECU get the calculated velocity
                // -----------------------------------------------------------------
                float velocityX = Math.abs (swipeVelocityTracker.getXVelocity ());
                float velocityY = Math.abs (swipeVelocityTracker.getYVelocity ());
                // -----------------------------------------------------------------
                // 09/06/2015 ECU now work out if the view is one to be dismissed
                // -----------------------------------------------------------------
                boolean dismiss         = false;
                boolean dismissRight    = false;
                // -----------------------------------------------------------------
                // 09/06/2015 ECU work out how much of a movement will trigger the
                //                dismissal
                // 17/05/2020 ECU Note - a dismissal is triggered by either of the
                //                       conditions
                //                         1) the user swipes the item either right
                //                            or left by an amount which exceeds half
                //                            the width of the view
                //                       or
                //                         2) the user 'flings' up with a velocity
                //                            which exceeds the horizontal speed
                //
                //                       Condition 1 is the primary trigger for
                //                       dismissal. Condition 2 is dependent on
                //                       VELOCITY_CHECK - currently set to 'false'
                //                       so that it is not checked - it was confusing
                //                       to have this active.
                // -----------------------------------------------------------------
                if (Math.abs (deltaX) > (listViewWidth / 2))
                {
                    // -------------------------------------------------------------
                    // 09/06/2015 ECU there has been enough of a movement to trigger
                    //                a 'dismissal'
                    // -------------------------------------------------------------
                    dismiss      = true;
                    // -------------------------------------------------------------
                    // 17/05/2020 ECU Note - indicate whether the user swept right
                    //                       (true) or left (false)
                    // -------------------------------------------------------------
                    dismissRight = (deltaX > 0);
                    // -------------------------------------------------------------
                }
                else
                // -----------------------------------------------------------------
                // 09/06/2015 ECU check if the dismissal is triggered by velocity
                // 16/05/2020 ECU added the VELOCITY_CHECK condition
                // -----------------------------------------------------------------
                if (VELOCITY_CHECK &&
                        (minimumFlingVelocity <= velocityX) && (velocityX <= maximumFlingVelocity) &&
                            (velocityY < velocityX))
                {
                    // -------------------------------------------------------------
                    // 09/06/2015 ECU the velocity indicates a dismissal
                    // -------------------------------------------------------------
                    dismiss         = true;
                    // -------------------------------------------------------------
                    // 17/05/2020 ECU Note - indicate whether the user was sweeping
                    //                       right (true) or left (false) when
                    //                       triggered by a vertical movement.
                    // -------------------------------------------------------------
                    dismissRight    = (swipeVelocityTracker.getXVelocity () > 0);
                    // -------------------------------------------------------------
                }
                // -----------------------------------------------------------------
                // 09/06/2015 ECU check if the user's movement has indicated a
                //                dismissal
                // -----------------------------------------------------------------
                if (dismiss)
                {
                    // -------------------------------------------------------------
                    // 16/05/2020 ECU the user has indicated that this item is to
                    //                be dismissed
                    // -------------------------------------------------------------
                    final int   downPosition  = touchedPosition;
                    final View  downView      = touchedView;
                    // -------------------------------------------------------------
                    // 09/06/2015 ECU increment the 'count of dismissal animations'
                    // -------------------------------------------------------------
                    ++dismissAnimationRefCount;
                    // -------------------------------------------------------------
                    // 09/06/2020 ECU set up the required animation
                    //            ECU the 'translation' depends on whether the
                    //                user is scrolling to the right or left
                    // -------------------------------------------------------------
                    animate (touchedView)
                            .translationX (dismissRight ? listViewWidth
                                                        : -listViewWidth)
                            .alpha (0)
                            .setDuration (animationTime)
                            .setListener (new AnimatorListenerAdapter()
                            {
                                // -------------------------------------------------
                                // 09/06/2015 ECU set up the listener for the 'end
                                //                of animation' which is when the
                                //                user has the option to confirm
                                //                the dismissal or not
                                // -------------------------------------------------
                                @Override
                                public void onAnimationEnd(Animator animation)
                                {
                                    // ---------------------------------------------
                                    // 09/06/2015 ECU perform the actual dismissal
                                    // ---------------------------------------------
                                    performDismiss (downView,downPosition);
                                    // ---------------------------------------------
                                }
                                // -------------------------------------------------
                            });
                    // -------------------------------------------------------------
                }
                else
                {
                    // -------------------------------------------------------------
                    // 09/06/2015 ECU the user's scrolling is not enough for the
                    //                dismissal so cancel any ongoing animation
                    // -------------------------------------------------------------
                    animate (touchedView).translationX (0)
                                         .alpha (1)
                                         .setDuration (animationTime)
                                         .setListener (null);
                    // -------------------------------------------------------------
                }
                // -----------------------------------------------------------------
                // 09/06/2015 ECU reset some variables
                // -----------------------------------------------------------------
                swipeVelocityTracker    = null;
                swipingInProgress       = false;
                touchedXPosition        = 0;
                touchedView             = null;
                touchedPosition         = ListView.INVALID_POSITION;
                // -----------------------------------------------------------------
                break;
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
            // ---------------------------------------------------------------------
            case MotionEvent.ACTION_MOVE:
            {
                // -----------------------------------------------------------------
                // 09/06/2015 ECU the user is moving a finger across the screen
                // -----------------------------------------------------------------
                // 09/06/2015 ECU if there is no velocity tracker or the mechanism is
                //                paused then ignore this event
                // ------------------------------------------------------------------
                if (swipeVelocityTracker == null || paused)
                {
                    break;
                }
                // -----------------------------------------------------------------
                // 09/06/2015 ECU add this event into the tracker
                // -----------------------------------------------------------------
                swipeVelocityTracker.addMovement (theMotionEvent);
                // -----------------------------------------------------------------
                // 09/06/2015 ECU work out the change in the X co-ordinate
                // -----------------------------------------------------------------
                float deltaX = theMotionEvent.getRawX () - touchedXPosition;
                // -----------------------------------------------------------------
                // 09/06/2015 ECU check if the change in X is more than the 'slop'
                // -----------------------------------------------------------------
                if (Math.abs (deltaX) > touchSlop)
                {
                    // -------------------------------------------------------------
                    // 09/06/2015 ECU a 'swipe' has occurred
                    // -------------------------------------------------------------
                    swipingInProgress = true;
                    listView.requestDisallowInterceptTouchEvent (true);
                    // -------------------------------------------------------------
                    // 09/06/2015 ECU cancel the ListView's touch - un-highlighting
                    //                the item
                    // -------------------------------------------------------------
                    MotionEvent cancelEvent = MotionEvent.obtain (theMotionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (theMotionEvent.getActionIndex()
                                 << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    listView.onTouchEvent (cancelEvent);
                    // -------------------------------------------------------------
                }
                // -----------------------------------------------------------------
                // 09/06/2015 ECU check if a 'swipe' has occurred
                // -----------------------------------------------------------------
                if (swipingInProgress)
                {
                    // -------------------------------------------------------------
                    // 09/06/2015 ECU set up the animation
                    // -------------------------------------------------------------
                    setTranslationX (touchedView,deltaX);
                    setAlpha (touchedView, Math.max (0f,
                                        Math.min (1f,1f - 2f * Math.abs (deltaX) / listViewWidth)));
                    // --------------------------------------------------------------
                    return true;
                    // --------------------------------------------------------------
                }
                break;
            }
            // ---------------------------------------------------------------------
        }
        return false;
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    class PendingDismissData implements Comparable<PendingDismissData>
    {
        // -------------------------------------------------------------------------
        // 09/06/2015 ECU created to hold details of the item in the listview that
        //                is to be dismissed
        // -------------------------------------------------------------------------
        public int  position;                   // the position in the list view
        public View view;                       // the view of this item
        // -------------------------------------------------------------------------
        public PendingDismissData (int thePosition, View theView)
        {
            // ----------------------------------------------------------------------
            // 09/06/2015 ECU remember the arguments for later use
            //                  thePosition ..... the position of this item in the
            //                                    listview
            //                  theView ......... the view associated with this item
            // ----------------------------------------------------------------------
            this.position = thePosition;
            this.view     = theView;
            // ----------------------------------------------------------------------
        }
        // --------------------------------------------------------------------------
        @Override
        public int compareTo (PendingDismissData theOther)
        {
            // ----------------------------------------------------------------------
            // 09/06/2015 ECU Sort by descending position
            // ----------------------------------------------------------------------
            return theOther.position - position;
            // ----------------------------------------------------------------------
        }
        // -------------------------------------------------------------------------
    }
    // =============================================================================
    private void performDismiss (final View dismissView, final int dismissPosition)
    {
        // -------------------------------------------------------------------------
        // 09/06/2015 ECU Animate the dismissed list item to zero-height and fire the
        //                dismiss callback when all dismissed list item animations
        //                have completed. This triggers layout on each animation frame;
        //                in the future we may want to do something smarter and more efficient.
        // -------------------------------------------------------------------------
        // 09/06/2015 ECU get the details of the view to be dismissed - the
        //                parameters, particularly height, will be modified during
        //                the animation
        // -------------------------------------------------------------------------
        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams ();
        final int originalHeight        = dismissView.getHeight ();
        // -------------------------------------------------------------------------
        // 09/06/2015 ECU initialise the animator
        // -------------------------------------------------------------------------
        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(animationTime);
        // -------------------------------------------------------------------------
        // -------------------------------------------------------------------------
        // 09/06/2015 ECU add listeners associated with the animation
        // -------------------------------------------------------------------------
        // -------------------------------------------------------------------------
        animator.addListener (new AnimatorListenerAdapter ()
        {
            // ---------------------------------------------------------------------
            @Override
            public void onAnimationEnd (Animator animation)
            {
                // -----------------------------------------------------------------
                // 09/06/2015 ECU at the end of the animation
                // -----------------------------------------------------------------
                --dismissAnimationRefCount;
                // -----------------------------------------------------------------
                if (dismissAnimationRefCount == 0)
                {
                    // -------------------------------------------------------------
                    // 09/06/2015 ECU there are no active animations so process all
                    //                pending dismissals
                    // -------------------------------------------------------------
                    // 09/06/2025 ECU sort by descending position
                    // -------------------------------------------------------------
                    Collections.sort (pendingDismissals);
                    // -------------------------------------------------------------
                    // 09/06/2015 ECU build up an array to hold the positions of
                    //                the items to be dismissed
                    // -------------------------------------------------------------
                    int [] dismissPositions = new int [pendingDismissals.size ()];
                    // -------------------------------------------------------------
                    // 09/06/2015 ECU now store the positions
                    // -------------------------------------------------------------
                    for (int index = pendingDismissals.size() - 1; index >= 0; index--)
                    {
                        dismissPositions [index] = pendingDismissals.get (index).position;
                    }
                    // -------------------------------------------------------------
                    // 09/06/2015 ECU now tell the 'listener' that the user's swipe
                    //                action indicates a possible desire to dismiss
                    //                the specified item(s) in the list view
                    // -------------------------------------------------------------
                    dismissalCallback.onDismiss (listView,dismissPositions);
                    // -------------------------------------------------------------
                    // 09/06/2015 ECU now want to tidy up the display
                    // -------------------------------------------------------------
                    ViewGroup.LayoutParams lp;
                    // -------------------------------------------------------------
                    // 09/06/2015 ECU loop through each pending dismissal
                    // -------------------------------------------------------------
                    for (PendingDismissData pendingDismiss : pendingDismissals)
                    {
                        // ---------------------------------------------------------
                        // 09/06/2015 ECU reset the display for this dismissal
                        // ---------------------------------------------------------
                        setAlpha (pendingDismiss.view,1f);
                        setTranslationX (pendingDismiss.view, 0);
                        // ---------------------------------------------------------
                        // 09/06/2015 ECU restore the height of the view
                        // ---------------------------------------------------------
                        lp = pendingDismiss.view.getLayoutParams ();
                        lp.height = originalHeight;
                        pendingDismiss.view.setLayoutParams(lp);
                        // ---------------------------------------------------------
                    }
                    // -------------------------------------------------------------
                    // 09/06/2015 ECU now indicate that all 'pending dismissals' have
                    //                been actioned
                    // -------------------------------------------------------------
                    pendingDismissals.clear();
                    // -------------------------------------------------------------
                }
            }
        });
        // -------------------------------------------------------------------------
        // -------------------------------------------------------------------------
        animator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener()
        {
            // ---------------------------------------------------------------------
            @Override
            public void onAnimationUpdate (ValueAnimator valueAnimator)
            {
                // -----------------------------------------------------------------
                // 09/06/2015 ECU modify the height as the animation progresses
                // -----------------------------------------------------------------
                lp.height = (Integer) valueAnimator.getAnimatedValue ();
                dismissView.setLayoutParams (lp);
                // -----------------------------------------------------------------
            }
            // ---------------------------------------------------------------------
        });
        // -------------------------------------------------------------------------
        // -------------------------------------------------------------------------
        // 09/06/2015 ECU add this dismissal to the list of those currently pending
        // -------------------------------------------------------------------------
        pendingDismissals.add (new PendingDismissData (dismissPosition,dismissView));
        // -------------------------------------------------------------------------
        // 09/06/2015 ECU start up the animation
        // -------------------------------------------------------------------------
        animator.start ();
        // -------------------------------------------------------------------------
    }
    // =============================================================================
}
