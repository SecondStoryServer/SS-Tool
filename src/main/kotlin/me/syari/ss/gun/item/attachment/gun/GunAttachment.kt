package me.syari.ss.gun.item.attachment.gun

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.core.config.dataType.ConfigDataType
import me.syari.ss.core.item.CustomItemStack
import me.syari.ss.core.message.Message.action
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
import kotlin.random.Random

class GunAttachment(
    override val wearOut: Int,
    val bulletOption: BulletOption,
    val shotOption: ShotOption,
    val hitOptionBase: HitOption.Base,
    val hitOptionHeadShot: HitOption.HeadShot,
    val hitOptionCritical: HitOption.Critical,
    val reloadOption: ReloadOption,
    val ammoOption: AmmoOption,
    val recoilOption: RecoilOption,
    val scopeOption: ScopeOption
) : Attachment {
    fun shoot(player: Player, item: CustomItemStack) {
        val isScope = ScopeOption.isUseScope(player)
        val isSneak = player.isSneaking
        if (!shotOption.canShoot(player, isScope, item)) return
        if (ammoOption.isTimingShot && !ammoOption.canConsume(player)) return player.action(Message.NoAmmo.message)
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
    }

    companion object {
        fun loadMessage(config: CustomConfig, section: String) {
            config.with {
                var editNum = 0
                Message.values().forEach {
                    val path = "$section.${it.configPath}"
                    val getValue = get(path, ConfigDataType.STRING, false)
                    if(getValue != null){
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
            NoAmmo("noammo", "&c弾薬がありません"),
            NoScope("noscope", "&cスコープを覗かなければ撃てません")
        }
    }

    object Loader : AttachmentLoader {
        override fun get(config: CustomConfig, section: String): Attachment? {
            val wearOut = config.get("$section.wearout", ConfigDataType.INT, 1, false)
            return GunAttachment(
                wearOut,
                getBulletOption(config, "$section.bullet"),
                getShotOption(config, "$section.shot"),
                getBaseHitOption(config, "$section.hit"),
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