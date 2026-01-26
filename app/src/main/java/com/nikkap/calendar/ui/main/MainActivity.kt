package com.nikkap.calendar.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.nikkap.calendar.R
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycleScope.launch { viewModel.syncAll() }
        val swipeRefreshLayout: SwipeRefreshLayout = findViewById(R.id.listSwipeRef)
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                swipeRefreshLayout.isRefreshing = state.isLoading
            }
        }
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshData()
        }
        val recyclerView: RecyclerView = findViewById(R.id.listRV)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = MainAdapter(mutableListOf())
        recyclerView.adapter = adapter
        val dividerItemDecoration = DividerItemDecoration(this, RecyclerView.VERTICAL)
        ContextCompat.getDrawable(this, R.drawable.list_divider)?.let {
            dividerItemDecoration.setDrawable(it)
        }
        recyclerView.addItemDecoration(dividerItemDecoration)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    adapter.updateList(state.items)
                    state.errorMessage?.let { message ->
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }
}