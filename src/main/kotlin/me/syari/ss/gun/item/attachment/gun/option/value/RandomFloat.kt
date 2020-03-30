package me.syari.ss.gun.item.attachment.gun.option.value

import me.syari.ss.core.config.CustomConfig
import me.syari.ss.core.config.dataType.ConfigDataType
import kotlin.random.Random

data class RandomFloat(val max: Float, val min: Float){
    fun generate() = min + (max - min) * Random.nextFloat()

    constructor(): this(0F, 0F)

    companion object {
        fun createSafety(max: Float, min: Float): RandomFloat {
            return if(max < min){
                min to max
            } else {
                max to min
            }.run { RandomFloat(first, second) }
        }

        fun getFromString(config: CustomConfig, section: String, default: RandomFloat): RandomFloat {
            val max = config.get("$section.max", ConfigDataType.FLOAT, false)
            return if(max != null){
                val min = config.get("$section.min", ConfigDataType.FLOAT, 0F, false)
                createSafety(max, min)
            } else {
                default
            }
        }
    }
}