package com.example.zenmoney.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenmoney.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).transactionDao()
    private val repository = TransactionRepository(dao)

    val allTransactions = repository.allTransactions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val totalIncome = repository.totalIncome.map { it ?: 0.0 }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0
    )

    val totalExpense = repository.totalExpense.map { it ?: 0.0 }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0
    )

    val balance = combine(totalIncome, totalExpense) { inc, exp -> inc - exp }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0
    )

    val expensesByCategory = repository.expensesByCategory.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun addTransaction(title: String, amount: Double, category: String, type: TransactionType) {
        viewModelScope.launch {
            repository.insert(
                Transaction(
                    title = title,
                    amount = amount,
                    category = category,
                    type = type
                )
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.delete(transaction)
        }
    }
}
