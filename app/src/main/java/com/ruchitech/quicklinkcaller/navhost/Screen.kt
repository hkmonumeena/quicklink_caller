package com.ruchitech.quicklinkcaller.navhost

import com.ruchitech.quicklinkcaller.helper.Constant
import com.ruchitech.quicklinkcaller.helper.Constant.RoutePaths.ChildCallLogRoute
import com.ruchitech.quicklinkcaller.helper.Constant.RoutePaths.HomeRoute
import com.ruchitech.quicklinkcaller.helper.Constant.RoutePaths.SplashRoute

sealed class Screen(val route: String) {
    data object HomeScreen : Screen(route = HomeRoute)
    data object ChildCallLogScreen : Screen(route = ChildCallLogRoute)
    data object SplashScreen : Screen(route = SplashRoute)
    data object PrepareRoute : Screen(route = Constant.RoutePaths.PrepareRoute)
    data object SettingsRoute : Screen(route = Constant.RoutePaths.SettingsRoute)
}
