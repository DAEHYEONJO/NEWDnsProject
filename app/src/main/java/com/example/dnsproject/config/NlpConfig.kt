package com.example.dnsproject.config

class NlpConfig(
    function: String,
    language: String,
    country: String,
    inputType: String,
    inputText: String?,
    deviceId: String,
    deviceTime: String,
    timeZone: String,
    nlpVersion: String?
) {
    var function: String
    var nlanguage: String
    var ncountry: String
    var inputType: String
    var ndeviceId: String
    var deviceTime: String
    var timeZone: String
    private var nconfig: Config
    fun setDeviceTime(currentTime: Long?) {
        deviceTime = java.lang.Long.toString(currentTime!!)
    }

    fun setCountry(country: String) {
        this.ncountry = country
    }

    fun setDeviceId(deviceId: String) {
        this.ndeviceId = deviceId
    }

    fun setLanguage(language: String) {
        this.nlanguage = language
    }

    internal inner class Config(var nlpVersion: String)

    init {
        nconfig = Config(nlpVersion!!)
        this.function = function
        this.nlanguage = language
        this.ncountry = country
        this.inputType = inputType
        this.ndeviceId = deviceId
        this.deviceTime = deviceTime
        this.timeZone = timeZone
    }
}
