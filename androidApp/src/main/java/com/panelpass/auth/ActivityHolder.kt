package com.panelpass.auth

import android.app.Activity
import java.lang.ref.WeakReference

/**
 * Holds a weak reference to the current Activity so that [GoogleAuthRepository]
 * can launch the Google Sign In intent. MainActivity should call [set] in onResume and [clear] in onPause.
 */
object ActivityHolder {
    private var ref: WeakReference<Activity>? = null

    fun set(activity: Activity) {
        ref = WeakReference(activity)
    }

    fun clear() {
        ref = null
    }

    fun get(): Activity? = ref?.get()
}
