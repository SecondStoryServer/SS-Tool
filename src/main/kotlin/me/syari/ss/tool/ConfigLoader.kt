package me.syari.ss.tool

import me.syari.ss.core.Main.Companion.console
import me.syari.ss.core.auto.OnEnable
import me.syari.ss.core.config.CreateConfig.config
import me.syari.ss.core.config.CreateConfig.configDir
import me.syari.ss.core.config.dataType.ConfigDataType
import me.syari.ss.core.message.Message.send
import me.syari.ss.tool.Main.Companion.toolPlugin
import me.syari.ss.tool.item.SSToolData
import me.syari.ss.tool.item.attachment.gun.GunAttachment
import me.syari.ss.tool.item.attachment.gun.option.AmmoOption
import me.syari.ss.tool.item.attachment.melee.MeleeAttachment
import me.syari.ss.tool.item.attachment.shield.ShieldAttachment
import org.bukkit.Material
import org.bukkit.command.CommandSender

object ConfigLoader : OnEnable {
    override fun onEnable() {
        loadConfig(console)
        loadMessage(console)
    }

    fun loadConfig(output: CommandSender) {
        SSToolData.clearAll()
        AmmoOption.clearAllItem()

        configDir(toolPlugin, output, "Tool") {
            val id = fileName.substringBeforeLast(".yml")
            val type = get("info.type", ConfigDataType.MATERIAL, Material.STONE, false)
            val name = get("info.name", ConfigDataType.STRING, "&b$id", true)
            val lore = get("info.lore", ConfigDataType.STRINGLIST, listOf(), false)
            val durability = get("info.durability", ConfigDataType.INT, type.maxDurability.toInt(), true)
            val gunAttachments = GunAttachment.loadAll(this)
            val meleeAttachment = MeleeAttachment.load(this)
            val shieldAttachments = ShieldAttachment.loadAll(this)
            SSToolData.register(id, type, name, lore, durability, gunAttachments, meleeAttachment, shieldAttachments)
        }
        output.send("&b[Tool] &6銃を${SSToolData.idList.size}個ロードしました")

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