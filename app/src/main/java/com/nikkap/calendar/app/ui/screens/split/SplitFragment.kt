package com.nikkap.calendar.app.ui.screens.split

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.nikkap.calendar.app.ui.screens.main.MainViewModel
import com.nikkap.calendar.app.ui.screens.split.utils.calendar.Calendar
import com.nikkap.calendar.app.ui.screens.split.utils.list.List
import com.nikkap.calendar.app.ui.theme.CalendarTheme
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplitFragment : Fragment() {

    private val viewModel: SplitViewModel by viewModel()
    private val sharedViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SplitScreen()
            }
        }
    }


    @Composable
    private fun SplitScreen() {
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val state = viewModel.state.collectAsState().value
        val listState = rememberLazyListState()
        CalendarTheme {
            Scaffold(
                backgroundColor = MaterialTheme.colorScheme.background
            ) { _ ->
                Calendar(
                    state.items,
                    listState,
                    state
                ) { viewModel.onIntent(SplitIntent.UpdateSelectedDate(it)) }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                List(
                    state.items,
                    itemsWithoutDateList = state.itemsWithoutDate,
                    onEditClick = { id, type ->
                        sharedViewModel.onEditListItemClicked(id, type)
                    },
                    onDeleteClick = { id, type ->
                        viewModel.onIntent(SplitIntent.PendingDeleteItem(id, type))

                        scope.launch {

                            snackbarHostState.currentSnackbarData?.dismiss()

                            sharedViewModel.showSnackbar("Item deleted", "Undo") {
                                viewModel.onIntent(SplitIntent.UndoPendingDelete(id))
                            }
                        }
                    },
                    onCompleteClick = { id, type ->
                        sharedViewModel.onCompleteListItemClicked(id, type)
                    },
                    listState = listState,
                )
            }
        }
        sharedViewModel.setSplitReady()
    }
}

