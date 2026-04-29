package com.ncorti.kotlin.template.app.ui

import android.net.Uri
import androidx.navigation.NavController
import com.chitalka.navigation.RootStackRoutes

internal object AppNavRoutes {
    const val MAIN: String = RootStackRoutes.MAIN

    val READER: String = "${RootStackRoutes.READER}/{bookId}/{bookPath}"

    fun navigateToReader(
        nav: NavController,
        bookId: String,
        bookPath: String,
    ) {
        nav.navigate(
            "${RootStackRoutes.READER}/" +
                Uri.encode(bookId) + "/" +
                Uri.encode(bookPath),
        ) {
            launchSingleTop = true
        }
    }
}
