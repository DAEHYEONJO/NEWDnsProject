package com.example.dnsproject.config

import com.google.gson.annotations.SerializedName


/**
 * Apply the changed NLP linking protocol to use the reference server for structuring.
 */
class SpeechConfig(asrConfig: AsrConfig, nlpConfig: NlpConfig) {
    var opMode: String? = null

    // The asrConfig and nlpConfig are used to work with servers using the socket.
    // For the legacy DeepThinQ Reference servers.
    @SerializedName("asr_config")
    var asrConfig: AsrConfig

    /**
     * In case of ASR-NLP mode, set NLP Config.
     */
    @SerializedName("nlp_config")
    var nlpConfig: NlpConfig

    ////////////////////////////////////////////////////////////////////////////////
    // IMPORTANT!
    // The following configs are used to work with servers using the HTTP2 standard.
    var enableHttp2 = false
    var serverIp: String? = null
    var serverPort: String? = null
    var voicePath: String? = null
    var controlPath: String? = null
    var authTokenPath: String? = null
    var appName: String? = null
    var applicationName: String? = null
    var authToken: String? = null
    var customKey: String? = null
    var deviceId: String? = null
    var deviceType: String? = null
    var locale: String? = null
    var recognitionMode: String? = null
    var userId: String? = null
    var enableLocalPcmDump = false
    var localPcmDumpPath: String? = null
    var asr: H2Asr? = null
    var nlp: H2Nlp? = null
    var userAgent: H2UserAgent? = null
    var mcertificate: String? = null
    override fun toString(): String {
        return "SpeechConfig{" +
                "opMode='" + opMode + '\'' +
                ", asrConfig=" + asrConfig +
                ", nlpConfig=" + nlpConfig +
                ", enableHttp2=" + enableHttp2 +
                ", serverIp='" + serverIp + '\'' +
                ", serverPort=" + serverPort +
                ", voicePath='" + voicePath + '\'' +
                ", controlPath='" + controlPath + '\'' +
                ", authTokenPath='" + authTokenPath + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", appName='" + appName + '\'' +
                ", authToken='" + authToken + '\'' +
                ", customKey='" + customKey + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", locale='" + locale + '\'' +
                ", recognitionMode='" + recognitionMode + '\'' +
                ", userId='" + userId + '\'' +
                ", asr=" + (if (asr != null) asr.toString() else "null") +
                ", nlp=" + (if (nlp != null) nlp.toString() else "null") +
                ", userAgent=" + (if (userAgent != null) userAgent.toString() else "null") +
                '}'
    }

    fun setCertificate(loadCertificate: String?) {
        mcertificate = loadCertificate
    }

    init {
        this.asrConfig = asrConfig
        this.nlpConfig = nlpConfig
    }
}
