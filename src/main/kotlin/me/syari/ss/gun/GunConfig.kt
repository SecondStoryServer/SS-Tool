package me.syari.ss.gun

import me.syari.ss.core.Main.Companion.console
import me.syari.ss.core.auto.OnEnable
import me.syari.ss.core.config.CreateConfig.config
import me.syari.ss.core.config.CreateConfig.configDir
import me.syari.ss.core.config.dataType.ConfigDataType
import me.syari.ss.core.message.Message.send
import me.syari.ss.gun.Main.Companion.gunPlugin
import me.syari.ss.gun.item.SSGun
import me.syari.ss.gun.item.attachment.GunAction
import me.syari.ss.gun.item.attachment.base.Attachment
import me.syari.ss.gun.item.attachment.gun.GunAttachment
import me.syari.ss.gun.item.attachment.gun.option.AmmoOption
import org.bukkit.Material
import org.bukkit.command.CommandSender

object GunConfig: OnEnable {
    override fun onEnable() {
        loadConfig(console)
        loadMessage(console)
    }

    fun loadConfig(output: CommandSender){
        SSGun.clearAll()
        AmmoOption.clearAllItem()

        configDir(gunPlugin, output, "Gun"){
            val id = fileName.substringBeforeLast(".yml")
            val type = get("info.type", ConfigDataType.MATERIAL, Material.STONE, false)
            val name = get("info.name", ConfigDataType.STRING, "&b$id", true)
            val lore = get("info.lore", ConfigDataType.STRINGLIST, listOf(), false)
            val durability = get("info.durability", ConfigDataType.INT, type.maxDurability.toInt(), true)
            val attachments = mutableMapOf<GunAction, Attachment>().also { map ->
                GunAction.values().forEach { action ->
                    val section = action.configSection
                    if(this@configDir.contains(section)){
                        action.attachmentLoader.objectInstance?.get(this@configDir, section)?.let {
                            map[action] = it
                        }
                    }
                }
            }
            SSGun.register(id, type, name, lore, durability, attachments)
        }
        output.send("&b[Gun] &6銃を${SSGun.gunIdList.size}個ロードしました")

        configDir(gunPlugin, output, "Ammo"){
            val id = fileName.substringBeforeLast(".yml")
            val type = get("info.type", ConfigDataType.MATERIAL, Material.STONE, false)
            val name = get("info.name", ConfigDataType.STRING, "&d$id", true)
            val lore = get("info.lore", ConfigDataType.STRINGLIST, listOf(), false)
            AmmoOption.registerItem(id, type, name, lore)
        }
    }

    fun loadMessage(output: CommandSender){
        config(gunPlugin, output, "message.yml"){
            GunAttachment.loadMessage(this, "gun")
        }
    }
}