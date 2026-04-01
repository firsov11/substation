package com.firsov.substation.data.local

import androidx.room.TypeConverter
import com.firsov.substation.data.model.Breaker
import com.firsov.substation.data.model.Busbar
import com.firsov.substation.data.model.Disconnector
import com.firsov.substation.data.model.Equipment
import com.firsov.substation.data.model.Transformer
import com.google.gson.Gson
import org.json.JSONObject

class EquipmentConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromEquipment(equipment: Equipment?): String? {
        if (equipment == null) return null
        val json = JSONObject()
        json.put("type", equipment::class.java.simpleName)
        // ВАЖНО: упаковываем данные объекта в строку под ключом "data"
        json.put("data", gson.toJson(equipment))
        return json.toString()
    }

    @TypeConverter
    fun toEquipment(value: String?): Equipment? {
        if (value.isNullOrBlank()) return null
        return try {
            val json = JSONObject(value)
            val type = json.getString("type")
            val dataJson = json.optString("data", "{}")

            when (type) {
                "Breaker" -> gson.fromJson(dataJson, Breaker::class.java)
                "Disconnector" -> gson.fromJson(dataJson, Disconnector::class.java)
                "Transformer" -> gson.fromJson(dataJson, Transformer::class.java)
                "Busbar" -> gson.fromJson(dataJson, Busbar::class.java)
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}

