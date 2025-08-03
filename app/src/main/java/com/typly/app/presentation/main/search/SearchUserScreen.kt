package com.typly.app.presentation.main.search

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.typly.app.presentation.components.SearchBar
import com.typly.app.presentation.components.UserSearchList

/**
 * User search screen for finding and selecting users to start conversations.
 * 
 * Provides a search interface with real-time user search results.
 * Features a search bar with query input and a scrollable list of matching users.
 * Handles user selection for navigation to chat screens.
 * 
 * @param onUserSelected Callback triggered when a user is selected, receives user ID
 * @param onBackPressed Callback triggered when back navigation is requested
 */
@Composable
fun SearchUserScreen(
    onUserSelected: (String) -> Unit,
    onBackPressed: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val viewModel = hiltViewModel<SearchUserViewModel>()
    val searchResult by viewModel.searchResult.collectAsState()
    Column (modifier = Modifier.fillMaxSize()) {
        SearchBar(
            searchText = searchQuery,
            onSearchTextChange = { query ->
                searchQuery = query
                viewModel.updateQuery(query)
                Log.d("SearchUserScreen", "Search Query Updated: $query")
                Log.d("SearchUserScreen", "Search Result: $searchResult")

            },
            onSearchClick = {
                Log.d("SearchUserScreen", "Button Search Result: $searchResult")
            },
            hint = "Search User"
        )
            UserSearchList(
                users = searchResult!!,
                onUserClick = { user ->
                   onUserSelected(user.id)
                },
                modifier = Modifier.fillMaxSize()
            )

    }

}
