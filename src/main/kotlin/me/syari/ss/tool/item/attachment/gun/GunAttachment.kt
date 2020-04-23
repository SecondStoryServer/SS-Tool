package me.syari.ss.tool.item.attachment.gun

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.core.config.dataType.ConfigDataType
import me.syari.ss.core.message.Message.action
import me.syari.ss.tool.item.SSTool
import me.syari.ss.tool.item.attachment.AttachmentLoader.getWearOut
import me.syari.ss.tool.item.attachment.ClickAction
import me.syari.ss.tool.item.attachment.ClickAction.Companion.getCursor
import me.syari.ss.tool.item.attachment.ClickAction.Companion.setCursor
import me.syari.ss.tool.item.attachment.ClickType
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
import kotlin.random.Random

class GunAttachment(
    override val clickType: ClickType,
    private val wearOut: Int,
    private val bulletOption: BulletOption,
    private val shotOption: ShotOption,
    private val hitOptionBase: HitOption.Base,
    private val hitOptionHeadShot: HitOption.HeadShot,
    private val hitOptionCritical: HitOption.Critical,
    private val reloadOption: ReloadOption,
    private val ammoOption: AmmoOption,
    private val recoilOption: RecoilOption,
    private val scopeOption: ScopeOption
) : ClickAction {
    override fun getText(ssTool: SSTool): String {
        return reloadOption.getBullet(ssTool).toString()
    }

    override fun click(
        player: Player,
        ssTool: SSTool
    ) {
        val cursor = getCursor(ssTool)
        if (cursor == clickType) {
            val isSuccess = shoot(player, ssTool)
            if (isSuccess) {
                ssTool.durability -= wearOut
            }
        } else if (player.isSneaking) {
            if (cursor != null) scope(player)
        } else {
            ReloadOption.cancelReload(player)
            setCursor(ssTool, clickType)
        }
    }

    override fun drop(player: Player, ssTool: SSTool) {
        reload(player, ssTool)
    }

    private fun shoot(player: Player, ssTool: SSTool): Boolean {
        val isScope = ScopeOption.isUseScope(player)
        val item = ssTool.item
        if (!shotOption.canShoot(player, isScope, item)) {
            return false
        }
        if (ammoOption.isTimingShot && !ammoOption.canConsume(player)) {
            player.action(Message.NoAmmo.message)
            return false
        }
        val lastBullet = reloadOption.getBullet(ssTool)
        val useBullet = bulletOption.burstAmount
        if (lastBullet < useBullet) {
            player.action(Message.NoBullet.message)
            reloadOption.sound.empty?.play(player)
            return false
        } else {
            reloadOption.setBullet(ssTool, lastBullet - useBullet)
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

    private fun reload(player: Player, ssTool: SSTool) {
        if (ammoOption.isTimingReload) {
            if (!ammoOption.canConsume(player)) {
                return player.action(Message.NoAmmo.message)
            }
            ammoOption.consume(player)
        }
        reloadOption.reload(player, ssTool)
    }

    private fun scope(player: Player) {
        scopeOption.scope(player)
    }

    companion object {
        fun loadMessage(config: CustomConfig) {
            config.with {
                var editNum = 0
                Message.values().forEach {
                    val path = "gun.${it.configPath}"
                    val getValue = get(path, ConfigDataType.STRING, false)
                    if (getValue != null) {
                        it.message = getValue
                    } else {
                        set(path, it.message)
                        editNum++
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

        private fun load(config: CustomConfig, clickType: ClickType): GunAttachment? {
            val section = "gun.${clickType.internalId}"
            return if (config.contains(section)) {
                GunAttachment(
                    clickType,
                    getWearOut(config, section),
                    getBulletOption(config, "$section.bullet"),
                    getShotOption(config, "$section.shot"),
                    getBaseHitOption(config, "$section.hit", true),
                    getHeadHitOption(config, "$section.hit.head"),
                    getCritHitOption(config, "$section.hit.crit"),
                    getReloadOption(config, "$section.reload", clickType),
                    getAmmoOption(config, "$section.ammo"),
                    getRecoilOption(config, "$section.recoil"),
                    getScopeOption(config, "$section.scope")
                )
            } else null
        }

        fun loadAll(config: CustomConfig): Map<ClickType, GunAttachment>? {
            return if (config.contains("gun")) {
                mutableMapOf<ClickType, GunAttachment>().also { map ->
                    ClickType.values().forEach { clickType ->
                        load(config, clickType)?.let {
                            map[clickType] = it
                        }
                    }
                }
            } else null
        }
    }
}