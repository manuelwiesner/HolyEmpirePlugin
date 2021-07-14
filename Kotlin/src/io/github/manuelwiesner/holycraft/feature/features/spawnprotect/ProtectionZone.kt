package io.github.manuelwiesner.holycraft.feature.features.spawnprotect//package io.github.manuelwiesner.holyempire.minecraft.server.feature.protection
//
//import com.google.gson.stream.JsonReader
//import com.google.gson.stream.JsonWriter
//import io.github.manuelwiesner.holyempire.minecraft.server.store.JsonStoreConverter
//import io.github.manuelwiesner.holyempire.minecraft.server.store.get
//import org.bukkit.Chunk
//
//data class ProtectionZone(val zoneX: IntRange, val zoneY: IntRange, val zoneZ: IntRange) {
//    fun isProtecting(chunk: Chunk): Boolean {
//        return false
//    }
//}
//
//object ConverterProtectionZone : JsonStoreConverter<ProtectionZone>() {
//    override fun fromJson(json: JsonReader): ProtectionZone {
//        json.beginObject()
//        val startX = json.get("startX") { it.nextInt() }
//        val endX = json.get("endX") { it.nextInt() }
//        val startY = json.get("startY") { it.nextInt() }
//        val endY = json.get("endY") { it.nextInt() }
//        val startZ = json.get("startZ") { it.nextInt() }
//        val endZ = json.get("endZ") { it.nextInt() }
//        json.endObject()
//        return ProtectionZone(startX..endX, startY..endY, startZ..endZ)
//    }
//
//    override fun toJson(json: JsonWriter, value: ProtectionZone) {
//        json.beginObject()
//        json.name("startX").value(value.zoneX.first)
//        json.name("endX").value(value.zoneX.last)
//        json.name("startY").value(value.zoneY.first)
//        json.name("endY").value(value.zoneY.last)
//        json.name("startZ").value(value.zoneZ.first)
//        json.name("endZ").value(value.zoneZ.last)
//        json.endObject()
//    }
//}