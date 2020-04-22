package me.syari.ss.tool.item.attachment.melee

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.tool.item.attachment.AttachmentLoader.getWearOut
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class MeleeAttachment(
    val wearOut: Int
) {
    fun damage(attacker: Player, victim: LivingEntity) {
        victim.damage(0.0, attacker)
    }

    companion object {
        fun load(config: CustomConfig): MeleeAttachment? {
            return if (config.contains("melee")) {
                MeleeAttachment(
                    getWearOut(config, "melee")
                )
            } else null
        }
    }
}