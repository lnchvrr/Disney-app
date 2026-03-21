package fr.isen.chevrier.disney_app.data

import android.content.Context
import android.net.Uri

object ProfilePhotoStorage {
    private const val PREFS_NAME = "profile_photo_prefs"
    private const val KEY_PREFIX = "profile_photo_uri_"

    fun saveProfilePhotoUri(context: Context, userId: String, uri: Uri) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PREFIX + userId, uri.toString())
            .apply()
    }

    fun getProfilePhotoUri(context: Context, userId: String): Uri? {
        val uriString = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PREFIX + userId, null)

        return uriString?.let { Uri.parse(it) }
    }

    fun clearProfilePhotoUri(context: Context, userId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PREFIX + userId)
            .apply()
    }
}