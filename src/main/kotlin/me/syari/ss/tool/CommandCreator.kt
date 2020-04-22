package me.syari.ss.tool

import me.syari.ss.core.auto.OnEnable
import me.syari.ss.core.command.create.CreateCommand.createCommand
import me.syari.ss.core.command.create.CreateCommand.element
import me.syari.ss.core.command.create.CreateCommand.onlinePlayers
import me.syari.ss.core.command.create.CreateCommand.tab
import me.syari.ss.core.command.create.ErrorMessage
import me.syari.ss.tool.Main.Companion.toolPlugin
import me.syari.ss.tool.config.ConfigLoader.loadConfig
import me.syari.ss.tool.config.ConfigLoader.loadMessage
import me.syari.ss.tool.item.SSTool
import me.syari.ss.tool.item.SSTool.Companion.getDurability
import me.syari.ss.tool.item.SSToolData
import me.syari.ss.tool.item.SSToolData.Companion.getToolId
import me.syari.ss.tool.item.attachment.ClickAction.Companion.getCursor
import org.bukkit.entity.Player

object CommandCreator : OnEnable {
    override fun onEnable() {
        createCommand(toolPlugin, "tool", "Tool",
            tab { _, _ -> element("item", "get", "give", "reload") },
            tab("give") { _, _ -> onlinePlayers },
            tab("get", "give *") { _, _ -> element(SSToolData.idList) },
            tab("reload") { _, _ -> element("item", "message") },
            tab("item") { _, _ -> element("info", "durability") },
            tab("item durability") { _, _ -> element("set", "inc", "dec") },
            tab("item durability set") { _, _ -> element("max") }
        ) { sender, args ->
            when (args.whenIndex(0)) {
                "item" -> {
                    if (sender !is Player) return@createCommand sendError(ErrorMessage.OnlyPlayer)
                    val ssTool = SSTool.from(sender.inventory.itemInMainHand)
                        ?: return@createCommand sendWithPrefix("&c銃を持っていません")
                    val item = ssTool.item
                    when (args.whenIndex(1)) {
                        "info" -> {
                            sendWithPrefix(buildString {
                                appendln("&fアイテム情報")
                                getToolId(item)?.let {
                                    appendln("&7- &6ID: &7$it")
                                }
                                getDurability(item)?.let {
                                    appendln("&7- &6Durability: &7$it")
                                }
                                getCursor(ssTool)?.let {
                                    appendln("&7- &6Cursor: &7${it.internalId}")
                                }
                            })
                        }
                        "durability" -> {
                            when (args.whenIndex(2)) {
                                "set" -> {
                                    val durability = args.getOrNull(3)?.let {
                                        if (it.toLowerCase() == "max") ssTool.data.maxDurability
                                        else it.toIntOrNull()
                                    } ?: return@createCommand sendWithPrefix("&c設定する耐久を入力してください")
                                    ssTool.durability = durability
                                }
                                "inc" -> {
                                    val durability = args.getOrNull(3)?.toIntOrNull()
                                        ?: return@createCommand sendWithPrefix("&c加算する耐久を入力してください")
                                    ssTool.durability += durability
                                }
                                "dec" -> {
                                    val durability = args.getOrNull(3)?.toIntOrNull()
                                        ?: return@createCommand sendWithPrefix("&c減算する耐久を入力してください")
                                    ssTool.durability -= durability
                                }
                                else -> sendHelp(
                                    "tool item durability set" to "アイテムの耐久を設定します",
                                    "tool item durability inc" to "アイテムの耐久を加算します",
                                    "tool item durability dec" to "アイテムの耐久を減算します"
                                )
                            }
                        }
                        else -> sendHelp(
                            "tool item info" to "銃の情報を表示します",
                            "tool item durability" to "銃の耐久を変更します"
                        )
                    }
                }
                "get" -> {
                    if (sender !is Player) return@createCommand sendError(ErrorMessage.OnlyPlayer)
                    val id = args.getOrNull(1) ?: return@createCommand sendWithPrefix("&c銃のIDを入力してください")
                    val ssTool = SSTool.get(id) ?: return@createCommand sendWithPrefix("&c存在しないIDです")
                    ssTool.give(sender)
                }
                "give" -> {
                    val player = args.getPlayer(1, false) ?: return@createCommand
                    val id = args.getOrNull(2) ?: return@createCommand sendWithPrefix("&c銃のIDを入力してください")
                    val ssTool = SSTool.get(id) ?: return@createCommand sendWithPrefix("&c存在しないIDです")
                    ssTool.give(player)
                }
                "reload" -> {
                    when (args.whenIndex(1)) {
                        "item" -> {
                            sendWithPrefix("&fアイテムをリロードします")
                            loadConfig(sender)
                        }
                        "message" -> {
                            sendWithPrefix("&fメッセージをリロードします")
                            loadMessage(sender)
                        }
                        else -> {
                            sendWithPrefix("&fアイテム・メッセージをリロードします")
                            loadConfig(sender)
                            loadMessage(sender)
                        }
                    }
                }
                else -> sendHelp(
                    "tool get" to "ツールを入手します",
                    "tool give" to "ツールを渡します",
                    "tool item" to "持っているツールをカスタマイズします",
                    "tool reload" to "コンフィグのリロードをします"
                )
            }
        }
    }
}