package me.syari.ss.gun.item.attachment

import me.syari.ss.gun.item.attachment.base.AttachmentLoader
import me.syari.ss.gun.item.attachment.gun.GunAttachment
import me.syari.ss.gun.item.attachment.melee.MeleeAttachment
import me.syari.ss.gun.item.attachment.shield.ShieldAttachment
import kotlin.reflect.KClass

enum class GunAction(val configSection: String, val attachmentLoader: KClass<out AttachmentLoader>) {
    ShootRight("right.gun", GunAttachment.Loader::class),
    ShootLeft("left.gun", GunAttachment.Loader::class),
    Melee("left.melee", MeleeAttachment.Loader::class),
    ShieldRight("right.shield", ShieldAttachment.Loader::class),
    ShieldLeft("left.shield", ShieldAttachment.Loader::class)
}