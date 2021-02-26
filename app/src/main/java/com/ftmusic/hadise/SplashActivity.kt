package com.ftmusic.hadise

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.podcopic.animationlib.library.AnimationType
import com.podcopic.animationlib.library.StartSmartAnimation
import java.util.concurrent.TimeUnit
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.Completable

class SplashActivity : AppCompatActivity() {

    lateinit var splashImage:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        splashImage = findViewById(R.id.splashImageButton)
        startHandler()
    }

    fun startHandler(){

        val timer = object: CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }
        }

        timer.start()

        StartSmartAnimation.startAnimation( findViewById(R.id.splashImageButton) , AnimationType.BounceInUp , 4000 , 0 , true );

        Completable.timer(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                splashImage.visibility = View.VISIBLE
            }

    }
}
