package me.syari.ss.tool.item.attachment

import me.syari.ss.tool.item.attachment.base.AttachmentLoader
import me.syari.ss.tool.item.attachment.gun.GunAttachment
import me.syari.ss.tool.item.attachment.melee.MeleeAttachment
import me.syari.ss.tool.item.attachment.shield.ShieldAttachment
import kotlin.reflect.KClass

enum class ToolAction(val configSection: String, val attachmentLoader: KClass<out AttachmentLoader>) {
    ShootRight("right.gun", GunAttachment.Loader::class),
    ShootLeft("left.gun", GunAttachment.Loader::class),
    Melee("left.melee", MeleeAttachment.Loader::class),
    ShieldRight("right.shield", ShieldAttachment.Loader::class),
    ShieldLeft("left.shield", ShieldAttachment.Loader::class)
}