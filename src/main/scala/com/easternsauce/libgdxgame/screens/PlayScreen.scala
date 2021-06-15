package com.easternsauce.libgdxgame.screens

import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.graphics.{Color, GL20}
import com.badlogic.gdx.{Gdx, Input, Screen}
import com.easternsauce.libgdxgame.GameSystem._
import com.easternsauce.libgdxgame.creature.Player
import com.easternsauce.libgdxgame.projectile.Arrow
import com.easternsauce.libgdxgame.util.{EsBatch, EsTimer}

class PlayScreen() extends Screen {

  var worldBatch: EsBatch = new EsBatch()
  var hudBatch: EsBatch = new EsBatch()

  override def show(): Unit = {}

  def update(delta: Float): Unit = {
    EsTimer.updateTimers()

    handleInput()

    currentArea.get.update()

    if (creaturesToMove.nonEmpty) {
      creaturesToMove.foreach {
        case (creature, area, x, y) =>
          creature.assignToArea(area, x, y)
          creature.passedGateRecently = true
      }

      creaturesToMove.clear()
    }

    updateCamera(player)

    currentArea.get.setView(camera)

    healthStaminaBar.update()

    managePlayerRespawns(player)

    notificationText.update()
  }

  override def render(delta: Float): Unit = {
    update(delta)

    worldBatch.spriteBatch.setProjectionMatrix(camera.combined)
    hudBatch.spriteBatch.setProjectionMatrix(hudCamera.combined)

    Gdx.gl.glClearColor(0, 0, 0, 1)

    Gdx.gl.glClear(
      GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
        | (if (Gdx.graphics.getBufferFormat.coverageSampling)
             GL20.GL_COVERAGE_BUFFER_BIT_NV
           else 0)
    )

    currentArea.get.renderBottomLayer()

    worldBatch.spriteBatch.begin()

    for (areaGate <- gateList) areaGate.render(worldBatch)

    currentArea.get.render(worldBatch)

    currentArea.get.arrowList.foreach((arrow: Arrow) => arrow.render(worldBatch))

    worldBatch.spriteBatch.end()

    currentArea.get.renderTopLayer()

    hudBatch.spriteBatch.begin()

    inventoryWindow.render(hudBatch)

    healthStaminaBar.render(hudBatch)

    defaultFont.setColor(Color.WHITE)
    defaultFont.draw(hudBatch.spriteBatch, Gdx.graphics.getFramesPerSecond + " fps", 3, WindowHeight - 3)

    renderDeathScreen(hudBatch)

    notificationText.render(hudBatch)

    lootPickupMenu.render(hudBatch)

    hudBatch.spriteBatch.end()

    if (debugMode) {
      b2DebugRenderer.render(currentArea.get.world, camera.combined)
    }

  }

  override def resize(width: Int, height: Int): Unit = {
    viewport.update(width, height)
    hudViewport.update(width, height)

  }

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def hide(): Unit = {}

  override def dispose(): Unit = {
    areaMap.values.foreach(_.dispose())
    atlas.dispose()
  }

  def managePlayerRespawns(player: Player) {
    if (player.respawning && player.respawnTimer.time > playerRespawnTime) {
      player.respawning = false

      player.healthPoints = player.maxHealthPoints
      player.staminaPoints = player.maxStaminaPoints
      player.isAttacking = false
      player.staminaOveruse = false
      player.effectMap("staminaRegenStopped").stop()

      val area = player.playerSpawnPoint.get.area
      currentArea = Option(area)
      area.reset()

      player.assignToArea(area, player.playerSpawnPoint.get.posX, player.playerSpawnPoint.get.posY)

      player.setRotation(0f)

      //stopBossBattleMusic()
    }
  }

  private def renderDeathScreen(batch: EsBatch) = {
    if (player.respawning) {
      hugeFont.setColor(Color.RED)
      hugeFont.draw(batch.spriteBatch, "YOU DIED", WindowWidth / 2f - 160, WindowHeight / 2 + 70)
    }
  }

  def handleInput(): Unit = {

    if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
      if (!debugMode) {
        notificationText.showNotification("Debug mode activated")
        debugMode = true
      } else {
        notificationText.showNotification("Debug mode deactivated")
        debugMode = false
      }
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.E)) player.interact()

    if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) savefileManager.saveGame()

    if (Gdx.input.isKeyJustPressed(Input.Keys.I)) inventoryWindow.visible = !inventoryWindow.visible

    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
      if (inventoryWindow.visible) inventoryWindow.visible = false
      else {
        mainMenuScreen.currentNode = mainMenuScreen.pausedOptionTreeRoot
        setScreen(mainMenuScreen)
      }

    if (Gdx.input.isButtonJustPressed(Buttons.LEFT)) {
      if (inventoryWindow.visible) {
        inventoryWindow.moveItemClick()
      } else if (lootPickupMenu.visible) {
        lootPickupMenu.pickUpItemClick()
      } else {
        player.currentAttack.perform()
      }
    }

    if (Gdx.input.isButtonJustPressed(Buttons.RIGHT)) {
      if (inventoryWindow.visible) {
        inventoryWindow.useItemClick()
      }
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.ALT_LEFT)) {
      if (inventoryWindow.visible) {
        inventoryWindow.tryDropSelectedItem()
      }
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
      inventoryWindow.swapPrimaryAndSecondaryWeapons()
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
      if (player.equipmentItems.contains(consumableIndex)) {

        val consumable = player.equipmentItems(consumableIndex)
        player.useItem(consumable)
        if (consumable.quantity <= 1) player.equipmentItems.remove(consumableIndex)
        else consumable.quantity = consumable.quantity - 1
      }
    }

    handlePlayerMovement()

  }

}
