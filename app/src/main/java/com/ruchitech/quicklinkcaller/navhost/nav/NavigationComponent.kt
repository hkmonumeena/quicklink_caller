package com.ruchitech.quicklinkcaller.navhost.nav

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.routes.ChildCallLogRoute
import com.ruchitech.quicklinkcaller.navhost.routes.HomeRoute
import com.ruchitech.quicklinkcaller.navhost.routes.PrepairDataRoute
import com.ruchitech.quicklinkcaller.navhost.routes.SettingsRoute
import com.ruchitech.quicklinkcaller.navhost.routes.SplashRoute


@Composable
fun NavigationComponent(
    navHostController: NavHostController,
    snackbarHostState: SnackbarHostState,
    paddingValues: PaddingValues
) {

    NavHost(
        route = "root",
        navController = navHostController,
        startDestination = Screen.SplashScreen.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        SplashRoute.composable(this, navHostController, snackbarHostState)
        PrepairDataRoute.composable(this, navHostController, snackbarHostState)
        HomeRoute.composable(this, navHostController, snackbarHostState)
        SettingsRoute.composable(this, navHostController, snackbarHostState)
        ChildCallLogRoute.composable(this, navHostController, snackbarHostState)
    }
}