package com.panelpass.shell

import android.app.Activity
import java.lang.ref.WeakReference

/**
 * Holds a weak reference to the current Activity for platform flows (Google Sign-In, billing).
 */
public object ActivityHolder {
    private var ref: WeakReference<Activity>? = null

    public fun set(activity: Activity) {
        ref = WeakReference(activity)
    }

    public fun clear() {
        ref = null
    }

    public fun get(): Activity? = ref?.get()
}
