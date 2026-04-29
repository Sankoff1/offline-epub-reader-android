package com.ncorti.kotlin.template.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import com.chitalka.navigation.ReaderNavCoordinator
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
internal fun rememberReaderNavCoordinator(
    activity: ComponentActivity,
    navController: NavHostController,
): ReaderNavCoordinator =
    remember(activity, navController) {
        ReaderNavCoordinator(
            scope = activity.lifecycleScope,
            isNavHostReady = { navController.currentDestination != null },
            performNavigateToReader = { params ->
                AppNavRoutes.navigateToReader(
                    navController,
                    bookId = params.bookId,
                    bookPath = params.bookPath,
                )
            },
        )
    }

@Composable
internal fun ReaderNavCoordinatorSideEffects(
    navController: NavHostController,
    coordinator: ReaderNavCoordinator,
) {
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow
            .map { it.destination.route }
            .distinctUntilChanged()
            .collect {
                coordinator.flushReaderNavigationIfPending()
            }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, coordinator) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_DESTROY) {
                    coordinator.clearPendingReader()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
