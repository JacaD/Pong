package com.example.pongkotlin

import android.graphics.RectF

import java.util.Random

class Ball(screenX: Int, screenY: Int) {
    val rect: RectF
    private var mXVelocity: Float = 0f
    private var mYVelocity: Float = 0f
    private val mBallWidth: Float
    private var mBallHeight: Float = 0f

    init {
        mBallHeight = screenX / 75f
        mBallWidth = mBallHeight
        mXVelocity = -screenY / 2f
        mYVelocity = mXVelocity
        rect = RectF()
    }

    fun update(fps: Float) {
        rect.left = rect.left + mXVelocity / fps
        rect.top = rect.top + mYVelocity / fps
        rect.right = rect.left + mBallWidth
        rect.bottom = rect.top - mBallHeight
    }

    fun reverseYVelocity() {
        mYVelocity = -mYVelocity
    }

    fun reverseXVelocity() {
        mXVelocity = -mXVelocity
    }

    fun getXVelocity(): Float{
        return mXVelocity
    }

    fun getYVelocity(): Float{
        return mYVelocity
    }

    fun setRandomXVelocity() {
        val generator = Random()
        val answer = generator.nextInt(2)

        if (answer == 0) {
            reverseXVelocity()
        }
    }

    fun getCenter(): Float{
        return (rect.left + rect.right) / 2f
    }

    fun increaseVelocity() {
        mXVelocity += mXVelocity / 10
        mYVelocity += mYVelocity / 10
    }

    fun clearObstacleY(y: Float, isAI: Boolean = true) {
        rect.bottom = y
        rect.top = if(!isAI) y - mBallHeight else y + mBallHeight
    }

    fun clearObstacleX(x: Float) {
        rect.left = x
        rect.right = x + mBallWidth
    }

    fun reset(x: Int, y: Int) {
        rect.left = x / 2f
        rect.top = (y - 20).toFloat()
        rect.right = x / 2f + mBallWidth
        rect.bottom = y.toFloat() - 20f - mBallHeight
        mXVelocity = -y / 2f
        mYVelocity = mXVelocity
    }
}