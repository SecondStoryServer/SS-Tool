package me.syari.ss.tool

import net.minecraft.server.v1_15_R1.Packet
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_15_R1.PacketPlayOutPosition
import net.minecraft.server.v1_15_R1.PacketPlayOutPosition.EnumPlayerTeleportFlags
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

    private val enumPlayerTeleportFlags = setOf(
        EnumPlayerTeleportFlags.X,
        EnumPlayerTeleportFlags.Y,
        EnumPlayerTeleportFlags.Z
    )

    fun cameraRecoil(player: Player, yaw: Float, pitch: Float) {
        val packetPlayOutPosition = PacketPlayOutPosition(0.0, 0.0, 0.0, yaw, pitch, enumPlayerTeleportFlags, 0)
        sendPacket(player, packetPlayOutPosition)
    }

    private fun sendPacket(player: Player, packet: Packet<*>) {
        val craftPlayer = (player as CraftPlayer)
        craftPlayer.handle.playerConnection.sendPacket(packet)
    }
}