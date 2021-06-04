package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.assets.AssetPaths
import com.easternsauce.libgdxgame.creature.traits.Enemy
import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.EsDirection

class Skeleton(val screen: PlayScreen, val id: String) extends Enemy {
  override val creatureWidth = 2.85f
  override val creatureHeight = 2.85f

  override val onGettingHitSound: Option[Sound] = Some(RpgGame.manager.get(AssetPaths.boneClickSound, classOf[Sound]))

  setBounds(0, 0, creatureWidth, creatureHeight)
  setOrigin(creatureWidth / 2f, creatureHeight / 2f)

  setupAnimation(
    atlas = screen.atlas,
    regionName = "skeleton",
    textureWidth = 64,
    textureHeight = 64,
    animationFrameCount = 9,
    frameDuration = 0.05f,
    neutralStanceFrame = 0,
    dirMap = Map(EsDirection.Up -> 0, EsDirection.Down -> 2, EsDirection.Left -> 1, EsDirection.Right -> 3)
  )

  initParams(300f)

  defineEffects()

  defineStandardAbilities()

  setRegion(standStillImage(currentDirection))
}
