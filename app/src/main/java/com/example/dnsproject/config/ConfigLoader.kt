package com.example.dnsproject.config

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import java.util.*

class ConfigLoader {
    private val TAG = ConfigLoader::class.java.simpleName

    private val FILENAME_ASSET_CONFIG_JSON = "config.json"

    val KEY_USE_EXTERNAL = "use_external"
    val KEY_EXTERNAL_PATH = "external_path"
    val KEY_SHOW_CONFIG = "show_config"

    private var mContext: Context? = null
    private var mPref: SharedPreferences? = null
    private var mAssetFileName = FILENAME_ASSET_CONFIG_JSON

    /**
     * Constructor for ConfigLoader. Use assets/config.json as the default config file.
     *
     * @param context Context
     */
    constructor(context: Context?) {
        Log.d("TAG", "loadConfig make")
        mContext = context
        mPref = PreferenceManager.getDefaultSharedPreferences(mContext)
    }

    /**
     * Constructor for ConfigLoader. Use assets/[assetFileName] as the default config file.
     *
     * @param context Context
     */
    fun ConfigLoader(
        context: Context?,
        assetFileName: String
    ) {
        mContext = context
        mAssetFileName = assetFileName
        mPref = PreferenceManager.getDefaultSharedPreferences(mContext)
    }

    /**
     * According to the preferences, create JsonReader instance.
     *
     * @return JsonReader instance to read the json config file.
     */
    fun <T> loadConfig(configClass: Type?): T? {
        try {
            getInputStream().use { stream ->
                InputStreamReader(
                    stream,
                    StandardCharsets.UTF_8
                ).use { streamReader ->
                    JsonReader(streamReader)
                        .use { reader -> return Gson().fromJson(reader, configClass) }
                }
            }
        } catch (e: JsonSyntaxException) {
            Log.e("TAG", "loadConfig: " + e.message)
            return null
        } catch (e: IOException) {
            Log.e("TAG", "loadConfig: " + e.message)
            return null
        }
    }

    /**
     * According to the preferences, create InputStream instance.
     *
     * @return InputStream instance to read the json config file.
     */
    fun getInputStream(): InputStream? {
        var stream: InputStream? = null
        try {
            if (isUsingExternal()) {
                val path = mPref!!.getString(KEY_EXTERNAL_PATH, "")
                if ("" != path) {
                    val uri = Uri.parse(path)
                    try {
                        stream = mContext!!.contentResolver.openInputStream(uri)
                        return stream
                    } catch (se: SecurityException) {
                        Toast.makeText(
                            mContext,
                            "R.string.popup_msg_config_error",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(
                            TAG,
                            "SecurityException: file permission problem - " + se.message
                        )
                    }
                }
            }
            stream = mContext!!.assets.open(mAssetFileName)
        } catch (ioe: IOException) {
            Log.e(TAG, "initEngine: config file is not valid")
        }
        return stream
    }

    /**
     * Read the contents of the json file as a String.
     *
     * @return the contents of the json.
     */
    fun getJsonConfigString(): String? {
        val config = StringBuilder()
        try {
            getInputStream().use { stream ->
                val scn =
                    Scanner(stream, StandardCharsets.UTF_8.name())
                while (scn.hasNext()) {
                    config.append(scn.next())
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "getJsonConfigString: " + e.message)
        }
        return config.toString()
    }

    /**
     * Gets the path to the json file.
     *
     * @param defaultString for path
     * @return the path to the json file.
     */
    fun getExternalPath(defaultString: String?): String? {
        return mPref!!.getString(KEY_EXTERNAL_PATH, defaultString)
    }

    /**
     * Records the external path into the preferences.
     * @param path selected external path.
     */
    fun putExternalPath(path: String?) {
        mPref!!.edit().putString("ConfigLoader.KEY_EXTERNAL_PATH", path).apply()
    }

    /**
     * Whether an external path is used.
     * @return true if the app using the external path.
     */
    fun isUsingExternal(): Boolean {
        return mPref!!.getBoolean(KEY_USE_EXTERNAL, false)
    }

    /**
     * Records whether an external path is used.
     * @param usingExternal true if the app using the external path.
     */
    fun setUsingExternal(usingExternal: Boolean) {
        mPref!!.edit().putBoolean(KEY_USE_EXTERNAL, usingExternal).apply()
    }
}