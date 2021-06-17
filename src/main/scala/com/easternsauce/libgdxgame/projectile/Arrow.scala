package com.easternsauce.libgdxgame.projectile

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, CircleShape, FixtureDef}
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.easternsauce.libgdxgame.area.{Area, TerrainTile}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets
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

  private val arrowImage: Image = new Image(Assets.atlas.findRegion("arrow"))
  var markedForDeletion: Boolean = false
  var b2body: Body = _
  var isActive: Boolean = true
  var landed: Boolean = false
  val arrowLandedTimer: EsTimer = EsTimer()

  val knockbackPower = 10f

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
          b2body.setLinearVelocity(new Vector2(0f, 0f))
          isActive = false
          arrowLandedTimer.stop()
        }
      }

      b2body.setLinearVelocity(dirVector.x * directionalVelocity, dirVector.y * directionalVelocity)

      arrowImage.setX(b2body.getPosition.x - width / 2f)
      arrowImage.setY(b2body.getPosition.y - height / 2f)

      val margin = 2
      if (
        !((b2body.getPosition.x >= 0 - margin
          && b2body.getPosition.x < area.width + margin)
          && (b2body.getPosition.y >= 0 - margin
            && b2body.getPosition.y < area.height + margin))
      ) markedForDeletion = true

    }
  }

  def onCollideWithCreature(creature: Creature): Unit = {
    if (!(shooter.isEnemy && creature.isEnemy) && isActive) {

      if (shooter != creature && creature.alive && !creature.immune) {
        creature.takeLifeDamage(damage, immunityFrames = true, Some(shooter), knockbackPower, startX, startY)
        markedForDeletion = true
      }
    }
  }

  def onCollideWithTerrain(areaTile: TerrainTile): Unit = {
    if (!areaTile.flyover) {
      landed = true
      arrowLandedTimer.restart()
    }
  }

  def initBody(x: Float, y: Float): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.position.set(x, y)

    bodyDef.`type` = BodyDef.BodyType.DynamicBody
    b2body = area.world.createBody(bodyDef)
    b2body.setUserData(this)

    val radius = 0.315f

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(radius)
    fixtureDef.shape = shape
    fixtureDef.isSensor = true
    b2body.createFixture(fixtureDef)
  }

  def destroyBody(): Unit = {
    area.world.destroyBody(b2body)
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
  ) = new Arrow(startX, startY, area, dirVector.cpy(), arrowList, tiledMap, creatures, shooter)
}
