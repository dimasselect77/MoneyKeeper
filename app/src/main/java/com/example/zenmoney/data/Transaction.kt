package com.example.zenmoney.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val type: TransactionType, // INCOME or EXPENSE
    val date: Long = System.currentTimeMillis()
)

enum class TransactionType {
    INCOME, EXPENSE
}
