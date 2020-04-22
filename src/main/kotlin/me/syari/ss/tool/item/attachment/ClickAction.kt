package me.syari.ss.tool.item.attachment

import me.syari.ss.tool.Main.Companion.toolPlugin
import me.syari.ss.tool.item.SSTool
import me.syari.ss.tool.item.attachment.gun.GunAttachment
import me.syari.ss.tool.item.attachment.gun.option.ReloadOption
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

interface ClickAction {
    fun getText(clickType: ClickType, ssTool: SSTool): String

    fun click(player: Player, clickType: ClickType, ssTool: SSTool)

    fun drop(player: Player, clickType: ClickType, ssTool: SSTool)

    fun switch(player: Player, ssTool: SSTool, clickType: ClickType) {
        ReloadOption.cancelReload(player)
        GunAttachment.setCursor(ssTool, clickType)
        ssTool.updateDisplayName()
    }

    companion object {
        private const val cursorPersistentKey = ""

        fun getCursor(ssTool: SSTool): ClickType? {
            val id = ssTool.item.getPersistentData(toolPlugin)?.get(cursorPersistentKey, PersistentDataType.STRING)
            return ClickType.values().firstOrNull { it.internalId == id }
        }

        fun setCursor(ssTool: SSTool, clickType: ClickType?) {
            ssTool.item.editPersistentData(toolPlugin) {
                set(cursorPersistentKey, PersistentDataType.STRING, clickType?.internalId)
            }
        }
    }
}