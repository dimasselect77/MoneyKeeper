package com.example.zenmoney.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.zenmoney.data.AppDatabase
import com.example.zenmoney.data.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return

        for (sms in messages) {
            val body = sms.messageBody ?: continue
            val sender = sms.displayOriginatingAddress ?: ""

            Log.d("SMS_RECEIVED", "From: $sender | Body: $body")

            // Парсим SMS
            val parsed = SmsParser.parse(body, sender)
            if (parsed != null) {
                Log.d("SMS_PARSED", "Amount: ${parsed.amount}, Title: ${parsed.title}, Category: ${parsed.category}")

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val dao = AppDatabase.getDatabase(context).transactionDao()
                        dao.insert(
                            Transaction(
                                title = parsed.title,
                                amount = parsed.amount,
                                category = parsed.category,
                                type = parsed.type
                            )
                        )
                        Log.d("SMS_SAVED", "Транзакция сохранена: ${parsed.title} ${parsed.amount}")
                    } catch (e: Exception) {
                        Log.e("SMS_ERROR", "Ошибка сохранения: ${e.message}")
                    }
                }
            } else {
                Log.d("SMS_SKIP", "SMS не распознано как банковское")
            }
        }
    }
}
