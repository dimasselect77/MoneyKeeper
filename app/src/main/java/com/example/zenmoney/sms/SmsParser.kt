package com.example.zenmoney.sms

import com.example.zenmoney.data.TransactionType
import java.util.regex.Pattern

data class ParsedSms(
    val title: String,
    val amount: Double,
    val category: String,
    val type: TransactionType
)

object SmsParser {

    // ─── СБЕРБАНК ───
    // "VISA1234 01.01.24 12:00 покупка 1500р Баланс: 10000р"
    // "Покупка 1500р. Карта *1234. Доступно 10000р. Пятерочка"
    // "Зачисление 50000р. Карта *1234. Отправитель: Иванов И.И."
    // "Списание 1500р. Карта *1234. Баланс: 10000р"
    // "Перевод 1000р. Карта *1234. Получатель: Петров П.П."
    private val SBER_PATTERNS = listOf(
        // Покупка/списание
        Pattern.compile(
            "(?i)(?:покупка|списание|оплата).*?(\d[\d\s]*(?:[.,]\d{2})?)\s*(?:р|руб|₽)",
            Pattern.CASE_INSENSITIVE
        ),
        // Зачисление/перевод
        Pattern.compile(
            "(?i)(?:зачисление|перевод|поступление).*?(\d[\d\s]*(?:[.,]\d{2})?)\s*(?:р|руб|₽)",
            Pattern.CASE_INSENSITIVE
        ),
        // VISA1234 01.01.24 12:00 покупка 1500р
        Pattern.compile(
            "(?i)\w+\s+\d{2}\.\d{2}\.\d{2,4}\s+\d{2}:\d{2}\s+(?:покупка|списание).*?(\d[\d\s]*(?:[.,]\d{2})?)\s*(?:р|руб|₽)",
            Pattern.CASE_INSENSITIVE
        )
    )

    // ─── РАЙФФАЙЗЕН ───
    // "Покупка 1 500,00 RUB. Карта *1234. Доступно 10 000,00 RUB. Место: Пятерочка"
    // "Списание 1 500,00 RUB. Карта *1234. Баланс: 10 000,00 RUB"
    // "Зачисление 50 000,00 RUB. Карта *1234. Отправитель: Иванов И.И."
    private val RAIFFEISEN_PATTERNS = listOf(
        Pattern.compile(
            "(?i)(?:покупка|списание|оплата).*?(\d[\d\s]*(?:[.,]\d{2})?)\s*(?:RUB|руб|₽)",
            Pattern.CASE_INSENSITIVE
        ),
        Pattern.compile(
            "(?i)(?:зачисление|перевод|поступление).*?(\d[\d\s]*(?:[.,]\d{2})?)\s*(?:RUB|руб|₽)",
            Pattern.CASE_INSENSITIVE
        )
    )

    fun parse(body: String, sender: String = ""): ParsedSms? {
        val cleanBody = body.replace("\s+".toRegex(), " ").trim()

        // Определяем банк по отправителю или тексту
        val isSber = sender.contains("Sberbank", ignoreCase = true) ||
                sender.contains("900", ignoreCase = true) ||
                cleanBody.contains("Сбербанк", ignoreCase = true) ||
                cleanBody.contains("VISA", ignoreCase = true) ||
                cleanBody.contains("MasterCard", ignoreCase = true)

        val isRaiff = sender.contains("Raiffeisen", ignoreCase = true) ||
                sender.contains("Райффайзен", ignoreCase = true) ||
                cleanBody.contains("Raiffeisen", ignoreCase = true)

        // Определяем тип операции
        val isIncome = cleanBody.contains("зачисление", ignoreCase = true) ||
                cleanBody.contains("поступление", ignoreCase = true) ||
                cleanBody.contains("перевод", ignoreCase = true) &&
                !cleanBody.contains("списание", ignoreCase = true)

        val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE

        // Выбираем паттерны
        val patterns = when {
            isRaiff -> RAIFFEISEN_PATTERNS
            else -> SBER_PATTERNS // по умолчанию Сбер
        }

        for (pattern in patterns) {
            val matcher = pattern.matcher(cleanBody)
            if (matcher.find()) {
                val amountStr = matcher.group(1)
                    ?.replace(" ", "")
                    ?.replace(",", ".")
                    ?: continue

                val amount = amountStr.toDoubleOrNull() ?: continue
                if (amount <= 0) continue

                val category = detectCategory(cleanBody)
                val title = extractTitle(cleanBody, type)

                return ParsedSms(
                    title = title,
                    amount = amount,
                    category = category,
                    type = type
                )
            }
        }
        return null
    }

