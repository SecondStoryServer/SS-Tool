package me.syari.ss.gun.item.attachment

import me.syari.ss.core.config.CustomConfig

interface Attachment {
    fun get(config: CustomConfig, section: String): Attachment? = null
}