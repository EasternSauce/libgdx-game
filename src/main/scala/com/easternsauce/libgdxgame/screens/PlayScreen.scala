package com.easternsauce.libgdxgame.screens

import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.graphics.{Color, GL20}
import com.badlogic.gdx.{Gdx, Input, Screen}
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.creature.Player
import com.easternsauce.libgdxgame.projectile.Arrow
import com.easternsauce.libgdxgame.util.{EsBatch, EsTimer}

class PlayScreen(val game: RpgGame) extends Screen {

  var worldBatch: EsBatch = new EsBatch()
  var hudBatch: EsBatch = new EsBatch()

  override def show(): Unit = {}

  def update(delta: Float): Unit = {
    EsTimer.updateTimers()

    handleInput()

    game.currentArea.get.update()

    if (game.creaturesToMove.nonEmpty) {
      game.creaturesToMove.foreach {
        case (creature, area, x, y) =>
          creature.assignToArea(area, x, y)
          creature.passedGateRecently = true
      }

      game.creaturesToMove.clear()
    }

    game.updateCamera(game.player)

    game.currentArea.get.setView(game.camera)

    game.healthStaminaBar.update()

    managePlayerRespawns(game.player)
  }

  override def render(delta: Float): Unit = {
    update(delta)

    worldBatch.spriteBatch.setProjectionMatrix(game.camera.combined)
    hudBatch.spriteBatch.setProjectionMatrix(game.hudCamera.combined)

    Gdx.gl.glClearColor(0, 0, 0, 1)

    Gdx.gl.glClear(
      GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
        | (if (Gdx.graphics.getBufferFormat.coverageSampling)
             GL20.GL_COVERAGE_BUFFER_BIT_NV
           else 0)
    )

    game.currentArea.get.renderBottomLayer()

    worldBatch.spriteBatch.begin()

    for (areaGate <- game.gateList) areaGate.render(worldBatch)

    game.currentArea.get.render(worldBatch)

    game.currentArea.get.arrowList.foreach((arrow: Arrow) => arrow.render(worldBatch))

    worldBatch.spriteBatch.end()

    game.currentArea.get.renderTopLayer()

    hudBatch.spriteBatch.begin()

    game.inventoryWindow.render(hudBatch)

    game.healthStaminaBar.render(hudBatch)

    RpgGame.defaultFont.setColor(Color.WHITE)
    RpgGame.defaultFont.draw(
      hudBatch.spriteBatch,
      Gdx.graphics.getFramesPerSecond + " fps",
      3,
      RpgGame.WindowHeight - 3
    )

    renderDeathScreen(hudBatch)

    hudBatch.spriteBatch.end()

    if (game.debugMode) {
      game.b2DebugRenderer.render(game.currentArea.get.world, game.camera.combined)
    }

  }

  override def resize(width: Int, height: Int): Unit = {
    game.viewport.update(width, height)
    game.hudViewport.update(width, height)

  }

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def hide(): Unit = {}

  override def dispose(): Unit = {
    game.areaMap.values.foreach(_.dispose())
    game.atlas.dispose()
  }

  def managePlayerRespawns(player: Player) {
    if (player.respawning && player.respawnTimer.time > 3f) {
      player.respawning = false

      player.healthPoints = game.player.maxHealthPoints
      player.staminaPoints = game.player.maxStaminaPoints
      player.isAttacking = false
      player.staminaOveruse = false
      player.effectMap("staminaRegenStopped").stop()

      val area = game.player.playerSpawnPoint.get.area
      game.currentArea = Option(area)
      area.reset(game)

      game.player.assignToArea(area, game.player.playerSpawnPoint.get.posX, game.player.playerSpawnPoint.get.posY)

      game.player.setRotation(0f)

      //GameSystem.stopBossBattleMusic()
    }
  }

  private def renderDeathScreen(batch: EsBatch) = {
    if (game.player.respawning) {
      RpgGame.hugeFont.setColor(Color.RED)
      RpgGame.hugeFont.draw(
        batch.spriteBatch,
        "YOU DIED",
        RpgGame.WindowWidth / 2f - 160,
        RpgGame.WindowHeight / 2 + 70
      )
    }
  }

  def handleInput(): Unit = {

    if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) game.debugMode = !game.debugMode

    if (Gdx.input.isKeyJustPressed(Input.Keys.E)) game.player.interact()

    if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) game.savefileManager.saveGame()

    if (Gdx.input.isKeyJustPressed(Input.Keys.I)) game.inventoryWindow.visible = !game.inventoryWindow.visible

    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
      if (game.inventoryWindow.visible) game.inventoryWindow.visible = false
      else {
        game.mainMenuScreen.currentNode = game.mainMenuScreen.pausedOptionTreeRoot
        game.setScreen(game.mainMenuScreen)
      }

    if (game.inventoryWindow.visible) {
      if (Gdx.input.isButtonJustPressed(Buttons.LEFT)) {
        game.inventoryWindow.handleMouseClicked()

      }
    } else {
      if (Gdx.input.isButtonPressed(Buttons.LEFT)) game.player.currentAttack.perform()

    }

    game.handlePlayerMovement()

  }

}
