package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.assets.AssetPaths
import com.easternsauce.libgdxgame.creature.traits.Enemy
import com.easternsauce.libgdxgame.util.EsDirection

class Wolf(val game: RpgGame, val id: String) extends Enemy {
  override val creatureWidth = 2.85f
  override val creatureHeight = 2.85f

  override val onGettingHitSound: Option[Sound] = Some(RpgGame.manager.get(AssetPaths.dogWhineSound, classOf[Sound]))

  setBounds(0, 0, creatureWidth, creatureHeight)
  setOrigin(creatureWidth / 2f, creatureHeight / 2f)

  setupAnimation(
    atlas = game.atlas,
    regionName = "wolf2",
    textureWidth = 32,
    textureHeight = 34,
    animationFrameCount = 6,
    frameDuration = 0.1f,
    neutralStanceFrame = 1,
    dirMap = Map(EsDirection.Up -> 3, EsDirection.Down -> 0, EsDirection.Left -> 1, EsDirection.Right -> 2)
  )

  initParams(300f)

  defineEffects()

  defineStandardAbilities()

  setRegion(standStillImage(currentDirection))

  dropTable.addAll(
    List(
      "ringmailGreaves" -> 0.1f,
      "leatherArmor" -> 0.05f,
      "hideGloves" -> 0.1f,
      "leatherHelmet" -> 0.1f,
      "healingPowder" -> 0.5f
    )
  )

}
