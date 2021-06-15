package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsDirection

class Goblin(val id: String) extends Enemy {
  override val creatureWidth = 2.85f
  override val creatureHeight = 2.85f

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.evilYellingSound))

  setBounds(0, 0, creatureWidth, creatureHeight)
  setOrigin(creatureWidth / 2f, creatureHeight / 2f)

  setupAnimation(
    regionName = "goblin",
    textureWidth = 32,
    textureHeight = 32,
    animationFrameCount = 3,
    frameDuration = 0.25f,
    neutralStanceFrame = 1,
    dirMap = Map(EsDirection.Up -> 3, EsDirection.Down -> 0, EsDirection.Left -> 1, EsDirection.Right -> 2)
  )

  defineEffects()

  defineStandardAbilities()

  setRegion(standStillImage(currentDirection))

  dropTable.addAll(
    List(
      "ironSword" -> 0.03f,
      "poisonDagger" -> 0.07f,
      "healingPowder" -> 0.3f,
      "steelArmor" -> 0.03f,
      "steelGreaves" -> 0.05f,
      "steelGloves" -> 0.05f,
      "steelHelmet" -> 0.05f
    )
  )

}
