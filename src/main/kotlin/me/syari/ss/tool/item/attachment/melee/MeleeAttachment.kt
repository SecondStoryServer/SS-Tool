package me.syari.ss.tool.item.attachment.melee

import me.syari.ss.core.config.CustomConfig
import org.bukkit.entity.LivingEntity

class MeleeAttachment(
    val wearOut: Int
) {
    fun damage(entity: LivingEntity) {

    }

    companion object {
        fun load(config: CustomConfig): MeleeAttachment? {
            return if (config.contains("melee")) {
                MeleeAttachment(0)
            } else null
        }
    }
}