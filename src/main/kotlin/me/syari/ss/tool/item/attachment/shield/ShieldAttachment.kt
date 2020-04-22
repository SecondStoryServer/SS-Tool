package me.syari.ss.tool.item.attachment.shield

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.tool.item.attachment.ClickType

class ShieldAttachment(
    val wearOut: Int
) {
    companion object {
        private fun load(config: CustomConfig, section: String): ShieldAttachment? {
            return if (config.contains(section)) {
                ShieldAttachment(0)
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