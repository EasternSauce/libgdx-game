package com.easternsauce.libgdxgame.system

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import com.badlogic.gdx.{Game, Gdx, Input}
import com.easternsauce.libgdxgame.area.{Area, AreaGate}
import com.easternsauce.libgdxgame.bossfight.BossfightManager
import com.easternsauce.libgdxgame.creature.{Creature, Player}
import com.easternsauce.libgdxgame.hud._
import com.easternsauce.libgdxgame.items.ItemTemplate
import com.easternsauce.libgdxgame.music.MusicManager
import com.easternsauce.libgdxgame.saving.SavefileManager
import com.easternsauce.libgdxgame.screens.{MainMenuScreen, PlayScreen}
import com.easternsauce.libgdxgame.system.Fonts.EnrichedBitmapFont
import com.easternsauce.libgdxgame.util.{EsBatch, EsDirection}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions
import scala.util.Random

object GameSystem extends Game {

  val randomGenerator: Random = new Random()

  val savefileManager: SavefileManager = new SavefileManager()

  var mainMenuScreen: MainMenuScreen = _
  var playScreen: PlayScreen = _

  var creatures: mutable.Map[String, Creature] = mutable.Map()

  val treasureLootedList: ListBuffer[(String, String)] = ListBuffer()

  def player: Player = creatures("player").asInstanceOf[Player]

  var areaMap: mutable.Map[String, Area] = _

  val musicManager: MusicManager = new MusicManager()

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

  var currentAreaId: Option[String] = _

  var gateList: ListBuffer[AreaGate] = ListBuffer()

  val creaturesToMove: mutable.Queue[(Creature, Area, Float, Float)] = mutable.Queue()

  var inventoryWindow: InventoryWindow = _

  var lifeStaminaBar: PlayerInfoHud = _

  var debugMode = false

  val notificationText: NotificationText = new NotificationText()

  val lootPickupMenu = new LootPickupMenu()

  val bossfightManager = new BossfightManager()

  def creature(id: String): Creature = {
    creatures(id) //.copy() TODO
  }

  def modifyCreature(id: String, modification: Creature => Creature): Unit = {
    // TODO: queue a creature modification
    creatures(id) = modification(creatures(id))
  }

  def creaturesForArea(id: String): List[Creature] = {
    GameSystem.creatures.values
      .filter(creature => creature.params.areaId.nonEmpty && creature.params.areaId.get == id)
      .toList
  }

  def addCreature(creature: Creature): Unit = {
    GameSystem.creatures += (creature.id -> creature)
  }

  def resetCreaturesInArea(id: String): Unit =
    creatures.filterInPlace { case (_, creature) => !(creature.isEnemy && creature.params.areaId.get == id) }

  def clearCreatures(): Unit = creatures = mutable.Map()

  def saveableCreatures(): List[Creature] = creatures.values.filter(c => c.isPlayer || c.isAlive).toList

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

  def initPlayer(): Unit = {
    inventoryWindow = new InventoryWindow()
    lifeStaminaBar = new PlayerInfoHud()

    currentAreaId = player.params.areaId

    if (areaMap(currentAreaId.get).music.nonEmpty) musicManager.playMusic(areaMap(currentAreaId.get).music.get, 0.2f)
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

    area2.music = Some(Assets.music(Assets.abandonedPlainsMusic))

  }

  def mousePositionWindowScaled: Vector3 = {
    val v = new Vector3(Gdx.input.getX.toFloat, Gdx.input.getY.toFloat, 0f)
    hudCamera.unproject(v)
    v
  }

  def playerMovementDirections: List[EsDirection.Value] = {
    List(Input.Keys.D, Input.Keys.A, Input.Keys.W, Input.Keys.S)
      .filter(dir => Gdx.input.isKeyPressed(dir))
      .map {
        case Input.Keys.D => EsDirection.Right
        case Input.Keys.A => EsDirection.Left
        case Input.Keys.W => EsDirection.Up
        case Input.Keys.S => EsDirection.Down
      }
  }

  def handlePlayerMovement(): Unit = {
    val dirs: List[EsDirection.Value] = playerMovementDirections

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

  def managePlayerRespawns(player: Player): Unit = {
    if (player.respawning && player.respawnTimer.time > Constants.PlayerRespawnTime) {
      player.respawning = false

      player.onRespawn()

      player.life = player.maxLife
      player.staminaPoints = player.maxStaminaPoints
      player.isAttacking = false
      player.staminaOveruse = false
      player.effectMap("staminaRegenerationStopped").stop()
      player.effectMap("poisoned").stop()

      musicManager.stopMusic()

      val areaId = Some(player.playerSpawnPoint.get.area.id)
      currentAreaId = Option(areaId.get)
      areaMap(areaId.get).reset()

      if (areaMap(areaId.get).music.nonEmpty) musicManager.playMusic(areaMap(areaId.get).music.get, 0.2f)

      player.sprite.setRotation(0f)

      modifyCreature(
        player.id,
        player => player.assignToArea(areaId.get, player.playerSpawnPoint.get.posX, player.playerSpawnPoint.get.posY)
      )

    }
  }

  def renderDeathScreen(batch: EsBatch): Unit = {
    if (player.respawning) {
      Fonts.hugeFont.draw(
        batch.spriteBatch,
        "YOU DIED",
        Constants.WindowWidth / 2f - 160,
        Constants.WindowHeight / 2 + 70,
        Color.RED
      )
    }
  }

  def handleCreaturesMovingBetweenAreas(): Unit = {
    if (creaturesToMove.nonEmpty) {
      creaturesToMove.foreach {
        case (creature, area, x, y) =>
          modifyCreature(creature.id, creature => creature.assignToArea(area.id, x, y))
          creature.passedGateRecently = true
      }

      creaturesToMove.clear()
    }
  }

  def setupNewGame(): Unit = {
    val creature = Player("player")

    // TODO: npcs?

    creatures = mutable.Map()
    creatures += (creature.id -> creature)

    modifyCreature(creatures("player").id, creature => creature.assignToArea("area1", 82f, 194f))

    player.playerSpawnPoint = Some(areaMap("area1").playerSpawns.head)

    currentAreaId = Some("area1")

    areaMap("area1").reset()

  }

}
