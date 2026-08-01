package com.example.zenmoney.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {
    val allTransactions: Flow<List<Transaction>> = dao.getAll()
    val totalIncome: Flow<Double?> = dao.getTotalIncome()
    val totalExpense: Flow<Double?> = dao.getTotalExpense()
    val expensesByCategory: Flow<List<CategorySum>> = dao.getExpensesByCategory()

    suspend fun insert(transaction: Transaction) = dao.insert(transaction)
    suspend fun delete(transaction: Transaction) = dao.delete(transaction)
}
