package me.syari.ss.gun.item.attachment

import kotlin.reflect.KClass

enum class GunAction(val configSection: String, val attachment: KClass<out Attachment>) {
    ShootRight("right.shoot", GunAttachment::class),
    ShootLeft("left.shoot", GunAttachment::class),
    Melee("left.melee", MeleeAttachment::class),
    ShieldRight("right.shield", ShieldAttachment::class),
    ShieldLeft("left.shield", ShieldAttachment::class)
}