package com.purchasely.samplev2.presentation.screen.custom_events

import android.app.Application
import androidx.lifecycle.ViewModel
import com.purchasely.samplev2.SampleV2Application
import com.purchasely.samplev2.data.repository.PreferencesRepositoryImpl.Companion.ALLOW_CAMPAIGNS
import io.purchasely.ext.Purchasely
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

/** [id] is stable for the list's lifetime, so an expanded row stays expanded as new ones arrive. */
data class EmittedEvent(val id: Int, val name: String, val properties: Map<String, Any?>, val emittedAt: Date)

/** Events emitted since the app started, newest first. In memory only: gone once the process dies. */
class CustomEventHistory {
    private val _events = MutableStateFlow(emptyList<EmittedEvent>())
    val events: StateFlow<List<EmittedEvent>> = _events.asStateFlow()

    // The list only grows, so its size before the insert is a unique id.
    fun add(name: String, properties: Map<String, Any?>) =
        _events.update { listOf(EmittedEvent(it.size, name, properties, Date())) + it }
}

class CustomEventsViewModel(
    private val application: Application,
    private val history: CustomEventHistory
) : ViewModel() {

    val emittedEvents = history.events

    private val preferences = (application as SampleV2Application).preferencesRepository
    val allowCampaigns = MutableStateFlow(Purchasely.allowCampaigns)

    fun setAllowCampaigns(allow: Boolean) {
        Purchasely.allowCampaigns = allow
        preferences.setBoolean(ALLOW_CAMPAIGNS, allow)
        allowCampaigns.value = allow
    }

    fun emit(name: String, properties: Map<String, Any?>) {
        Purchasely.emit(name, properties)
        history.add(name, properties)
    }
}
