package me.syari.ss.tool.item.attachment.shield

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.tool.item.SSTool
import me.syari.ss.tool.item.attachment.AttachmentLoader.getWearOut
import me.syari.ss.tool.item.attachment.ClickAction
import me.syari.ss.tool.item.attachment.ClickType
import org.bukkit.entity.Player

class ShieldAttachment(
    val wearOut: Int
) : ClickAction {
    override fun getText(clickType: ClickType, ssTool: SSTool): String {
        return ""
    }

    override fun click(
        player: Player,
        clickType: ClickType,
        ssTool: SSTool
    ) {

    }

    override fun drop(player: Player, clickType: ClickType, ssTool: SSTool) {

    }

    companion object {
        private fun load(config: CustomConfig, section: String): ShieldAttachment? {
            return if (config.contains(section)) {
                ShieldAttachment(
                    getWearOut(config, section)
                )
            } else null
        }

        fun loadAll(config: CustomConfig): Map<ClickType, ShieldAttachment>? {
            return if (config.contains("shield")) {
                mutableMapOf<ClickType, ShieldAttachment>().also { map ->
                    ClickType.values().forEach { clickType ->
                        load(config, "shield.${clickType.internalId}")?.let {
                            map[clickType] = it
                        }
                    }
                }
            } else null
        }
    }
}