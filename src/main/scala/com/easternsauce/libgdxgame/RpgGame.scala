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
import com.easternsauce.libgdxgame.RpgGame.defaultFont
import com.easternsauce.libgdxgame.area.{Area, AreaGate}
import com.easternsauce.libgdxgame.assets.AssetPaths
import com.easternsauce.libgdxgame.creature.traits.Creature
import com.easternsauce.libgdxgame.creature.{Player, Skeleton, Wolf}
import com.easternsauce.libgdxgame.hud.{InventoryWindow, PlayerHealthStaminaBar}
import com.easternsauce.libgdxgame.items.ItemTemplate
import com.easternsauce.libgdxgame.saving.SavefileManager
import com.easternsauce.libgdxgame.screens.{MainMenuScreen, PlayScreen}
import com.easternsauce.libgdxgame.util.{EsBatch, EsDirection}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Random

class RpgGame extends Game {



  var savefileManager: SavefileManager = _

  var mainMenuScreen: MainMenuScreen = _
  var playScreen: PlayScreen = _

  var allAreaCreaturesMap: mutable.Map[String, Creature] = mutable.Map()

  var player: Creature = _

  var areaMap: mutable.Map[String, Area] = _

  var atlas: TextureAtlas = _

  val camera: OrthographicCamera = new OrthographicCamera()
  val hudCamera: OrthographicCamera = new OrthographicCamera()
  hudCamera.position.set(RpgGame.WindowWidth / 2, RpgGame.WindowHeight / 2, 0)

  val viewport: Viewport =
    new FitViewport(RpgGame.VWidth / RpgGame.PPM, RpgGame.VHeight / RpgGame.PPM, camera)

  val hudViewport: Viewport =
    new FitViewport(RpgGame.WindowWidth, RpgGame.WindowHeight, hudCamera)

  private var b2DebugRenderer: Box2DDebugRenderer = _

  private val mapLoader: TmxMapLoader = new TmxMapLoader()

  var currentArea: Option[Area] = _

  var gateList: ListBuffer[AreaGate] = ListBuffer()

  val creaturesToMove: mutable.Queue[(Creature, Area, Float, Float)] = mutable.Queue()

  var inventoryWindow: InventoryWindow = _

  var healthStaminaBar: PlayerHealthStaminaBar = _

  override def create(): Unit = {
    RpgGame.manager = new AssetManager()

    savefileManager = new SavefileManager(this)

    AssetPaths.sounds.foreach(RpgGame.manager.load(_, classOf[Sound]))
    AssetPaths.textures.foreach(RpgGame.manager.load(_, classOf[Texture]))
    AssetPaths.music.foreach(RpgGame.manager.load(_, classOf[Music]))

    RpgGame.manager.finishLoading()

    atlas = new TextureAtlas("assets/atlas/packed_atlas.atlas")

    defaultFont = RpgGame.loadFont(AssetPaths.youngSerif, 16)

    playScreen = new PlayScreen(this)
    mainMenuScreen = new MainMenuScreen(this)

    b2DebugRenderer = new Box2DDebugRenderer()

    ItemTemplate.loadItemTemplates(this)

    loadAreas()

    if (!savefileManager.savefileFound) {
      loadCreatures()
      assignCreaturesToAreas()
      player.asInstanceOf[Player].generateStartingInventory()
    } else {
      savefileManager.loadGame()
    }

    setScreen(mainMenuScreen)

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

    currentArea = player.area
  }

  def assignCreaturesToAreas(): Unit = {
    allAreaCreaturesMap("player").assignToArea(areaMap("area1"), 30, 30)
    allAreaCreaturesMap("skellie").assignToArea(areaMap("area3"), 34, 42)
    allAreaCreaturesMap("wolf").assignToArea(areaMap("area1"), 34, 42)

  }

  private def loadAreas(): Unit = {
    val area1: Area = new Area(this, mapLoader, AssetPaths.area1Data, "area1", 4.0f)
    val area2: Area = new Area(this, mapLoader, AssetPaths.area2Data, "area2", 4.0f)
    val area3: Area = new Area(this, mapLoader, AssetPaths.area3Data, "area3", 4.0f)

    areaMap = mutable.Map()
    areaMap += (area1.id -> area1)
    areaMap += (area2.id -> area2)
    areaMap += (area3.id -> area3)

    //currentArea = Some(area1)

    gateList += AreaGate(this, areaMap("area1"), 197, 15, areaMap("area3"), 17, 2)
    gateList += AreaGate(this, areaMap("area1"), 2, 63, areaMap("area2"), 58, 9)

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
}

object RpgGame {
  val Random: Random = new Random()

  var manager: AssetManager = _

  val VWidth = 1650
  val VHeight = 864
  val PPM = 32

  val WindowWidth = 1360
  val WindowHeight = 720

  val equipmentTypes = Map(0 -> "weapon", 1 -> "helmet", 2 -> "body", 3 -> "gloves", 4 -> "ring", 5 -> "boots")
  val equipmentTypeIndices: Map[String, Int] = for ((k, v) <- equipmentTypes) yield (v, k)

  val TiledMapCellSize: Float = 2f

  var defaultFont: BitmapFont = _

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
