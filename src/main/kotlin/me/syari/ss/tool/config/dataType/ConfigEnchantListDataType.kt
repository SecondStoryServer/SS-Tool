package me.syari.ss.tool.config.dataType

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.core.config.dataType.ConfigDataType
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment

object ConfigEnchantListDataType : ConfigDataType<Map<Enchantment, Int>> {
    override val typeName = "Enchantment"

    override fun get(config: CustomConfig, path: String, notFoundError: Boolean): Map<Enchantment, Int>? {
        return config.get(path, ConfigDataType.STRINGLIST, notFoundError)?.mapIndexedNotNull { index, line ->
            val splitLine = line.split(":")
            if (splitLine.size == 2) {
                val enchantType = splitLine[0].toLowerCase()
                val enchant = Enchantment.getByKey(NamespacedKey.minecraft(enchantType)) ?: return null
                val level = splitLine[1].toIntOrNull() ?: return null
                enchant to level
            } else {
                config.formatMismatchError("$path:$index")
                null
            }
        }?.toMap()
    }
}