package me.syari.ss.gun.item

import me.syari.ss.core.item.CustomItemStack
import me.syari.ss.gun.Main.Companion.gunPlugin
import me.syari.ss.gun.item.attachment.Attachment
import me.syari.ss.gun.item.attachment.GunAction
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class SSGun(private val id: String, private val type: Material, private val name: String, private val lore: List<String>, val maxDurability: Int) {
    private val event = mutableMapOf<GunAction, Attachment>()

    fun setAttachment(attachments: Map<GunAction, Attachment>?){
        if(attachments == null) return
    }

    fun runEvent(gunAction: GunAction){

    }

    fun register(){
        gunList[id] = this
    }

    fun create() = SSGunItem(CustomItemStack.create(type, name, lore).apply {
        editPersistentData(gunPlugin){
            setString("ss-gun-id", id)
        }
    }, this)

    companion object {
        private val gunList = mutableMapOf<String, SSGun>()

        fun from(item: ItemStack): SSGun? {
            return from(CustomItemStack.create(item))
        }

        fun from(item: CustomItemStack): SSGun? {
            return get(item.getPersistentData(gunPlugin){ getString("ss-gun-id") })
        }

        fun get(id: String?): SSGun? {
            return gunList[id]
        }
    }
}