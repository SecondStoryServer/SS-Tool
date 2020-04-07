package me.syari.ss.gun.item.attachment.gun.option.value

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.core.config.dataType.ConfigDataType

interface SneakOption<T> {
    val base: T
    val sneak: T

    fun get(isSneak: Boolean): T {
        return if (isSneak) {
            sneak
        } else {
            base
        }
    }

    data class IntValue(
        override val base: Int,
        override val sneak: Int
    ): SneakOption<Int> {
        constructor(): this(0, 0)

        companion object {
            fun getFromConfig(config: CustomConfig, section: String, default: IntValue): IntValue {
                return if (!config.contains("$section.base") && !config.contains("$section.sneak")) {
                    config.get(section, ConfigDataType.INT, false)?.let {
                        IntValue(it, it)
                    } ?: IntValue(default.base, default.sneak)
                } else {
                    IntValue(
                        config.get("$section.base", ConfigDataType.INT, default.base, false),
                        config.get("$section.sneak", ConfigDataType.INT, default.sneak, false)
                    )
                }
            }
        }
    }

    data class FloatValue(
        override val base: Float,
        override val sneak: Float
    ): SneakOption<Float> {
        constructor(): this(0F, 0F)

        companion object {
            fun getFromConfig(config: CustomConfig, section: String, default: FloatValue): FloatValue {
                return if (!config.contains("$section.base") && !config.contains("$section.sneak")) {
                    config.get(section, ConfigDataType.FLOAT, false)?.let {
                        FloatValue(it, it)
                    } ?: FloatValue(default.base, default.sneak)
                } else {
                    FloatValue(
                        config.get("$section.base", ConfigDataType.FLOAT, default.base, false),
                        config.get("$section.sneak", ConfigDataType.FLOAT, default.sneak, false)
                    )
                }
            }
        }
    }

    data class RandomFloatValue(
        override val base: RandomFloat,
        override val sneak: RandomFloat
    ): SneakOption<RandomFloat> {
        constructor(): this(RandomFloat(), RandomFloat())

        companion object {
            fun getFromConfig(config: CustomConfig, section: String, default: RandomFloatValue): RandomFloatValue {
                if(config.contains("$section.max")){
                    return RandomFloat.getFromString(config, section, default.base).let {
                        RandomFloatValue(it, it)
                    }
                }
                return RandomFloatValue(
                    RandomFloat.getFromString(config, "$section.base", default.base),
                    RandomFloat.getFromString(config, "$section.sneak", default.sneak)
                )
            }
        }
    }
}