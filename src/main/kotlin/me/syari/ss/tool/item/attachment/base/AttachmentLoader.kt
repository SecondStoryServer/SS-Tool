package me.syari.ss.tool.item.attachment.base

import me.syari.ss.core.config.CustomConfig

interface AttachmentLoader {
    fun get(config: CustomConfig, section: String): Attachment?
}