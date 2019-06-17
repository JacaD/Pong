package com.example.pongkotlin

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.graphics.Point
import android.view.View

class MainActivity : Activity() {
    private var game: Game? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun startGame(mode: Int){
        val gameIntent = Intent(this, GameActivity::class.java)
        gameIntent.putExtra("mode", mode)
        startActivity(gameIntent)
    }

    fun startEasy(v: View){
       startGame(Game.EASY)
    }

    fun startNormal(v: View){
        startGame(Game.NORMAL)
    }

    fun startHard(v: View){
        startGame(Game.HARD)
    }

    fun startDarkSouls(v: View){
        startGame(Game.DARK_SOULS)
    }

    override fun onResume() {
        super.onResume()
        if(game != null) {
            game!!.pause()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(game != null) {
            game!!.pause()
        }
        setContentView(R.layout.activity_main)
    }
    override fun onPause() {
        super.onPause()
//        game!!.pause()
    }
}
