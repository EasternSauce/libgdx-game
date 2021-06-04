package com.easternsauce.libgdxgame.screens

import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.{BitmapFont, TextureAtlas}
import com.badlogic.gdx.graphics.{Color, GL20, OrthographicCamera}
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import com.badlogic.gdx.{Gdx, Input, Screen}
import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.area.{Area, AreaGate}
import com.easternsauce.libgdxgame.assets.AssetPaths
import com.easternsauce.libgdxgame.creature.traits.Creature
import com.easternsauce.libgdxgame.creature.{Player, Skeleton, Wolf}
import com.easternsauce.libgdxgame.hud.{InventoryWindow, PlayerHealthStaminaBar}
import com.easternsauce.libgdxgame.items.ItemTemplate
import com.easternsauce.libgdxgame.projectile.Arrow
import com.easternsauce.libgdxgame.util.{EsDirection, EsTimer}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class PlayScreen(val game: RpgGame) extends Screen {

  private val camera: OrthographicCamera = new OrthographicCamera()
  private val hudCamera: OrthographicCamera = new OrthographicCamera()
  hudCamera.position.set(RpgGame.WindowWidth / 2, RpgGame.WindowHeight / 2, 0)

  val viewport: Viewport =
    new FitViewport(RpgGame.VWidth / RpgGame.PPM, RpgGame.VHeight / RpgGame.PPM, camera)

  val hudViewport: Viewport =
    new FitViewport(RpgGame.WindowWidth, RpgGame.WindowHeight, hudCamera)

  val atlas: TextureAtlas = new TextureAtlas("assets/atlas/packed_atlas.atlas")

  private val b2DebugRenderer: Box2DDebugRenderer = new Box2DDebugRenderer()

  var player: Creature = _

  private val mapLoader: TmxMapLoader = new TmxMapLoader()

  var areaMap: mutable.Map[String, Area] = _

  var allAreaCreaturesMap: mutable.Map[String, Creature] = mutable.Map()

  var currentArea: Option[Area] = _

  var gateList: ListBuffer[AreaGate] = ListBuffer()

  val creaturesToMove: mutable.Queue[(Creature, Area, Float, Float)] = mutable.Queue()

  var inventoryWindow: InventoryWindow = _

  val defaultFont: BitmapFont = loadFont(AssetPaths.youngSerif, 16)

  var healthStaminaBar: PlayerHealthStaminaBar = _

  ItemTemplate.loadItemTemplates(this)

  loadAreas()

  if (!game.savefileManager.savefileFound) {
    loadCreatures()
    assignCreaturesToAreas()
    player.asInstanceOf[Player].generateStartingInventory()
  } else {
    game.savefileManager.loadGame(this)
  }

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
    val wolf = new Wolf(this, "wolf")

    allAreaCreaturesMap = mutable.Map()
    allAreaCreaturesMap += (creature1.id -> creature1)
    allAreaCreaturesMap += (skeleton.id -> skeleton)
    allAreaCreaturesMap += (wolf.id -> wolf)

    setPlayer(creature1)
  }

  def setPlayer(creature1: Creature): Unit = {
    player = creature1
    inventoryWindow = new InventoryWindow(this)
    healthStaminaBar = new PlayerHealthStaminaBar(this)
  }

  def assignCreaturesToAreas(): Unit = {
    allAreaCreaturesMap("player").assignToArea(areaMap("area1"), 30, 30)
    allAreaCreaturesMap("skellie").assignToArea(areaMap("area3"), 34, 42)
    allAreaCreaturesMap("wolf").assignToArea(areaMap("area1"), 34, 42)

  }

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

    adjustCamera(player)

    currentArea.get.setView(camera)

    healthStaminaBar.update()

  }

  override def render(delta: Float): Unit = {
    update(delta)

    game.worldBatch.spriteBatch.setProjectionMatrix(camera.combined)
    game.hudBatch.spriteBatch.setProjectionMatrix(hudCamera.combined)

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

    currentArea.get.arrowList.foreach((arrow: Arrow) => arrow.render(game.worldBatch))

    game.worldBatch.spriteBatch.end()

    currentArea.get.renderTopLayer()

    game.hudBatch.spriteBatch.begin()

    inventoryWindow.render(game.hudBatch)

    healthStaminaBar.render(game.hudBatch)

    defaultFont.setColor(Color.WHITE)
    defaultFont.draw(game.hudBatch.spriteBatch, Gdx.graphics.getFramesPerSecond + " fps", 3, RpgGame.WindowHeight - 3)

    game.hudBatch.spriteBatch.end()

//    b2DebugRenderer.render(currentArea.get.world, camera.combined)

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

  def handleInput(): Unit = {

    if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) game.savefileManager.saveGame(this)

    if (Gdx.input.isKeyJustPressed(Input.Keys.I)) inventoryWindow.visible = !inventoryWindow.visible

    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) if (inventoryWindow.visible) inventoryWindow.visible = false

    if (inventoryWindow.visible) {
      if (Gdx.input.isButtonJustPressed(Buttons.LEFT)) {
        inventoryWindow.handleMouseClicked()

      }
    } else {
      if (Gdx.input.isButtonPressed(Buttons.LEFT)) player.currentAttack.perform()

    }

    handlePlayerMovement()

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

    player.sprinting = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)

  }

  def adjustCamera(creature: Creature): Unit = {

    val camPosition = camera.position

    camPosition.x = creature.pos.x
    camPosition.y = creature.pos.y

    camera.update()

  }

  def moveCreature(creature: Creature, destination: Area, x: Float, y: Float): Unit = {
    creaturesToMove.enqueue((creature, destination, x, y))
  }

  def loadFont(assetPath: String, size: Int): BitmapFont = {
    val generator = new FreeTypeFontGenerator(Gdx.files.internal(assetPath))
    val parameter = new FreeTypeFontGenerator.FreeTypeFontParameter
    parameter.size = size
    val font: BitmapFont = generator.generateFont(parameter)
    font.getRegion.getTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear)

    generator.dispose()
    font
  }

  def mousePositionWindowScaled: Vector3 = {
    val v = new Vector3(Gdx.input.getX, Gdx.input.getY, 0f)
    hudCamera.unproject(v)
    v
  }
}
