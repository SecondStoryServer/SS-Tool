package me.syari.ss.tool

import me.syari.ss.core.auto.Event
import me.syari.ss.core.auto.OnEnable
import org.bukkit.plugin.java.JavaPlugin

class Main: JavaPlugin() {
    companion object {
        lateinit var toolPlugin: JavaPlugin
    }

    override fun onEnable() {
        toolPlugin = this
        OnEnable.register(ConfigLoader, CommandCreator)
        Event.register(this, EventListener)
    }
}