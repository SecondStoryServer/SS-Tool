package me.syari.ss.gun

import me.syari.ss.core.auto.Event
import org.bukkit.plugin.java.JavaPlugin

class Main: JavaPlugin() {
    companion object {
        lateinit var gunPlugin: JavaPlugin
    }

    override fun onEnable() {
        gunPlugin = this
        Event.register(this, GunListener)
    }
}