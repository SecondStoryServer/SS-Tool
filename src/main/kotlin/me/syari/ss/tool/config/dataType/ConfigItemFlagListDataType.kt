package me.syari.ss.tool.config.dataType

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.core.config.dataType.ConfigDataType
import org.bukkit.inventory.ItemFlag

object ConfigItemFlagListDataType : ConfigDataType<List<ItemFlag>> {
    override val typeName = "ItemFlag"

    private fun getFlag(text: String): ItemFlag? {
        return ItemFlag.values().firstOrNull { it.name == text }
    }

    override fun get(config: CustomConfig, path: String, notFoundError: Boolean): List<ItemFlag>? {
        return config.get(path, ConfigDataType.STRINGLIST, notFoundError)?.mapIndexedNotNull { index, line ->
            getFlag(line.toUpperCase()).apply {
                if (this == null) {
                    config.nullError("$path:$index", "String(ItemFlag)")
                }
            }
        }
    }
}