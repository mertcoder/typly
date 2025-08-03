package com.typly.app.presentation.main.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typly.app.domain.repository.ChatRepository
import com.typly.app.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for user search functionality.
 * 
 * Manages search query state and provides reactive search results with debouncing.
 * Handles user search operations through the repository layer and provides
 * loading states for UI consumption. Features automatic search debouncing
 * to reduce unnecessary API calls.
 * 
 * @property userRepository Repository for user-related operations
 * @property firestore Firestore database instance
 * @property chatRepository Repository for chat-related operations
 */
@HiltViewModel
class SearchUserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val firestore: FirebaseFirestore,
    private val chatRepository: ChatRepository
): ViewModel(){

    /** Internal StateFlow for the current search query */
    private val _searchQuery = MutableStateFlow("")

    /** StateFlow indicating whether search operation is in progress */
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()



    /**
     * StateFlow containing search results with automatic debouncing and loading management.
     * 
     * Implements debounced search with 300ms delay, filters queries with minimum 2 characters,
     * and handles loading states automatically. Emits empty list for short queries or errors.
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResult = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .onEach { _isLoading.value = it.isNotEmpty()}
        .flatMapLatest { query->
            if(query.length>=2){
                userRepository.searchUsersByUserName(query)
                    .onEach { _isLoading.value = false }
                    .catch {
                        _isLoading.value = false
                        Log.e("SearchUserViewModel", "Error searching users: ${it.message}", it)
                        emit(emptyList())
                    }
            }else{
                _isLoading.value = false
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    /**
     * Updates the search query with proper formatting.
     * 
     * Trims whitespace and converts to lowercase for consistent searching.
     * Triggers the reactive search flow through the searchResult StateFlow.
     * 
     * @param query The new search query string
     */
    fun updateQuery(query: String){
        _searchQuery.value = query.trim().lowercase()
    }





}
