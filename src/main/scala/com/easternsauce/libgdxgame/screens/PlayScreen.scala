package com.easternsauce.libgdxgame.screens

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera}
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import com.badlogic.gdx.{Gdx, Input, Screen}
import com.easternsauce.libgdxgame.LibgdxGame
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.creatures.Player
import com.easternsauce.libgdxgame.util.{EsDirection, EsTimer}

import scala.collection.mutable

class PlayScreen(val game: LibgdxGame) extends Screen {
  private val camera: OrthographicCamera = new OrthographicCamera()
  private val viewport: Viewport =
    new FitViewport(LibgdxGame.VWidth / LibgdxGame.PPM, LibgdxGame.VHeight / LibgdxGame.PPM, camera)

  val atlas: TextureAtlas = new TextureAtlas("assets/atlas/packed_atlas.atlas")

  private val b2DebugRenderer: Box2DDebugRenderer = new Box2DDebugRenderer()

  var player: Player = _

  private val mapLoader: TmxMapLoader = new TmxMapLoader()

  private var areaMap: mutable.Map[String, Area] = _

  private var currentArea: Area = _

  loadAreas()
  loadCreatures()

  private def loadAreas(): Unit = {
    val area1: Area = new Area(mapLoader, "assets/areas/area1/tile_map.tmx", "area1", 4.0f)

    areaMap = mutable.Map()
    areaMap += (area1.id -> area1)

    currentArea = area1
  }

  def loadCreatures(): Unit = {
    player = new Player(this, 30, 30, areaMap("area1"))
  }

  override def show(): Unit = {}

  def update(delta: Float): Unit = {
    EsTimer.updateTimers()

    handleInput()

    currentArea.world.step(Math.min(Gdx.graphics.getDeltaTime, 0.15f), 6, 2)

    player.update()

    adjustCamera(player)

    currentArea.setView(camera)

  }

  override def render(delta: Float): Unit = {
    update(delta)

    game.batch.setProjectionMatrix(camera.combined)

    Gdx.gl.glClearColor(0, 0, 0, 1)

    Gdx.gl.glClear(
      GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
        | (if (Gdx.graphics.getBufferFormat.coverageSampling)
             GL20.GL_COVERAGE_BUFFER_BIT_NV
           else 0)
    )

    currentArea.render()

    b2DebugRenderer.render(currentArea.world, camera.combined)

    game.batch.begin()
    player.draw(game.batch)
    game.batch.end()

  }

  override def resize(width: Int, height: Int): Unit = {
    viewport.update(width, height)

  }

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def hide(): Unit = {}

  override def dispose(): Unit = {
    areaMap.values.foreach(_.dispose())
  }

  def handleInput(): Unit = {

    val dirs: List[EsDirection.Value] = List(Input.Keys.RIGHT, Input.Keys.LEFT, Input.Keys.UP, Input.Keys.DOWN)
      .filter(dir => Gdx.input.isKeyPressed(dir))
      .map {
        case Input.Keys.RIGHT => EsDirection.Right
        case Input.Keys.LEFT  => EsDirection.Left
        case Input.Keys.UP    => EsDirection.Up
        case Input.Keys.DOWN  => EsDirection.Down
      }

    if (dirs.nonEmpty) player.moveInDirection(dirs, 8000f)

  }

  def adjustCamera(creature: Player): Unit = {

    val lerp = 30f
    val camPosition = camera.position

    camPosition.x += (creature.posX - camPosition.x) * lerp * Gdx.graphics.getDeltaTime
    camPosition.y += (creature.posY - camPosition.y) * lerp * Gdx.graphics.getDeltaTime

    camera.update()
  }
}
