package com.easternsauce.libgdxgame.screens

import com.badlogic.gdx.graphics.{Color, GL20}
import com.badlogic.gdx.{Gdx, Input, Screen}
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable.ListBuffer

class MainMenuScreen(game: RpgGame) extends Screen {
  private var currentSelected: Int = 0

  private var optionList: ListBuffer[String] = ListBuffer()

  private var startMenu: Boolean = true

  private var prompt: Boolean = false
  private var promptOption: String = ""
  private var promptText: String = ""
  private var savedOptionList: ListBuffer[String] = ListBuffer()

  var batch: EsBatch = new EsBatch()

  if (game.savefileManager.savefileFound) {
    optionList += "Continue"
  }

  optionList += "New game"
  optionList += "Exit"

  override def show(): Unit = {}

  override def render(delta: Float): Unit = {
    update()

    Gdx.gl.glClearColor(0, 0, 0, 1)

    Gdx.gl.glClear(
      GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
        | (if (Gdx.graphics.getBufferFormat.coverageSampling)
             GL20.GL_COVERAGE_BUFFER_BIT_NV
           else 0)
    )

    batch.spriteBatch.begin()
    println("rendering")
    if (!prompt) for (i <- 0 until Math.min(4, optionList.size)) {
      RpgGame.defaultFont.setColor(Color.WHITE)
      RpgGame.defaultFont.draw(
        batch.spriteBatch,
        (if (currentSelected == i) ">"
         else "") + optionList(i),
        100,
        RpgGame.WindowHeight - (100 + 30 * i)
      )
    }
    else {
      RpgGame.defaultFont.setColor(Color.WHITE)
      RpgGame.defaultFont.draw(batch.spriteBatch, promptText, 100, RpgGame.WindowHeight - 100)
      for (i <- 0 until Math.min(4, optionList.size)) {
        RpgGame.defaultFont.setColor(Color.WHITE)
        RpgGame.defaultFont.draw(
          batch.spriteBatch,
          (if (currentSelected == i) ">"
           else "") + optionList(i),
          100,
          RpgGame.WindowHeight - (130 + 30 * i)
        )
      }
    }
    batch.spriteBatch.end()

  }

  def update(): Unit = {
    if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {

      if (optionList(currentSelected) == "Continue") {
        game.setScreen(game.playScreen)
        if (startMenu) {
          startMenu = false
          optionList = ListBuffer()
          optionList += "Continue"
          optionList += "New game"
          optionList += "Save game"
          optionList += "Exit"
          game.savefileManager.loadGame()
        }
      } else if (optionList(currentSelected) == "New game") {
        if (!prompt) {
          prompt = true
          promptOption = "New game"
          savedOptionList = optionList
          optionList = ListBuffer()
          optionList += "No"
          optionList += "Yes"
          promptText = "Start new game?"
          currentSelected = 0
        }
      } else if (optionList(currentSelected) == "Save game") {
        game.savefileManager.saveGame(game.playScreen)
      } else if (optionList(currentSelected) == "Exit") {
        if (!prompt) {
          prompt = true
          promptOption = "Exit"
          savedOptionList = optionList
          optionList = ListBuffer()
          optionList += "No"
          optionList += "Yes"
          promptText = "Quit without saving?"
          currentSelected = 0
        }
      } else if (
        optionList(currentSelected).equals("Yes") || optionList(currentSelected)
          .equals("No")
      ) {
        val option: String = optionList(currentSelected)
        if (option.equals("Yes")) {
          if (promptOption.equals("Exit")) {
            prompt = false
            System.exit(0)
          } else if (promptOption == "New game") {

            // TODO: remove existing save file?

            game.savefileManager.loadGame()
            game.setScreen(game.playScreen)

            if (startMenu) {

              startMenu = false
              optionList = ListBuffer()
              optionList += "Continue"
              optionList += "New game"
              optionList += "Save game"
              optionList += "Exit"
            } else optionList = savedOptionList
            prompt = false
            currentSelected = 0
          }
        } else {
          optionList = savedOptionList
          prompt = false
          currentSelected = 0
        }
      }
    }
    if (Gdx.input.isKeyJustPressed(Input.Keys.W))
      if (currentSelected > 0) currentSelected -= 1
    if (Gdx.input.isKeyJustPressed(Input.Keys.S))
      if (currentSelected < optionList.size - 1) currentSelected += 1
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      if (!startMenu) game.setScreen(game.playScreen)

    }
  }

  override def resize(width: Int, height: Int): Unit = {}

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def hide(): Unit = {}

  override def dispose(): Unit = {}

}
