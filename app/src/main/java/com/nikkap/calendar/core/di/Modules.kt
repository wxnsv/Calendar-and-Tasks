package com.nikkap.calendar.core.di

import androidx.room.Room
import com.nikkap.calendar.core.auth.AuthManager
import com.nikkap.calendar.data.local.AppDatabase
import com.nikkap.calendar.data.remote.api.CalendarApi
import com.nikkap.calendar.data.remote.api.TasksApi
import com.nikkap.calendar.data.repository.CalendarRepositoryImpl
import com.nikkap.calendar.data.repository.TaskRepositoryImpl
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository
import com.nikkap.calendar.ui.auth.AuthViewModel
import com.nikkap.calendar.ui.main.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    single {
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single { get<Retrofit>().create(TasksApi::class.java) }
    single { get<Retrofit>().create(CalendarApi::class.java) }
}
val localModule = module {
    single {
        Room.databaseBuilder(
            get(),
            AppDatabase::class.java,
            "app_database"
        ).build()
    }
    single { get<AppDatabase>().taskDao() }
    single { get<AppDatabase>().calendarDao() }
}
val authModule = module {
    single { AuthManager(androidContext()) }
}
val appModule = module {
    single<TaskRepository> { TaskRepositoryImpl(get(), get(), get()) }
    single<CalendarRepository> { CalendarRepositoryImpl(get(), get(), get()) }
    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { MainViewModel(get(), get()) }
}