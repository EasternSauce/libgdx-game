package com.easternsauce.libgdxgame.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import com.badlogic.gdx.{Game, Gdx, Input}
import com.easternsauce.libgdxgame.area.{Area, AreaGate}
import com.easternsauce.libgdxgame.creature.{Creature, Player, Skeleton, Wolf}
import com.easternsauce.libgdxgame.hud.{InventoryWindow, LootPickupMenu, NotificationText, PlayerInfoHud}
import com.easternsauce.libgdxgame.items.ItemTemplate
import com.easternsauce.libgdxgame.saving.SavefileManager
import com.easternsauce.libgdxgame.screens.{MainMenuScreen, PlayScreen}
import com.easternsauce.libgdxgame.system.Fonts.EnrichedBitmapFont
import com.easternsauce.libgdxgame.util.EsDirection

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions
import scala.util.Random

object GameSystem extends Game {

  val randomGenerator: Random = new Random()

  var savefileManager: SavefileManager = new SavefileManager()

  var mainMenuScreen: MainMenuScreen = _
  var playScreen: PlayScreen = _

  var allAreaCreaturesMap: mutable.Map[String, Creature] = mutable.Map()

  var player: Player = _

  var areaMap: mutable.Map[String, Area] = _

  val camera: OrthographicCamera = new OrthographicCamera()
  val hudCamera: OrthographicCamera = new OrthographicCamera()
  hudCamera.position.set(Constants.WindowWidth / 2f, Constants.WindowHeight / 2f, 0)

  val viewport: Viewport =
    new FitViewport(
      Constants.ViewpointWorldWidth / Constants.PPM,
      Constants.ViewpointWorldHeight / Constants.PPM,
      camera
    )

  val hudViewport: Viewport =
    new FitViewport(Constants.WindowWidth.toFloat, Constants.WindowHeight.toFloat, hudCamera)

  var b2DebugRenderer: Box2DDebugRenderer = _

  private val mapLoader: TmxMapLoader = new TmxMapLoader()

  var currentArea: Option[Area] = _

  var gateList: ListBuffer[AreaGate] = ListBuffer()

  val creaturesToMove: mutable.Queue[(Creature, Area, Float, Float)] = mutable.Queue()

  var inventoryWindow: InventoryWindow = _

  var healthStaminaBar: PlayerInfoHud = _

  var debugMode = false

  val notificationText: NotificationText = new NotificationText()

  val lootPickupMenu = new LootPickupMenu()

  implicit def bitmapFontToEnrichedBitmapFont(font: BitmapFont): EnrichedBitmapFont = new EnrichedBitmapFont(font)

  override def create(): Unit = {
    Assets.loadAssets()

    Fonts.loadFonts()

    playScreen = new PlayScreen()
    mainMenuScreen = new MainMenuScreen()

    b2DebugRenderer = new Box2DDebugRenderer()

    ItemTemplate.loadItemTemplates()

    loadAreas()

//    if (!savefileManager.savefileFound) { TODO?
//      loadCreatures()
//      assignCreaturesToAreas()
//      player.asInstanceOf[Player].generateStartingInventory()
//    } else {
//      savefileManager.loadGame()
//    }

    setScreen(mainMenuScreen)

  }

  def loadCreatures(): Unit = {

    val creature = new Player("player")
    val skeleton = new Skeleton("skellie")
    val wolf = new Wolf("wolf")

    allAreaCreaturesMap = mutable.Map()
    allAreaCreaturesMap += (creature.id -> creature)
    allAreaCreaturesMap += (skeleton.id -> skeleton)
    allAreaCreaturesMap += (wolf.id -> wolf)

    setPlayer(creature)
  }

  def setPlayer(creature: Creature): Unit = {
    if (!creature.isPlayer) throw new RuntimeException("creature is not a player")
    player = creature.asInstanceOf[Player]
    inventoryWindow = new InventoryWindow()
    healthStaminaBar = new PlayerInfoHud()

    currentArea = player.area
  }

  def assignCreaturesToAreas(): Unit = {
    allAreaCreaturesMap("player").assignToArea(areaMap("area1"), 30, 30)
    allAreaCreaturesMap("skellie").assignToArea(areaMap("area3"), 34, 42)
    allAreaCreaturesMap("wolf").assignToArea(areaMap("area1"), 34, 42)

  }

  private def loadAreas(): Unit = {
    val area1: Area = new Area(mapLoader, Assets.area1DataLocation, "area1", 4.0f)
    val area2: Area = new Area(mapLoader, Assets.area2DataLocation, "area2", 4.0f)
    val area3: Area = new Area(mapLoader, Assets.area3DataLocation, "area3", 4.0f)

    areaMap = mutable.Map()
    areaMap += (area1.id -> area1)
    areaMap += (area2.id -> area2)
    areaMap += (area3.id -> area3)

    gateList += AreaGate(areaMap("area1"), 199.5f, 15f, areaMap("area3"), 17f, 2.5f)
    gateList += AreaGate(areaMap("area1"), 2f, 63f, areaMap("area2"), 58f, 9f)

  }

  def mousePositionWindowScaled: Vector3 = {
    val v = new Vector3(Gdx.input.getX.toFloat, Gdx.input.getY.toFloat, 0f)
    hudCamera.unproject(v)
    v
  }

  def handlePlayerMovement(): Unit = {
    val dirs: List[EsDirection.Value] = List(Input.Keys.D, Input.Keys.A, Input.Keys.W, Input.Keys.S)
      .filter(dir => Gdx.input.isKeyPressed(dir))
      .map {
        case Input.Keys.D => EsDirection.Right
        case Input.Keys.A => EsDirection.Left
        case Input.Keys.W => EsDirection.Up
        case Input.Keys.S => EsDirection.Down
      }

    if (dirs.nonEmpty) player.moveInDirection(dirs)

    player.sprinting = player.isMoving && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)

  }

  def updateCamera(creature: Creature): Unit = {

    val camPosition = camera.position

    camPosition.x = (math.floor(creature.pos.x * 100) / 100).toFloat
    camPosition.y = (math.floor(creature.pos.y * 100) / 100).toFloat

    camera.update()

  }

  def moveCreature(creature: Creature, destination: Area, x: Float, y: Float): Unit = {
    creaturesToMove.enqueue((creature, destination, x, y))
  }

}
