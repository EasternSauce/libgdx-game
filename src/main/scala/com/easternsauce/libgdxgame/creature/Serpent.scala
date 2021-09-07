package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.ability.traits.Ability
import com.easternsauce.libgdxgame.ability.{BubbleAbility, IceShardAbility}
import com.easternsauce.libgdxgame.creature.traits.AbilityUsage
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsDirection

class Serpent(val id: String) extends Enemy {
  override val creatureWidth = 3.85f
  override val creatureHeight = 3.85f

  override val maxLife = 160f

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.boneClickSound))

  override val activeSound: Option[Sound] = Some(Assets.sound(Assets.boneRattleSound))

  override val dropTable = Map(
    "ringmailGreaves" -> 0.1f,
    "leatherArmor" -> 0.05f,
    "hideGloves" -> 0.1f,
    "leatherHelmet" -> 0.1f,
    "woodenSword" -> 0.1f,
    "healingPowder" -> 0.5f
  )

  override val abilityMap: Map[String, Ability] =
    standardAbilities ++
      Map(BubbleAbility(this).asMapEntry, IceShardAbility(this).asMapEntry)

  setupAnimation(
    regionName = "serpent",
    textureWidth = 48,
    textureHeight = 56,
    animationFrameCount = 3,
    frameDuration = 0.15f,
    neutralStanceFrame = 0,
    dirMap = Map(EsDirection.Up -> 0, EsDirection.Down -> 2, EsDirection.Left -> 1, EsDirection.Right -> 3)
  )

  initCreature()

  abilityUsages.addAll(List("bubble" -> AbilityUsage(weight = 100f, minimumDistance = 2f)))
  abilityUsages.addAll(List("ice_shard" -> AbilityUsage(weight = 100f, minimumDistance = 2f)))

}
