package com.easternsauce.libgdxgame.creature

import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.EsDirection

class Player(val screen: PlayScreen, val id: String, val initX: Float, val initY: Float) extends Creature {
  override val creatureWidth = 1.85f
  override val creatureHeight = 1.85f

  setBounds(0, 0, creatureWidth, creatureHeight)

  setupAnimation(
    screen.atlas,
    "male1",
    32,
    32,
    3,
    0.1f,
    1,
    Map(EsDirection.Up -> 3, EsDirection.Down -> 0, EsDirection.Left -> 1, EsDirection.Right -> 2)
  )

  initParams(300f)

}
