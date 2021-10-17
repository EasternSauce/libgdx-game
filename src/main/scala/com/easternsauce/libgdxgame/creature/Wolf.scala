package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.creature.traits.{AbilityUsage, AnimationParams}
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.{CreatureInfo, EsDirection}

class Wolf private (val id: String) extends Enemy {
  override val creatureWidth = 2.85f
  override val creatureHeight = 2.85f

  override val maxLife = 110f

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.dogWhineSound))

  override val activeSound: Option[Sound] = Some(Assets.sound(Assets.dogBarkSound))

  override val dropTable = Map(
    "ringmailGreaves" -> 0.1f,
    "leatherArmor" -> 0.05f,
    "hideGloves" -> 0.1f,
    "leatherHelmet" -> 0.1f,
    "healingPowder" -> 0.5f
  )

  override val abilityUsages: Map[String, AbilityUsage] =
    Map("dash" -> AbilityUsage(weight = 100f, minimumDistance = 8f))

  override val animationParams: AnimationParams = AnimationParams(
    regionName = "wolf2",
    textureWidth = 32,
    textureHeight = 34,
    animationFrameCount = 6,
    frameDuration = 0.1f,
    neutralStanceFrame = 1,
    dirMap = Map(EsDirection.Up -> 3, EsDirection.Down -> 0, EsDirection.Left -> 1, EsDirection.Right -> 2)
  )

}

object Wolf extends CreatureInfo {
  def apply(id: String): Wolf = {
    val obj = new Wolf(id)
    obj.init()
    obj
  }

  override val additionalAbilities: List[String] = List("dash")
}
