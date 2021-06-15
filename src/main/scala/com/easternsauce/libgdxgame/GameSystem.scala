package com.easternsauce.libgdxgame

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.{Music, Sound}
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.{BitmapFont, TextureAtlas}
import com.badlogic.gdx.graphics.{OrthographicCamera, Texture}
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import com.badlogic.gdx.{Game, Gdx, Input}
import com.easternsauce.libgdxgame.area.{Area, AreaGate}
import com.easternsauce.libgdxgame.assets.Assets
import com.easternsauce.libgdxgame.creature.{Creature, Player, Skeleton, Wolf}
import com.easternsauce.libgdxgame.hud.{InventoryWindow, LootPickupMenu, NotificationText, PlayerInfoHud}
import com.easternsauce.libgdxgame.items.ItemTemplate
import com.easternsauce.libgdxgame.saving.SavefileManager
import com.easternsauce.libgdxgame.screens.{MainMenuScreen, PlayScreen}
import com.easternsauce.libgdxgame.util.EsDirection

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Random

object GameSystem extends Game {

  val Random: Random = new Random()

  private var assetManager: AssetManager = _

  val PPM = 32

  val WindowWidth = 1360
  val WindowHeight = 720

  val equipmentTypes = Map(
    0 -> "weapon",
    1 -> "weapon",
    2 -> "helmet",
    3 -> "body",
    4 -> "gloves",
    5 -> "ring",
    6 -> "boots",
    7 -> "consumable"
  )
  val equipmentTypeNames = Map(
    0 -> "Primary Weapon",
    1 -> "Secondary Weapon",
    2 -> "Helmet",
    3 -> "Body",
    4 -> "Gloves",
    5 -> "Ring",
    6 -> "Boots",
    7 -> "Consumable"
  )

  val primaryWeaponIndex: Int = (for ((k, v) <- equipmentTypeNames) yield (v, k))("Primary Weapon")
  val secondaryWeaponIndex: Int = (for ((k, v) <- equipmentTypeNames) yield (v, k))("Secondary Weapon")
  val consumableIndex: Int = (for ((k, v) <- equipmentTypeNames) yield (v, k))("Consumable")

  val TiledMapCellSize: Float = 2f

  var defaultFont: BitmapFont = _
  var hugeFont: BitmapFont = _

  var savefileManager: SavefileManager = _

  var mainMenuScreen: MainMenuScreen = _
  var playScreen: PlayScreen = _

  var allAreaCreaturesMap: mutable.Map[String, Creature] = mutable.Map()

  var player: Player = _

  var areaMap: mutable.Map[String, Area] = _

  var atlas: TextureAtlas = _

  val camera: OrthographicCamera = new OrthographicCamera()
  val hudCamera: OrthographicCamera = new OrthographicCamera()
  hudCamera.position.set(WindowWidth / 2, WindowHeight / 2, 0)

  val viewport: Viewport =
    new FitViewport(1650 / PPM, 864 / PPM, camera)

  val hudViewport: Viewport =
    new FitViewport(WindowWidth, WindowHeight, hudCamera)

  var b2DebugRenderer: Box2DDebugRenderer = _

  private val mapLoader: TmxMapLoader = new TmxMapLoader()

  var currentArea: Option[Area] = _

  var gateList: ListBuffer[AreaGate] = ListBuffer()

  val creaturesToMove: mutable.Queue[(Creature, Area, Float, Float)] = mutable.Queue()

  var inventoryWindow: InventoryWindow = _

  var healthStaminaBar: PlayerInfoHud = _

  var debugMode = false

  val notificationText: NotificationText = new NotificationText()

  val playerRespawnTime = 3f

  val lootPickupMenu = new LootPickupMenu()

  def sound(path: String): Sound = {
    assetManager.get(path, classOf[Sound])
  }

  def texture(path: String): Texture = {
    assetManager.get(path, classOf[Texture])
  }

  def music(path: String): Music = {
    assetManager.get(path, classOf[Music])
  }

  override def create(): Unit = {
    assetManager = new AssetManager()

    savefileManager = new SavefileManager()

    Assets.sounds.foreach(assetManager.load(_, classOf[Sound]))
    Assets.textures.foreach(assetManager.load(_, classOf[Texture]))
    Assets.music.foreach(assetManager.load(_, classOf[Music]))

    assetManager.finishLoading()

    atlas = new TextureAtlas("assets/atlas/packed_atlas.atlas")

    defaultFont = loadFont(Assets.youngSerif, 16)
    hugeFont = loadFont(Assets.youngSerif, 64)

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
    val area1: Area = new Area(mapLoader, Assets.area1Data, "area1", 4.0f)
    val area2: Area = new Area(mapLoader, Assets.area2Data, "area2", 4.0f)
    val area3: Area = new Area(mapLoader, Assets.area3Data, "area3", 4.0f)

    areaMap = mutable.Map()
    areaMap += (area1.id -> area1)
    areaMap += (area2.id -> area2)
    areaMap += (area3.id -> area3)

    gateList += AreaGate(areaMap("area1"), 199.5f, 15f, areaMap("area3"), 17f, 2.5f)
    gateList += AreaGate(areaMap("area1"), 2f, 63f, areaMap("area2"), 58f, 9f)

  }

  def mousePositionWindowScaled: Vector3 = {
    val v = new Vector3(Gdx.input.getX, Gdx.input.getY, 0f)
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

  def loadFont(assetPath: String, size: Int): BitmapFont = {
    val generator = new FreeTypeFontGenerator(Gdx.files.internal(assetPath))
    val parameter = new FreeTypeFontGenerator.FreeTypeFontParameter
    parameter.size = size
    val font: BitmapFont = generator.generateFont(parameter)
    font.getRegion.getTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear)

    generator.dispose()
    font
  }

}
