package com.ruchitech.quicklinkcaller.navhost.routes


import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.NavRoute
import com.ruchitech.quicklinkcaller.navhost.nav.getOrThrow
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.HomeScreen
import com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel.HomeVm

object HomeRoute : NavRoute<HomeVm> {
    const val KEY_IS_EDIT_EMP = "KEY_IS_EDIT_EMP"
   const val KEY_USERID = "KEY_USERID"
    override val route = Screen.HomeScreen.route
    fun withArgs(isEditable: String, userId: String): String = route
        .replace("{$KEY_IS_EDIT_EMP}", isEditable)
        .replace("{$KEY_USERID}", userId.ifEmpty { "na" })

    fun getArgs(savedStateHandle: SavedStateHandle, key: String) =
        savedStateHandle.getOrThrow<String>(key)

    @Composable
    override fun viewModel(): HomeVm = hiltViewModel()

    @Composable
    override fun Content(
        viewModel: HomeVm,
    ) = HomeScreen(viewModel)
}
