package me.syari.ss.tool

import me.syari.ss.core.auto.Event
import me.syari.ss.core.message.Message.action
import me.syari.ss.tool.item.SSTool
import me.syari.ss.tool.item.attachment.ClickType
import me.syari.ss.tool.item.attachment.gun.GunAttachment
import me.syari.ss.tool.item.attachment.gun.option.ReloadOption
import me.syari.ss.tool.item.attachment.gun.option.ScopeOption
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.*

object EventListener : Event {
    @EventHandler
    fun onUseGun(e: PlayerInteractEvent) {
        val action = e.action
        if (action == Action.PHYSICAL) return
        val ssTool = SSTool.from(e.item) ?: return
        e.isCancelled = true
        val player = e.player
        if (ssTool.durability < 1) return player.action(GunAttachment.Companion.Message.BrokenGun.message)
        val clickType = ClickType.from(action) ?: return
        ssTool.data.clickAction[clickType]?.click(player, clickType, ssTool)
    }

    @EventHandler
    fun onGunReload(e: PlayerDropItemEvent) {
        val ssTool = SSTool.from(e.itemDrop.itemStack) ?: return
        e.isCancelled = true
        val cursor = GunAttachment.getCursor(ssTool) ?: return
        val player = e.player
        ssTool.data.clickAction[cursor]?.drop(player, cursor, ssTool)
    }

    @EventHandler
    fun onMeleeDamage(e: EntityDamageByEntityEvent) {
        val attacker = e.damager as? Player ?: return
        val victim = e.entity as? LivingEntity ?: return
        val ssTool = SSTool.from(attacker.inventory.itemInMainHand) ?: return
        ssTool.data.meleeAttachment?.let {
            it.damage(victim)
            ssTool.durability -= it.wearOut
            ssTool.updateDurability()
        }
    }

    @EventHandler
    fun onCancelGun(e: PlayerToggleSneakEvent) {
        val player = e.player
        if (!e.isSneaking && ScopeOption.isUseScope(player)) {
            ScopeOption.cancelScope(player)
        }
    }

    @EventHandler
    fun onCancelGun(e: PlayerItemHeldEvent) {
        val player = e.player
        ReloadOption.cancelReload(player)
        if (ScopeOption.isUseScope(player)) {
            ScopeOption.cancelScope(player)
        }
    }

    @EventHandler
    fun onCancelGun(e: PlayerQuitEvent) {
        val player = e.player
        ReloadOption.cancelReload(player)
        if (ScopeOption.isUseScope(player)) {
            ScopeOption.cancelScope(player)
        }
    }
}