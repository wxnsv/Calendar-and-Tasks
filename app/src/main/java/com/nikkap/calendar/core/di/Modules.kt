package com.nikkap.calendar.core.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.nikkap.calendar.core.auth.AuthManager
import com.nikkap.calendar.data.local.AppDatabase
import com.nikkap.calendar.data.remote.api.CalendarApi
import com.nikkap.calendar.data.remote.api.TasksApi
import com.nikkap.calendar.data.remote.interceptor.AuthInterceptor
import com.nikkap.calendar.data.repository.CalendarRepositoryImpl
import com.nikkap.calendar.data.repository.TaskRepositoryImpl
import com.nikkap.calendar.data.repository.UserPreferencesRepository
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository
import com.nikkap.calendar.ui.screens.auth.AuthViewModel
import com.nikkap.calendar.ui.screens.create.CreateViewModel
import com.nikkap.calendar.ui.screens.list.ListViewModel
import com.nikkap.calendar.ui.screens.main.MainViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val networkModule = module {
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    single {
        Retrofit.Builder()
            .client(get<OkHttpClient>())
            .baseUrl("https://www.googleapis.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    single { get<Retrofit>().create(TasksApi::class.java) }

    single { get<Retrofit>().create(CalendarApi::class.java) }

    single {
        AuthInterceptor(
            tokenProvider = { get<AuthManager>().getAccessToken() }
        )
    }

    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }
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
    single {
        PreferenceDataStoreFactory.create(
            produceFile = { androidContext().preferencesDataStoreFile("user_prefs") }
        )
    }

    single { UserPreferencesRepository(get()) }
}
val authModule = module {
    single { AuthManager(androidContext()) }
}
val appModule = module {
    single<TaskRepository> { TaskRepositoryImpl(get(), get()) }
    single<CalendarRepository> { CalendarRepositoryImpl(get(), get()) }
    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { ListViewModel(get(), get()) }
    viewModelOf(::CreateViewModel)
    viewModel { MainViewModel(get(), get(), get()) }
}