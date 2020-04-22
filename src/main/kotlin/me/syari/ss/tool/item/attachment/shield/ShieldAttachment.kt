package me.syari.ss.tool.item.attachment.shield

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.tool.item.SSTool
import me.syari.ss.tool.item.attachment.AttachmentLoader.getWearOut
import me.syari.ss.tool.item.attachment.ClickAction
import me.syari.ss.tool.item.attachment.ClickType
import org.bukkit.entity.Player

class ShieldAttachment(
    override val clickType: ClickType,
    val wearOut: Int
) : ClickAction {
    override fun getText(ssTool: SSTool): String {
        return ""
    }

    override fun click(
        player: Player,
        ssTool: SSTool
    ) {

    }

    override fun drop(player: Player, ssTool: SSTool) {

    }

    companion object {
        private fun load(config: CustomConfig, clickType: ClickType): ShieldAttachment? {
            val section = "shield.${clickType.internalId}"
            return if (config.contains(section)) {
                ShieldAttachment(
                    clickType,
                    getWearOut(config, section)
                )
            } else null
        }

        fun loadAll(config: CustomConfig): Map<ClickType, ShieldAttachment>? {
            return if (config.contains("shield")) {
                mutableMapOf<ClickType, ShieldAttachment>().also { map ->
                    ClickType.values().forEach { clickType ->
                        load(config, clickType)?.let {
                            map[clickType] = it
                        }
                    }
                }
            } else null
        }
    }
}