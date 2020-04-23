package me.syari.ss.tool.item

import me.syari.ss.core.item.CustomItemStack
import me.syari.ss.core.item.ItemStackPlus.give
import me.syari.ss.core.item.ItemStackPlus.giveOrDrop
import me.syari.ss.tool.Main.Companion.toolPlugin
import me.syari.ss.tool.item.attachment.ClickAction.Companion.getCursor
import me.syari.ss.tool.item.attachment.ClickType
import me.syari.ss.tool.item.module.ModuleType
import me.syari.ss.tool.item.module.ToolModule
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class SSTool(val item: CustomItemStack, val data: SSToolData) {
    var durability = getDurability(item) ?: data.maxDurability
        set(value) {
            field = value
            updateDurability()
        }

    private fun updateDurability() {
        item.editPersistentData(toolPlugin) {
            set(toolDurabilityPersistentKey, PersistentDataType.INTEGER, durability)
        }
        item.damage = item.type.maxDurability - ((durability * item.type.maxDurability) / data.maxDurability)
    }

    fun updateDisplayName() {
        item.display = buildString {
            append(data.name)
            val (left, right) = ClickType.values().map { type ->
                type to data.clickAction[type]?.getText(this@SSTool)
            }.toMap().let {
                it[ClickType.Left] to it[ClickType.Right]
            }
            when {
                left != null && right != null -> {
                    val cursor = when (getCursor(this@SSTool)) {
                        ClickType.Right -> "◁▶"
                        ClickType.Left -> "◀▷"
                        else -> "◁▷"
                    }
                    append("  《 $left $cursor $right 》")
                }
                left != null -> {
                    append("  《 $left 》")
                }
                right != null -> {
                    append("  《 $right 》")
                }
            }
        }
    }

    private val toolModuleMap = mutableMapOf<ModuleType, MutableSet<ToolModule.Data>>()

    private fun give(player: Player, orDrop: Boolean) {
        if (orDrop) {
            player.giveOrDrop(item)
        } else {
            player.give(item)
        }
    }

    fun resetEnchant() {
        data.enchant?.let {
            item.setEnchant(it)
        }
    }

    fun give(player: Player) {
        give(player, false)
    }

    fun giveOrDrop(player: Player) {
        give(player, true)
    }

    companion object {
        fun from(item: ItemStack?): SSTool? {
            return item?.let { from(CustomItemStack.create(item)) }
        }

        fun from(item: CustomItemStack?): SSTool? {
            return item?.let { SSToolData.from(item)?.let { SSTool(item, it) } }
        }

        fun get(id: String?): SSTool? {
            return SSToolData.get(id)?.create()
        }

        private const val toolDurabilityPersistentKey = "ss-tool-durability"

        fun getDurability(item: CustomItemStack): Int? {
            return item.getPersistentData(toolPlugin)?.get(toolDurabilityPersistentKey, PersistentDataType.INTEGER)
        }
    }
}