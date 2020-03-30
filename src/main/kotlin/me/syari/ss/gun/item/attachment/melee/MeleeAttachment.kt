package me.syari.ss.gun.item.attachment.melee

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.gun.item.attachment.base.Attachment
import me.syari.ss.gun.item.attachment.base.AttachmentLoader

class MeleeAttachment(
    override val wearOut: Int
) : Attachment {
    companion object Loader: AttachmentLoader {
        override fun get(config: CustomConfig, section: String): Attachment? {
            return MeleeAttachment(0)
        }
    }
}