package com.ruchitech.quicklinkcaller.navhost.nav

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.quicklink.caller.navhost.nav.NavigationState
import com.quicklink.caller.navhost.nav.RouteNavigator

/**
 * A route the app can navigate to.
 */
interface NavRoute<T : RouteNavigator> {

    val route: String

    /**
     * Returns the screen's content.
     */
    @Composable
    fun Content(viewModel: T)

    /**
     * Returns the screen's ViewModel. Needs to be overridden so that Hilt can generate code for the factory for the ViewModel class.
     */
    @Composable
    fun viewModel(): T

    /**
     * Override when this page uses arguments.
     *
     * We do it here and not in the [NavigationComponent to keep it centralized]
     */
    fun getArguments(): List<NamedNavArgument> = listOf()

    /**
     * Generates the composable for this route.
     */
    fun composable(
        builder: NavGraphBuilder,
        navHostController: NavHostController,
        snackbarHostState: SnackbarHostState,
    ) {
        builder.composable(route, getArguments()) {
            val viewModel = viewModel()
            val viewStateAsState by viewModel.navigationState.collectAsState()
            LaunchedEffect(viewStateAsState) {
                updateNavigationState(
                    navHostController,
                    viewStateAsState,
                    viewModel::onNavigated,
                    snackbarHostState
                )
            }
            Content(viewModel)
        }
    }

    /**
     * Navigates to viewState.
     */
    private suspend fun updateNavigationState(
        navHostController: NavHostController,
        navigationState: NavigationState,
        onNavigated: (navState: NavigationState) -> Unit,
        snackbarHostState: SnackbarHostState,
    ) {
        when (navigationState) {
            is NavigationState.NavigateToRoute -> {
                navHostController.navigate(navigationState.route)
                onNavigated(navigationState)
            }

            is NavigationState.PopToRoute -> {
                navHostController.popBackStack(
                    navigationState.staticRoute,
                    inclusive = true,
                    saveState = false
                )
                onNavigated(navigationState)
            }

            is NavigationState.NavigateUp -> {
                navHostController.navigateUp()
            }

            is NavigationState.Idle -> {

            }

            is NavigationState.ShowSnackbar -> {
                if (!navigationState.msg.isNullOrEmpty()) {
                    snackbarHostState.showSnackbar(navigationState.msg)
                }
            }

            is NavigationState.PopToRouteAndNavigate -> {
                navHostController.navigate(navigationState.routeToNavigate) {
                    launchSingleTop = true
                    popUpTo(navigationState.routeToPopup) {
                        //    saveState = true
                        inclusive = navigationState.inclusive
                    }
                    //   restoreState = false
                }
                onNavigated(NavigationState.Idle)
            }


        }
    }

}

fun <T> SavedStateHandle.getOrThrow(key: String): T =
    get<T>(key) ?: throw IllegalArgumentException(
        "Mandatory argument $key missing in arguments."
    )