package me.syari.ss.gun

import me.syari.ss.core.auto.OnEnable
import me.syari.ss.core.command.create.CreateCommand.createCommand
import me.syari.ss.core.command.create.CreateCommand.element
import me.syari.ss.core.command.create.CreateCommand.onlinePlayers
import me.syari.ss.core.command.create.CreateCommand.tab
import me.syari.ss.core.command.create.ErrorMessage
import me.syari.ss.gun.GunConfig.loadConfig
import me.syari.ss.gun.GunConfig.loadMessage
import me.syari.ss.gun.Main.Companion.gunPlugin
import me.syari.ss.gun.item.SSGun
import me.syari.ss.gun.item.SSGun.Companion.getGunId
import me.syari.ss.gun.item.SSGunItem
import me.syari.ss.gun.item.SSGunItem.Companion.getDurability
import me.syari.ss.gun.item.attachment.gun.GunAttachment
import me.syari.ss.gun.item.attachment.gun.GunAttachment.Companion.getCursor
import org.bukkit.entity.Player

object GunCommand : OnEnable {
    override fun onEnable() {
        createCommand(gunPlugin, "gun", "Gun",
            tab { _, _ -> element("item", "get", "give", "reload") },
            tab("give") { _, _ -> onlinePlayers },
            tab("get", "give *") { _, _ -> element(SSGun.gunIdList) },
            tab("reload") { _, _ -> element("item", "message") },
            tab("item") { _, _ -> element("info", "bullet", "durability") },
            tab("item bullet", "item durability") { _, _ -> element("set", "inc", "dec") }
        ) { sender, args ->
            when (args.whenIndex(0)) {
                "item" -> {
                    if (sender !is Player) return@createCommand sendError(ErrorMessage.OnlyPlayer)
                    val ssGunItem = SSGunItem.from(sender.inventory.itemInMainHand)
                        ?: return@createCommand sendWithPrefix("&c銃を持っていません")
                    val item = ssGunItem.item
                    when (args.whenIndex(1)) {
                        "info" -> {
                            sendWithPrefix(buildString {
                                appendln("&fアイテム情報")
                                getGunId(item)?.let {
                                    appendln("&7- &6ID: &7$it")
                                }
                                getDurability(item)?.let {
                                    appendln("&7- &6Durability: &7$it")
                                }
                                getCursor(ssGunItem)?.let {
                                    appendln("&7- &6Cursor: &7${it.internalId}")
                                }
                            })
                        }
                        "durability" -> {
                            when (args.whenIndex(2)) {
                                "set" -> {
                                    val durability = args.getOrNull(3)?.toIntOrNull()
                                        ?: return@createCommand sendWithPrefix("&c設定する耐久を入力してください")
                                    ssGunItem.durability = durability
                                    ssGunItem.updateDurability()
                                }
                                "inc" -> {
                                    val durability = args.getOrNull(3)?.toIntOrNull()
                                        ?: return@createCommand sendWithPrefix("&c加算する耐久を入力してください")
                                    ssGunItem.durability += durability
                                    ssGunItem.updateDurability()
                                }
                                "dec" -> {
                                    val durability = args.getOrNull(3)?.toIntOrNull()
                                        ?: return@createCommand sendWithPrefix("&c減算する耐久を入力してください")
                                    ssGunItem.durability -= durability
                                    ssGunItem.updateDurability()
                                }
                                else -> sendHelp(
                                    "gun item durability set" to "銃の耐久を設定します",
                                    "gun item durability inc" to "銃の耐久を加算します",
                                    "gun item durability dec" to "銃の耐久を減算します"
                                )
                            }
                        }
                        "bullet" -> {
                            val cursor = getCursor(ssGunItem)
                                ?: return@createCommand sendWithPrefix("&c銃弾が選択されていません")
                            val reloadOption = ssGunItem.gun.attachments[cursor.dependencyAction] as? GunAttachment
                                ?: return@createCommand sendWithPrefix("&c銃が取得できませんでした")
                            when (args.whenIndex(2)) {
                                "set" -> {
                                    val bullet = args.getOrNull(3)?.toIntOrNull()
                                        ?: return@createCommand sendWithPrefix("&c設定する弾数を入力してください")
                                    reloadOption.setBullet(ssGunItem, cursor, bullet)
                                    ssGunItem.updateDisplayName()
                                }
                                "inc" -> {
                                    val bullet = args.getOrNull(3)?.toIntOrNull()
                                        ?: return@createCommand sendWithPrefix("&c加算する弾数を入力してください")
                                    val lastBullet = reloadOption.getBullet(ssGunItem, cursor)
                                    reloadOption.setBullet(ssGunItem, cursor, lastBullet + bullet)
                                    ssGunItem.updateDisplayName()
                                }
                                "dec" -> {
                                    val bullet = args.getOrNull(3)?.toIntOrNull()
                                        ?: return@createCommand sendWithPrefix("&c減算する弾数を入力してください")
                                    val lastBullet = reloadOption.getBullet(ssGunItem, cursor)
                                    reloadOption.setBullet(ssGunItem, cursor, lastBullet - bullet)
                                    ssGunItem.updateDisplayName()
                                }
                                else -> sendHelp(
                                    "gun item bullet set" to "銃の弾数を設定します",
                                    "gun item bullet inc" to "銃の弾数を加算します",
                                    "gun item bullet dec" to "銃の弾数を減算します"
                                )
                            }
                        }
                        else -> sendHelp(
                            "gun item info" to "銃の情報を表示します",
                            "gun item durability" to "銃の耐久を変更します",
                            "gun item bullet" to "銃の弾数を変更します"
                        )
                    }
                }
                "get" -> {
                    if (sender !is Player) return@createCommand sendError(ErrorMessage.OnlyPlayer)
                    val id = args.getOrNull(1) ?: return@createCommand sendWithPrefix("&c銃のIDを入力してください")
                    val ssGunItem = SSGunItem.get(id) ?: return@createCommand sendWithPrefix("&c存在しないIDです")
                    ssGunItem.give(sender)
                }
                "give" -> {
                    val player = args.getPlayer(1, false) ?: return@createCommand
                    val id = args.getOrNull(2) ?: return@createCommand sendWithPrefix("&c銃のIDを入力してください")
                    val ssGunItem = SSGunItem.get(id) ?: return@createCommand sendWithPrefix("&c存在しないIDです")
                    ssGunItem.give(player)
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
                    "gun get" to "銃を入手します",
                    "gun give" to "銃を渡します",
                    "gun item" to "持っている銃をカスタマイズします",
                    "gun reload" to "コンフィグのリロードをします"
                )
            }
        }
    }
}