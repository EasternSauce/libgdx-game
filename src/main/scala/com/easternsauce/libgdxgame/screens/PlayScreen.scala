package com.easternsauce.libgdxgame.screens

import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera}
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import com.badlogic.gdx.{Gdx, Input, Screen}
import com.easternsauce.libgdxgame.LibgdxGame
import com.easternsauce.libgdxgame.ability.hud.InventoryWindow
import com.easternsauce.libgdxgame.area.{Area, AreaGate}
import com.easternsauce.libgdxgame.assets.AssetPaths
import com.easternsauce.libgdxgame.creature.traits.Creature
import com.easternsauce.libgdxgame.creature.{Player, Skeleton}
import com.easternsauce.libgdxgame.items.ItemTemplate
import com.easternsauce.libgdxgame.util.{EsDirection, EsTimer}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class PlayScreen(val game: LibgdxGame) extends Screen {

  private val camera: OrthographicCamera = new OrthographicCamera()
  private val viewport: Viewport =
    new FitViewport(LibgdxGame.VWidth / LibgdxGame.PPM, LibgdxGame.VHeight / LibgdxGame.PPM, camera)

  val atlas: TextureAtlas = new TextureAtlas("assets/atlas/packed_atlas.atlas")

  private val b2DebugRenderer: Box2DDebugRenderer = new Box2DDebugRenderer()

  var player: Player = _

  private val mapLoader: TmxMapLoader = new TmxMapLoader()

  private var areaMap: mutable.Map[String, Area] = _

  private var creatureMap: mutable.Map[String, Creature] = _

  var currentArea: Option[Area] = _

  var gateList: ListBuffer[AreaGate] = ListBuffer()

  val creaturesToMove: mutable.Queue[(Creature, Area, Float, Float)] = mutable.Queue()

  var inventoryWindow: InventoryWindow = _

  ItemTemplate.loadItemTemplates(this)

  loadAreas()
  loadCreatures()
  assignCreaturesToAreas()

  private def loadAreas(): Unit = {
    val area1: Area = new Area(this, mapLoader, AssetPaths.area1Map, "area1", 4.0f)
    val area2: Area = new Area(this, mapLoader, AssetPaths.area2Map, "area2", 4.0f)
    val area3: Area = new Area(this, mapLoader, AssetPaths.area3Map, "area3", 4.0f)

    areaMap = mutable.Map()
    areaMap += (area1.id -> area1)
    areaMap += (area2.id -> area2)
    areaMap += (area3.id -> area3)

    currentArea = Some(area1)

    gateList += AreaGate(this, areaMap("area1"), 197, 15, areaMap("area3"), 17, 2)
    gateList += AreaGate(this, areaMap("area1"), 2, 63, areaMap("area2"), 58, 9)

  }

  def loadCreatures(): Unit = {

    val creature1 = new Player(this, "player")
    val skeleton = new Skeleton(this, "skellie")

    creatureMap = mutable.Map()
    creatureMap += (creature1.id -> creature1)
    creatureMap += (skeleton.id -> skeleton)

    setPlayer(creature1)
  }

  private def setPlayer(creature1: Player): Unit = {
    player = creature1
    inventoryWindow = new InventoryWindow(player)
  }

  def assignCreaturesToAreas(): Unit = {
    creatureMap("player").assignToArea(areaMap("area1"), 30, 30)
    creatureMap("skellie").assignToArea(areaMap("area1"), 34, 42)
  }

  override def show(): Unit = {}

  def update(delta: Float): Unit = {
    EsTimer.updateTimers()

    handleInput()

    currentArea.get.world.step(Math.min(Gdx.graphics.getDeltaTime, 0.15f), 6, 2)

    currentArea.get.creatureMap.values.foreach(_.update())

    if (creaturesToMove.nonEmpty) {
      creaturesToMove.foreach {
        case (creature, area, x, y) =>
          creature.assignToArea(area, x, y)
          creature.passedGateRecently = true
      }

      creaturesToMove.clear()
    }

    adjustCamera(player)

    currentArea.get.setView(camera)

  }

  override def render(delta: Float): Unit = {
    update(delta)

    game.worldBatch.spriteBatch.setProjectionMatrix(camera.combined)

    Gdx.gl.glClearColor(0, 0, 0, 1)

    Gdx.gl.glClear(
      GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
        | (if (Gdx.graphics.getBufferFormat.coverageSampling)
             GL20.GL_COVERAGE_BUFFER_BIT_NV
           else 0)
    )

    currentArea.get.renderBottomLayer()

    game.worldBatch.spriteBatch.begin()

    for (areaGate <- gateList) areaGate.render(game.worldBatch)

    currentArea.get.render(game.worldBatch)

    game.worldBatch.spriteBatch.end()

    currentArea.get.renderTopLayer()

    game.hudBatch.spriteBatch.begin()

    inventoryWindow.render(game.hudBatch)

    game.hudBatch.spriteBatch.end()

    b2DebugRenderer.render(currentArea.get.world, camera.combined)

  }

  override def resize(width: Int, height: Int): Unit = {
    viewport.update(width, height)

  }

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def hide(): Unit = {}

  override def dispose(): Unit = {
    areaMap.values.foreach(_.dispose())
    atlas.dispose()
  }

  def handleInput(): Unit = {

    if (Gdx.input.isKeyJustPressed(Input.Keys.I)) inventoryWindow.visible = !inventoryWindow.visible

    if (inventoryWindow.visible) {
      if (Gdx.input.isButtonJustPressed(Buttons.LEFT)) {
        val x = Gdx.input.getX
        val y = Gdx.input.getY

        inventoryWindow.handleMouseClick(x, y)

      }
    } else {
      if (Gdx.input.isButtonPressed(Buttons.LEFT)) player.currentAttack.perform()

      handlePlayerMovement()
    }

  }

  private def handlePlayerMovement(): Unit = {
    val dirs: List[EsDirection.Value] = List(Input.Keys.D, Input.Keys.A, Input.Keys.W, Input.Keys.S)
      .filter(dir => Gdx.input.isKeyPressed(dir))
      .map {
        case Input.Keys.D => EsDirection.Right
        case Input.Keys.A => EsDirection.Left
        case Input.Keys.W => EsDirection.Up
        case Input.Keys.S => EsDirection.Down
      }

    if (dirs.nonEmpty) player.moveInDirection(dirs)
  }

  def adjustCamera(creature: Player): Unit = {

    val lerp = 30f
    val camPosition = camera.position

    camPosition.x += (creature.pos.x - camPosition.x) * lerp * Gdx.graphics.getDeltaTime
    camPosition.y += (creature.pos.y - camPosition.y) * lerp * Gdx.graphics.getDeltaTime

    camera.update()
  }

  def moveCreature(creature: Creature, destination: Area, x: Float, y: Float): Unit = {
    creaturesToMove.enqueue((creature, destination, x, y))
  }

}
