package com.example.pongkotlin

import android.app.Activity
import android.graphics.Point
import android.os.Bundle

class GameActivity : Activity() {
    private lateinit var game: Game

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val mode = intent?.extras?.getInt("mode")
        game = Game(this, size.x, size.y, mode ?: 0)
        setContentView(game)
    }


    override fun onResume() {
        super.onResume()
        game.resume()
    }

    override fun onPause() {
        super.onPause()
        game.pause()
    }
}
