package com.karthik.nammakelsa

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        "home",
        "Home",
        Icons.Default.Home
    )

    object Requests : BottomNavItem(
        "requests",
        "Requests",
        Icons.Default.Notifications
    )

    object Chats : BottomNavItem(
        "chats",
        "Chats",
        Icons.Default.Chat
    )

    object Saved : BottomNavItem(
        "saved",
        "Saved",
        Icons.Default.Favorite
    )

    object Profile : BottomNavItem(
        "profile",
        "Profile",
        Icons.Default.Person
    )
}