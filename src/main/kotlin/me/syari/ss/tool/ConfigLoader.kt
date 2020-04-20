package me.syari.ss.tool

import me.syari.ss.core.Main.Companion.console
import me.syari.ss.core.auto.OnEnable
import me.syari.ss.core.config.CreateConfig.config
import me.syari.ss.core.config.CreateConfig.configDir
import me.syari.ss.core.config.dataType.ConfigDataType
import me.syari.ss.core.message.Message.send
import me.syari.ss.tool.Main.Companion.toolPlugin
import me.syari.ss.tool.item.SSTool
import me.syari.ss.tool.item.attachment.ToolAction
import me.syari.ss.tool.item.attachment.base.Attachment
import me.syari.ss.tool.item.attachment.gun.GunAttachment
import me.syari.ss.tool.item.attachment.gun.option.AmmoOption
import org.bukkit.Material
import org.bukkit.command.CommandSender

object ConfigLoader : OnEnable {
    override fun onEnable() {
        loadConfig(console)
        loadMessage(console)
    }

    fun loadConfig(output: CommandSender) {
        SSTool.clearAll()
        AmmoOption.clearAllItem()

        configDir(toolPlugin, output, "Tool") {
            val id = fileName.substringBeforeLast(".yml")
            val type = get("info.type", ConfigDataType.MATERIAL, Material.STONE, false)
            val name = get("info.name", ConfigDataType.STRING, "&b$id", true)
            val lore = get("info.lore", ConfigDataType.STRINGLIST, listOf(), false)
            val durability = get("info.durability", ConfigDataType.INT, type.maxDurability.toInt(), true)
            val attachments = mutableMapOf<ToolAction, Attachment>().also { map ->
                ToolAction.values().forEach { action ->
                    val section = action.configSection
                    if (this@configDir.contains(section)) {
                        action.attachmentLoader.objectInstance?.get(this@configDir, section)?.let {
                            map[action] = it
                        }
                    }
                }
            }
            SSTool.register(id, type, name, lore, durability, attachments)
        }
        output.send("&b[Tool] &6銃を${SSTool.idList.size}個ロードしました")

        configDir(toolPlugin, output, "Ammo") {
            val id = fileName.substringBeforeLast(".yml")
            val type = get("info.type", ConfigDataType.MATERIAL, Material.STONE, false)
            val name = get("info.name", ConfigDataType.STRING, "&d$id", true)
            val lore = get("info.lore", ConfigDataType.STRINGLIST, listOf(), false)
            AmmoOption.registerItem(id, type, name, lore)
        }
    }

    fun loadMessage(output: CommandSender){
        config(toolPlugin, output, "message.yml") {
            GunAttachment.loadMessage(this, "gun")
        }
    }
}