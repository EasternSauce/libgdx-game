package com.easternsauce.libgdxgame.creature

import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.EsDirection

class Skeleton(val screen: PlayScreen, val id: String, val initX: Float, val initY: Float) extends Enemy {
  override val creatureWidth = 2.85f
  override val creatureHeight = 2.85f

  setBounds(0, 0, creatureWidth, creatureHeight)

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

}
