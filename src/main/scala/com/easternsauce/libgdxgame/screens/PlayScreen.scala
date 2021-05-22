package com.easternsauce.libgdxgame.screens

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Box2DDebugRenderer, World}
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import com.badlogic.gdx.{Gdx, Input, Screen}
import com.easternsauce.libgdxgame.LibgdxGame
import com.easternsauce.libgdxgame.creatures.Player
import com.easternsauce.libgdxgame.util.{EsDirection, EsTimer}

class PlayScreen(val game: LibgdxGame) extends Screen {
  private val camera: OrthographicCamera = new OrthographicCamera()
  private val viewport: Viewport =
    new FitViewport(LibgdxGame.VWidth / LibgdxGame.PPM, LibgdxGame.VHeight / LibgdxGame.PPM, camera)

  val atlas: TextureAtlas = new TextureAtlas("assets/atlas/packed_atlas.atlas")

  val world: World = new World(new Vector2(0, 0), true)
  private val b2DebugRenderer: Box2DDebugRenderer = new Box2DDebugRenderer()

  val player: Player = new Player(this, 30, 30)

  private val mapLoader: TmxMapLoader = new TmxMapLoader()
  private val map: TiledMap = mapLoader.load("assets/areas/area1/tile_map.tmx")
  private val mapScale: Float = 4.0f
  private val tiledMapRenderer: OrthogonalTiledMapRenderer =
    new OrthogonalTiledMapRenderer(map, mapScale / LibgdxGame.PPM)

  override def show(): Unit = {}

  def update(delta: Float): Unit = {
    EsTimer.updateTimers()

    handleInput()

    world.step(Math.min(Gdx.graphics.getDeltaTime, 0.15f), 6, 2)

    player.update()

    adjustCamera(player)

    tiledMapRenderer.setView(camera)

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

    tiledMapRenderer.render()

    b2DebugRenderer.render(world, camera.combined)

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
    world.dispose()
  }

  def handleInput(): Unit = {

    val dirs: List[EsDirection.Value] = List(Input.Keys.RIGHT, Input.Keys.LEFT, Input.Keys.UP, Input.Keys.DOWN)
      .filter(dir => Gdx.input.isKeyPressed(dir))
      .map {
        case Input.Keys.RIGHT => EsDirection.Right
        case Input.Keys.LEFT => EsDirection.Left
        case Input.Keys.UP => EsDirection.Up
        case Input.Keys.DOWN => EsDirection.Down
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
