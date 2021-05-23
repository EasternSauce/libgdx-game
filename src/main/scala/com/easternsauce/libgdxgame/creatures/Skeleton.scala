package com.easternsauce.libgdxgame.creatures

import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.EsDirection

class Skeleton(val screen: PlayScreen, val id: String, val initX: Float, val initY: Float) extends Enemy {
  override val creatureWidth = 2.85f
  override val creatureHeight = 2.85f

  setBounds(0, 0, creatureWidth, creatureHeight)

  setupTextures(
    screen.atlas,
    "skeleton",
    64,
    64,
    9,
    0.05f,
    Map(EsDirection.Up -> 0, EsDirection.Down -> 2, EsDirection.Left -> 1, EsDirection.Right -> 3)
  )

  initParams(300f)
}
