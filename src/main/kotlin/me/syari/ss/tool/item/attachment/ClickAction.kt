package me.syari.ss.tool.item.attachment

import me.syari.ss.tool.Main.Companion.toolPlugin
import me.syari.ss.tool.item.SSTool
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

interface ClickAction {
    val clickType: ClickType

    fun getText(ssTool: SSTool): String

    fun click(player: Player, ssTool: SSTool)

    fun drop(player: Player, ssTool: SSTool)

    fun switch(player: Player, ssTool: SSTool) {
        setCursor(ssTool, clickType)
        ssTool.updateDisplayName()
    }

    companion object {
        private const val cursorPersistentKey = "ss-tool-cursor"

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