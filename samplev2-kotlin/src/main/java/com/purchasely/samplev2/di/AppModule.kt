package com.purchasely.samplev2.di

import android.app.Application
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl
import com.purchasely.samplev2.domain.preferences.PreferencesRepository
import com.purchasely.samplev2.presentation.screen.attributes.AttributesViewModel
import com.purchasely.samplev2.presentation.screen.built_in_attributes.BuiltInAttributesViewModel
import com.purchasely.samplev2.presentation.screen.custom_events.CustomEventHistory
import com.purchasely.samplev2.presentation.screen.custom_events.CustomEventsViewModel
import com.purchasely.samplev2.presentation.screen.home.HomeViewModel
import com.purchasely.samplev2.presentation.screen.logs.LogsViewModel
import com.purchasely.samplev2.presentation.screen.offerings.OfferingsViewModel
import com.purchasely.samplev2.presentation.screen.paywall.PaywallViewModel
import com.purchasely.samplev2.presentation.screen.privacy.PrivacyViewModel
import com.purchasely.samplev2.presentation.screen.products.ProductsViewModel
import com.purchasely.samplev2.presentation.screen.settings.SettingsViewModel
import com.purchasely.samplev2.presentation.screen.subscriptions.SubscriptionsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val appModule = module {
    // Repositories
    single<PreferencesRepository> { PreferencesRepositoryImpl(context = get()) }
    single { CustomEventHistory() }

    // ViewModels
    viewModel { HomeViewModel(application = get<Application>(), preferencesRepository = get()) }
    viewModel { PaywallViewModel(preferencesRepository = get()) }
    viewModel { SettingsViewModel(application = get<Application>(), preferencesRepository = get()) }
    viewModel { LogsViewModel(application = get<Application>()) }
    viewModel { CustomEventsViewModel(application = get<Application>(), history = get()) }
    viewModel { OfferingsViewModel() }
    viewModel { AttributesViewModel() }
    viewModel { BuiltInAttributesViewModel() }
    viewModel { ProductsViewModel() }
    viewModel { PrivacyViewModel() }
    viewModel { SubscriptionsViewModel() }
}