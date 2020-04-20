package me.syari.ss.tool.item.attachment.gun.option

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.tool.NMS
import me.syari.ss.tool.item.attachment.gun.option.value.SneakOption
import org.bukkit.entity.Player
import org.bukkit.util.Vector

data class RecoilOption(
    val playerRecoil: PlayerRecoil,
    val cameraRecoil: CameraRecoil
) {
    fun recoil(player: Player) {
        val isSneak = player.isSneaking
        val location = player.location
        if (playerRecoil.isEnable) {
            player.velocity =
                location.direction.multiply(-playerRecoil.value.get(isSneak)).multiply(Vector(0.1, 0.0, 0.1))
        }
        if (cameraRecoil.isEnable) {
            val up = cameraRecoil.up.get(isSneak).generate()
            val side = cameraRecoil.side.get(isSneak).generate()
            NMS.cameraRecoil(player, location.yaw + side, location.pitch + up)
        }
    }

    data class PlayerRecoil(
        val value: SneakOption.FloatValue
    ) {
        val isEnable by lazy { value != default.playerRecoil.value }
    }

    data class CameraRecoil(
        val up: SneakOption.RandomFloatValue,
        val side: SneakOption.RandomFloatValue
    ) {
        val isEnable by lazy { up != default.cameraRecoil.up || side != default.cameraRecoil.side }
    }

    companion object {
        private val default = RecoilOption(
            PlayerRecoil(SneakOption.FloatValue()),
            CameraRecoil(
                SneakOption.RandomFloatValue(),
                SneakOption.RandomFloatValue()
            )
        )

        fun getRecoilOption(config: CustomConfig, section: String): RecoilOption {
            if (!config.contains(section)) return default
            return RecoilOption(
                PlayerRecoil(
                    SneakOption.FloatValue.getFromConfig(
                        config, "$section.player", default.playerRecoil.value
                    )
                ),
                CameraRecoil(
                    SneakOption.RandomFloatValue.getFromConfig(
                        config, "$section.camera.up", default.cameraRecoil.up
                    ),
                    SneakOption.RandomFloatValue.getFromConfig(
                        config, "$section.camera.side", default.cameraRecoil.side
                    )
                )
            )
        }
    }
}