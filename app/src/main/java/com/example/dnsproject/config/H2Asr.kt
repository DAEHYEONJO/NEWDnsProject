package com.example.dnsproject.config

class H2Asr {
    var support = false

    var version = "2.0"

    var data: AsrData? = null

    class AsrData {
        var channels: Int? = null
        var codec: String? = null
        var engine: String? = null
        var epd: String? = null
        var epdSkipBytes: Int? = null
        var enableKeywordReject = false
        var nBest: Int? = null
        var needKwdResult = false
        var enablePartialResult = false
        var enablePcmDump = false
        var rate: Int? = null
        var triggerWord: String? = null
        var enableDeveloperOption = false
        override fun toString(): String {
            return "AsrData{" +
                    "channels='" + channels + '\'' +
                    ", codec='" + codec + '\'' +
                    ", epd='" + epd + '\'' +
                    ", epdSkipBytes=" + epdSkipBytes +
                    ", enableKeywordReject=" + enableKeywordReject +
                    ", nBest=" + nBest +
                    ", needKwdResult=" + needKwdResult +
                    ", enablePartialResult=" + enablePartialResult +
                    ", enablePcmDump=" + enablePcmDump +
                    ", rate=" + rate +
                    ", triggerWord='" + triggerWord + '\'' +
                    ", enableDeveloperOption=" + enableDeveloperOption +
                    '}'
        }
    }

    // The following configs are for Android only.
    var encryptionKey: String? = null

    override fun toString(): String {
        return "H2Asr{" +
                "support=" + support +
                ", version=" + version +
                ", data=" + (if (data != null) data.toString() else "null") +
                ", encryptionKey='" + encryptionKey + '\'' +
                '}'
    }
}