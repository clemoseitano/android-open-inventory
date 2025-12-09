package com.coptimize.openinventory.data.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class StringOrListDeserializer : JsonDeserializer<List<String>?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<String>? {
        if (json == null || json.isJsonNull) {
            return null
        }

        return if (json.isJsonArray) {
            // It's an array: ["Item 1", "Item 2"]
            val list = mutableListOf<String>()
            json.asJsonArray.forEach { element ->
                if (!element.isJsonNull) {
                    list.add(element.asString)
                }
            }
            list
        } else if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
            // It's a single string: "Item 1, Item 2" or just "Item 1"
            listOf(json.asString)
        } else {
            // Fallback for unexpected types
            null
        }
    }
}