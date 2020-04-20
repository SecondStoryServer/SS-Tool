package me.syari.ss.tool.item.attachment.gun

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.core.config.dataType.ConfigDataType
import me.syari.ss.core.message.Message.action
import me.syari.ss.tool.Main.Companion.toolPlugin
import me.syari.ss.tool.item.SSTool
import me.syari.ss.tool.item.attachment.ToolAction
import me.syari.ss.tool.item.attachment.base.Attachment
import me.syari.ss.tool.item.attachment.base.AttachmentLoader
import me.syari.ss.tool.item.attachment.gun.option.*
import me.syari.ss.tool.item.attachment.gun.option.AmmoOption.Companion.getAmmoOption
import me.syari.ss.tool.item.attachment.gun.option.BulletOption.Companion.getBulletOption
import me.syari.ss.tool.item.attachment.gun.option.HitOption.Companion.getBaseHitOption
import me.syari.ss.tool.item.attachment.gun.option.HitOption.Companion.getCritHitOption
import me.syari.ss.tool.item.attachment.gun.option.HitOption.Companion.getHeadHitOption
import me.syari.ss.tool.item.attachment.gun.option.RecoilOption.Companion.getRecoilOption
import me.syari.ss.tool.item.attachment.gun.option.ReloadOption.Companion.getReloadOption
import me.syari.ss.tool.item.attachment.gun.option.ScopeOption.Companion.getScopeOption
import me.syari.ss.tool.item.attachment.gun.option.ShotOption.Companion.getShotOption
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import kotlin.random.Random

class GunAttachment(
    override val wearOut: Int,
    private val bulletOption: BulletOption,
    private val shotOption: ShotOption,
    private val hitOptionBase: HitOption.Base,
    private val hitOptionHeadShot: HitOption.HeadShot,
    private val hitOptionCritical: HitOption.Critical,
    private val reloadOption: ReloadOption,
    private val ammoOption: AmmoOption,
    private val recoilOption: RecoilOption,
    private val scopeOption: ScopeOption
) : Attachment {
    fun shoot(player: Player, cursor: Cursor, ssTool: SSTool): Boolean {
        val isScope = ScopeOption.isUseScope(player)
        val item = ssTool.item
        if (!shotOption.canShoot(player, isScope, item)) {
            return false
        }
        if (ammoOption.isTimingShot && !ammoOption.canConsume(player)) {
            player.action(Message.NoAmmo.message)
            return false
        }
        val lastBullet = getBullet(ssTool, cursor)
        val useBullet = bulletOption.burstAmount
        if (lastBullet < useBullet) {
            player.action(Message.NoBullet.message)
            reloadOption.sound.empty?.play(player)
            return false
        } else {
            reloadOption.setBullet(ssTool, cursor, lastBullet - useBullet)
        }
        val isSneak = player.isSneaking
        bulletOption.shoot(player, isSneak, isScope) { victim, bullet, isHeadShot ->
            var damage = hitOptionBase.damage
            hitOptionBase.runEvent(player, victim)
            if (isHeadShot) {
                hitOptionHeadShot.runEvent(player, victim)
                damage *= hitOptionHeadShot.damage
            }
            if (Random.nextFloat() < hitOptionCritical.chance) {
                damage *= hitOptionCritical.damage
                hitOptionCritical.runEvent(player, victim)
            }
            victim.damage(damage.toDouble(), bullet)
        }
        shotOption.shoot(player, item)
        ammoOption.consume(player)
        recoilOption.recoil(player)
        return true
    }

    fun reload(player: Player, cursor: Cursor, ssTool: SSTool) {
        if (ammoOption.isTimingReload) {
            if (!ammoOption.canConsume(player)) {
                return player.action(Message.NoAmmo.message)
            }
            ammoOption.consume(player)
        }
        reloadOption.reload(player, cursor, ssTool)
    }

    fun scope(player: Player) {
        scopeOption.scope(player)
    }

    fun setBullet(ssTool: SSTool, cursor: Cursor, bullet: Int) {
        reloadOption.setBullet(ssTool, cursor, bullet)
    }

    fun getBullet(ssTool: SSTool, cursor: Cursor): Int {
        return reloadOption.getBullet(ssTool, cursor)
    }

    fun getMaxBullet(): Int {
        return reloadOption.maxBullet
    }

    enum class Cursor(val internalId: String, val dependencyAction: ToolAction) {
        Right("right", ToolAction.ShootRight),
        Left("left", ToolAction.ShootLeft)
    }

    companion object {
        const val gunCursorPersistentKey = "ss-tool-gun-cursor"

        fun loadMessage(config: CustomConfig, section: String) {
            config.with {
                var editNum = 0
                Message.values().forEach {
                    val path = "$section.${it.configPath}"
                    val getValue = get(path, ConfigDataType.STRING, false)
                    if (getValue != null) {
                        it.message = getValue
                    } else {
                        set(path, it.message)
                        editNum ++
                    }
                }
                if(0 < editNum) {
                    save()
                }
            }
        }

        internal enum class Message(val configPath: String, var message: String) {
            NoAmmo("noammo", "&4弾薬がありません"),
            NoScope("noscope", "&4スコープを覗かなければ撃てません"),
            NoBullet("nobullet", "&4銃弾がありません"),
            BrokenGun("brokengun", "&4銃が壊れています")
        }

        fun getCursor(ssTool: SSTool): Cursor? {
            val id =
                ssTool.item.getPersistentData(toolPlugin)?.get(gunCursorPersistentKey, PersistentDataType.STRING)
            return Cursor.values().firstOrNull { it.internalId == id }
        }

        fun setCursor(ssTool: SSTool, cursor: Cursor?) {
            ssTool.item.editPersistentData(toolPlugin) {
                set(gunCursorPersistentKey, PersistentDataType.STRING, cursor?.internalId)
            }
        }

        fun getAttachment(ssTool: SSTool, cursor: Cursor): GunAttachment? {
            return ssTool.data.attachments[cursor.dependencyAction] as? GunAttachment
        }
    }

    object Loader : AttachmentLoader {
        override fun get(config: CustomConfig, section: String): GunAttachment? {
            val wearOut = config.get("$section.wearout", ConfigDataType.INT, 1, false)
            return GunAttachment(
                wearOut,
                getBulletOption(config, "$section.bullet"),
                getShotOption(config, "$section.shot"),
                getBaseHitOption(config, "$section.hit", true),
                getHeadHitOption(config, "$section.hit.head"),
                getCritHitOption(config, "$section.hit.crit"),
                getReloadOption(config, "$section.reload"),
                getAmmoOption(config, "$section.ammo"),
                getRecoilOption(config, "$section.recoil"),
                getScopeOption(config, "$section.scope")
            )
        }
    }
}