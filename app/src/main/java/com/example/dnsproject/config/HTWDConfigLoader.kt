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

internal class HTWDConfigLoader {
    private var mContext: Context
    private var mPref: SharedPreferences
    private var mAssetFileName: String = "HTWDconfig.json"

    /**
     * Constructor for ConfigLoader. Use assets/config.json as the default config file.
     *
     * @param context Context
     */
    constructor(context: Context) {
        mContext = context
        mPref = PreferenceManager.getDefaultSharedPreferences(mContext)
    }

    /**
     * Constructor for ConfigLoader. Use assets/[assetFileName] as the default config file.
     *
     * @param context Context
     */
    constructor(context: Context, assetFileName: String) {
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
            inputStream.use { stream ->
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
    val inputStream: InputStream?
        get() {
            var stream: InputStream? = null
            try {
                if (isUsingExternal) {
                    val path =
                        mPref.getString("external_path", "")
                    if ("" != path) {
                        val uri = Uri.parse(path)
                        try {
                            stream = mContext.contentResolver.openInputStream(uri)
                            return stream
                        } catch (se: SecurityException) {
                            Toast.makeText(
                                mContext,
                                "R.string.popup_msg_config_error",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d(
                                "TAG",
                                "SecurityException: file permission problem - " + se.message
                            )
                        }
                    }
                }
                stream = mContext.assets.open(mAssetFileName)
            } catch (ioe: IOException) {
                Log.e("TAG", "initEngine: config file is not valid")
            }
            return stream
        }

    /**
     * Read the contents of the json file as a String.
     *
     * @return the contents of the json.
     */
    val jsonConfigString: String
        get() {
            val config = StringBuilder()
            try {
                inputStream.use { stream ->
                    val scn =
                        Scanner(stream, StandardCharsets.UTF_8.name())
                    while (scn.hasNext()) {
                        config.append(scn.next())
                    }
                }
            } catch (e: IOException) {
                Log.e("TAG", "getJsonConfigString: " + e.message)
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
        return mPref.getString("external_path", defaultString)
    }

    /**
     * Records the external path into the preferences.
     * @param path selected external path.
     */
    fun putExternalPath(path: String?) {
        mPref.edit().putString("external_path", path).apply()
    }

    /**
     * Whether an external path is used.
     * @return true if the app using the external path.
     */
    /**
     * Records whether an external path is used.
     * @param usingExternal true if the app using the external path.
     */
    var isUsingExternal: Boolean
        get() = mPref.getBoolean("external_path", false)
        set(usingExternal) {
            mPref.edit().putBoolean("external_path", usingExternal).apply()
        }


}
