package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.assets.AssetPaths
import com.easternsauce.libgdxgame.creature.traits.Enemy
import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.EsDirection

class Wolf(val screen: PlayScreen, val id: String) extends Enemy {
  override val creatureWidth = 2.85f
  override val creatureHeight = 2.85f

  override val onGettingHitSound: Option[Sound] = Some(RpgGame.manager.get(AssetPaths.dogWhineSound, classOf[Sound]))

  setBounds(0, 0, creatureWidth, creatureHeight)
  setOrigin(creatureWidth / 2f, creatureHeight / 2f)

  setupAnimation(
    atlas = screen.atlas,
    regionName = "wolf2",
    textureWidth = 32,
    textureHeight = 34,
    animationFrameCount = 6,
    frameDuration = 0.1f,
    neutralStanceFrame = 0,
    dirMap = Map(EsDirection.Up -> 3, EsDirection.Down -> 0, EsDirection.Left -> 1, EsDirection.Right -> 2)
  )

  initParams(300f)

  defineEffects()

  defineStandardAbilities()

  setRegion(standStillImage(currentDirection))

}
