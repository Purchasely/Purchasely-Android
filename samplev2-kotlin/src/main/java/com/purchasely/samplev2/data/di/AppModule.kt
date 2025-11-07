package com.purchasely.samplev2.data.di


import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl
import com.purchasely.samplev2.domain.preferences.PreferencesRepository
import com.purchasely.samplev2.presentation.screen.attributes.AttributesViewModel
import com.purchasely.samplev2.presentation.screen.home.HomeViewModel
import com.purchasely.samplev2.presentation.screen.paywall.PaywallViewModel
import com.purchasely.samplev2.presentation.screen.products.ProductsViewModel
import com.purchasely.samplev2.presentation.screen.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


@RequiresApi(Build.VERSION_CODES.O)
val appModule = module {

    // Repositories
    single<PreferencesRepository> { PreferencesRepositoryImpl(context = get()) }

    // ViewModels
    viewModel { HomeViewModel(preferencesRepository = get()) }
    viewModel { PaywallViewModel(preferencesRepository = get()) }
    viewModel { SettingsViewModel(application = get<Application>(), preferencesRepository = get()) }
    viewModel { AttributesViewModel() }
    viewModel { ProductsViewModel() }
}