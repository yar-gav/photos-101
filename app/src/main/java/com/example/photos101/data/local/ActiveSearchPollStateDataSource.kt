package com.example.photos101.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

private val Context.activeSearchPollStateDataStore: DataStore<Preferences> by preferencesDataStore(name = "photos_poll_state")

private val KEY_ACTIVE_SEARCH_QUERY = stringPreferencesKey("active_search_query")
private val KEY_FIRST_PAGE_IDS = stringSetPreferencesKey("first_page_photo_ids")

/**
 * Persists and reads the active search and first-page result for background polling.
 */
class ActiveSearchPollStateDataSource(private val context: Context) {

    val activeSearchPollState: Flow<ActiveSearchPollState?> = context.activeSearchPollStateDataStore.data.map { prefs ->
        val query = prefs[KEY_ACTIVE_SEARCH_QUERY] ?: return@map null
        val ids = prefs[KEY_FIRST_PAGE_IDS]?.toList() ?: return@map null
        ActiveSearchPollState(activeSearchQuery = query, firstPagePhotoIds = ids)
    }

    suspend fun savePollState(query: String, firstPagePhotoIds: List<String>) {
        context.activeSearchPollStateDataStore.edit { prefs ->
            prefs[KEY_ACTIVE_SEARCH_QUERY] = query
            prefs[KEY_FIRST_PAGE_IDS] = firstPagePhotoIds.toSet()
        }
    }

    suspend fun getActiveSearchPollState(): ActiveSearchPollState? = activeSearchPollState.first()

    suspend fun clearPollState() {
        context.activeSearchPollStateDataStore.edit { prefs ->
            prefs.remove(KEY_ACTIVE_SEARCH_QUERY)
            prefs.remove(KEY_FIRST_PAGE_IDS)
        }
    }
}
