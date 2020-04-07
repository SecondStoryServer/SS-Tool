package me.syari.ss.gun

import me.syari.ss.core.auto.Event
import me.syari.ss.core.auto.OnEnable
import org.bukkit.plugin.java.JavaPlugin

class Main: JavaPlugin() {
    companion object {
        lateinit var gunPlugin: JavaPlugin
    }

    override fun onEnable() {
        gunPlugin = this
        OnEnable.register(GunConfig, GunCommand)
        Event.register(this, GunListener)
    }
}