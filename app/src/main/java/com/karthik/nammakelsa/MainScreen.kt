package com.karthik.nammakelsa

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun MainScreen(
    role: String,
    openTab: String = "home"
) {

    val workerItems = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Requests", Icons.Default.Work),
        NavItem("Chats", Icons.Default.Chat),
        NavItem("Profile", Icons.Default.Person)
    )

    val hirerItems = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Saved", Icons.Default.Bookmark),
        NavItem("Requests", Icons.Default.Send),
        NavItem("Chats", Icons.Default.Chat),
        NavItem("Profile", Icons.Default.Person)
    )

    val items =
        if (role == "worker") workerItems else hirerItems

    val initialIndex =
        when {
            role == "worker" && openTab == "requests" -> 1
            role == "worker" && openTab == "chats" -> 2
            role == "worker" && openTab == "profile" -> 3

            role == "hirer" && openTab == "saved" -> 1
            role == "hirer" && openTab == "requests" -> 2
            role == "hirer" && openTab == "chats" -> 3
            role == "hirer" && openTab == "profile" -> 4

            else -> 0
        }

    var selectedIndex by rememberSaveable {
        mutableStateOf(initialIndex)
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(item.title)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->

        Surface(
            modifier = Modifier.padding(paddingValues)
        ) {

            when (role) {

                "worker" -> {
                    when (selectedIndex) {
                        0 -> WorkerHomeScreen()
                        1 -> WorkerRequestsScreen()
                        2 -> ChatListScreen()
                        3 -> WorkerProfileScreen()
                    }
                }

                else -> {
                    when (selectedIndex) {
                        0 -> HirerHomeScreen()
                        1 -> SavedWorkersScreen()
                        2 -> HirerSentRequestsScreen()
                        3 -> ChatListScreen()
                        4 -> HirerProfileScreen()
                    }
                }
            }
        }
    }
}