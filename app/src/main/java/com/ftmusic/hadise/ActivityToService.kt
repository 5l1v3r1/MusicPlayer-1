package com.ftmusic.hadise


class ActivityToService {
    var data: Int = 0
    var sendMediCommand:String="nothing"

    constructor(sendMediCommand: String){
        this.sendMediCommand = sendMediCommand
    }


    constructor(sendMediCommand: String, data: Int){
        this.sendMediCommand = sendMediCommand
        this.data = data
    }

}