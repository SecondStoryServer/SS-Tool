package me.syari.ss.tool.item

import me.syari.ss.core.item.CustomItemStack
import me.syari.ss.core.item.ItemStackPlus.give
import me.syari.ss.core.item.ItemStackPlus.giveOrDrop
import me.syari.ss.tool.Main.Companion.toolPlugin
import me.syari.ss.tool.item.attachment.ToolAction
import me.syari.ss.tool.item.attachment.base.Attachment
import me.syari.ss.tool.item.attachment.gun.GunAttachment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class SSTool(val item: CustomItemStack, val data: SSToolData) {
    var durability = getDurability(item) ?: data.maxDurability

    fun updateDurability() {
        item.editPersistentData(toolPlugin) {
            set(gunDurabilityPersistentKey, PersistentDataType.INTEGER, durability)
        }
        item.damage = item.type.maxDurability - ((durability * item.type.maxDurability) / data.maxDurability)
    }

    fun runEvent(toolAction: ToolAction, run: (Attachment) -> Unit) {
        data.attachments[toolAction]?.let(run)
    }

    fun updateDisplayName() {
        item.display = buildString {
            append(data.name)
            val leftGun = data.attachments[ToolAction.ShootLeft] as? GunAttachment
            val rightGun = data.attachments[ToolAction.ShootRight] as? GunAttachment
            val leftGunBullet = leftGun?.getBullet(this@SSTool, GunAttachment.Cursor.Left)
            val rightGunBullet = rightGun?.getBullet(this@SSTool, GunAttachment.Cursor.Right)
            when {
                leftGunBullet != null && rightGunBullet != null -> {
                    val cursor = when (GunAttachment.getCursor(this@SSTool)) {
                        GunAttachment.Cursor.Right -> "◁▶"
                        GunAttachment.Cursor.Left -> "◀▷"
                        else -> "◁▷"
                    }
                    append("  《 $leftGunBullet $cursor $rightGunBullet 》")
                }
                leftGunBullet != null -> {
                    append("  《 $leftGunBullet 》")
                }
                rightGunBullet != null -> {
                    append("  《 $rightGunBullet 》")
                }
            }
        }
    }

    private fun give(player: Player, orDrop: Boolean) {
        updateDisplayName()
        updateDurability()
        if (orDrop) {
            player.giveOrDrop(item)
        } else {
            player.give(item)
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

        private const val gunDurabilityPersistentKey = "ss-tool-durability"

        fun getDurability(item: CustomItemStack): Int? {
            return item.getPersistentData(toolPlugin)?.get(gunDurabilityPersistentKey, PersistentDataType.INTEGER)
        }
    }
}