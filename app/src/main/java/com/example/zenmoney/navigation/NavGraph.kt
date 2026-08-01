package com.example.zenmoney.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Главная")
    object Statistics : Screen("statistics", "Статистика")
    object Budget : Screen("budget", "Бюджет")
    object Settings : Screen("settings", "Настройки")
    object AddTransaction : Screen("add_transaction", "Добавить")
}
