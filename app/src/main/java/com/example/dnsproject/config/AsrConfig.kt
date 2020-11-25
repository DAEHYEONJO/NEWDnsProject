package com.example.dnsproject.config

class AsrConfig {
    var language: String? = null
    var serverIp: String? = null
    var serverPort: String? = null
    var applicationName: String? = null
    var deviceId: String? = null
    var customKey: String? = null
    var recognitionMode: String? = null
    var userAgent: UserAgent? = null
    var epdSkipBytes: Int? = null
    var enableCompleteMode = false
    var enableTriggerWordReject = false
    var enableServerPcmDump = false
    var enableTwdResult = false
    var encryptionKey: String? = null
    var enablePcmDump = false
    var pcmDumpPath: String? = null
    var channels: Int? = null

    class UserAgent {
        var os: String? = null
        var model: String? = null
        var pcmSource: String? = null
    }

}
