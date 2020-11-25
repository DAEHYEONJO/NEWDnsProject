package com.example.dnsproject.model

class TwdModel {
    /**
     * am file path
     */
    var amModelFile: String? = null

    /**
     * net file path
     */
    var netModelFile: String? = null

    /**
     * Sensitivity value, decimal number
     */
    var sensitivity = 0

    /**
     * cm value, floating-point number
     */
    var cm = 0f

    /**
     * Weight value, decimal number
     */
    var weight = 0

    override fun toString(): String {
        return "TwdModel{" +
                "amModelFile='" + amModelFile + '\'' +
                ", netModelFile='" + netModelFile + '\'' +
                ", sensitivity=" + sensitivity +
                ", cm=" + cm +
                ", weight=" + weight +
                '}'
    }

}
