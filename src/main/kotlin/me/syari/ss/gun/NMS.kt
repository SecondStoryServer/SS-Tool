package me.syari.ss.gun

import net.minecraft.server.v1_15_R1.Packet
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityDestroy
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

object NMS {
    fun makeInvisibleEntity(entity: Entity) {
        val packetPlayOutEntityDestroy = PacketPlayOutEntityDestroy(entity.entityId)
        entity.world.players.forEach { player ->
            sendPacket(player, packetPlayOutEntityDestroy)
        }
    }

    private fun sendPacket(player: Player, packet: Packet<*>) {
        val craftPlayer = (player as CraftPlayer)
        craftPlayer.handle.playerConnection.sendPacket(packet)
    }
}