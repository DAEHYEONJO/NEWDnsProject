package com.example.dnsproject.model

import android.content.Context
import android.util.Log
import com.example.dnsproject.engine.KeywordLanguageMap
import com.example.dnsproject.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class TwdModelLoader {
    private val TAG = TwdModelLoader::class.java.simpleName

    /**
     * Json model file, [keyword][language] format
     */
    private val MODELS =
        arrayOf(
            arrayOf<String?>(
                "config_airstar_kr.json",
                "config_airstar_en.json",
                "config_airstar_cn.json",
                "config_airstar_jp.json"
            ),
            arrayOf<String?>("config_hilg_kr.json", "config_hilg_en.json"),
            arrayOf<String?>(
                "config_heycloi_kr.json",
                "config_heycloi_en.json",
                "config_heycloi_cn.json",
                "config_heycloi_jp.json"
            )
        ) // [keyword][language]


    private val KEYWORD_MODEL_PATH = "keyword_model/"

    /**
     * Cached model information
     */
    private val mModelMap: MutableMap<String, TwdModel?> =
        HashMap<String, TwdModel?>()

    /**
     * Loaded model information
     */
    private var mModel: TwdModel? = null

    private var mContext: Context? = null

    /**
     * Creates a model loader to load information of model files.
     *
     * @param context Context to access assets
     */
    constructor(context: Context?) {
        mContext = context
    }

    /**
     * Load a model information and store inside. Use another get methods to retrieve the information after invoking this.
     *
     * @param keyword  index of keyword, Airstart:0, HiLG:1, HeyCloi:2
     * @param language index of language, KO:0, EN:1, CN:2, JP:3
     */
    fun load(keyword: Int, language: Int) {
        val key =
            String.format(Locale.getDefault(), "%1\$d_%2\$d", keyword, language)
        mModel = if (mModelMap.containsKey(key)) {
            mModelMap[key]
        } else {
            loadModel(keyword, language, key)
        }
        if (mModel != null) {
            Log.d(TAG, "load: " + mModel.toString())
        }
    }

    /**
     * Load a model information and store inside. Use another get methods to retrieve the information after invoking this.
     *
     * @param keyword  Value of keyword : "HILG"
     * @param language Value of language : "KOR", "ENG"
     */
    fun load(keyword: String, language: String) {
        load(getKeywordIndex(keyword), getLanguageIndex(language))
    }

    /**
     * Load a model information from json
     *
     * @param keyword HiLG:0
     * @param language KOR:0, ENG:1
     * @param key key for cache map
     * @return TwdModel instance
     */
    private fun loadModel(keyword: Int, language: Int, key: String): TwdModel? {
        require(!(keyword >= MODELS.size || keyword < 0)) { "Keyword is not valid." }
        require(!(language >= MODELS[keyword].size || language < 0)) { "Language is not valid." }
        requireNotNull(MODELS[keyword][language]) { "Model parameters are not valid." }
        val jsonFileName: String =
            KeywordLanguageMap.keywordMap.get(keyword).toString() + "/" +
                    KeywordLanguageMap.languageMap.get(language) + "/" +
                    MODELS[keyword][language]
        val gson = Gson()
        try {
            val inputStream =
                mContext!!.assets.open(KEYWORD_MODEL_PATH + jsonFileName)
            val reader =
                JsonReader(InputStreamReader(inputStream, "UTF-8"))
            val model: TwdModel =
                gson.fromJson<TwdModel>(reader, object : TypeToken<TwdModel?>() {}.type)
                    ?: return null
            mModelMap[key] = model
            return model
        } catch (e: IOException) {
            Log.w(TAG, "loadModel: " + e.message)
        }
        return null
    }

    /**
     * Return am file path
     * @return am file path
     */
    val amFilePath: String?
        get() {
            checkNotNull(mModel) { "Model is not loaded." }
            return mModel!!.amModelFile
        }

    /**
     * Return net file path
     * @return net file path
     */
    val netFilePath: String?
        get() {
            checkNotNull(mModel) { "Model is not loaded." }
            return mModel!!.netModelFile
        }

    /**
     * Return sensitivity value that is decimal number
     * @return Sensitivity value, decimal number
     */
    val sensitivity: Int
        get() {
            checkNotNull(mModel) { "Model is not loaded." }
            return mModel!!.sensitivity
        }

    /**
     * Return cm value that is floating-point number
     * @return cm value, floating-point number
     */
    val cm: Float
        get() {
            checkNotNull(mModel) { "Model is not loaded." }
            return mModel!!.cm
        }

    /**
     * Return weight value that is decimal number
     * @return Weight value, decimal number
     */
    val weight: Int
        get() {
            checkNotNull(mModel) { "Model is not loaded." }
            return mModel!!.weight
        }

    fun readAmAsset(): ByteArray? {
        return amFilePath?.let { readAsset(it) }
    }

    fun readNetAsset(): ByteArray? {
        return netFilePath?.let { readAsset(it) }
    }

    private fun readAsset(fileName: String): ByteArray? {
        var arr = ByteArray(0)
        val modelKeyData = modelKeyData
        val fullFilePath: String = (KeywordLanguageMap.KEYWORD_MODEL_PATH.toString() + "/"
                + getModelKeyword(modelKeyData) + "/"
                + getModelLanguage(modelKeyData) + "/" + fileName)
        try {
            mContext!!.assets.open(fullFilePath).use { `is` ->
                val size = `is`.available()
                arr = ByteArray(size)
                val result = `is`.read(arr)
                Log.d(TAG, "readAsset: result:" + (size == result))
            }
        } catch (e: IOException) {
            Log.w(TAG, "readAsset error", e)
        }
        return arr
    }

    private fun getModelKeyword(modelKeyData: String): String {
        val keyData = modelKeyData.split("_".toRegex()).toTypedArray()
        return KeywordLanguageMap.keywordMap.get(keyData[0].toInt())
    }

    private fun getModelLanguage(modelKeyData: String): String {
        val keyData = modelKeyData.split("_".toRegex()).toTypedArray()
        return KeywordLanguageMap.languageMap.get(keyData[1].toInt())
    }

    val modelKeyData: String
        get() {
            checkNotNull(mModel) { "Model is not loaded." }
            for (key in mModelMap.keys) {
                if (mModelMap[key]!!.equals(mModel)) {
                    return key
                }
            }
            Log.e(TAG, "Model key not found. change to default value")
            return KeywordLanguageMap.DEFAULT_MODEL_KEY
        }

    fun getLanguageIndex(language: String): Int {
        val _array =
            mContext!!.resources.getStringArray(R.array.array_language)
        var index = _array.size - 1
        while (index > 0 && _array[index] != language) {
            index--
        }
        return index
    }

    fun getKeywordIndex(keyword: String): Int {
        val _array =
            mContext!!.resources.getStringArray(R.array.array_keyword)
        var index = _array.size - 1
        while (index > 0 && _array[index] != keyword) {
            index--
        }
        return index
    }

}
