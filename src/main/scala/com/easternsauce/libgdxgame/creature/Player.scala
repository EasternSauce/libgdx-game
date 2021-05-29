package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.creature.traits.Creature
import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.EsDirection

class Player(val screen: PlayScreen, val id: String) extends Creature {
  override val creatureWidth = 1.85f
  override val creatureHeight = 1.85f

  override val isPlayer: Boolean = true

  setBounds(0, 0, creatureWidth, creatureHeight)

  setupAnimation(
    atlas = screen.atlas,
    regionName = "male1",
    textureWidth = 32,
    textureHeight = 32,
    animationFrameCount = 3,
    frameDuration = 0.1f,
    neutralStanceFrame = 1,
    dirMap = Map(EsDirection.Up -> 3, EsDirection.Down -> 0, EsDirection.Left -> 1, EsDirection.Right -> 2)
  )

  initParams(300f)

  defineEffects()

  defineStandardAbilities()

  override def setFacingDirection(): Unit = {
    val mouseX = Gdx.input.getX
    val mouseY = Gdx.input.getY

    val centerX = Gdx.graphics.getWidth / 2f
    val centerY = Gdx.graphics.getHeight / 2f

    facingVector = new Vector2(mouseX - centerX, (Gdx.graphics.getHeight - mouseY) - centerY)
      .nor() // we need to reverse y due to mouse coordinates being in different system
  }

}
