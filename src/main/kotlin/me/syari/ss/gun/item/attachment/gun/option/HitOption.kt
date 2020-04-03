package me.syari.ss.gun.item.attachment.gun.option

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.core.config.dataType.ConfigDataType
import me.syari.ss.core.particle.CustomParticleList
import me.syari.ss.core.sound.CustomSoundList
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect

sealed class HitOption(
    val damage: Float,
    val particle: Particle,
    val sound: Sound,
    val potion: Potion
) {
    fun runEvent(shooter: Player, victim: LivingEntity) {
        particle.shooter?.spawn(shooter)
        particle.victim?.spawn(victim)
        sound.shooter?.play(shooter)
        sound.victim?.play(victim)
        potion.shooter?.forEach { it.apply(shooter) }
        potion.victim?.forEach { it.apply(victim) }
    }

    class Base(
        damage: Float,
        particle: Particle,
        sound: Sound,
        potion: Potion
    ) : HitOption(
        damage,
        particle,
        sound,
        potion
    )

    class HeadShot(
        damage: Float,
        particle: Particle,
        sound: Sound,
        potion: Potion
    ) : HitOption(
        damage,
        particle,
        sound,
        potion
    )

    class Critical(
        damage: Float,
        val chance: Float,
        particle: Particle,
        sound: Sound,
        potion: Potion
    ) : HitOption(
        damage,
        particle,
        sound,
        potion
    )

    data class Particle(
        val shooter: CustomParticleList?,
        val victim: CustomParticleList?
    )

    data class Sound(
        val shooter: CustomSoundList?,
        val victim: CustomSoundList?
    )

    data class Potion(
        val shooter: List<PotionEffect>?,
        val victim: List<PotionEffect>?
    )

    companion object {
        fun getBaseHitOption(config: CustomConfig, section: String): Base {
            return Base(
                config.get("$section.damage", ConfigDataType.FLOAT, 1F, true),
                if (config.contains("$section.particle"))
                    Particle(
                        config.get("$section.particle.shooter", ConfigDataType.PARTICLE, false),
                        config.get("$section.particle.victim", ConfigDataType.PARTICLE, false)
                    )
                else Particle(null, null),
                if (config.contains("$section.sound"))
                    Sound(
                        config.get("$section.sound.shooter", ConfigDataType.SOUND, false),
                        config.get("$section.sound.victim", ConfigDataType.SOUND, false)
                    )
                else Sound(null, null),
                if (config.contains("$section.potion"))
                    Potion(
                        config.get("$section.potion.shooter", ConfigDataType.POTION, false),
                        config.get("$section.potion.victim", ConfigDataType.POTION, false)
                    )
                else Potion(null, null)
            )
        }

        fun getHeadHitOption(config: CustomConfig, section: String): HeadShot {
            return with(getBaseHitOption(config, section)) {
                HeadShot(
                    damage,
                    particle,
                    sound,
                    potion
                )
            }
        }

        fun getCritHitOption(config: CustomConfig, section: String): Critical {
            return with(getBaseHitOption(config, section)) {
                Critical(
                    damage,
                    if (damage != 1F) config.get("$section.chance", ConfigDataType.FLOAT, 0.0F, true) else 0.0F,
                    particle,
                    sound,
                    potion
                )
            }
        }
    }
}