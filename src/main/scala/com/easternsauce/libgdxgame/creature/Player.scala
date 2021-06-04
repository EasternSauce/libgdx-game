package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.assets.AssetPaths
import com.easternsauce.libgdxgame.creature.traits.Creature
import com.easternsauce.libgdxgame.items.Item
import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.EsDirection

class Player(val screen: PlayScreen, val id: String) extends Creature {

  override val creatureWidth = 1.85f
  override val creatureHeight = 1.85f

  override val isPlayer: Boolean = true

  override val onGettingHitSound: Option[Sound] = Some(RpgGame.manager.get(AssetPaths.painSound, classOf[Sound]))

  override val walkSound: Option[Sound] = Some(RpgGame.manager.get(AssetPaths.runningSound, classOf[Sound]))

  setBounds(0, 0, creatureWidth, creatureHeight)
  setOrigin(creatureWidth / 2f, creatureHeight / 2f)

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

  setRegion(standStillImage(currentDirection))

  def generateStartingInventory(): Unit = {
    // TODO remove this later
    inventoryItems += (3 -> Item.generateFromTemplate("ironSword"))
    inventoryItems += (4 -> Item.generateFromTemplate("leatherArmor"))
    inventoryItems += (5 -> Item.generateFromTemplate("crossbow"))
    inventoryItems += (20 -> Item.generateFromTemplate("lifeRing"))
    inventoryItems += (21 -> Item.generateFromTemplate("trident"))
  }

  override def calculateFacingVector(): Unit = {
    val mouseX = Gdx.input.getX
    val mouseY = Gdx.input.getY

    val centerX = Gdx.graphics.getWidth / 2f
    val centerY = Gdx.graphics.getHeight / 2f

    // we need to reverse y due to mouse coordinates being in different system
    facingVector = new Vector2(mouseX - centerX, (Gdx.graphics.getHeight - mouseY) - centerY).nor()
  }
}
