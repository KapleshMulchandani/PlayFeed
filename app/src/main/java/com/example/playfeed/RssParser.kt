package com.example.playfeed

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream



class RssParser {
    fun parse(inputStream: InputStream): List<RssArticle> {
        val articles = mutableListOf<RssArticle>()
        var title: String? = null
        var link: String? = null
        var pubDate: String? = null
        var imageUrl: String? = null
        var description: String? = null

        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(inputStream, null)

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name

            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (tagName) {
                        "title" -> title = parser.nextText()
                        "link" -> link = parser.nextText()
                        "pubDate" -> pubDate = parser.nextText()
                        "description" -> description = parser.nextText()
                        "content:encoded" -> description = parser.nextText()
                        "media:thumbnail" -> {

                            imageUrl = parser.getAttributeValue(null, "url")
                        }

                        "enclosure" -> {

                            imageUrl = parser.getAttributeValue(null, "url")
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (tagName == "item" && title != null && link != null) {

                        imageUrl = imageUrl ?: extractImageFromDescription(description)


                        articles.add(RssArticle(title, link, pubDate, imageUrl))


                        title = null
                        link = null
                        pubDate = null
                        imageUrl = null
                        description = null
                    }
                }
            }
            eventType = parser.next()
        }
        return articles
    }


    private fun extractImageFromDescription(description: String?): String? {
        return try {
            description?.let {

                val imgTagRegex = """<img [^>]*src=["']([^"']+)["']""".toRegex()
                imgTagRegex.find(it)?.groups?.get(1)?.value
            }
        } catch (e: Exception) {
            null
        }
    }
}