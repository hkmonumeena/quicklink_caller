package com.quicklink.caller.navhost.nav

import java.util.UUID

/**
 * State that can be used to trigger navigation.
 */
sealed class NavigationState {

    /**
     * @param id is used so that multiple instances of the same route will trigger multiple navigation calls.
     */

    object Idle : NavigationState()

    data class NavigateToRoute(
        val route: String,
        val id: String = UUID.randomUUID().toString(),
        val argsHashMap: HashMap<String, String> = hashMapOf()
    ) :
        NavigationState()

    /**
     * @param staticRoute is the static route to pop to, without parameter replacements.
     */
    data class PopToRoute(val staticRoute: String, val id: String = UUID.randomUUID().toString()) :
        NavigationState()

    data class PopToRouteAndNavigate(
        val routeToNavigate: String,
        val routeToPopup: String,
        val inclusive: Boolean = true,
        val id: String = UUID.randomUUID().toString()
    ) :
        NavigationState()

    data class NavigateUp(val id: String = UUID.randomUUID().toString()) : NavigationState()
    data class ShowSnackbar(val msg: String?, val id: String = UUID.randomUUID().toString()) :
        NavigationState()
}