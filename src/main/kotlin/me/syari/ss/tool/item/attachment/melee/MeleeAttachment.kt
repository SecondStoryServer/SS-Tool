package me.syari.ss.tool.item.attachment.melee

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.tool.item.attachment.base.Attachment
import me.syari.ss.tool.item.attachment.base.AttachmentLoader
import org.bukkit.entity.LivingEntity

class MeleeAttachment(
    override val wearOut: Int
) : Attachment {
    fun damage(entity: LivingEntity) {

    }

    companion object Loader : AttachmentLoader {
        override fun get(config: CustomConfig, section: String): Attachment? {
            return MeleeAttachment(0)
        }
    }
}