    private fun detectCategory(body: String): String {
        return when {
            body.contains("пятерочка", ignoreCase = true) ||
                    body.contains("магнит", ignoreCase = true) ||
                    body.contains("перекресток", ignoreCase = true) ||
                    body.contains("ашан", ignoreCase = true) ||
                    body.contains("продукт", ignoreCase = true) -> "Еда"

            body.contains("аптека", ignoreCase = true) ||
                    body.contains("аптечка", ignoreCase = true) -> "Здоровье"

            body.contains("такси", ignoreCase = true) ||
                    body.contains("uber", ignoreCase = true) ||
                    body.contains("яндекс.такси", ignoreCase = true) ||
                    body.contains("ситимобил", ignoreCase = true) ||
                    body.contains("метро", ignoreCase = true) ||
                    body.contains("автобус", ignoreCase = true) -> "Транспорт"

            body.contains("кино", ignoreCase = true) ||
                    body.contains("steam", ignoreCase = true) ||
                    body.contains("apple", ignoreCase = true) ||
                    body.contains("google", ignoreCase = true) ||
                    body.contains("netflix", ignoreCase = true) ||
                    body.contains("spotify", ignoreCase = true) -> "Развлечения"

            body.contains("жкх", ignoreCase = true) ||
                    body.contains("коммунал", ignoreCase = true) ||
                    body.contains("тсж", ignoreCase = true) ||
                    body.contains("управляющая", ignoreCase = true) -> "Жильё"

            body.contains("зарплат", ignoreCase = true) ||
                    body.contains("аванс", ignoreCase = true) -> "Зарплата"

            body.contains("бензин", ignoreCase = true) ||
                    body.contains("азс", ignoreCase = true) ||
                    body.contains("газпромнефть", ignoreCase = true) ||
                    body.contains("лукойл", ignoreCase = true) ||
                    body.contains("топливо", ignoreCase = true) -> "Авто"

            body.contains("кофейня", ignoreCase = true) ||
                    body.contains("кофе", ignoreCase = true) ||
                    body.contains("starbucks", ignoreCase = true) ||
                    body.contains("шоколадница", ignoreCase = true) -> "Кафе"

            body.contains("мтс", ignoreCase = true) ||
                    body.contains("билайн", ignoreCase = true) ||
                    body.contains("мегафон", ignoreCase = true) ||
                    body.contains("теле2", ignoreCase = true) ||
                    body.contains("связь", ignoreCase = true) -> "Связь"

            else -> "Другое"
        }
    }

    private fun extractTitle(body: String, type: TransactionType): String {
        // Пытаемся найти название места после суммы
        val afterAmount = body.substringAfter("₽", "")
            .substringAfter("RUB", "")
            .substringAfter("руб", "")
            .substringAfter(".", "")

        // Ищем ключевые слова "Место:", "Получатель:", "Отправитель:"
        val place = when {
            body.contains("место:", ignoreCase = true) ->
                body.substringAfter("место:", "").take(30).trim()
            body.contains("получатель:", ignoreCase = true) ->
                body.substringAfter("получатель:", "").take(30).trim()
            body.contains("отправитель:", ignoreCase = true) ->
                body.substringAfter("отправитель:", "").take(30).trim()
            afterAmount.length > 3 -> afterAmount.trim().take(30)
            else -> ""
        }

        return if (place.isNotBlank()) place else {
            if (type == TransactionType.INCOME) "Поступление" else "Покупка"
        }
    }
}
