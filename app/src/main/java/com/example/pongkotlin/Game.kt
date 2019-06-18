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

internal class Game(context: Context, var screenX: Int, var screenY: Int, mode: Int = 0) : SurfaceView(context), Runnable,
    SensorEventListener {

    private var gameThread: Thread? = null
    @Volatile
    var playing: Boolean = false
    private var paused = true
    private lateinit var canvas: Canvas
    private var paint: Paint
    private var fps: Float = 0f
    private var bat: Bat
    private var batAI: Bat
    private var ball: Ball
    private var sp: SoundPool
    private var beep1ID = -1
    private var beep2ID = -1
    private var beep3ID = -1
    private var loseLifeID = -1
    private var explodeID = -1
    private var score = 0
    private var lives = 3
    private val sensorManager: SensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor

    init {
        var length = 0f
        var speed = 1f

        when (mode) {
            EASY -> {
                length = screenX / 15f
                speed = 0.7f
            }
            NORMAL -> {
                length = screenX / 10f
                speed = 0.75f
            }
            HARD -> {
                length = screenX / 8f
                speed = 0.85f
            }
            DARK_SOULS -> {
                length = screenX / 2f
                speed = 20f
            }
        }

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        paint = Paint()

        bat = Bat(screenX, screenY, false)
        batAI = Bat(screenX, screenY, true, length, speed)
        ball = Ball(screenX, screenY)


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
        ball.reset(screenX, screenY)

        if (lives == 0) {
            score = 0
            lives = 3
        }
    }

    override fun run() {
        while (playing) {
            val startFrameTime = System.currentTimeMillis()
            if (!paused) {
                update()
            }
            draw()
            val timeThisFrame = System.currentTimeMillis() - startFrameTime
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame.toFloat()
            }
        }
    }

    private fun update() {

        bat.update(fps)
        ball.update(fps)
        batAI.update(fps * 1.6f)

        if (RectF.intersects(bat.rect, ball.rect)) {
            if(ball.getXVelocity() > 0 && bat.getCenter() > ball.getCenter()){
                ball.reverseXVelocity()
            }
            else if(ball.getXVelocity() < 0 && bat.getCenter() < ball.getCenter()){
                ball.reverseXVelocity()
            }
            ball.reverseYVelocity()
            ball.clearObstacleY(bat.rect.top - 5)
//            ball.increaseVelocity()
            sp.play(beep1ID, 1f, 1f, 0, 0, 1f)
        }

        if (RectF.intersects(batAI.rect, ball.rect)) {
            ball.reverseYVelocity()
            ball.clearObstacleY(batAI.rect.bottom + 5, true)
//            ball.increaseVelocity()
            sp.play(beep1ID, 1f, 1f, 0, 0, 1f)
        }

        if (ball.rect.bottom > screenY) {
            ball.rect.bottom = screenY.toFloat()
            ball.reverseYVelocity()
            ball.clearObstacleY(screenY.toFloat() - 2)
            lives--
            sp.play(loseLifeID, 1f, 1f, 0, 0, 1f)
            if (lives == 0) {
                paused = true
                setupAndRestart()
            }
        }

        if (ball.rect.top < 0) {
            ball.rect.top = 0f
            score++
            ball.reverseYVelocity()
            sp.play(beep2ID, 1f, 1f, 0, 0, 1f)
        }

        if (ball.rect.left < 0) {
            ball.rect.left = 0f
            ball.reverseXVelocity()
            ball.clearObstacleX(2f)
            sp.play(beep3ID, 1f, 1f, 0, 0, 1f)
        }

        if (ball.rect.right > screenX) {
            ball.rect.right = screenX.toFloat()
            ball.reverseXVelocity()
            ball.clearObstacleX(screenX.toFloat() - 22)
            sp.play(beep3ID, 1f, 1f, 0, 0, 1f)
        }

        when {
            ball.getYVelocity() < 0 && batAI.getCenter() < ball.getCenter() - 5 -> batAI.setMovementState(Bat.RIGHT)
            ball.getYVelocity() < 0 && batAI.getCenter() > ball.getCenter() + 5 -> batAI.setMovementState(Bat.LEFT)
            else -> batAI.setMovementState(Bat.STOPPED)
        }
    }

    private fun draw() {

        if (holder.surface.isValid) {
            canvas = holder.lockCanvas()
            canvas.drawColor(Color.argb(255, 26, 128, 182))
            paint.color = Color.argb(255, 255, 255, 255)
            canvas.drawRect(bat.rect, paint)
            canvas.drawRect(ball.rect, paint)
            canvas.drawRect(batAI.rect, paint)
            paint.color = Color.argb(255, 249, 129, 0)
            paint.color = Color.argb(255, 255, 255, 255)
            paint.textSize = 40f
            canvas.drawText("Score: $score   Lives: $lives", 10f, 100f, paint)
            holder.unlockCanvasAndPost(canvas)
        }
    }

    fun pause() {
        sensorManager.unregisterListener(this)
        playing = false
        try {
            gameThread!!.join()
        } catch (e: InterruptedException) {
            Log.e("Error:", "joining thread")
        }

    }

    fun resume() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        playing = true
        gameThread = Thread(this)
        gameThread!!.start()
    }


    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        paused = !paused
//        paused = false
        return true
    }

    override fun onSensorChanged(event: SensorEvent) {
        val tilt = event.values[1].toDouble()
        when {
            tilt > 0.5 -> {
                bat.setMovementState(Bat.RIGHT)
                bat.setmBatSpeed(tilt)
            }
            tilt < -0.5 -> {
                bat.setMovementState(Bat.LEFT)
                bat.setmBatSpeed(tilt)
            }
            else -> bat.setMovementState(Bat.STOPPED)
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
