package me.syari.ss.gun.item.attachment.gun.option

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.core.config.dataType.ConfigDataType
import me.syari.ss.core.item.CustomItemStack
import me.syari.ss.core.message.Message.action
import me.syari.ss.core.player.UUIDPlayer
import me.syari.ss.core.scheduler.CustomScheduler.runLater
import me.syari.ss.core.scheduler.CustomScheduler.runRepeatTimes
import me.syari.ss.core.scheduler.CustomTask
import me.syari.ss.core.sound.CustomSoundList
import me.syari.ss.gun.Main.Companion.gunPlugin
import me.syari.ss.gun.item.attachment.gun.GunAttachment
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

data class ReloadOption(
    val maxBullet: Int,
    val onceBullet: Int,
    val duration: Int,
    val delay: Int,
    val bar: Bar,
    val sound: Sound
) {
    fun reload(player: Player, cursor: GunAttachment.Cursor, item: CustomItemStack) {
        val lastBullet = getBullet(item, cursor)
        val maxBullet = maxBullet
        if (lastBullet < maxBullet) {
            fun endReload() {
                var bullet = lastBullet + onceBullet
                if (maxBullet < bullet) bullet = maxBullet
                setBullet(item, cursor, bullet)
                sound.end?.play(player)
                val endMessage = bar.endMessage
                if (endMessage.isNotEmpty()) player.action(endMessage)
            }

            sound.begin?.play(player)
            val style = bar.style
            if (style.isNotEmpty()) {
                runRepeatTimes(gunPlugin, 5, duration / 5, duration % 5L) {
                    val barBuilder = StringBuilder()
                    val leftAmount = (duration - repeatRemain) / bar.amount
                    for (i in 0 until leftAmount) {
                        barBuilder.append(bar.left)
                    }
                    val rightAmount = bar.amount - leftAmount
                    for (i in 0 until rightAmount) {
                        barBuilder.append(bar.right)
                    }
                    style
                        .replace("\${bar}", barBuilder.toString())
                        .replace("\${time}", String.format("%.1f", repeatRemain / 20F))
                }?.onEndRepeat {
                    endReload()
                }
            } else {
                runLater(gunPlugin, duration.toLong()) {
                    endReload()
                }
            }?.let { setReloadTask(player, it) }
        } else if (maxBullet < lastBullet) {
            setBullet(item, cursor, maxBullet)
        }
    }

    fun getBullet(item: CustomItemStack, cursor: GunAttachment.Cursor): Int {
        return item.getPersistentData(gunPlugin)?.get(getBulletPersistentKey(cursor), PersistentDataType.INTEGER)
            ?: maxBullet
    }

    fun setBullet(item: CustomItemStack, cursor: GunAttachment.Cursor, bullet: Int) {
        item.editPersistentData(gunPlugin) {
            set(getBulletPersistentKey(cursor), PersistentDataType.INTEGER, bullet)
        }
    }

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

        fun getBulletPersistentKey(cursor: GunAttachment.Cursor): String {
            return "ss-gun-bullet-" + cursor.internalId
        }

        private val reloadTask = mutableMapOf<UUIDPlayer, CustomTask>()

        private fun setReloadTask(player: Player, task: CustomTask) {
            reloadTask[UUIDPlayer(player)] = task
        }

        fun cancelReload(player: Player) {
            reloadTask.remove(UUIDPlayer(player))?.cancel()
        }
    }
}