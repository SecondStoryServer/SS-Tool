package me.syari.ss.gun.item.attachment.gun

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.core.config.dataType.ConfigDataType
import me.syari.ss.core.message.Message.action
import me.syari.ss.gun.Main.Companion.gunPlugin
import me.syari.ss.gun.item.SSGunItem
import me.syari.ss.gun.item.attachment.GunAction
import me.syari.ss.gun.item.attachment.base.Attachment
import me.syari.ss.gun.item.attachment.base.AttachmentLoader
import me.syari.ss.gun.item.attachment.gun.option.*
import me.syari.ss.gun.item.attachment.gun.option.AmmoOption.Companion.getAmmoOption
import me.syari.ss.gun.item.attachment.gun.option.BulletOption.Companion.getBulletOption
import me.syari.ss.gun.item.attachment.gun.option.HitOption.Companion.getBaseHitOption
import me.syari.ss.gun.item.attachment.gun.option.HitOption.Companion.getCritHitOption
import me.syari.ss.gun.item.attachment.gun.option.HitOption.Companion.getHeadHitOption
import me.syari.ss.gun.item.attachment.gun.option.RecoilOption.Companion.getRecoilOption
import me.syari.ss.gun.item.attachment.gun.option.ReloadOption.Companion.getReloadOption
import me.syari.ss.gun.item.attachment.gun.option.ScopeOption.Companion.getScopeOption
import me.syari.ss.gun.item.attachment.gun.option.ShotOption.Companion.getShotOption
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
    fun shoot(player: Player, cursor: Cursor, ssGunItem: SSGunItem): Boolean {
        val isScope = ScopeOption.isUseScope(player)
        val item = ssGunItem.item
        if (!shotOption.canShoot(player, isScope, item)) {
            return false
        }
        if (ammoOption.isTimingShot && !ammoOption.canConsume(player)) {
            player.action(Message.NoAmmo.message)
            return false
        }
        val lastBullet = getBullet(ssGunItem, cursor)
        val useBullet = bulletOption.burstAmount
        if (lastBullet < useBullet) {
            player.action(Message.NoBullet.message)
            reloadOption.sound.empty?.play(player)
            return false
        } else {
            reloadOption.setBullet(item, cursor, lastBullet - useBullet)
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
        ssGunItem.updateDisplayName()
        return true
    }

    fun reload(player: Player, cursor: Cursor, ssGunItem: SSGunItem) {
        if (ammoOption.isTimingReload && !ammoOption.canConsume(player)) {
            return player.action(Message.NoAmmo.message)
        }
        ammoOption.consume(player)
        reloadOption.reload(player, cursor, ssGunItem.item)
    }

    fun changeScope(player: Player, delta: Int) {
        scopeOption.changeScope(player, delta)
    }

    fun setBullet(ssGunItem: SSGunItem, cursor: Cursor, bullet: Int) {
        reloadOption.setBullet(ssGunItem.item, cursor, bullet)
    }

    fun getBullet(ssGunItem: SSGunItem, cursor: Cursor): Int {
        return reloadOption.getBullet(ssGunItem.item, cursor)
    }

    enum class Cursor(val internalId: String, val dependencyAction: GunAction) {
        Right("right", GunAction.ShootRight),
        Left("left", GunAction.ShootLeft)
    }

    companion object {
        const val gunCursorPersistentKey = "ss-gun-cursor"

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

        fun getCursor(ssGunItem: SSGunItem): Cursor? {
            val id = ssGunItem.item.getPersistentData(gunPlugin)?.get(gunCursorPersistentKey, PersistentDataType.STRING)
            return Cursor.values().firstOrNull { it.internalId == id }
        }

        fun setCursor(ssGunItem: SSGunItem, cursor: Cursor?) {
            ssGunItem.item.editPersistentData(gunPlugin) {
                set(gunCursorPersistentKey, PersistentDataType.STRING, cursor?.internalId)
            }
        }

        fun getAttachment(ssGunItem: SSGunItem, cursor: Cursor): GunAttachment? {
            return ssGunItem.gun.attachments[cursor.dependencyAction] as? GunAttachment
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