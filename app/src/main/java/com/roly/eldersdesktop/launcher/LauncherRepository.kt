package com.roly.eldersdesktop.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

class LauncherRepository(private val context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val packageManager = context.packageManager

    fun loadCards(): List<LauncherCard> {
        val storedCards = preferences.getString(KEY_CARDS, null)
            ?.let(::parseCards)
            .orEmpty()
        val sanitizedCards = sanitizeCards(storedCards)
        if (sanitizedCards.isNotEmpty()) {
            return sanitizedCards
        }

        return defaultCards()
    }

    fun saveCards(cards: List<LauncherCard>) {
        val jsonArray = JSONArray()
        cards.forEach { card ->
            jsonArray.put(
                JSONObject()
                    .put("id", card.id)
                    .put("type", card.type.storageValue)
                    .put("packageName", card.packageName)
            )
        }
        preferences.edit().putString(KEY_CARDS, jsonArray.toString()).apply()
    }

    fun listInstalledApps(): List<InstalledApp> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        return packageManager.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
            .mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo?.packageName ?: return@mapNotNull null
                if (packageName == context.packageName) {
                    return@mapNotNull null
                }

                val label = resolveInfo.loadLabel(packageManager)
                    ?.toString()
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?: packageName

                InstalledApp(
                    packageName = packageName,
                    label = label
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    fun sanitizeCards(cards: List<LauncherCard>): List<LauncherCard> {
        return cards.filter { card ->
            when (card.type) {
                LauncherCardType.APP -> {
                    val packageName = card.packageName ?: return@filter false
                    packageManager.getLaunchIntentForPackage(packageName) != null
                }

                else -> true
            }
        }
    }

    private fun parseCards(rawJson: String): List<LauncherCard> {
        return runCatching {
            val jsonArray = JSONArray(rawJson)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.optJSONObject(index) ?: continue
                    val type = LauncherCardType.fromStorage(item.optString("type")) ?: continue
                    add(
                        LauncherCard(
                            id = item.optString("id").takeIf { it.isNotBlank() } ?: continue,
                            type = type,
                            packageName = item.optString("packageName").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun defaultCards(): List<LauncherCard> {
        val starterApps = listInstalledApps().take(2).map { app ->
            LauncherCard(
                type = LauncherCardType.APP,
                packageName = app.packageName
            )
        }

        return listOf(
            LauncherCard(type = LauncherCardType.CLOCK),
            LauncherCard(type = LauncherCardType.PHONE),
            LauncherCard(type = LauncherCardType.CONTACTS),
            LauncherCard(type = LauncherCardType.CAMERA)
        ) + starterApps
    }

    private companion object {
        const val PREFS_NAME = "elders_launcher"
        const val KEY_CARDS = "cards"
    }
}

class LauncherController(context: Context) {
    private val repository = LauncherRepository(context.applicationContext)

    var cards by mutableStateOf(repository.loadCards())
        private set

    var installedApps by mutableStateOf(repository.listInstalledApps())
        private set

    fun refreshInstalledApps() {
        installedApps = repository.listInstalledApps()
        val sanitizedCards = repository.sanitizeCards(cards)
        if (sanitizedCards != cards) {
            cards = sanitizedCards.ifEmpty { repository.loadCards() }
            persist()
        }
    }

    fun availableTemplates(): List<BuiltInCardTemplate> {
        return BuiltInCardTemplates.filterNot { template ->
            cards.any { it.type == template.type }
        }
    }

    fun availableApps(): List<InstalledApp> {
        val existingPackages = cards.mapNotNull { it.packageName }.toSet()
        return installedApps.filterNot { app -> app.packageName in existingPackages }
    }

    fun labelFor(packageName: String?): String {
        val safePackage = packageName ?: return "未知应用"
        return installedApps.firstOrNull { it.packageName == safePackage }?.label ?: safePackage
    }

    fun addBuiltInAfter(currentIndex: Int, type: LauncherCardType): Int {
        if (cards.any { it.type == type }) {
            return cards.indexOfFirst { it.type == type }.coerceAtLeast(0)
        }

        val newCard = LauncherCard(type = type)
        cards = cards.toMutableList().apply {
            add((currentIndex + 1).coerceAtMost(size), newCard)
        }
        persist()
        return cards.indexOfFirst { it.id == newCard.id }
    }

    fun addAppAfter(currentIndex: Int, packageName: String): Int {
        val existingIndex = cards.indexOfFirst {
            it.type == LauncherCardType.APP && it.packageName == packageName
        }
        if (existingIndex >= 0) {
            return existingIndex
        }

        val newCard = LauncherCard(
            type = LauncherCardType.APP,
            packageName = packageName
        )
        cards = cards.toMutableList().apply {
            add((currentIndex + 1).coerceAtMost(size), newCard)
        }
        persist()
        return cards.indexOfFirst { it.id == newCard.id }
    }

    fun moveLeft(index: Int): Int {
        if (index <= 0 || index > cards.lastIndex) {
            return index.coerceIn(0, cards.lastIndex)
        }

        val mutableCards = cards.toMutableList()
        val currentCard = mutableCards.removeAt(index)
        mutableCards.add(index - 1, currentCard)
        cards = mutableCards
        persist()
        return index - 1
    }

    fun moveRight(index: Int): Int {
        if (index < 0 || index >= cards.lastIndex) {
            return index.coerceIn(0, cards.lastIndex)
        }

        val mutableCards = cards.toMutableList()
        val currentCard = mutableCards.removeAt(index)
        mutableCards.add(index + 1, currentCard)
        cards = mutableCards
        persist()
        return index + 1
    }

    fun removeAt(index: Int): Int? {
        if (cards.size <= 1 || index !in cards.indices) {
            return null
        }

        cards = cards.toMutableList().apply {
            removeAt(index)
        }
        persist()
        return index.coerceAtMost(cards.lastIndex)
    }

    private fun persist() {
        repository.saveCards(cards)
    }
}
