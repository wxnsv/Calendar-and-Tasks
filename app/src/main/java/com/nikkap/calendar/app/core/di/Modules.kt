package com.nikkap.calendar.app.core.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.work.WorkManager
import com.nikkap.calendar.app.core.auth.AuthentificationManager
import com.nikkap.calendar.app.core.auth.AuthorizationManager
import com.nikkap.calendar.app.ui.screens.about.AboutViewModel
import com.nikkap.calendar.app.ui.screens.auth.AuthViewModel
import com.nikkap.calendar.app.ui.screens.create.CreateViewModel
import com.nikkap.calendar.app.ui.screens.list.ListViewModel
import com.nikkap.calendar.app.ui.screens.main.MainViewModel
import com.nikkap.calendar.app.ui.screens.mainpager.MainPagerViewModel
import com.nikkap.calendar.app.ui.screens.settings.SettingsViewModel
import com.nikkap.calendar.app.ui.screens.split.SplitViewModel
import com.nikkap.calendar.data.local.AppDatabase
import com.nikkap.calendar.data.remote.api.CalendarApi
import com.nikkap.calendar.data.remote.api.TasksApi
import com.nikkap.calendar.data.remote.interceptor.AuthInterceptor
import com.nikkap.calendar.data.remote.interceptor.ErrorInterceptor
import com.nikkap.calendar.data.repository.CalendarRepositoryImpl
import com.nikkap.calendar.data.repository.TaskRepositoryImpl
import com.nikkap.calendar.data.repository.UserPreferencesRepository
import com.nikkap.calendar.data.worker.SavePhotoWorker
import com.nikkap.calendar.data.worker.SyncWorker
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

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
            tokenProvider = { get<AuthorizationManager>().getAccessToken() }
        )
    }
    single { ErrorInterceptor() }

    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single {
        val cacheSize = 10 * 1024 * 1024L
        Cache(androidContext().cacheDir, cacheSize)
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .addInterceptor(get<ErrorInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .cache(get())
            .dispatcher(Dispatcher().apply {
                maxRequestsPerHost = 15
            })
            .connectTimeout(15, TimeUnit.SECONDS)
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
    single { AuthorizationManager(androidContext()) }
    single { AuthentificationManager(androidContext()) }
}
val appModule = module {
    single<WorkManager> {
        WorkManager.getInstance(androidContext())
    }
    worker { SyncWorker(get(), get(), get(), get()) }
    worker { SavePhotoWorker(get(), get(), get()) }
    single<TaskRepository> { TaskRepositoryImpl(get(), get(), get()) }
    single<CalendarRepository> { CalendarRepositoryImpl(get(), get(), get()) }
    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { SplitViewModel(get(), get(), get()) }
    viewModel { ListViewModel(get(), get(), get()) }
    viewModel { MainPagerViewModel(get()) }
    viewModelOf(::CreateViewModel)
    viewModel { MainViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { AboutViewModel() }
}