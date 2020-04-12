package me.syari.ss.gun.item.attachment.gun.option

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.core.config.dataType.ConfigDataType
import me.syari.ss.core.item.CustomItemStack
import me.syari.ss.gun.Main.Companion.gunPlugin
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

data class AmmoOption(
    val id: String?,
    val timing: Timing?,
    val amount: Int
) {
    val item by lazy { getItem(id) }
    val isTimingShot = timing == Timing.Shot
    val isTimingReload = timing == Timing.Reload

    private fun runEachAmmo(player: Player, run: (CustomItemStack) -> Unit){
        if (id == null) return
        player.inventory.contents.forEach {
            val item = CustomItemStack.create(it)
            if(item.type == Material.AIR) return@forEach
            if(item.getPersistentData(gunPlugin)?.get(gunAmmoPersistentKey, PersistentDataType.STRING) == id){
                run.invoke(item)
            }
        }
    }

    private fun hasAmmo(player: Player): Boolean {
        var sum = 0
        var has = false
        runEachAmmo(player) { item ->
            sum -= item.amount
            if (amount < sum) {
                has = true
                return@runEachAmmo
            }
        }
        return has
    }

    private val isUseAmmo get() = item != null && timing != null

    fun canConsume(player: Player): Boolean {
        return !isUseAmmo || hasAmmo(player)
    }

    fun consume(player: Player) {
        if (!isUseAmmo) return
        var sum = amount
        runEachAmmo(player) { item ->
            val amount = item.amount
            if (sum < amount) {
                item.amount = sum - amount
                return@runEachAmmo
            } else {
                item.amount = 0
                sum -= amount
            }
        }
    }

    enum class Timing(private val configValue: String) {
        Shot("shot"),
        Reload("reload");

        companion object {
            fun fromString(value: String?): Timing? {
                if (value == null) return null
                val lower = value.toLowerCase()
                return values().firstOrNull { lower == it.configValue }
            }
        }
    }

    data class Item(
        val id: String,
        val type: Material,
        val name: String,
        val lore: List<String>
    ) {
        fun create() = CustomItemStack.create(type, name, lore).apply {
            editPersistentData(gunPlugin) {
                set(gunAmmoPersistentKey, PersistentDataType.STRING, id)
            }
        }
    }

    companion object {
        private val default = AmmoOption(null, null, 1)

        fun getAmmoOption(config: CustomConfig, section: String): AmmoOption {
            if (!config.contains(section)) return default
            val id = config.get("$section.id", ConfigDataType.STRING, true) ?: return default
            val timing = Timing.fromString(config.get("$section.timing", ConfigDataType.STRING, true))
            val amount = config.get("$section.amount", ConfigDataType.INT, default.amount, false)
            return AmmoOption(id, timing, amount)
        }

        private val itemList = mutableMapOf<String, Item>()

        fun clearAllItem() {
            itemList.clear()
        }

        fun registerItem(id: String, type: Material, name: String, lore: List<String>) {
            itemList[id] = Item(id, type, name, lore)
        }

        fun getItem(id: String?) = id?.let { itemList[id] }

        private const val gunAmmoPersistentKey = "ss-gun-ammo-id"
    }
}