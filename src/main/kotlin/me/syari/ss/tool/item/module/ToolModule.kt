package me.syari.ss.tool.item.module

import me.syari.ss.core.item.CustomItemStack
import org.bukkit.ChatColor
import org.bukkit.Material

interface ToolModule<T : ToolModule.Data> {
    val type: ModuleType

    fun from(splitData: List<String>): T?

    fun to(data: T): List<String>

    interface Data {
        val useSlot: Int
        val type: Material
        val color: ChatColor
        val name: String
        val description: List<String>
        val apply: ((CustomItemStack) -> Unit)?

        val toItemStack
            get() = CustomItemStack.create(
                type,
                "&b[Module] &${color.char}$name",
                "&7使用枠: &6$useSlot",
                "",
                *description.toTypedArray()
            ).apply { apply?.invoke(this) }
    }
}