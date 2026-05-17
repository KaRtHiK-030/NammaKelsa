package com.karthik.nammakelsa

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

sealed class WorkerTab(val index: Int, val label: String, val icon: ImageVector) {
    data object Home     : WorkerTab(0, "Home",     Icons.Default.Home)
    data object Requests : WorkerTab(1, "Requests", Icons.Default.Notifications)
    data object Chat     : WorkerTab(2, "Chats",    Icons.AutoMirrored.Filled.Chat)
    data object Profile  : WorkerTab(3, "Profile",  Icons.Default.Person)
    companion object { val all = listOf(Home, Requests, Chat, Profile) }
}

sealed class HirerTab(val index: Int, val label: String, val icon: ImageVector) {
    data object Home     : HirerTab(0, "Home",     Icons.Default.Home)
    data object Saved    : HirerTab(1, "Saved",    Icons.Default.Favorite)
    data object Requests : HirerTab(2, "Requests", Icons.Default.Notifications)
    data object Chat     : HirerTab(3, "Chats",    Icons.AutoMirrored.Filled.Chat)
    data object Profile  : HirerTab(4, "Profile",  Icons.Default.Person)
    companion object { val all = listOf(Home, Saved, Requests, Chat, Profile) }
}

@Composable
fun MainScreen(role: String) {

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                if (role == "worker") {
                    WorkerTab.all.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab.index,
                            onClick  = { selectedTab = tab.index },
                            icon     = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                            label    = { Text(tab.label) }
                        )
                    }
                } else {
                    HirerTab.all.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab.index,
                            onClick  = { selectedTab = tab.index },
                            icon     = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                            label    = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            if (role == "worker") {
                when (selectedTab) {
                    WorkerTab.Home.index     -> WorkerHomeScreen()
                    WorkerTab.Requests.index -> WorkerRequestsScreen()
                    WorkerTab.Chat.index     -> ChatListScreen()
                    WorkerTab.Profile.index  -> WorkerProfileScreen()
                }
            } else {
                when (selectedTab) {
                    HirerTab.Home.index     -> HirerHomeScreen()
                    HirerTab.Saved.index    -> SavedWorkersScreen()
                    HirerTab.Requests.index -> HirerRequestsScreen()
                    HirerTab.Chat.index     -> ChatListScreen()
                    HirerTab.Profile.index  -> HirerProfileScreen()
                }
            }
        }
    }
}
