package me.syari.ss.gun.item

import me.syari.ss.core.item.CustomItemStack
import org.bukkit.inventory.ItemStack

class SSGunItem(val item: CustomItemStack, val gun: SSGun) {

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