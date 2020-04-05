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
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent

object GunListener: Event {
    @EventHandler
    fun onUseGun(e: PlayerInteractEvent) {
        val action = e.action
        if (action == Action.PHYSICAL) return
        val ssGunItem = SSGunItem.from(e.item) ?: return
        val player = e.player

        fun useGun(action: GunAction, cursor: GunAttachment.Cursor) {
            ssGunItem.runEvent(action) {
                if (it is GunAttachment) {
                    if (GunAttachment.getCursor(ssGunItem) == cursor) {
                        val isSuccess = it.shoot(player, cursor, ssGunItem)
                        if (isSuccess) {
                            ssGunItem.durability -= it.wearOut
                            ssGunItem.updateDurability()
                        }
                    } else {
                        ReloadOption.cancelReload(player)
                        GunAttachment.setCursor(ssGunItem, cursor)
                        ssGunItem.updateDisplayName()
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
    fun onGunReload(e: PlayerDropItemEvent) {
        val ssGunItem = SSGunItem.from(e.itemDrop.itemStack) ?: return
        e.isCancelled = true
        val cursor = GunAttachment.getCursor(ssGunItem) ?: return
        val attachment = GunAttachment.getAttachment(ssGunItem, cursor) ?: return
        val player = e.player
        attachment.reload(player, cursor, ssGunItem)
    }

    @EventHandler
    fun onGunScope(e: PlayerItemHeldEvent) {
        val player = e.player
        if (player.isSneaking) {
            val previousSlot = e.previousSlot
            val scopeDownSlot = ((previousSlot + 9) - 1) % 9
            val scopeUpSlot = (previousSlot + 1) % 9
            val delta = when (e.newSlot) {
                scopeDownSlot -> -1
                scopeUpSlot -> +1
                else -> null
            }
            if (delta != null) {
                val ssGunItem = SSGunItem.from(player.inventory.itemInMainHand) ?: return
                val cursor = GunAttachment.getCursor(ssGunItem) ?: return
                val attachment = GunAttachment.getAttachment(ssGunItem, cursor) ?: return
                attachment.changeScope(player, delta)
                e.isCancelled = true
                return
            }
        }
        if (ScopeOption.isUseScope(player)) {
            ScopeOption.cancelScope(player)
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
        val player = e.player
        ReloadOption.cancelReload(player)
        if (ScopeOption.isUseScope(player)) {
            ScopeOption.cancelScope(player)
        }
    }

    @EventHandler
    fun onCancelReload(e: PlayerItemHeldEvent) {
        val player = e.player
        ReloadOption.cancelReload(player)
    }
}