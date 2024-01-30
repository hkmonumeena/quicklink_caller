package com.ruchitech.quicklinkcaller.navhost.routes


import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.NavRoute
import com.ruchitech.quicklinkcaller.navhost.nav.getOrThrow
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.childui.ChildCallLogsUi
import com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel.ChildCallLogVm

object ChildCallLogRoute : NavRoute<ChildCallLogVm> {
    const val KEY_CALLER_ID = "KEY_CALLER_ID"
    const val KEY_PRIMARY = "KEY_PRIMARY"
    const val KEY_NAME = "KEY_NAME"
    override val route = "${Screen.ChildCallLogScreen.route}/{$KEY_CALLER_ID}/{$KEY_PRIMARY}/{$KEY_NAME}"
    fun withArgs(callerId: String, keyPrimary: String,name:String): String = route
        .replace("{$KEY_CALLER_ID}", callerId)
        .replace("{$KEY_PRIMARY}", keyPrimary.ifEmpty { "na" })
        .replace("{$KEY_NAME}", name.ifEmpty { "na" })

    fun getArgs(savedStateHandle: SavedStateHandle, key: String) =
        savedStateHandle.getOrThrow<String>(key)

    @Composable
    override fun viewModel(): ChildCallLogVm = hiltViewModel()

    @Composable
    override fun Content(
        viewModel: ChildCallLogVm,
    ) = ChildCallLogsUi(viewModel)
}
