package com.example.sevg.swipeToDismiss

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.*
import android.view.View.OnTouchListener
import android.widget.LinearLayout
import kotlin.math.abs


/**
 * A [View.OnTouchListener] that makes any [View] dismissable when the
 * user swipes (drags her finger) horizontally across the view.
 *
 *
 * *For [ListView] list items that don't manage their own touch events
 * (i.e. you're using
 * [ListView.setOnItemClickListener]
 * or an equivalent listener on [ListActivity] or
 * [ListFragment], use [SwipeDismissListViewTouchListener] instead.*
 *
 *
 * Example usage:
 *
 * <pre>
 * view.setOnTouchListener(new SwipeDismissTouchListener(
 * view,
 * null, // Optional token/cookie object
 * new SwipeDismissTouchListener.OnDismissCallback() {
 * public void onDismiss(View view, Object token) {
 * parent.removeView(view);
 * }
 * }));
</pre> *
 *
 *
 * This class Requires API level 12 or later due to use of [ ].
 *
 *
 */
class SwipeDismissTouchListener(view: View, token: Any?, callbacks: DismissCallbacks?) :
    OnTouchListener
{
    // Cached ViewConfiguration and system-wide constant values
    private val mSlop: Int
    private val mMinFlingVelocity: Int
    private val mMaxFlingVelocity: Int
    private val mAnimationTime: Long

    // Fixed properties
    private val mView: View
    private val mCallbacks: DismissCallbacks?
    private var mViewWidth = 1 // 1 and not 0 to prevent dividing by zero

    // Transient properties
    private var mDownX = 0f
    private var mDownY = 0f
    private var mSwiping = false
    private var mSwipingSlop = 0
    private val mToken: Any?
    private var mVelocityTracker: VelocityTracker? = null
    private var mTranslationX = 0f

    var lp: ViewGroup.LayoutParams? = null
    var originalHeight = 0

    /**
     * The callback interface used by [SwipeDismissTouchListener] to inform its client
     * about a successful dismissal of the view for which it was created.
     */
    interface DismissCallbacks
    {
        /**
         * Called to determine whether the view can be dismissed.
         */
        fun canDismiss(token: Any?): Boolean

        /**
         * Called when the user has indicated they she would like to dismiss the view.
         *
         * @param view  The originating [View] to be dismissed.
         * @param token The optional token passed to this object's constructor.
         */
        fun onDismiss(view: View?, token: Any?)

        fun shareDismissView(viewLayoutParams: ViewGroup.LayoutParams?, height: Int)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean
    {
        // offset because the view is translated during swipe
        motionEvent.offsetLocation(mTranslationX, 0f)
        if (mViewWidth < 2)
        {
            mViewWidth = mView.width
        }
        when (motionEvent.actionMasked)
        {
            MotionEvent.ACTION_DOWN ->
            {

                // TODO: ensure this is a finger, and set a flag
                mDownX = motionEvent.rawX
                mDownY = motionEvent.rawY
                if (mCallbacks?.canDismiss(mToken) == true)
                {
                    mVelocityTracker = VelocityTracker.obtain()
                    mVelocityTracker?.addMovement(motionEvent)
                }
                return false
            }
            MotionEvent.ACTION_UP ->
            {
                mVelocityTracker?.let {mVelocityTrackerObj ->


                    val deltaX = motionEvent.rawX - mDownX
                    mVelocityTrackerObj.addMovement(motionEvent)
                    mVelocityTrackerObj.computeCurrentVelocity(1000)
                    val velocityX = mVelocityTrackerObj.xVelocity
                    val absVelocityX = abs(velocityX)
                    val absVelocityY = abs(mVelocityTrackerObj.yVelocity)
                    var dismiss = false
                    var dismissRight = false
                    if (abs(deltaX) > mViewWidth / 2 && mSwiping)
                    {
                        dismiss = true
                        dismissRight = deltaX > 0
                    } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity && absVelocityY < absVelocityX && absVelocityY < absVelocityX && mSwiping)
                    {
                        // dismiss only if flinging in the same direction as dragging
                        dismiss = velocityX < 0 == deltaX < 0
                        dismissRight = mVelocityTrackerObj.xVelocity > 0
                    }
                    if (dismiss)
                    {
                        // dismiss
                        mView.animate()
                            .translationX(if (dismissRight) mViewWidth.toFloat() else -mViewWidth.toFloat())
                            .alpha(0f)
                            .setDuration(mAnimationTime)
                            .setListener(object : AnimatorListenerAdapter()
                            {
                                override fun onAnimationEnd(animation: Animator)
                                {
                                    performDismiss()
                                }
                            })
                    } else if (mSwiping)
                    {
                        // cancel
                        mView.animate()
                            .translationX(0f)
                            .alpha(1f)
                            .setDuration(mAnimationTime)
                            .setListener(null)
                    }
                    mVelocityTrackerObj.recycle()
                    mVelocityTracker = null
                    mTranslationX = 0f
                    mDownX = 0f
                    mDownY = 0f
                    mSwiping = false
                }
            }
            MotionEvent.ACTION_CANCEL ->
            {
                mVelocityTracker?.let {mVelocityTrackerObj ->


                    mView.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(mAnimationTime)
                        .setListener(null)
                    mVelocityTrackerObj.recycle()
                    mVelocityTracker = null
                    mTranslationX = 0f
                    mDownX = 0f
                    mDownY = 0f
                    mSwiping = false
                }
            }
            MotionEvent.ACTION_MOVE ->
            {
                mVelocityTracker?.let {mVelocityTrackerObj ->

                    mVelocityTrackerObj.addMovement(motionEvent)
                    val deltaX = motionEvent.rawX - mDownX
                    val deltaY = motionEvent.rawY - mDownY
                    if (abs(deltaX) > mSlop && abs(deltaY) < abs(deltaX) / 2)
                    {
                        mSwiping = true
                        mSwipingSlop = if (deltaX > 0) mSlop else -mSlop
                        mView.parent.requestDisallowInterceptTouchEvent(true)

                        // Cancel listview's touch
                        val cancelEvent = MotionEvent.obtain(motionEvent)
                        cancelEvent.action = MotionEvent.ACTION_CANCEL or
                                (motionEvent.actionIndex shl
                                        MotionEvent.ACTION_POINTER_INDEX_SHIFT)
                        mView.onTouchEvent(cancelEvent)
                        cancelEvent.recycle()
                    }
                    if (mSwiping)
                    {
                        mTranslationX = deltaX
                        mView.translationX = deltaX - mSwipingSlop
                        // TODO: use an ease-out interpolator or such
                        mView.alpha = 0f.coerceAtLeast(
                            1f.coerceAtMost(1f - 2f * Math.abs(deltaX) / mViewWidth)
                        )
                        return true
                    }
                }
            }
        }
        return false
    }

    fun resetTheDeletedView(viewLayoutParams: ViewGroup.LayoutParams?,
                            height: Int, view: View)
    {
        view.postDelayed({
            lp = viewLayoutParams
            originalHeight = height

            view.alpha = 1f
            view.translationX = 0f
            lp?.height = originalHeight
            view.layoutParams = lp
        }, 200)
    }

    private fun performDismiss()
    {
        // Animate the dismissed view to zero-height and then fire the dismiss callback.
        // This triggers layout on each animation frame; in the future we may want to do something
        // smarter and more performant.

        lp = mView.layoutParams
        originalHeight = mView.height

        mCallbacks?.shareDismissView(mView.layoutParams, mView.height)

        val animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime)
        animator.addListener(object : AnimatorListenerAdapter()
        {
            override fun onAnimationEnd(animation: Animator)
            {
                mCallbacks?.onDismiss(mView, mToken)
            }
        })
        animator.addUpdateListener { valueAnimator ->
            lp?.height = (valueAnimator.animatedValue as Int)
            mView.layoutParams = lp
        }
        animator.start()
    }

    /**
     * Constructs a new swipe-to-dismiss touch listener for the given view.
     *
     * @param view     The view to make dismissable.
     * @param token    An optional token/cookie object to be passed through to the callback.
     * @param callbacks The callback to trigger when the user has indicated that she would like to
     * dismiss this view.
     */
    init
    {
        val vc = ViewConfiguration.get(view.context)
        mSlop = vc.scaledTouchSlop
        mMinFlingVelocity = vc.scaledMinimumFlingVelocity * 16
        mMaxFlingVelocity = vc.scaledMaximumFlingVelocity
        mAnimationTime = view.context.resources.getInteger(
            android.R.integer.config_shortAnimTime
        ).toLong()
        mView = view
        mToken = token
        mCallbacks = callbacks
    }
}