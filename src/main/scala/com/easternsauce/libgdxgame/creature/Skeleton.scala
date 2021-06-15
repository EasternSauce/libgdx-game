package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.GameSystem._
import com.easternsauce.libgdxgame.assets.Assets
import com.easternsauce.libgdxgame.util.EsDirection

class Skeleton(val id: String) extends Enemy {
  override val creatureWidth = 2.85f
  override val creatureHeight = 2.85f

  override val onGettingHitSound: Option[Sound] = Some(sound(Assets.boneClickSound))

  setBounds(0, 0, creatureWidth, creatureHeight)
  setOrigin(creatureWidth / 2f, creatureHeight / 2f)

  setupAnimation(
    regionName = "skeleton",
    textureWidth = 64,
    textureHeight = 64,
    animationFrameCount = 9,
    frameDuration = 0.05f,
    neutralStanceFrame = 0,
    dirMap = Map(EsDirection.Up -> 0, EsDirection.Down -> 2, EsDirection.Left -> 1, EsDirection.Right -> 3)
  )

  defineEffects()

  defineStandardAbilities()

  setRegion(standStillImage(currentDirection))

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

}
