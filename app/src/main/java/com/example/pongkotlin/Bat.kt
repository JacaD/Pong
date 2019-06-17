package com.example.pongkotlin

import android.graphics.RectF

class Bat(private val mScreenX: Int,
          private val mScreenY: Int,
          private val isAI: Boolean,
          private val mLength: Float = mScreenX / 6.0f,
          private val speed: Float = 1.0f) {

    val rect: RectF
//    private val mLength: Float = mScreenX / 6.0f
    private val mHeight: Float = mScreenY / 25.0f
    private var mXCoord: Float = 0f
    private val mYCoord: Float
    private var mBatSpeed: Float = 0f
    private val mBatBaseSpeed: Float
    private var mBatMoving = STOPPED

    init {

        mXCoord = mScreenX / 2.0f
        mYCoord = if(isAI){
            -10f
        } else{
            (mScreenY - 30).toFloat()
        }

        rect = RectF(mXCoord, mYCoord, mXCoord + mLength, mYCoord + mHeight)
        mBatBaseSpeed = mScreenX / 2.0f
        mBatSpeed = mScreenX / 2.0f
    }

    fun setmBatSpeed(acceleratorValue: Double) {
        mBatSpeed = mBatBaseSpeed * Math.abs(acceleratorValue).toFloat()
    }

    fun setMovementState(state: Int) {
        mBatMoving = state
    }

    fun getCenter(): Float{
        return (rect.left + rect.right) / 2f
    }

    fun update(fps: Float) {
        if (mBatMoving == LEFT) {
            mXCoord -= mBatSpeed / fps * speed
        }

        if (mBatMoving == RIGHT) {
            mXCoord += mBatSpeed / fps * speed
        }
        if (rect.left < 0) {
            mXCoord = 0f
        }
        if (rect.right > mScreenX) {
            mXCoord = mScreenX - (rect.right - rect.left)
        }
        rect.left = mXCoord
        rect.right = mXCoord + mLength
    }
    companion object {
        const val STOPPED = 0
        const val LEFT = 1
        const val RIGHT = 2
    }
}
