package me.syari.ss.tool

import me.syari.ss.core.auto.OnEnable
import me.syari.ss.core.command.create.CreateCommand.createCommand
import me.syari.ss.core.command.create.CreateCommand.element
import me.syari.ss.core.command.create.CreateCommand.onlinePlayers
import me.syari.ss.core.command.create.CreateCommand.tab
import me.syari.ss.core.command.create.ErrorMessage
import me.syari.ss.tool.ConfigLoader.loadConfig
import me.syari.ss.tool.ConfigLoader.loadMessage
import me.syari.ss.tool.Main.Companion.toolPlugin
import me.syari.ss.tool.item.SSTool
import me.syari.ss.tool.item.SSTool.Companion.getDurability
import me.syari.ss.tool.item.SSToolData
import me.syari.ss.tool.item.SSToolData.Companion.getToolId
import me.syari.ss.tool.item.attachment.gun.GunAttachment
import me.syari.ss.tool.item.attachment.gun.GunAttachment.Companion.getCursor
import org.bukkit.entity.Player

object CommandCreator : OnEnable {
    override fun onEnable() {
        createCommand(toolPlugin, "tool", "Tool",
            tab { _, _ -> element("item", "get", "give", "reload") },
            tab("give") { _, _ -> onlinePlayers },
            tab("get", "give *") { _, _ -> element(SSToolData.idList) },
            tab("reload") { _, _ -> element("item", "message") },
            tab("item") { _, _ -> element("info", "bullet", "durability") },
            tab("item bullet", "item durability") { _, _ -> element("set", "inc", "dec") },
            tab("item bullet set", "item durability set") { _, _ -> element("max") }
        ) { sender, args ->
            when (args.whenIndex(0)) {
                "item" -> {
                    if (sender !is Player) return@createCommand sendError(ErrorMessage.OnlyPlayer)
                    val ssToolItem = SSTool.from(sender.inventory.itemInMainHand)
                        ?: return@createCommand sendWithPrefix("&c銃を持っていません")
                    val item = ssToolItem.item
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
                                getCursor(ssToolItem)?.let {
                                    appendln("&7- &6Cursor: &7${it.internalId}")
                                }
                            })
                        }
                        "durability" -> {
                            when (args.whenIndex(2)) {
                                "set" -> {
                                    val durability = args.getOrNull(3)?.let {
                                        if (it.toLowerCase() == "max") ssToolItem.data.maxDurability
                                        else it.toIntOrNull()
                                    } ?: return@createCommand sendWithPrefix("&c設定する耐久を入力してください")
                                    ssToolItem.durability = durability
                                    ssToolItem.updateDurability()
                                }
                                "inc" -> {
                                    val durability = args.getOrNull(3)?.toIntOrNull()
                                        ?: return@createCommand sendWithPrefix("&c加算する耐久を入力してください")
                                    ssToolItem.durability += durability
                                    ssToolItem.updateDurability()
                                }
                                "dec" -> {
                                    val durability = args.getOrNull(3)?.toIntOrNull()
                                        ?: return@createCommand sendWithPrefix("&c減算する耐久を入力してください")
                                    ssToolItem.durability -= durability
                                    ssToolItem.updateDurability()
                                }
                                else -> sendHelp(
                                    "tool item durability set" to "アイテムの耐久を設定します",
                                    "tool item durability inc" to "アイテムの耐久を加算します",
                                    "tool item durability dec" to "アイテムの耐久を減算します"
                                )
                            }
                        }
                        "bullet" -> {
                            val cursor = getCursor(ssToolItem)
                                ?: return@createCommand sendWithPrefix("&c銃弾が選択されていません")
                            val gunAttachment = ssToolItem.data.attachments[cursor.dependencyAction] as? GunAttachment
                                ?: return@createCommand sendWithPrefix("&c銃が取得できませんでした")
                            when (args.whenIndex(2)) {
                                "set" -> {
                                    val bullet = args.getOrNull(3)?.let {
                                        if (it.toLowerCase() == "max") gunAttachment.getMaxBullet()
                                        else it.toIntOrNull()
                                    } ?: return@createCommand sendWithPrefix("&c設定する弾数を入力してください")
                                    gunAttachment.setBullet(ssToolItem, cursor, bullet)
                                    ssToolItem.updateDisplayName()
                                }
                                "inc" -> {
                                    val bullet = args.getOrNull(3)?.toIntOrNull()
                                        ?: return@createCommand sendWithPrefix("&c加算する弾数を入力してください")
                                    val lastBullet = gunAttachment.getBullet(ssToolItem, cursor)
                                    gunAttachment.setBullet(ssToolItem, cursor, lastBullet + bullet)
                                    ssToolItem.updateDisplayName()
                                }
                                "dec" -> {
                                    val bullet = args.getOrNull(3)?.toIntOrNull()
                                        ?: return@createCommand sendWithPrefix("&c減算する弾数を入力してください")
                                    val lastBullet = gunAttachment.getBullet(ssToolItem, cursor)
                                    gunAttachment.setBullet(ssToolItem, cursor, lastBullet - bullet)
                                    ssToolItem.updateDisplayName()
                                }
                                else -> sendHelp(
                                    "tool item bullet set" to "銃の弾数を設定します",
                                    "tool item bullet inc" to "銃の弾数を加算します",
                                    "tool item bullet dec" to "銃の弾数を減算します"
                                )
                            }
                        }
                        else -> sendHelp(
                            "tool item info" to "銃の情報を表示します",
                            "tool item durability" to "銃の耐久を変更します",
                            "tool item bullet" to "銃の弾数を変更します"
                        )
                    }
                }
                "get" -> {
                    if (sender !is Player) return@createCommand sendError(ErrorMessage.OnlyPlayer)
                    val id = args.getOrNull(1) ?: return@createCommand sendWithPrefix("&c銃のIDを入力してください")
                    val ssToolItem = SSTool.get(id) ?: return@createCommand sendWithPrefix("&c存在しないIDです")
                    ssToolItem.give(sender)
                }
                "give" -> {
                    val player = args.getPlayer(1, false) ?: return@createCommand
                    val id = args.getOrNull(2) ?: return@createCommand sendWithPrefix("&c銃のIDを入力してください")
                    val ssToolItem = SSTool.get(id) ?: return@createCommand sendWithPrefix("&c存在しないIDです")
                    ssToolItem.give(player)
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