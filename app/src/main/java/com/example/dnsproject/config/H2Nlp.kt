package com.example.dnsproject.config

class H2Nlp {
    var support = false

    var version = 1

    var data: NlpData? = null

    class NlpData {
        var function: String? = null
        var language: String? = null
        var country: String? = null
        var deviceId: String? = null
        var timeZone: String? = null
        var deviceTime: String? = null
        var inputType: String? = null
        var config: Config? = null

        class Config {
            var nlpVersion: String? = null
        }
    }

    override fun toString(): String {
        return "H2Nlp{" +
                "support=" + support +
                ", version=" + version +
                ", data=" + (if (data != null) data.toString() else "null") +
                '}'
    }
}