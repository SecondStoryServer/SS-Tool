package me.syari.ss.tool.item.module.general

import me.syari.ss.core.item.CustomItemStack
import me.syari.ss.tool.item.module.ModuleType
import me.syari.ss.tool.item.module.ToolModule
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment

object EnchantModule : GeneralModule<EnchantModule.Data> {
    override val type = ModuleType.Enchant

    data class Data(
        override val useSlot: Int,
        val enchant: Enchantment,
        val level: Int
    ) : ToolModule.Data {
        override val type = Material.LAPIS_LAZULI
        override val color = ChatColor.AQUA
        override val name = "エンチャント付与"
        override val description = listOf(
            "&bアイテムに装着することでエンチャントを付与します"
        )
        override val apply = { item: CustomItemStack ->
            item.addEnchant(enchant, level)
        }
    }

    override fun from(splitData: List<String>): Data? {
        val useSlot = splitData[0].toIntOrNull() ?: return null
        val enchant = Enchantment.getByKey(NamespacedKey.minecraft(splitData[1])) ?: return null
        val level = splitData[2].toIntOrNull() ?: return null
        return Data(useSlot, enchant, level)
    }

    override fun to(data: Data): List<String> {
        return listOf(data.useSlot.toString(), data.enchant.key.key, data.level.toString())
    }
}