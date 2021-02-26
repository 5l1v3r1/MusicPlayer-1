package com.ftmusic.hadise

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import cafe.adriel.kbus.KBus
import com.chibatching.kotpref.KotprefModel

class MusicService: Service() {

    var mediaPlayer: MediaPlayer? = null
    var fileName : ArrayList<String> = ArrayList()
    var musicNameList : ArrayList<String> = ArrayList()
    var playerIndex = 0
    var isRepeat: Boolean = false
    var isPaused:Boolean = true
    private val myBinder = MyLocalBinder()
    var isResume = false
    private lateinit var mHandler: Handler
    private lateinit var mRunnable:Runnable

    override fun onCreate() {
        super.onCreate()
        init()

        getBackgroundNotification(MainActivity.PrefModel.context, this, musicNameList.get(playerIndex))
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }



    fun init(){

        getMusicList()

        checkReplay()

        KBus.subscribe<ActivityToService>(this){ it->
            when(it.sendMediCommand){
                "next" -> changeMusic("next")
                "back" -> changeMusic("back")
                "pause_resume" -> clickPauseresume()
                "stop" -> stopMusic()
                "repeat" -> isRepeat = true
                "auto" -> isRepeat = false
                "playIndex" -> {
                    playerIndex = it.data.toString().toInt()
                    playMusic()
                }
                "seekTo" -> {
                    if (mediaPlayer!=null){
                        mediaPlayer?.seekTo(it.data!!)
                    }

                }
                "play" -> {
                    if (mediaPlayer == null){
                        playMusic()
                    } else if (!mediaPlayer!!.isPlaying){
                        mediaPlayer?.start()
                    }
                }
                "pause" -> {
                    if (mediaPlayer != null){
                        if (mediaPlayer!!.isPlaying){
                            mediaPlayer?.pause()
                        }
                    }

                }
                "resume" -> isResume = true
            }

        }
        stateSeekBar()

    }

    object PrefModel : KotprefModel() {
        var repeatPref: Boolean by PrefModel.booleanPref(false)
    }

    fun checkReplay(){
        if (PrefModel.repeatPref){
            isRepeat = true

        } else {
            isRepeat = false
        }
    }

    fun resumeApp(){
        isResume = false
        KBus.post(ServiceToActivity(sendMediCommand = "playerIndex", data = playerIndex ))
        var durationMedia: Int = 0
        durationMedia =  mediaPlayer!!.duration
        if (durationMedia==null) durationMedia = 0
        KBus.post(ServiceToActivity(mediaStatusCmd = "duration", position = durationMedia, isDuration = true))
        KBus.post(ServiceToActivity(sendMediCommand = "play", data = playerIndex ))
        playBus()
    }

    fun clickPauseresume(){
        if (mediaPlayer!!.isPlaying){
            mediaPlayer!!.pause()
            isPaused = true
        } else if (mediaPlayer!=null && !mediaPlayer!!.isPlaying){
            mediaPlayer!!.start()
            isPaused = false
        }
    }

    inner class MyLocalBinder : Binder() {
        fun getService() : MusicService {
            return this@MusicService
        }
    }


    override fun onBind(p0: Intent?): IBinder? {

        return myBinder
    }

    fun getMedia():Int {
        return resources.getIdentifier("raw/" + fileName.get(playerIndex), null, packageName)
    }

    fun getMusicList(){
        for (name in resources.getStringArray(R.array.fileName)) fileName.add(name)
        for (name in resources.getStringArray(R.array.musicTitle)) musicNameList.add(name)
    }

    fun stopMusic(){
        KBus.post(ServiceToActivity(sendMediCommand = "pause", data = playerIndex ))

        isPaused = true
        if (mediaPlayer != null) {
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
            mediaPlayer = null
        }

        KBus.post(ServiceToActivity(mediaStatusCmd = "position", position = 0, isDuration = false))
        KBus.post(ServiceToActivity(mediaStatusCmd = "duration", position = 0, isDuration = true))



    }

    fun playMusic(){
        stopMusic()
        mediaPlayer = MediaPlayer.create(applicationContext, getMedia())
        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener(MediaPlayer.OnCompletionListener {
            if (isRepeat){
                changeMusic("repeat")
            } else {
                changeMusic("next")
            }
        })
        playBus()
        
        getBackgroundNotification(MainActivity.PrefModel.context, this, musicNameList.get(playerIndex))
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

    }

    fun playBus(){

        KBus.post(ServiceToActivity(sendMediCommand = "playerIndex", data = playerIndex ))

        KBus.post(ServiceToActivity(mediaStatusCmd = "duration", position = mediaPlayer!!.duration, isDuration = true))

        isPaused = false
        KBus.post(ServiceToActivity(sendMediCommand = "play", data = playerIndex ))
    }

    fun changeMusic(param:String){
        when(param){
            "next"->{
                if (playerIndex < fileName.size-1){
                    playerIndex++
                } else {
                    playerIndex = 0
                }
            }
            "back"->{
                if (playerIndex > 0){
                    playerIndex--
                } else  {
                    playerIndex = fileName.size-1
                }
            }
        }
        playMusic()
    }


    fun stateSeekBar(){
        mHandler = Handler()
        mRunnable =  Runnable {

            if (!isPaused){

                if (mediaPlayer!=null && mediaPlayer!!.isPlaying){

                    var pos = mediaPlayer?.currentPosition!!
                    KBus.post(ServiceToActivity(mediaStatusCmd = "position", position = pos, isDuration = false))

                    if (isResume){
                        resumeApp()
                    }

                }
            }

            mHandler.postDelayed(mRunnable, 1000)
        }
        mHandler.postDelayed(mRunnable, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        KBus.unsubscribe(this)
    }

}
