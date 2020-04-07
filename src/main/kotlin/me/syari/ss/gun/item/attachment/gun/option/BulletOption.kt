package me.syari.ss.gun.item.attachment.gun.option

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.core.config.dataType.ConfigDataType
import me.syari.ss.core.particle.CustomParticleList
import me.syari.ss.core.scheduler.CustomScheduler.runLater
import me.syari.ss.core.scheduler.CustomScheduler.runRepeatTimes
import me.syari.ss.core.scheduler.CustomScheduler.runTimer
import me.syari.ss.core.scheduler.CustomTask
import me.syari.ss.gun.Main.Companion.gunPlugin
import me.syari.ss.gun.item.attachment.gun.option.value.SneakOption
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class BulletOption(
    val type: Type,
    val burstAmount: Int,
    val burstDelay: Int,
    val bulletVector: BulletVector,
    val spread: SneakOption.IntValue,
    val scopeSpread: Float,
    val remove: Int,
    val particleList: CustomParticleList?
) {
    fun shoot(player: Player, isSneak: Boolean, isScope: Boolean, hitEvent: (LivingEntity, Entity, Boolean) -> Unit) {
        data class RandomSpread(private val spread: Float) {
            fun generate() = (Random.nextFloat() - Random.nextFloat()) * spread
        }

        fun getSpread(isSneak: Boolean, isScope: Boolean): RandomSpread {
            return RandomSpread(spread.get(isSneak).toFloat() * (if (isScope) scopeSpread else 1F) * 0.1F)
        }

        fun getProjectileLocation(): Location {
            val playerYaw = (player.location.yaw + 180) * Math.PI / 180.0
            val vector = Vector(cos(playerYaw), 0.0, sin(playerYaw)).multiply(0.2)
            return player.eyeLocation.toVector().add(vector).toLocation(player.world)
        }

        val spread = getSpread(isSneak, isScope)
        val yaw = Math.toRadians((-player.location.yaw - 90F).toDouble())
        val pitch = Math.toRadians((-player.location.pitch).toDouble())
        val x = cos(pitch) * cos(yaw)
        val y = sin(pitch)
        val z = -sin(yaw) * cos(pitch)
        val projectileLocation = getProjectileLocation()

        runRepeatTimes(gunPlugin, burstDelay.toLong(), burstAmount) {
            val direction = Vector(x + spread.generate(), y + spread.generate(), z + spread.generate())
            val bullet = type.spawn(projectileLocation, player)
            var speed = bulletVector.speed
            val up = bulletVector.up
            val gravity = bulletVector.gravity
            bullet.velocity = direction.clone().multiply(speed)
            val taskList = mutableSetOf<CustomTask>()
            runLater(gunPlugin, remove.toLong()) {
                bullet.remove()
                taskList.forEach { it.cancel() }
            }?.let { taskList.add(it) }
            if (up != 0F || gravity != 0F) {
                runTimer(gunPlugin, 1) {
                    speed += up
                    bullet.velocity = direction
                        .add(Vector(0F, -gravity, 0F))
                        .clone().multiply(speed)
                }?.let { taskList.add(it) }
            }
            if (particleList != null) {
                var period = particleList.getRequireTime()
                if (period < 1L) period = 1
                runTimer(gunPlugin, period) {
                    particleList.spawn(bullet)
                }?.let { taskList.add(it) }
            }
            runTimer(gunPlugin, 1) {
                if (bullet.isOnGround) {
                    bullet.remove()
                }
                bullet.getNearbyEntities(0.7, 1.0, 0.7)
                    .filterIsInstance<LivingEntity>()
                    .firstOrNull { it != player }?.let { victim ->
                        val isHeadShot = victim.eyeLocation.y - bullet.location.y in -0.5..0.5
                        hitEvent.invoke(victim, bullet, isHeadShot)
                        bullet.remove()
                    }
                if (bullet.isValid) {
                    taskList.forEach { it.cancel() }
                    return@runTimer
                }
            }?.let { taskList.add(it) }
        }
    }

    sealed class Type(private val entityType: EntityType) {
        class Snow(private val fire: Boolean) : Type(EntityType.SNOWBALL){
            override fun spawn(location: Location, source: Player): Entity {
                return super.spawn(location, source).apply {
                    if(this is Projectile){
                        shooter = source
                    }
                    if(fire) fireTicks = 6000
                }
            }
        }

        class Arrow(private val fire: Boolean) : Type(EntityType.ARROW){
            override fun spawn(location: Location, source: Player): Entity {
                return super.spawn(location, source).apply {
                    if(this is Projectile){
                        shooter = source
                    }
                    if(fire) fireTicks = 6000
                }
            }
        }

        class Item(private val material: Material) : Type(EntityType.DROPPED_ITEM){
            override fun spawn(location: Location, source: Player): Entity {
                return super.spawn(location, source).apply {
                    if(this is org.bukkit.entity.Item){
                        setItemStack(ItemStack(material))
                        setCanMobPickup(false)
                        pickupDelay = Int.MAX_VALUE
                    }
                }
            }
        }

        object None : Type(EntityType.BAT){
            override fun spawn(location: Location, source: Player): Entity {
                return super.spawn(location, source).apply {
                    if(this is LivingEntity) {
                        setAI(false)
                        isSilent = true
                        PotionEffect(PotionEffectType.INVISIBILITY, 6000, 1, false, false)
                            .apply(this)
                    }
                }
            }
        }

        open fun spawn(location: Location, source: Player): Entity {
            return location.world.spawnEntity(location, entityType).apply {
                setMetadata(sourcePlayerMetaDataKey, FixedMetadataValue(gunPlugin, source))
            }
        }

        companion object {
            fun fromString(value: String?): Type? {
                val splitValue = value?.split("-") ?: return null
                return when (splitValue[0].toLowerCase()) {
                    "snow" -> Snow(
                        splitValue.getOrNull(1)?.toLowerCase() == "fire"
                    )
                    "arrow" -> Arrow(
                        splitValue.getOrNull(1)?.toLowerCase() == "fire"
                    )
                    "item" -> Item(
                        splitValue.getOrNull(1)?.toUpperCase()?.let {
                            Material.getMaterial(it)
                        } ?: Material.STONE)
                    "none" -> None
                    else -> null
                }
            }
        }
    }

    data class BulletVector(
        val speed: Float,
        val up: Float,
        val gravity: Float
    )

    companion object {
        private val default = BulletOption(
            Type.Snow(false),
            1,
            0,
            BulletVector(30F, 0F, 0F),
            SneakOption.IntValue(),
            1F,
            100,
            null
        )

        fun getBulletOption(config: CustomConfig, section: String): BulletOption {
            if (!config.contains(section)) return default
            var burstAmount = config.get("$section.burst.amount", ConfigDataType.INT, false)
            var burstDelay = default.burstDelay
            if (burstAmount != null && 1 < burstAmount) {
                config.get("$section.burst.delay", ConfigDataType.INT, false)?.let {
                    burstDelay = it
                }
            } else {
                burstAmount = default.burstAmount
            }
            val spread = SneakOption.IntValue.getFromConfig(config, "$section.spread", default.spread)
            val scopeSpread = config.get("$section.spread.scope", ConfigDataType.FLOAT, 1.0F, false)
            val bulletVector = if (config.contains("$section.vector")) {
                with(default.bulletVector) {
                    BulletVector(
                        config.get("$section.vector.speed", ConfigDataType.FLOAT, speed, false),
                        config.get("$section.vector.up", ConfigDataType.FLOAT, up, false),
                        config.get("$section.vector.gravity", ConfigDataType.FLOAT, gravity, false)
                    )
                }
            } else {
                default.bulletVector
            }
            return with(default){
                BulletOption(
                    Type.fromString(config.get("$section.type", ConfigDataType.STRING, false)) ?: type,
                    burstAmount,
                    burstDelay,
                    bulletVector,
                    spread,
                    scopeSpread,
                    config.get("$section.remove", ConfigDataType.INT, remove, false),
                    config.get("$section.particle", ConfigDataType.PARTICLE, false)
                )
            }
        }

        private const val sourcePlayerMetaDataKey = "ss-gun-source-player"
    }
}