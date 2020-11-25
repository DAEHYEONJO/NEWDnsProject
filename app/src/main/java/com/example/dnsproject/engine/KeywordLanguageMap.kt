package com.example.dnsproject.engine

import com.lge.aip.engine.hybridtwd.AI_HybridTWDEngineAPI

object KeywordLanguageMap {
    const val KEYWORD_MODEL_PATH = "keyword_model"
    const val DEFAULT_MODEL_KEY = AI_HybridTWDEngineAPI.AI_VA_KEYWORD_HI_LG.toString() +
            "_" + AI_HybridTWDEngineAPI.AI_LANG_KO_KR
    val keywordMap =
        arrayOf("airstar", "hilg", "heycloi")
    val languageMap = arrayOf("kr", "en", "cn", "jp")
}
