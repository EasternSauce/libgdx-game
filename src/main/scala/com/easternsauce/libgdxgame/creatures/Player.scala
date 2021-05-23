package com.easternsauce.libgdxgame.creatures

import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.EsDirection

class Player(val screen: PlayScreen, val initX: Float, val initY: Float, override val area: Area) extends Creature {
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

  initCircularBody(area.world, 300f, initX, initY, creatureWidth / 2f)

}
