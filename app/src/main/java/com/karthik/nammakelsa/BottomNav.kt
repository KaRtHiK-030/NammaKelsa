package com.karthik.nammakelsa

sealed class BottomNavItem(
    val route: String,
    val title: String
) {
    object Home : BottomNavItem("home", "Home")
    object Profile : BottomNavItem("profile", "Profile")
}