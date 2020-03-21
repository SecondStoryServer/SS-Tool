package me.syari.ss.gun

import me.syari.ss.core.Main.Companion.console
import me.syari.ss.core.auto.OnEnable
import me.syari.ss.core.config.CreateConfig.configDir
import me.syari.ss.gun.Main.Companion.gunPlugin
import me.syari.ss.gun.item.attachment.GunAction
import me.syari.ss.gun.item.SSGun
import me.syari.ss.gun.item.attachment.Attachment
import org.bukkit.Material
import org.bukkit.command.CommandSender
import kotlin.reflect.full.createInstance

object GunConfig: OnEnable {
    override fun onEnable() {
        loadConfig(console)
    }

    fun loadConfig(output: CommandSender){
        configDir(gunPlugin, output, "Gun"){
            val id = fileName.substringBeforeLast(".yml")
            val type = getMaterial("info.type", Material.STONE, false)
            val name = getString("info.name", "&b$id")
            val lore = getStringList("info.lore", listOf(), false)
            val durability = getInt("info.durability", type.maxDurability.toInt())
            val gun = SSGun(id, type, name, lore, durability)
            gun.setAttachment(mutableMapOf<GunAction, Attachment>().also { map ->
                GunAction.values().forEach { action ->
                    val section = action.configSection
                    if(this@configDir.contains(section)){
                        action.attachment.createInstance().get(this@configDir, section)?.let {
                            map[action] = it
                        }
                    }
                }
            })
            gun.register()
        }
    }
}