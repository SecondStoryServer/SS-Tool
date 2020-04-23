package me.syari.ss.tool.item

import me.syari.ss.core.item.CustomItemStack
import me.syari.ss.tool.Main.Companion.toolPlugin
import me.syari.ss.tool.item.attachment.ClickAction
import me.syari.ss.tool.item.attachment.ClickType
import me.syari.ss.tool.item.attachment.melee.MeleeAttachment
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class SSToolData(
    val id: String,
    val type: Material,
    val name: String,
    val lore: List<String>,
    val maxDurability: Int,
    val itemFlag: List<ItemFlag>?,
    val enchant: Map<Enchantment, Int>?,
    val clickAction: Map<ClickType, ClickAction>,
    val meleeAttachment: MeleeAttachment?
) {
    fun create() = SSTool(CustomItemStack.create(type, name, lore).apply {
        editPersistentData(toolPlugin) {
            set(toolIdPersistentKey, PersistentDataType.STRING, id)
        }
        itemFlag?.let {
            addItemFlag(*it.toTypedArray())
        }
    }, this).apply {
        resetEnchant()
    }

    companion object {
        private val toolList = mutableMapOf<String, SSToolData>()

        val idList get() = toolList.keys

        fun from(item: ItemStack?): SSToolData? {
            return item?.let { from(CustomItemStack.create(item)) }
        }

        fun from(item: CustomItemStack?): SSToolData? {
            return item?.let { get(getToolId(item)) }
        }

        fun get(id: String?): SSToolData? {
            return id?.let { toolList[id.toLowerCase()] }
        }

        fun register(
            id: String,
            type: Material,
            name: String?,
            lore: List<String>,
            maxDurability: Int?,
            itemFlag: List<ItemFlag>?,
            enchant: Map<Enchantment, Int>?,
            clickAction: Map<ClickType, ClickAction>?,
            meleeAttachment: MeleeAttachment?
        ) {
            toolList[id.toLowerCase()] = SSToolData(
                id,
                type,
                name ?: "&b$id",
                lore,
                maxDurability ?: type.maxDurability.toInt(),
                itemFlag,
                enchant,
                clickAction ?: mapOf(),
                meleeAttachment
            )
        }

        fun clearAll() {
            toolList.clear()
        }

        private const val toolIdPersistentKey = "ss-tool-id"

        fun getToolId(item: CustomItemStack): String? {
            return item.getPersistentData(toolPlugin)?.get(toolIdPersistentKey, PersistentDataType.STRING)
        }

        private const val toolModulePersistentKey = "ss-tool-module"
    }
}