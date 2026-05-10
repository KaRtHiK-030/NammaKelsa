package com.karthik.nammakelsa

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun MainScreen(role: String) {

    var selectedTab by remember {
        mutableStateOf(0)
    }

    Scaffold(

        bottomBar = {

            NavigationBar {

                // HOME TAB
                NavigationBarItem(

                    selected = selectedTab == 0,

                    onClick = {
                        selectedTab = 0
                    },

                    icon = {

                        Icon(
                            imageVector =
                                Icons.Default.Home,

                            contentDescription = null
                        )
                    },

                    label = {
                        Text("Home")
                    }
                )

                // SAVED TAB FOR HIRER
                if (role == "hirer") {

                    NavigationBarItem(

                        selected = selectedTab == 1,

                        onClick = {
                            selectedTab = 1
                        },

                        icon = {

                            Icon(
                                imageVector =
                                    Icons.Default.Favorite,

                                contentDescription = null
                            )
                        },

                        label = {
                            Text("Saved")
                        }
                    )
                }

                // REQUESTS TAB FOR WORKER
                if (role == "worker") {

                    NavigationBarItem(

                        selected = selectedTab == 1,

                        onClick = {
                            selectedTab = 1
                        },

                        icon = {

                            Icon(
                                imageVector =
                                    Icons.Default.Notifications,

                                contentDescription = null
                            )
                        },

                        label = {
                            Text("Requests")
                        }
                    )
                }

                // REQUESTS TAB FOR HIRER
                if (role == "hirer") {

                    NavigationBarItem(

                        selected = selectedTab == 2,

                        onClick = {
                            selectedTab = 2
                        },

                        icon = {

                            Icon(
                                imageVector =
                                    Icons.Default.Notifications,

                                contentDescription = null
                            )
                        },

                        label = {
                            Text("Requests")
                        }
                    )
                }

                // CHAT TAB
                NavigationBarItem(

                    selected =
                        if (role == "hirer")
                            selectedTab == 3
                        else
                            selectedTab == 2,

                    onClick = {

                        selectedTab =
                            if (role == "hirer")
                                3
                            else
                                2
                    },

                    icon = {

                        Icon(
                            imageVector =
                                Icons.Default.Chat,

                            contentDescription = null
                        )
                    },

                    label = {
                        Text("Chats")
                    }
                )

                // PROFILE TAB
                NavigationBarItem(

                    selected =
                        if (role == "hirer")
                            selectedTab == 4
                        else
                            selectedTab == 3,

                    onClick = {

                        selectedTab =
                            if (role == "hirer")
                                4
                            else
                                3
                    },

                    icon = {

                        Icon(
                            imageVector =
                                Icons.Default.Person,

                            contentDescription = null
                        )
                    },

                    label = {
                        Text("Profile")
                    }
                )
            }
        }

    ) { paddingValues ->

        Surface(

            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            when (selectedTab) {

                // HOME
                0 -> {

                    if (role == "worker") {

                        WorkerHomeScreen()

                    } else {

                        HirerHomeScreen()
                    }
                }

                // SAVED / WORKER REQUESTS
                1 -> {

                    if (role == "hirer") {

                        SavedWorkersScreen()

                    } else {

                        WorkerRequestsScreen()
                    }
                }

                // HIRER REQUESTS / CHAT
                2 -> {

                    if (role == "hirer") {

                        HirerRequestsScreen()

                    } else {

                        ChatListScreen()
                    }
                }

                // CHAT / WORKER PROFILE
                3 -> {

                    if (role == "hirer") {

                        ChatListScreen()

                    } else {

                        WorkerProfileScreen()
                    }
                }

                // HIRER PROFILE
                4 -> {

                    HirerProfileScreen()
                }
            }
        }
    }
}