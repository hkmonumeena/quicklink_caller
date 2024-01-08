package com.ruchitech.quicklinkcaller.navhost.routes


import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.NavRoute
import com.ruchitech.quicklinkcaller.navhost.nav.getOrThrow
import com.ruchitech.quicklinkcaller.ui.screens.preparingdata.PrepareDataUi
import com.ruchitech.quicklinkcaller.ui.screens.preparingdata.PrepareDataVm

object PrepairDataRoute : NavRoute<PrepareDataVm> {
    const val COMING_FROM = "COMING_FROM"
    override val route =" ${Screen.PrepareRoute.route}/{$COMING_FROM}"
    fun withArgs(comingFrom: String): String = route
        .replace("{$COMING_FROM}", comingFrom.ifEmpty { "na" })

    fun getArgs(savedStateHandle: SavedStateHandle, key: String) =
        savedStateHandle.getOrThrow<String>(key)

    @Composable
    override fun viewModel(): PrepareDataVm = hiltViewModel()

    @Composable
    override fun Content(
        viewModel: PrepareDataVm,
    ) = PrepareDataUi(viewModel)
}
