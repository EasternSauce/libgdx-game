package com.easternsauce.libgdxgame.creatures

import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.EsDirection

class Player(val screen: PlayScreen, val id: String, val initX: Float, val initY: Float) extends Creature {
  override val creatureWidth = 2f
  override val creatureHeight = 2f

  setBounds(0, 0, creatureWidth, creatureHeight)

  setupTextures(
    screen.atlas,
    "male1",
    32,
    32,
    3,
    0.2f,
    Map(EsDirection.Up -> 3, EsDirection.Down -> 0, EsDirection.Left -> 1, EsDirection.Right -> 2)
  )

  initParams(300f)

}
