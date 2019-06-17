package com.example.pongkotlin

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceView

import java.io.IOException

import android.content.Context.SENSOR_SERVICE

internal class Game(context: Context, var mScreenX: Int, var mScreenY: Int, mode: Int = 0) : SurfaceView(context), Runnable,
    SensorEventListener {

    private var mGameThread: Thread? = null
    @Volatile
    var mPlaying: Boolean = false
    private var mPaused = true
    private lateinit var mCanvas: Canvas
    private var mPaint: Paint
    private var mFPS: Float = 0f
    private var mBat: Bat
    private var mBatAI: Bat
    private var mBall: Ball
    private var sp: SoundPool
    private var beep1ID = -1
    private var beep2ID = -1
    private var beep3ID = -1
    private var loseLifeID = -1
    private var explodeID = -1
    private var mScore = 0
    private var mLives = 3
    private val mSensorManager: SensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
    private val mAccelerometer: Sensor

    init {
        var length = 0f
        var speed = 1f

        when (mode) {
            EASY -> {
                length = mScreenX / 15f
                speed = 0.7f
            }
            NORMAL -> {
                length = mScreenX / 10f
                speed = 0.75f
            }
            HARD -> {
                length = mScreenX / 8f
                speed = 0.85f
            }
            DARK_SOULS -> {
                length = mScreenX / 2f
                speed = 20f
            }
        }

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        mPaint = Paint()

        mBat = Bat(mScreenX, mScreenY, false)
        mBatAI = Bat(mScreenX, mScreenY, true, length, speed)
        mBall = Ball(mScreenX, mScreenY)


        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        sp = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        try {
            val assetManager = context.assets
            var descriptor: AssetFileDescriptor

            descriptor = assetManager.openFd("beep1.ogg")
            beep1ID = sp.load(descriptor, 0)

            descriptor = assetManager.openFd("beep2.ogg")
            beep2ID = sp.load(descriptor, 0)

            descriptor = assetManager.openFd("beep3.ogg")
            beep3ID = sp.load(descriptor, 0)

            descriptor = assetManager.openFd("loseLife.ogg")
            loseLifeID = sp.load(descriptor, 0)

            descriptor = assetManager.openFd("explode.ogg")
            explodeID = sp.load(descriptor, 0)

        } catch (e: IOException) {
            Log.e("error", "failed to load sound files")
        }

        setupAndRestart()
    }

    private fun setupAndRestart() {
        mBall.reset(mScreenX, mScreenY)

        if (mLives == 0) {
            mScore = 0
            mLives = 3
        }
    }

    override fun run() {
        while (mPlaying) {
            val startFrameTime = System.currentTimeMillis()
            if (!mPaused) {
                update()
            }
            draw()
            val timeThisFrame = System.currentTimeMillis() - startFrameTime
            if (timeThisFrame >= 1) {
                mFPS = 1000 / timeThisFrame.toFloat()
            }
        }
    }

    private fun update() {

        mBat.update(mFPS)
        mBall.update(mFPS)
        mBatAI.update(mFPS * 1.6f)

        if (RectF.intersects(mBat.rect, mBall.rect)) {
            if(mBall.getXVelocity() > 0 && mBat.getCenter() > mBall.getCenter()){
                mBall.reverseXVelocity()
            }
            else if(mBall.getXVelocity() < 0 && mBat.getCenter() < mBall.getCenter()){
                mBall.reverseXVelocity()
            }
            mBall.reverseYVelocity()
            mBall.clearObstacleY(mBat.rect.top - 5)
//            mBall.increaseVelocity()
            sp.play(beep1ID, 1f, 1f, 0, 0, 1f)
        }

        if (RectF.intersects(mBatAI.rect, mBall.rect)) {
            mBall.reverseYVelocity()
            mBall.clearObstacleY(mBatAI.rect.bottom + 5, true)
//            mBall.increaseVelocity()
            sp.play(beep1ID, 1f, 1f, 0, 0, 1f)
        }

        if (mBall.rect.bottom > mScreenY) {
            mBall.rect.bottom = mScreenY.toFloat()
            mBall.reverseYVelocity()
            mBall.clearObstacleY(mScreenY.toFloat() - 2)
            mLives--
            sp.play(loseLifeID, 1f, 1f, 0, 0, 1f)
            if (mLives == 0) {
                mPaused = true
                setupAndRestart()
            }
        }

        if (mBall.rect.top < 0) {
            mBall.rect.top = 0f
            mScore++
            mBall.reverseYVelocity()
            sp.play(beep2ID, 1f, 1f, 0, 0, 1f)
        }

        if (mBall.rect.left < 0) {
            mBall.rect.left = 0f
            mBall.reverseXVelocity()
            mBall.clearObstacleX(2f)
            sp.play(beep3ID, 1f, 1f, 0, 0, 1f)
        }

        if (mBall.rect.right > mScreenX) {
            mBall.rect.right = mScreenX.toFloat()
            mBall.reverseXVelocity()
            mBall.clearObstacleX(mScreenX.toFloat() - 22)
            sp.play(beep3ID, 1f, 1f, 0, 0, 1f)
        }

        when {
            mBall.getYVelocity() < 0 && mBatAI.getCenter() < mBall.getCenter() - 5 -> mBatAI.setMovementState(Bat.RIGHT)
            mBall.getYVelocity() < 0 && mBatAI.getCenter() > mBall.getCenter() + 5 -> mBatAI.setMovementState(Bat.LEFT)
            else -> mBatAI.setMovementState(Bat.STOPPED)
        }
    }

    private fun draw() {

        if (holder.surface.isValid) {
            mCanvas = holder.lockCanvas()
            mCanvas.drawColor(Color.argb(255, 26, 128, 182))
            mPaint.color = Color.argb(255, 255, 255, 255)
            mCanvas.drawRect(mBat.rect, mPaint)
            mCanvas.drawRect(mBall.rect, mPaint)
            mCanvas.drawRect(mBatAI.rect, mPaint)
            mPaint.color = Color.argb(255, 249, 129, 0)
            mPaint.color = Color.argb(255, 255, 255, 255)
            mPaint.textSize = 40f
            mCanvas.drawText("Score: $mScore   Lives: $mLives", 10f, 100f, mPaint)
            holder.unlockCanvasAndPost(mCanvas)
        }
    }

    fun pause() {
        mSensorManager.unregisterListener(this)
        mPlaying = false
        try {
            mGameThread!!.join()
        } catch (e: InterruptedException) {
            Log.e("Error:", "joining thread")
        }

    }

    fun resume() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        mPlaying = true
        mGameThread = Thread(this)
        mGameThread!!.start()
    }


    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        mPaused = !mPaused
//        mPaused = false
        return true
    }

    override fun onSensorChanged(event: SensorEvent) {
        val tilt = event.values[1].toDouble()
        when {
            tilt > 0.5 -> {
                mBat.setMovementState(Bat.RIGHT)
                mBat.setmBatSpeed(tilt)
            }
            tilt < -0.5 -> {
                mBat.setMovementState(Bat.LEFT)
                mBat.setmBatSpeed(tilt)
            }
            else -> mBat.setMovementState(Bat.STOPPED)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    companion object{
        const val EASY: Int = 0
        const val NORMAL: Int = 1
        const val HARD: Int = 2
        const val DARK_SOULS = 3
    }
}
