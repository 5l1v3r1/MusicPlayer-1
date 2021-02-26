package com.ftmusic.hadise

import android.Manifest
import android.app.ActivityManager
import android.content.*
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.StrictMode
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnItemClick
import cafe.adriel.kbus.KBus
import com.chibatching.kotpref.Kotpref
import com.chibatching.kotpref.KotprefModel
import com.droidman.ktoasty.KToasty
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.podcopic.animationlib.library.AnimationType
import com.podcopic.animationlib.library.StartSmartAnimation
import com.suddenh4x.ratingdialog.AppRating
import com.suddenh4x.ratingdialog.buttons.RateDialogClickListener
import com.suddenh4x.ratingdialog.preferences.RatingThreshold
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {


    lateinit var drawer : DrawerLayout
    var musicListName : ArrayList<String> = ArrayList()
    var fileName : ArrayList<String> = ArrayList()
    var playerIndex = 0;
    var isRepeat:Boolean = false
    var isPaused:Boolean = true
    var isPlayMedia:Boolean = false

    @BindView(R.id.listMusicNameId) lateinit var musicListView : ListView
    @BindView(R.id.repeatButtonID) lateinit var repeatButton: ImageButton
    @BindView(R.id.seekBarId) lateinit var seekBar: SeekBar
    @BindView(R.id.playButtonID) lateinit var playButton:ImageButton
    @BindView(R.id.musicNameId) lateinit var tvMusicMane:TextView

    @BindView(R.id.tvCurrentPosition) lateinit var tvCurrentPosition:TextView
    @BindView(R.id.tvDuration) lateinit var tvDuration:TextView

    @BindView(R.id.lyShowContentId) lateinit var lyShowContent : LinearLayout

    var serverAppUrl:String = "https://storage.googleapis.com/androidfilespace/music_app_sources/app_list.json"
    @BindView(R.id.listAppNameId) lateinit var appNameListView : ListView


    var musicServicess: MusicService? = null
    lateinit var serviceIntent:Intent
    var isBound: Boolean = false

    lateinit var mInterstitialAd:InterstitialAd
    var rklmIndex:Int = 4

    lateinit var mAdView:AdView
    lateinit var adRequest:AdRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        toggle.syncState()


        toggle.isDrawerIndicatorEnabled = false;
        toggle.setHomeAsUpIndicator(R.drawable.drawer_icon);
        toolbar.setNavigationIcon(R.drawable.drawer_icon);
        toolbar.setNavigationOnClickListener(clickToolbar);
        drawer.addDrawerListener(toggle);

        drawer.openDrawer(GravityCompat.START);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        
        MobileAds.initialize(this) {}

        mAdView = findViewById(R.id.bannerDefault)
        adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        init()

    }

    fun init(){
        serviceIntent =  Intent(applicationContext, MusicService::class.java)

        ButterKnife.bind(this)

        Kotpref.init(this)

        getMusicList()

        checkReplay()

        checkService()

        getAllCommand()

        preSeekBar()

        loadFullPage()

        rateMyApp()

        getOurAppList()

    }


    fun rateMyApp(){
        val appRatingBuilder = AppRating.Builder(this)

        appRatingBuilder
            .setMinimumLaunchTimes(3)
            .setMinimumDays(0)
            .setMinimumLaunchTimesToShowAgain(2)
            .setMinimumDaysToShowAgain(2)
            .setIconDrawable(ContextCompat.getDrawable(applicationContext, R.mipmap.ic_launcher))
            .setRatingThreshold(RatingThreshold.THREE)
            .setCancelable(false)
            .setStoreRatingMessageTextId(R.string.rate_us_desc)
            .setTitleTextId(R.string.app_name)
            .setUseCustomFeedback(false)
            .setStoreRatingTitleTextId(R.string.app_name)
            .setRateNowButtonTextId(R.string.rate_us)
            .setFeedbackTitleTextId(R.string.app_name)
            .setMailFeedbackMessageTextId(R.string.rate_us_desc)
            .setMailFeedbackButton(R.string.rate_us, object : RateDialogClickListener {
                override fun onClick() {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                    } catch (anfe: android.content.ActivityNotFoundException) {
                        AppRating.reset(this@MainActivity)
                    }
                }
            })
            .setRateLaterButton(R.string.later_rate_us)
            .setNoFeedbackButton(R.string.later_rate_us)
            .setRateNowButtonClickListener(object : RateDialogClickListener {
                override fun onClick() {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                    } catch (anfe: android.content.ActivityNotFoundException) {
                        AppRating.reset(this@MainActivity)
                    }
                }
            })
            .showIfMeetsConditions()
    }

    override fun onPause() {
        super.onPause()
        if (mAdView!=null) mAdView.pause()
    }
    fun createNotify(){

        tvMusicMane.text = (musicListName.get(playerIndex))
        setTitle(musicListName.get(playerIndex))
        createAnimation()
    }

    fun createAnimation(){
        Completable.timer(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                if (!(lyShowContent.visibility == View.VISIBLE)) lyShowContent.visibility = View.VISIBLE
            }
        StartSmartAnimation.startAnimation( findViewById(R.id.musicNameId) , AnimationType.BounceInDown , 4000 , 0 , true );
        StartSmartAnimation.startAnimation( findViewById(R.id.profile_image) , AnimationType.BounceInUp , 5000 , 0 , true );
        StartSmartAnimation.startAnimation( findViewById(R.id.profile_image) , AnimationType.Swing , 4000 , 200 , true );
    }

    fun checkReplay(){
        if (PrefModel.repeatPref){
            isRepeat = true
            repeatButton.setBackgroundResource(R.mipmap.repeat_enabled_r)
            KBus.post(ActivityToService("repeat"))

        } else {
            isRepeat = false
            KBus.post(ActivityToService("auto"))
            repeatButton.setBackgroundResource(R.mipmap.repeat_disable_r)
        }
    }

    object PrefModel : KotprefModel() {
        var repeatPref: Boolean by booleanPref(false)
    }

    val clickToolbar = View.OnClickListener{
        if (!drawer.isDrawerOpen(GravityCompat.START)){
            drawer.openDrawer(GravityCompat.START)
        }
    }
    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        }
        else {
            super.onBackPressed()
        }
    }

    fun getMusicList(){
        musicListName.clear()
        for (name in resources.getStringArray(R.array.musicTitle)) musicListName.add(name)
        for (name in resources.getStringArray(R.array.fileName)) fileName.add(name)
        musicListView.adapter = MusicAdapter(applicationContext, musicListName)
    }

    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (packageName+".MusicService" == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun checkService(){

        if (!isServiceRunning()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            bindService(serviceIntent, myConnection, Context.BIND_AUTO_CREATE)

        } else {
            resumeApp()
        }
    }


    @OnItemClick(R.id.listMusicNameId)
    fun clickMedia(position: Int){
        rklmIndex += 1
        KBus.post(ActivityToService("playIndex", position))
        playerIndex = position
        createNotify()
        isPaused = false
        closeMyDrawer()
        if (rklmIndex >= 3) showFullPage()

    }

    @OnClick(R.id.nextButtonID)
    fun clickNext(){
        KBus.post(ActivityToService("next"))
    }
    @OnClick(R.id.repeatButtonID)
    fun clickRepeat(view: ImageButton){

        if (isRepeat){
            KBus.post(ActivityToService("auto"))
            view.setBackgroundResource(R.mipmap.repeat_disable_r)
            KToasty.info(applicationContext, "Auto play enabled!", Toast.LENGTH_SHORT, true).show()
            isRepeat = false
        }
        else{
            isRepeat = true
            KBus.post(ActivityToService("repeat"))
            view.setBackgroundResource(R.mipmap.repeat_enabled_r)
            KToasty.info(applicationContext, "Repeat play enabled!", Toast.LENGTH_SHORT, true).show()

        }

        PrefModel.repeatPref = isRepeat
    }

    @OnClick(R.id.backButtonID)
    fun clickBack(){
        KBus.post(ActivityToService("back"))
    }

    fun preSeekBar(){

        seekBar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if(p2){
                    KBus.post(ActivityToService("seekTo", p1))
                }
            }
        })
    }



    fun closeMyDrawer(){
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START)
    }

    @OnClick(R.id.playButtonID)
    fun clickPlayBtn(){
        if (isPlayMedia){
            KBus.post(ActivityToService("pause"))
            playButton.setImageResource(R.mipmap.play_r)
            isPlayMedia = false
        } else {
            KBus.post(ActivityToService("play"))
            createNotify()
            playButton.setImageResource(R.mipmap.pause_r)
            isPlayMedia = true

        }

    }

    @OnClick(R.id.stopButtonID)
    fun clickStopBtn(){
        KBus.post(ActivityToService("stop"))
        if (lyShowContent.visibility == View.VISIBLE) lyShowContent.visibility = View.GONE
    }

    fun setTime(finalTime:Long) : String{
        var start = TimeUnit.MILLISECONDS.toMinutes(finalTime)
        var end = TimeUnit.MILLISECONDS.toSeconds(finalTime)-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime))
        return String.format("%d:%d",start, end)
    }

    @OnClick(R.id.goToButtonID)
    fun clickSettingBtn(){
        askPermission()
    }
    fun askPermission(){
        val request = permissionsBuilder(
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.INTERNET
        ).build()
        request.listeners {

            onAccepted { permissions ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.System.canWrite(applicationContext)) {
                        showDialog()
                    } else {
                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                            .setData(Uri.parse("package:"+packageName))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        showDialog()
                    }
                } else {
                    showDialog()
                }
            }

            onDenied { permissions ->
            }

            onPermanentlyDenied { permissions ->
            }

            onShouldShowRationale { permissions, nonce ->
            }
        }
        request.send()
    }

    fun loadFullPage(){
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = resources.getString(R.string.fullPageAds)
        mInterstitialAd.loadAd(AdRequest.Builder().build())
    }

    fun showFullPage(){
        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
            loadFullPage()
            rklmIndex = 0
        }
    }

    fun showDialog(){
        var selectionIndex = 0
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setSingleChoiceItems(resources.getStringArray(R.array.ringtone), 0, object : DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                selectionIndex = p1
            }
        })
            .setCancelable(true)
            .setPositiveButton("SAVE", DialogInterface.OnClickListener {
                    dialog, id -> chooseRingtone(selectionIndex)
            })
            .setNegativeButton("CANCEL", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })
        val alert = dialogBuilder.create()
        alert.setTitle(musicListName.get(playerIndex))
        alert.show()
    }

    fun getAllCommand(){

        if (isPlayMedia){
            closeMyDrawer()
        }
        KBus.subscribe<ServiceToActivity>(this){ it->

            when(it.mediaStatusCmd){
                "position" -> {
                    tvCurrentPosition.text = setTime(it.currentPosition.toLong())
                    seekBar.progress = it.currentPosition
                }

                "duration" -> {
                    seekBar.max = it.duration
                    seekBar.progress = 0
                    tvDuration.text = setTime(it.duration.toLong())
                }
            }

            when(it.sendMediCommand){
                "playerIndex" -> {
                    playerIndex = it.data!!
                }

                "play" -> {
                    playButton.setImageResource(R.mipmap.pause_r)
                    isPlayMedia = true
                    createNotify()
                }

                "pause" -> {
                    playButton.setImageResource(R.mipmap.play_r)
                    isPlayMedia = false
                }

            }
        }

        KBus.subscribe<ActivityToService>(this){ it->
            when(it.sendMediCommand){
                "next" -> {
                    isPaused = false
                }
                "back" -> {
                    isPaused = false
                }
                "stop" -> {
                    isPaused = true
                    isPlayMedia = false
                }
                "repeat" -> repeatButton.setImageResource(R.mipmap.repeat_enabled_r)
                "auto" -> repeatButton.setImageResource(R.mipmap.repeat_disable_r)
            }

        }

    }

    override fun onResume() {
        super.onResume()
        if (mAdView!=null) mAdView.resume()
        resumeApp()
    }

    fun resumeApp(){
        if (isServiceRunning()){
            getAllCommand()
            KBus.post(ActivityToService("resume"))
            if (isPlayMedia) closeMyDrawer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mAdView!=null) mAdView.destroy()
        if (!isPlayMedia){
            if (isBound){
                unbindService(myConnection)
                isBound = false
            }
            stopService(serviceIntent)
            KBus.unsubscribe(this)
        }
    }

    fun chooseRingtone(typeRing:Int){

        when(typeRing){
            0 -> setRingtone(RingtoneManager.TYPE_RINGTONE)
            1 -> setRingtone(RingtoneManager.TYPE_ALARM)
        }

    }

    fun setRingtone(ringType:Int){

        var statusType:Boolean = true
        var soundPath:Uri = Uri.parse("android.resource://"+getPackageName()+"/raw/"+fileName.get(playerIndex))

        try {
            RingtoneManager.setActualDefaultRingtoneUri(applicationContext, ringType,
                    soundPath)

        }catch (e:Exception){
            statusType = false
            KToasty.error(this, "Failed!", Toast.LENGTH_LONG, true).show()
        } finally {
            if (statusType){
                KToasty.success(this, "Success!", Toast.LENGTH_LONG, true).show()
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.right_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_right_drawer -> drawer.openDrawer(GravityCompat.END)
        }
        return super.onOptionsItemSelected(item)
    }

    @OnClick(R.id.tvGetNewAppsId)
    fun getNewApp(){
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(
                "https://play.google.com/store/apps/dev?id=" + resources.getString(R.string.developer_account))
        }
        startActivity(intent)
    }

    @OnClick(R.id.tvShareAppId)
    fun getShareApp(){
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "App Name: " + resources.getString(R.string.app_name)
                    + "\n\nApp URL: https://play.google.com/store/apps/details?id=" + packageName)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    fun getOurAppList(){
        var appNameList:ArrayList<String> = ArrayList()
        var packageNameList:ArrayList<String> = ArrayList()
        var imageNameList:ArrayList<String> = ArrayList()

        val policy:StrictMode.ThreadPolicy  =  StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        var appListData:String = ""

        val cacheSize = 50 * 1024 * 1024 // 50 MiB

        val cache = Cache(
            File(application.cacheDir, "cacheFileName"),
            cacheSize.toLong()
        )
        val client = OkHttpClient().newBuilder().cache(cache).build()

        val request = Request.Builder()
            .cacheControl(CacheControl.Builder().maxStale(15, TimeUnit.DAYS).build())
            .url(serverAppUrl)
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful){
                appListData = response.body!!.string()
            }
        }

        if (appListData.length > 10){
            try {
                val jsonObject = JSONObject(appListData)
                    val dataArray = jsonObject.getJSONArray("app_list")
                    for (i in 0 until dataArray.length()) {
                        var dataobj = dataArray.getJSONObject(i)
                        appNameList.add(dataobj.getString("name"))
                        packageNameList.add(dataobj.getString("package"))
                        imageNameList.add(dataobj.getString("image"))
                    }

            } catch (e: JSONException) {
                e.printStackTrace()
            } finally {
                var appListAdapter = AppListAdapter(applicationContext, appNameList, packageNameList, imageNameList)
                appListAdapter.notifyDataSetChanged()
                appNameListView.adapter = appListAdapter
                appNameListView.setOnItemClickListener{ parent, view, position, id ->
                    startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${packageNameList.get(position)}")))
                }
            }
        }

    }


    private val myConnection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            isBound = false
        }

        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            isBound = true
            val binder = service as MusicService.MyLocalBinder
            musicServicess = binder.getService()
        }
    }
}
