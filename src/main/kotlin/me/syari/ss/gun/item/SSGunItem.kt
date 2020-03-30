package me.syari.ss.gun.item

import me.syari.ss.core.item.CustomItemStack
import me.syari.ss.gun.Main.Companion.gunPlugin
import me.syari.ss.gun.item.attachment.base.Attachment
import me.syari.ss.gun.item.attachment.GunAction
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class SSGunItem(val item: CustomItemStack, val gun: SSGun) {
    var durability: Int = item.getPersistentData(gunPlugin)?.get("ss-gun-durability", PersistentDataType.INTEGER) ?: gun.maxDurability

    fun updateDurability(){
        item.editPersistentData(gunPlugin){
            set("ss-gun-durability", PersistentDataType.INTEGER, durability)
        }
        item.durability = (item.type.maxDurability * durability) / gun.maxDurability
    }

    fun runEvent(gunAction: GunAction, run: (Attachment?) -> Unit){
        run.invoke(gun.attachments[gunAction])
    }

    companion object {
        fun from(item: ItemStack): SSGunItem? {
            return from(CustomItemStack.create(item))
        }

        fun from(item: CustomItemStack): SSGunItem? {
            return SSGun.from(item)?.let { SSGunItem(item, it) }
        }

        fun get(id: String?): SSGunItem? {
            return SSGun.get(id)?.create()
        }
    }
}