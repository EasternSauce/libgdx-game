package com.easternsauce.libgdxgame.screens

import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.graphics.{Color, GL20}
import com.badlogic.gdx.{Gdx, Input, Screen}
import com.easternsauce.libgdxgame.RpgGame
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

    game.adjustCamera(game.player)

    game.currentArea.get.setView(game.camera)

    game.healthStaminaBar.update()

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

    hudBatch.spriteBatch.end()

    game.b2DebugRenderer.render(game.currentArea.get.world, game.camera.combined)

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

  def handleInput(): Unit = {

    if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) game.savefileManager.saveGame(this)

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
