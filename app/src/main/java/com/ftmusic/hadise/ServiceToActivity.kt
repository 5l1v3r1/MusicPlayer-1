package com.ftmusic.hadise


class ServiceToActivity {
    var data: Int? = null
    var sendMediCommand:String="nothing"
    var currentPosition:Int = 0
    var duration:Int = 0
    var mediaStatusCmd:String = "nothing"

    constructor(sendMediCommand: String, data: Int){
        this.sendMediCommand = sendMediCommand
        this.data = data
    }

    constructor(mediaStatusCmd:String, position:Int, isDuration:Boolean){
        this.mediaStatusCmd = mediaStatusCmd
        if (!isDuration){
            currentPosition = position
        } else {
            duration = position
        }
    }
}