package me.syari.ss.tool.item

import me.syari.ss.core.item.CustomItemStack
import me.syari.ss.tool.Main.Companion.toolPlugin
import me.syari.ss.tool.item.attachment.ToolAction
import me.syari.ss.tool.item.attachment.base.Attachment
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class SSToolData(
    val id: String,
    val type: Material,
    val name: String,
    val lore: List<String>,
    val maxDurability: Int,
    val attachments: Map<ToolAction, Attachment>
) {
    fun create() = SSTool(CustomItemStack.create(type, name, lore).apply {
        editPersistentData(toolPlugin) {
            set(toolIdPersistentKey, PersistentDataType.STRING, id)
        }
    }, this)

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
            name: String,
            lore: List<String>,
            maxDurability: Int,
            attachments: Map<ToolAction, Attachment>
        ) {
            toolList[id.toLowerCase()] = SSToolData(id, type, name, lore, maxDurability, attachments)
        }

        fun clearAll() {
            toolList.clear()
        }

        private const val toolIdPersistentKey = "ss-tool-id"

        fun getToolId(item: CustomItemStack): String? {
            return item.getPersistentData(toolPlugin)?.get(toolIdPersistentKey, PersistentDataType.STRING)
        }
    }
}