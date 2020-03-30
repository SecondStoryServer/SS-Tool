package me.syari.ss.gun.item.attachment.base

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.gun.item.attachment.base.Attachment

interface AttachmentLoader {
    fun get(config: CustomConfig, section: String): Attachment?
}