package com.easternsauce.libgdxgame.projectile

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, CircleShape, FixtureDef}
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.easternsauce.libgdxgame.LibgdxGame
import com.easternsauce.libgdxgame.area.{Area, AreaTile}
import com.easternsauce.libgdxgame.assets.AssetPaths
import com.easternsauce.libgdxgame.creature.traits.Creature
import com.easternsauce.libgdxgame.util.{EsBatch, EsTimer}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Arrow private (
  var startX: Float,
  var startY: Float,
  val area: Area,
  var dirVector: Vector2,
  var arrowList: ListBuffer[Arrow],
  val tiledMap: TiledMap,
  val creatures: mutable.Map[String, Creature],
  val shooter: Creature
) {

  val damage: Float = shooter.weaponDamage

  private val arrowTexture: Texture = LibgdxGame.manager.get(AssetPaths.arrowTexture, classOf[Texture])
  private val arrowImage: Image = new Image(arrowTexture)
  var markedForDeletion: Boolean = false
  var body: Body = _
  var isActive: Boolean = true
  var landed: Boolean = false
  val arrowLandedTimer: EsTimer = EsTimer()

  val directionalVelocity = 48f

  val width = 1.25f
  val height = 1.25f

  arrowImage.setWidth(width)
  arrowImage.setHeight(height)
  arrowImage.setOriginX(width / 2)
  arrowImage.setOriginY(height / 2)
  arrowImage.rotateBy(dirVector.angleDeg())

  initBody(startX, startY)

  def render(batch: EsBatch): Unit = {

    arrowImage.draw(batch.spriteBatch, 1.0f)
  }

  def update(): Unit = {
    if (isActive) {
      if (landed) {
        if (arrowLandedTimer.time > 0.02f) {
          body.setLinearVelocity(new Vector2(0f, 0f))
          isActive = false
          arrowLandedTimer.stop()
        }
      }

      body.setLinearVelocity(dirVector.x * directionalVelocity, dirVector.y * directionalVelocity)

      arrowImage.setX(body.getPosition.x - width / 2f)
      arrowImage.setY(body.getPosition.y - height / 2f)

      val margin = 2
      if (
        !((body.getPosition.x >= 0 - margin
          && body.getPosition.x < area.width + margin)
          && (body.getPosition.y >= 0 - margin
            && body.getPosition.y < area.height + margin))
      ) markedForDeletion = true

    }
  }

  def onCollideWithCreature(creature: Creature): Unit = {
    if (!(shooter.isEnemy && creature.isEnemy) && isActive) {

      if (shooter != creature && creature.isAlive && !creature.isImmune) {
        creature.takeHealthDamage(damage, immunityFrames = true, 7000f, startX, startY)
        markedForDeletion = true
      }
    }
  }

  def onCollideWithTerrain(areaTile: AreaTile): Unit = {
    if (!areaTile.flyover) {
      landed = true
      arrowLandedTimer.restart()
    }
  }

  def initBody(x: Float, y: Float): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.position.set(x, y)

    bodyDef.`type` = BodyDef.BodyType.DynamicBody
    body = area.world.createBody(bodyDef)
    body.setUserData(this)

    val radius = 0.315f

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(radius)
    fixtureDef.shape = shape
    fixtureDef.isSensor = true
    body.createFixture(fixtureDef)
  }
}

object Arrow {
  def apply(
    startX: Float,
    startY: Float,
    area: Area,
    dirVector: Vector2,
    arrowList: ListBuffer[Arrow],
    tiledMap: TiledMap,
    creatures: mutable.Map[String, Creature],
    shooter: Creature
  ) = new Arrow(startX, startY, area, dirVector, arrowList, tiledMap, creatures, shooter)
}
