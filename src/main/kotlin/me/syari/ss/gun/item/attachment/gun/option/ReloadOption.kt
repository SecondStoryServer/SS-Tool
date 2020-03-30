package me.syari.ss.gun.item.attachment.gun.option

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.core.config.dataType.ConfigDataType
import me.syari.ss.core.sound.CustomSoundList

data class ReloadOption(
    val maxBullet: Int,
    val onceBullet: Int,
    val duration: Int,
    val delay: Int,
    val bar: Bar,
    val sound: Sound
) {
    data class Bar(
        val style: String,
        val left: String,
        val right: String,
        val amount: Int,
        val endMessage: String
    )

    data class Sound(
        val empty: CustomSoundList?,
        val begin: CustomSoundList?,
        val end: CustomSoundList?
    )

    companion object {
        private val default = ReloadOption(
            0,
            0,
            1,
            0,
            Bar("\${bar} &6\${time}", "&c|", "&7|", 30, "&6Reloaded"),
            Sound(null, null, null)
        )

        fun getReloadOption(config: CustomConfig, section: String): ReloadOption {
            if (!config.contains(section)) return default
            var maxBullet: Int? = null
            var onceBullet: Int? = null
            if(config.contains("$section.bullet")){
                maxBullet = config.get("$section.bullet.max", ConfigDataType.INT, true)
                onceBullet = config.get("$section.bullet.once", ConfigDataType.INT, false)
            }
            if(maxBullet == null || maxBullet < 0) maxBullet = default.maxBullet
            if(onceBullet == null || onceBullet < 0) onceBullet = default.onceBullet
            val bar = if(config.contains("$section.bar")){
                with(default.bar){
                    Bar(
                        config.get("$section.bar.style", ConfigDataType.STRING, style, false),
                        config.get("$section.bar.left", ConfigDataType.STRING, left, false),
                        config.get("$section.bar.right", ConfigDataType.STRING, right, false),
                        config.get("$section.bar.amount", ConfigDataType.INT, amount, false),
                        config.get("$section.bar.end", ConfigDataType.STRING, endMessage, false)
                    )
                }
            } else {
                default.bar
            }
            val sound = if(config.contains("$section.sound")){
                Sound(
                    config.get("$section.sound.empty", ConfigDataType.SOUND, false),
                    config.get("$section.sound.begin", ConfigDataType.SOUND, false),
                    config.get("$section.sound.end", ConfigDataType.SOUND, false)
                )
            } else {
                default.sound
            }
            return ReloadOption(
                maxBullet,
                onceBullet,
                config.get("$section.duration", ConfigDataType.INT, default.duration, true),
                config.get("$section.delay", ConfigDataType.INT, default.delay, false),
                bar,
                sound
            )
        }
    }
}