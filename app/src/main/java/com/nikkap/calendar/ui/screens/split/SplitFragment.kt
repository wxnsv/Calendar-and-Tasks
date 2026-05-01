package com.nikkap.calendar.ui.screens.split

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.nikkap.calendar.ui.screens.main.MainViewModel
import com.nikkap.calendar.ui.screens.split.utils.calendar.Calendar
import com.nikkap.calendar.ui.screens.split.utils.list.List
import com.nikkap.calendar.ui.theme.CalendarTheme
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
        val state = viewModel.state.collectAsState().value

        CalendarTheme {
            Column {
                Calendar(state.items)
                List(
                    state.items,
                    onEditClick = { id, type ->
                        sharedViewModel.onEditListItemClicked(id, type)
                    },
                    onDeleteClick = { id, type ->
                        sharedViewModel.onDeleteListItemClicked(id, type)
                    },
                    onCompleteClick = { id, type ->
                        sharedViewModel.onCompleteListItemClicked(id, type)
                    },
                )
            }
        }
    }
}

