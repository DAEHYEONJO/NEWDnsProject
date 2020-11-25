package com.example.dnsproject.config

import com.google.gson.annotations.SerializedName

class HybridTwdConfig {
    /**
     * Set server operation status.
     */
    var enableHybrid = false

    /**
     * Sets the polling cycle value for data delay processing. (in milliseconds)
     * The engine periodically requests data, which takes a delay of this value.
     *
     *
     * Decreasing the value increases the CPU load because the Loop is called frequently,
     * and increasing the value can slow down the load instead of reducing the load.
     *
     *
     * When tested in V30 and V20,
     * we determined that the load was not large and that the results were unchanged.
     *
     *
     * Please make adjustments through testing as there may be differences depending on system performance.
     */
    var bufferPollingCycle = 10L

    /**
     * Operates the buffer for Hybrid speech recognition inside the engine.
     * You can specify the size of this buffer in seconds.
     *
     *
     * The internal buffer size is determined like below
     * 60 secs for pcm data (32000Bytes per 1sec = 16bits * 16000Hz) * 60 = 1920000
     */
    var internalBufferLength = 60

    /**
     * Keyword language setting, refer to AI_TWDEngineAPI constants
     *
     * @see com.lge.aip.engine.hybridtwd.AI_HybridTWDEngineAPI
     */
    var locale: String? = null

    /**
     * Set type of keyword, AI_VA_KEYWORD_AIR_STAR, AI_VA_KEYWORD_HI_LG
     *
     * @see com.lge.aip.engine.hybridtwd.AI_HybridTWDEngineAPI
     */
    var triggerWord: String? = null

    @SerializedName("embedded")
    var embeddedConfig: EmbeddedConfig? = null

    class EmbeddedConfig {
        /**
         * Set Sensitivity value given in advance, Sensitivity adjustment
         */
        var sensitivity = 0

        /**
         * Set cm value given in advance
         */
        var cm = 0f

        /**
         * Set weight value given in advance
         */
        var weight = 0

        /**
         * Set Threshold of engine recognition performance
         */
        var noiseThreshold = 0

        /**
         * AM file path to use for keyword detection
         */
        var amModelFile: String? = null

        /**
         * NET file path to use for keyword detection
         */
        var netModelFile: String? = null

        /**
         * Flag for dump audio
         */
        var enableLocalPcmDump = false

        /**
         * Debugging information on / off function
         */
        var enableDebug = false

        /**
         * Flag for dump audio
         */
        var localPcmDumpPath = ""
    }

    @SerializedName("server")
    var serverConfig: ServerConfig? = null

    class ServerConfig {
        /**
         * Flag for connection(socket/http2) on server
         */
        var enableHttp2 = false

        /**
         * The pre-issued server ip
         */
        var serverIp: String? = null

        /**
         * The pre-issued server port number
         */
        var serverPort: String? = null

        /**
         * The pre-issued app name
         */
        var appName: String? = null

        /**
         * unique id for the device
         */
        var deviceId: String? = null

        /**
         * The pre-issued API key
         */
        var customKey: String? = null

        /**
         * Flag for dump on server
         */
        var enablePcmDump = false
        var engine: String? = null

        /**
         * Deliver model information to differentiate your device model
         */
        var userId: String? = null

        /**
         * Key value used for AES encryption of ASR input data (note that it is not exposed to the outside!)
         */
        var encryptionKey: String? = null

        /**
         * When using HTWD, set whether to receive the recognition result for the start word.
         */
        var needKwdResult = false
    }
}
