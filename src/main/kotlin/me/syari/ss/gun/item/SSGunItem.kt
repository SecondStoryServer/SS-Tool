package me.syari.ss.gun.item

import me.syari.ss.core.item.CustomItemStack
import me.syari.ss.gun.Main.Companion.gunPlugin
import me.syari.ss.gun.item.attachment.GunAction
import me.syari.ss.gun.item.attachment.base.Attachment
import me.syari.ss.gun.item.attachment.gun.GunAttachment
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class SSGunItem(val item: CustomItemStack, val gun: SSGun) {
    var durability: Int =
        item.getPersistentData(gunPlugin)?.get(gunDurabilityPersistentKey, PersistentDataType.INTEGER)
            ?: gun.maxDurability

    fun updateDurability() {
        item.editPersistentData(gunPlugin) {
            set(gunDurabilityPersistentKey, PersistentDataType.INTEGER, durability)
        }
        item.durability = (item.type.maxDurability * durability) / gun.maxDurability
    }

    fun runEvent(gunAction: GunAction, run: (Attachment) -> Unit) {
        gun.attachments[gunAction]?.let(run)
    }

    fun updateDisplayName() {
        item.display = buildString {
            append(gun.name)
            val leftGun = gun.attachments[GunAction.ShootLeft] as? GunAttachment
            val rightGun = gun.attachments[GunAction.ShootRight] as? GunAttachment
            val leftGunBullet = leftGun?.getBullet(this@SSGunItem, GunAttachment.Cursor.Left)
            val rightGunBullet = rightGun?.getBullet(this@SSGunItem, GunAttachment.Cursor.Right)
            when {
                leftGunBullet != null && rightGunBullet != null -> {
                    val cursor = when (GunAttachment.getCursor(this@SSGunItem)) {
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

    companion object {
        fun from(item: ItemStack?): SSGunItem? {
            return item?.let { from(CustomItemStack.create(item)) }
        }

        fun from(item: CustomItemStack?): SSGunItem? {
            return item?.let { SSGun.from(item)?.let { SSGunItem(item, it) } }
        }

        fun get(id: String?): SSGunItem? {
            return SSGun.get(id)?.create()
        }

        private const val gunDurabilityPersistentKey = "ss-gun-durability"
    }
}