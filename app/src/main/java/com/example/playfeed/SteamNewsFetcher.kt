package com.example.playfeed

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

class SteamNewsFetcher {

    private val client = OkHttpClient()

    fun fetchSteamNews(appId: Int, onSuccess: (List<SteamArticle>) -> Unit, onFailure: (String) -> Unit) {
        val url = "https://api.steampowered.com/ISteamNews/GetNewsForApp/v0002/?appid=$appId&count=3&maxlength=300&format=json"
        val request = Request.Builder()
            .url(url)
            .build()

        Thread {
            try {
                val response: Response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val articles = parseSteamNews(responseBody)
                        // Log articles count here to ensure data is coming through
                        Log.d("SteamNewsFetcher", "Fetched ${articles.size} articles")
                        onSuccess(articles)
                    } else {
                        onFailure("No response body found")
                    }
                } else {
                    onFailure("Failed to fetch Steam news")
                }
            } catch (e: Exception) {
                onFailure("Error: ${e.message}")
            }
        }.start()
    }


    private fun parseSteamNews(response: String): List<SteamArticle> {
        val steamArticles = mutableListOf<SteamArticle>()
        try {
            val jsonObject = JSONObject(response)
            val newsItems = jsonObject.getJSONObject("appnews").getJSONArray("newsitems")
            for (i in 0 until newsItems.length()) {
                val item = newsItems.getJSONObject(i)
                val title = item.getString("title")
                val url = item.getString("url")

                // Add source "Steam" to identify these articles
                steamArticles.add(SteamArticle(title, url)) // No imageUrl or timestamp needed
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return steamArticles
    }
}
