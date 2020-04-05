package me.syari.ss.gun

import me.syari.ss.core.auto.Event
import me.syari.ss.gun.item.attachment.gun.option.ReloadOption
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent

object GunListener: Event {
    @EventHandler
    fun onCancelReload(e: PlayerQuitEvent) {
        val p = e.player
        ReloadOption.cancelReload(p)
    }

    @EventHandler
    fun onCancelReload(e: PlayerItemHeldEvent) {
        val p = e.player
        ReloadOption.cancelReload(p)
    }
}