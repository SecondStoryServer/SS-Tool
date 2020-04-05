package me.syari.ss.gun

import me.syari.ss.core.auto.Event
import me.syari.ss.gun.item.SSGunItem
import me.syari.ss.gun.item.attachment.GunAction
import me.syari.ss.gun.item.attachment.gun.GunAttachment
import me.syari.ss.gun.item.attachment.gun.option.ReloadOption
import me.syari.ss.gun.item.attachment.gun.option.ScopeOption
import me.syari.ss.gun.item.attachment.melee.MeleeAttachment
import me.syari.ss.gun.item.attachment.shield.ShieldAttachment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent

object GunListener: Event {
    @EventHandler
    fun onUseGun(e: PlayerInteractEvent) {
        val action = e.action
        if (action == Action.PHYSICAL) return
        val ssGunItem = SSGunItem.from(e.item) ?: return
        val item = ssGunItem.item
        val player = e.player

        fun useGun(action: GunAction, cursor: GunAttachment.Cursor) {
            ssGunItem.runEvent(action) {
                if (it is GunAttachment) {
                    if (it.getCursor(item) == cursor) {
                        val isSuccess = it.shoot(player, item)
                        if (isSuccess) {
                            ssGunItem.durability -= it.wearOut
                            ssGunItem.updateDurability()
                        }
                    } else {
                        it.setCursor(item, cursor)
                        it.updateName(item)
                    }
                }
            }
        }

        fun useShield(action: GunAction) {
            ssGunItem.runEvent(action) {
                if (it is ShieldAttachment) {

                }
            }
        }

        when (action) {
            Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR -> {
                useGun(GunAction.ShootRight, GunAttachment.Cursor.Right)
                useShield(GunAction.ShieldRight)
            }
            Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> {
                useGun(GunAction.ShootLeft, GunAttachment.Cursor.Left)
                useShield(GunAction.ShieldLeft)
            }
            else -> return
        }
    }

    @EventHandler
    fun onMeleeDamage(e: EntityDamageByEntityEvent) {
        val attacker = e.damager as? Player ?: return
        val victim = e.entity as? LivingEntity ?: return
        val ssGunItem = SSGunItem.from(attacker.inventory.itemInMainHand) ?: return
        ssGunItem.runEvent(GunAction.Melee) {
            if (it is MeleeAttachment) {
                it.damage(victim)
                ssGunItem.durability -= it.wearOut
                ssGunItem.updateDurability()
            }
        }
    }

    @EventHandler
    fun onCancelReload(e: PlayerQuitEvent) {
        val p = e.player
        ReloadOption.cancelReload(p)
        if (ScopeOption.isUseScope(p)) {
            ScopeOption.cancelScope(p)
        }
    }

    @EventHandler
    fun onCancelReload(e: PlayerItemHeldEvent) {
        val p = e.player
        ReloadOption.cancelReload(p)
    }
}