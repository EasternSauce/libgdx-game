package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.ability.FistSlamAbility
import com.easternsauce.libgdxgame.creature.traits.AbilityUsage
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsDirection

class Skeleton(val id: String) extends Enemy {
  override val creatureWidth = 2.85f
  override val creatureHeight = 2.85f

  override val maxLife = 160f

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.boneClickSound))

  override val activeSound: Option[Sound] = Some(Assets.sound(Assets.boneRattleSound))

  var fistSlamAbility = new FistSlamAbility(this)

  setupAnimation(
    regionName = "skeleton",
    textureWidth = 64,
    textureHeight = 64,
    animationFrameCount = 9,
    frameDuration = 0.05f,
    neutralStanceFrame = 0,
    dirMap = Map(EsDirection.Up -> 0, EsDirection.Down -> 2, EsDirection.Left -> 1, EsDirection.Right -> 3)
  )

  initCreature()

  fistSlamAbility = new FistSlamAbility(this)
  abilityMap += (fistSlamAbility.id -> fistSlamAbility)

  dropTable.addAll(
    List(
      "ringmailGreaves" -> 0.1f,
      "leatherArmor" -> 0.05f,
      "hideGloves" -> 0.1f,
      "leatherHelmet" -> 0.1f,
      "woodenSword" -> 0.1f,
      "healingPowder" -> 0.5f
    )
  )

  abilityUsages.addAll(List("fistSlam" -> AbilityUsage(100f, 6f)))

}
