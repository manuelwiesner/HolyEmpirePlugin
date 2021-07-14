package io.github.manuelwiesner.holycraft.feature.features.spawnprotect//package io.github.manuelwiesner.holyempire.minecraft.server.feature.protection
//
//import io.github.manuelwiesner.holyempire.minecraft.server.feature.FeatureBase
//import io.github.manuelwiesner.holyempire.minecraft.server.feature.FeatureManager
//import io.github.manuelwiesner.holyempire.minecraft.server.store.Store
//import io.github.manuelwiesner.holyempire.minecraft.server.store.StoreConverters
//import org.bukkit.Chunk
//import org.bukkit.World
//import java.util.*
//
//class ProtectionFeature(manager: FeatureManager) : FeatureBase<Unit>(manager, "PROTECTION") {
//
//    /**
//     * store-protections.json
//     */
//    private val protectionZones: Store<UUID, MutableList<ProtectionZone>> = getPlugin().getStoreManager().getUUID("protections", StoreConverters.LIST(ConverterProtectionZone))
//
//    init {
//        this.children += ProtectionListener(this)
//    }
//
//    fun getProtectionZones(chunk: Chunk): List<ProtectionZone> {
//        return this.protectionZones[chunk.world.uid]?.filter { it.isProtecting(chunk) } ?: emptyList()
//    }
//
//    fun addProtectionZone(world: World, protectionZone: ProtectionZone) {
//        this.protectionZones.computeIfAbsent(world.uid) { arrayListOf() } += protectionZone
//    }
//}