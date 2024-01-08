package com.quicklink.caller.navhost.nav

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Navigator to use when initiating navigation from a ViewModel.
 */
interface RouteNavigator {
    fun onNavigated(state: NavigationState)
    fun navigateUp()
    fun popToRoute(route: String)
    fun popToRouteAndNavigate(
        routeToNavigate: String,
        routeToPopup: String,
        inclusive: Boolean = true,
    )

    fun showSnackbar(msg: String?)
    fun clearSnackbar()
    fun navigateToRoute(route: String)

    val navigationState: StateFlow<NavigationState>

}

class MyRouteNavigator : RouteNavigator {

    /**
     * Note that I'm using a single state here, not a list of states. As a result, if you quickly
     * update the state multiple times, the view will only receive and handle the latest state,
     * which is fine for my use case.
     */
    override val navigationState: MutableStateFlow<NavigationState> =
        MutableStateFlow(NavigationState.Idle)

    override fun onNavigated(state: NavigationState) {
        // clear navigation state, if state is the current state:
        navigationState.compareAndSet(state, NavigationState.Idle)
    }

    override fun popToRoute(route: String) = navigate(NavigationState.PopToRoute(route))

    override fun popToRouteAndNavigate(
        routeToNavigate: String,
        routeToPopup: String,
        inclusive: Boolean,
    ) = navigate(
        NavigationState.PopToRouteAndNavigate(
            routeToNavigate,
            routeToPopup,
            inclusive = inclusive
        )
    )

    override fun showSnackbar(msg: String?) {
        navigate(NavigationState.ShowSnackbar(msg))
        CoroutineScope(Dispatchers.IO).launch {
            delay(1500)
            clearSnackbar()
            this.cancel("Snackbar cleared")
        }
    }

    override fun clearSnackbar() = navigate(NavigationState.ShowSnackbar(null))

    override fun navigateUp() = navigate(NavigationState.NavigateUp())

    override fun navigateToRoute(route: String) = navigate(NavigationState.NavigateToRoute(route))

    @VisibleForTesting
    fun navigate(state: NavigationState) {
        navigationState.value = state
    }
}