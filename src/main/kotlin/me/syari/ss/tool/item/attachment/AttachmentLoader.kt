package me.syari.ss.tool.item.attachment

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.core.config.dataType.ConfigDataType

object AttachmentLoader {
    fun getWearOut(config: CustomConfig, section: String): Int {
        return config.get("$section.wearout", ConfigDataType.INT, 1, false)
    }
}