package com.mountsa.fmsimulation.core.match.commentary

import android.content.Context
import android.util.Log
import org.json.JSONObject

object CommentaryRepository {

    private val commentaryMap = mutableMapOf<String, List<String>>()

    fun load(context: Context, locale: String = "id") {
        try {
            val fileName = "commentary/commentary_$locale.json"
            val json = context.assets
                .open(fileName)
                .bufferedReader()
                .use { it.readText() }

            val root = JSONObject(json)
            commentaryMap.clear()

            root.keys().forEach { key ->
                val array = root.getJSONArray(key)
                val texts = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    texts.add(array.getString(i))
                }
                commentaryMap[key] = texts
            }
            Log.d("CommentaryRepository", "Loaded $fileName with ${commentaryMap.size} keys")
        } catch (e: Exception) {
            Log.e("CommentaryRepository", "Failed to load commentary", e)
        }
    }

    fun get(eventType: String): List<String> {
        return commentaryMap[eventType] ?: emptyList()
    }
}