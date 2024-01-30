package com.ruchitech.quicklinkcaller.ui.screens.connectedui


import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.window.DialogProperties
import com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel.ChildCallLogVm
import com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel.HomeVm

@Composable
fun AddNoteDialog(
    viewModel: Any,
    note: String? = "",
    onDismiss: () -> Unit,
    confirmButton: (note: String) -> Unit,
) {
    var noteStr by remember {
        mutableStateOf(note)
    }
    val focusRequester = remember { FocusRequester() }
    var dialogOpen by remember { mutableStateOf(true) }

    LaunchedEffect(dialogOpen) {
        focusRequester.requestFocus()
    }
    val scope = rememberCoroutineScope()
    // Ensure the keyboard controller is available
    DisposableEffect(dialogOpen) {
        if (dialogOpen) {
           if (viewModel is HomeVm)  viewModel.openKeyboardWithoutFocus() else  (viewModel as ChildCallLogVm).openKeyboardWithoutFocus()
        } else {
            focusRequester.freeFocus()
            if (viewModel is HomeVm)  viewModel.hideKeyboard() else  (viewModel as ChildCallLogVm).hideKeyboard()
        }
        onDispose {  }
    }
    MyAlertDialog(
        title = {
            Text(text = "Add Call Notes")
        },
        content = {
            TextField(
                value = noteStr ?: "",
                onValueChange = { noteStr = it },
                singleLine = true,
                label = { Text("Enter call notes...") },
                modifier = Modifier.focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Handle done action if needed
                    }
                ),
            )
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    focusRequester.freeFocus()
                    if (viewModel is HomeVm)  viewModel.hideKeyboard() else  (viewModel as ChildCallLogVm).hideKeyboard()
                    dialogOpen = false
                   onDismiss()
                },
                content = { Text("Cancel") },
            )
        },
        confirmButton = {
            OutlinedButton(
                onClick = {
                    if (!noteStr.isNullOrEmpty()) {
                        focusRequester.freeFocus()
                        if (viewModel is HomeVm)  viewModel.hideKeyboard() else  (viewModel as ChildCallLogVm).hideKeyboard()
                        dialogOpen = false
                        dialogOpen = false
                        confirmButton(noteStr ?: "")
                    }
                },
                content = { Text("Save") },
            )
        },
        onDismiss = {
            focusRequester.freeFocus()
            if (viewModel is HomeVm)  viewModel.hideKeyboard() else  (viewModel as ChildCallLogVm).hideKeyboard()
            dialogOpen = false
            dialogOpen = false
            onDismiss()
        },
    )
}

@Composable
fun MyAlertDialog(
    title: @Composable () -> Unit,
    content: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    onDismiss: () -> Unit,
) {

    AlertDialog(
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = true),
        onDismissRequest = {
            onDismiss.invoke()
        },
        confirmButton = {
            confirmButton.invoke()
        }, dismissButton = {
            dismissButton.invoke()
        },
        title = title, text = {
            content.invoke()
        })
}