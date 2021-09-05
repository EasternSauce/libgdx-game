package com.easternsauce.libgdxgame.screens

import com.badlogic.gdx.graphics.{Color, GL20}
import com.badlogic.gdx.{Gdx, Input, Screen}
import com.easternsauce.libgdxgame.system.GameSystem._
import com.easternsauce.libgdxgame.system.{Constants, Fonts, GameSystem}
import com.easternsauce.libgdxgame.util.EsBatch

class MainMenuScreen extends Screen {
  private var currentSelected: Int = 0

  var batch: EsBatch = new EsBatch()

  val saveExistsOptions: List[MenuOptionNode] = List(
    MenuOptionNode(name = "Continue", MenuAction.LoadGame),
    MenuOptionNode(
      "New game",
      MenuAction.NextMenu,
      List(MenuOptionNode("No", MenuAction.PreviousMenu), MenuOptionNode("Yes", MenuAction.NewGame)),
      Some("Are you sure?")
    ),
    MenuOptionNode(
      "Exit",
      MenuAction.NextMenu,
      List(MenuOptionNode("No", MenuAction.PreviousMenu), MenuOptionNode("Yes", MenuAction.Exit)),
      Some("Are you sure?")
    )
  )

  val saveDoesntExistOptions: List[MenuOptionNode] =
    List(
      MenuOptionNode(
        "New game",
        MenuAction.NextMenu,
        List(MenuOptionNode("No", MenuAction.PreviousMenu), MenuOptionNode("Yes", MenuAction.NewGame)),
        Some("Are you sure?")
      ),
      MenuOptionNode(
        "Exit",
        MenuAction.NextMenu,
        List(MenuOptionNode("No", MenuAction.PreviousMenu), MenuOptionNode("Yes", MenuAction.Exit))
      )
    )

  val pauseOptions: List[MenuOptionNode] =
    List(
      MenuOptionNode(name = "Continue", MenuAction.Continue),
      MenuOptionNode(
        "New game",
        MenuAction.NextMenu,
        List(MenuOptionNode("No", MenuAction.PreviousMenu), MenuOptionNode("Yes", MenuAction.NewGame)),
        Some("Are you sure?")
      ),
      MenuOptionNode(
        "Exit",
        MenuAction.NextMenu,
        List(MenuOptionNode("No", MenuAction.PreviousMenu), MenuOptionNode("Yes", MenuAction.Exit))
      )
    )

  val optionTreeRoot: MenuOptionNode = MenuOptionNode(
    "",
    MenuAction.NextMenu,
    if (savefileManager.savefileFound) saveExistsOptions else saveDoesntExistOptions
  )

  val pausedOptionTreeRoot: MenuOptionNode = MenuOptionNode("", MenuAction.NextMenu, pauseOptions)

  var currentNode: MenuOptionNode = optionTreeRoot
  val gameplayStarted = false
  var previousNode: Option[MenuOptionNode] = None

  val controlsCheatsheetEntries = List(
    ("CONTROLS", ""),
    ("", ""),
    ("Movement:", "W/S/A/D"),
    ("Action:", "E"),
    ("Sprint:", "Left Shift"),
    ("Attack:", "Left Mouse Button"),
    ("Inventory:", "I"),
    ("Drop item:", "Alt"),
    ("Save:", "F5"),
    ("Menu:", "Escape"),
    ("Swap weapons:", "Tab"),
    ("Quick consumable:", "F")
  )

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

    val posX = 100
    val posY = if (currentNode.promptText.nonEmpty) 130 else 100

    if (currentNode.promptText.nonEmpty) {
      Fonts.defaultFont.draw(batch.spriteBatch, currentNode.promptText.get, posX.toFloat, Constants.WindowHeight - 100)
    }

    for (i <- currentNode.children.indices) {
      val option = currentNode.children(i)
      Fonts.defaultFont.draw(
        batch.spriteBatch,
        (if (currentSelected == i) ">" else "") + option.name,
        posX.toFloat,
        Constants.WindowHeight.toFloat - (posY + 30 * i),
        Color.WHITE
      )
    }

    Fonts.defaultFont.draw(
      batch.spriteBatch,
      controlsCheatsheetEntries.map { case (key, _) => key }.mkString("\n"),
      Constants.WindowWidth - 550,
      350,
      Color.WHITE
    )

    Fonts.defaultFont.draw(
      batch.spriteBatch,
      controlsCheatsheetEntries.map { case (_, value) => value }.mkString("\n"),
      Constants.WindowWidth - 350,
      350,
      Color.WHITE
    )

    batch.spriteBatch.end()

  }

  def update(): Unit = {
    if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
      handleAction(currentNode.children(currentSelected))
      currentSelected = 0
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.W))
      if (currentSelected > 0) currentSelected -= 1
    if (Gdx.input.isKeyJustPressed(Input.Keys.S))
      if (currentSelected < currentNode.children.size - 1) currentSelected += 1
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      if (gameplayStarted) setScreen(playScreen)

    }

  }

  def handleAction(node: MenuOptionNode): Unit =
    node.action match {
      case MenuAction.NextMenu =>
        previousNode = Some(currentNode)
        currentNode = node
      case MenuAction.PreviousMenu =>
        if (previousNode.nonEmpty) currentNode = previousNode.get
      case MenuAction.NewGame =>
        GameSystem.setupNewGame()
        setScreen(playScreen)
      case MenuAction.LoadGame =>
        savefileManager.loadGame()
        setScreen(playScreen)
      case MenuAction.Continue =>
        setScreen(playScreen)
      case MenuAction.Exit =>
        Gdx.app.exit()
    }

  override def resize(width: Int, height: Int): Unit = {}

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def hide(): Unit = {}

  override def dispose(): Unit = {}

}

object MenuAction extends Enumeration {
  type MenuAction = Value
  val NewGame, NextMenu, PreviousMenu, Continue, LoadGame, Exit = Value
}

case class MenuOptionNode(
  name: String,
  action: MenuAction.Value,
  children: List[MenuOptionNode] = List(),
  promptText: Option[String] = None
)
