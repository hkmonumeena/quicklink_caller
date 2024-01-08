package com.ruchitech.quicklinkcaller.ui.screens.home.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel.HomeVm

@Composable
fun HomeScreen(viewModel: HomeVm) {
    TabControl(
        selectedColor = Color(0xFFF4E6E7),
        unselectedColor = Color(0xFFCECCCC),
        firstTabContent = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .background(Color(0xFFF4E6E7)),
                contentAlignment = Alignment.TopStart
            ) {
                CallLogScreen(viewModel)
            }
        }, lastTabContent = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .background(Color(0xFFF4E6E7)),
                contentAlignment = Alignment.TopStart
            ) {
                ShowContactsUi(viewModel)
            }
        })

}