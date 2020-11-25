package com.example.dnsproject.config

class H2UserAgent {
    var product: String? = null
    var pcmSource: String? = null
    var os: String? = null
    var model: String? = null
    var version: String? = null
    var thinqVersion: String? = null
    override fun toString(): String {
        return "H2UserAgent{" +
                "product='" + product + '\'' +
                ", pcmSource='" + pcmSource + '\'' +
                ", os='" + os + '\'' +
                ", model='" + model + '\'' +
                ", version='" + version + '\'' +
                ", thinqVersion='" + thinqVersion + '\'' +
                '}'
    }
}