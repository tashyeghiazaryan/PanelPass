package com.panelpass.shell.navigation

/**
 * Top-level navigation targets. Extend this when you add new feature screens
 * (e.g. Profile, Settings) and wire them in Android Navigation / SwiftUI.
 */
public sealed class AppDestination {
    public data object Home : AppDestination()

    /** Placeholder for future modules */
    public data object Profile : AppDestination()
}
