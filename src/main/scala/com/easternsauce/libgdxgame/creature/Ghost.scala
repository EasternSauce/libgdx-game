package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.ability.misc.Ability
import com.easternsauce.libgdxgame.ability.other.ExplodeAbility
import com.easternsauce.libgdxgame.creature.traits.{AbilityUsage, AnimationParams}
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsDirection

class Ghost private (val id: String) extends Enemy {
  override val creatureWidth = 2.85f
  override val creatureHeight = 2.85f

  override val maxLife = 300f

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.evilYellingSound))

  override val dropTable = Map(
    "ironSword" -> 0.03f,
    "poisonDagger" -> 0.005f,
    "healingPowder" -> 0.3f,
    "steelArmor" -> 0.03f,
    "steelGreaves" -> 0.05f,
    "steelGloves" -> 0.05f,
    "steelHelmet" -> 0.05f
  )

  override val additionalAbilities: Map[String, Ability] =
    Map({
      // TODO: refactor this?
      val explodeAbility = ExplodeAbility(this)
      explodeAbility.channelSound = Some(Assets.sound(Assets.darkLaughSound))
      explodeAbility.channelSoundVolume = Some(0.2f)
      explodeAbility.activeSound = Some(Assets.sound(Assets.explosionSound))
      explodeAbility.activeSoundVolume = Some(0.5f)
      explodeAbility
    }.asMapEntry)

  override val abilityUsages: Map[String, AbilityUsage] =
    Map("explode" -> AbilityUsage(weight = 100f, minimumDistance = 6f, lifeThreshold = 0.5f))

  override val animationParams: AnimationParams = AnimationParams(
    regionName = "ghost",
    textureWidth = 32,
    textureHeight = 32,
    animationFrameCount = 3,
    frameDuration = 0.25f,
    neutralStanceFrame = 1,
    dirMap = Map(EsDirection.Up -> 3, EsDirection.Down -> 0, EsDirection.Left -> 1, EsDirection.Right -> 2)
  )

}

object Ghost {
  def apply(id: String): Ghost = {
    val obj = new Ghost(id)
    obj.init()
    obj
  }
}
