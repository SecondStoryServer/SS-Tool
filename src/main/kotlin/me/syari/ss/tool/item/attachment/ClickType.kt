package me.syari.ss.tool.item.attachment

import org.bukkit.event.block.Action

enum class ClickType(val internalId: String) {
    Right("right"),
    Left("left");

    val inverse by lazy {
        when (this) {
            Right -> Left
            Left -> Right
        }
    }

    companion object {
        fun from(action: Action): ClickType? {
            return when (action) {
                Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR -> {
                    Right
                }
                Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> {
                    Left
                }
                else -> null
            }
        }
    }
}