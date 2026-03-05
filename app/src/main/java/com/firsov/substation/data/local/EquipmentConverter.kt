package com.firsov.substation.data.local

import androidx.room.TypeConverter
import com.firsov.substation.data.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser

class EquipmentConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromEquipment(equipment: Equipment?): String? {
        if (equipment == null) return null
        val jsonObject = JsonObject()
        // Сохраняем имя класса, чтобы потом понять: Breaker это или Transformer
        jsonObject.addProperty("type", equipment::class.java.name)
        jsonObject.add("data", gson.toJsonTree(equipment))
        return jsonObject.toString()
    }

    @TypeConverter
    fun toEquipment(json: String?): Equipment? {
        if (json == null) return null
        return try {
            val jsonObject = JsonParser.parseString(json).asJsonObject
            val className = jsonObject.get("type").asString
            val data = jsonObject.get("data").toString()
            // Восстанавливаем конкретный класс
            gson.fromJson(data, Class.forName(className)) as Equipment
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
