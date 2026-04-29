package com.ncorti.kotlin.template.app.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chitalka.library.LastOpenBookPersistence
import com.chitalka.library.LibrarySessionState
import com.chitalka.storage.StorageService

@Composable
internal fun ChitalkaNavHost(
    navController: NavHostController,
    persistence: LastOpenBookPersistence,
    librarySession: LibrarySessionState,
    storage: StorageService,
    mainContent: @Composable () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = AppNavRoutes.MAIN,
    ) {
        composable(AppNavRoutes.MAIN) {
            mainContent()
        }
        composable(
            route = AppNavRoutes.READER,
            arguments =
                listOf(
                    navArgument("bookId") { type = NavType.StringType },
                    navArgument("bookPath") { type = NavType.StringType },
                ),
        ) { entry ->
            val rawId = entry.arguments?.getString("bookId").orEmpty()
            val rawPath = entry.arguments?.getString("bookPath").orEmpty()
            ReaderRouteScreen(
                ReaderRouteUiModel(
                    bookId = Uri.decode(rawId),
                    bookPath = Uri.decode(rawPath),
                    persistence = persistence,
                    librarySession = librarySession,
                    storage = storage,
                    onPop = { navController.popBackStack() },
                ),
            )
        }
    }
}
