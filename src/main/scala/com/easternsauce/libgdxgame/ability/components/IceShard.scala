package com.easternsauce.libgdxgame.ability.components

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, CircleShape, FixtureDef}
import com.easternsauce.libgdxgame.ability.AbilityState
import com.easternsauce.libgdxgame.ability.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.traits.Ability
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsBatch

class IceShard(val mainAbility: Ability, var startX: Float, var startY: Float, val speed: Float, val startTime: Float)
    extends AbilityComponent {
  override val activeTime: Float = 1.5f
  override val channelTime: Float = 0.6f

  var dirVector = new Vector2(1, 0)

  override var state: AbilityState = AbilityState.Inactive
  override var started = false
  override var body: Body = _
  override var destroyed = false

  val radius = 1f

  val spriteWidth = 152
  val spriteHeight = 72

  def start(): Unit = {
    started = true
    state = AbilityState.Channeling
    channelTimer.restart()
  }

  override def onUpdateActive(): Unit = {
    if (started) {
      if (state == AbilityState.Channeling)
        if (channelTimer.time > channelTime) {
          onActiveStart()
        }
      if (state == AbilityState.Active) {
        if (!destroyed && activeTimer.time >= activeTime) {
          body.getWorld.destroyBody(body)
          destroyed = true
        }
        if (activeTimer.time > activeTime) {
          // on active stop
          state = AbilityState.Inactive
        }
        if (!destroyed) {
          body.setLinearVelocity(dirVector.x * speed, dirVector.y * speed)
        }
      }
    }

  }

  private def onActiveStart(): Unit = {
    state = AbilityState.Active
    //Assets.sound(Assets.explosionSound).play(0.01f)
    activeTimer.restart()
    initBody(startX, startY)
    if (mainAbility.creature.asInstanceOf[Enemy].aggroedTarget.nonEmpty) {
      dirVector = mainAbility.creature.facingVector.cpy
    } else {
      dirVector = new Vector2(1.0f, 0.0f)
    }
  }

  def initBody(x: Float, y: Float): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.position.set(x, y)

    bodyDef.`type` = BodyDef.BodyType.DynamicBody
    body = mainAbility.creature.area.get.world.createBody(bodyDef)
    body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(radius)
    fixtureDef.shape = shape
    fixtureDef.isSensor = true
    body.createFixture(fixtureDef)
  }

  override def render(batch: EsBatch): Unit = {
    if (state == AbilityState.Active) {
      val scale = radius * 2 / spriteWidth
      val image = Assets.atlas.findRegion("ice_shard")
      batch.spriteBatch.draw(
        image,
        body.getPosition.x - radius,
        body.getPosition.y - radius,
        0,
        0,
        image.getRegionWidth.toFloat,
        image.getRegionHeight.toFloat,
        scale,
        scale,
        dirVector.angleDeg()
      )
    }
  }

  override def onCollideWithCreature(creature: Creature): Unit = {
    if (!(mainAbility.creature.isEnemy && creature.isEnemy) && creature.isAlive) {
      if (!creature.isImmune) creature.takeLifeDamage(70f, immunityFrames = true)
    }
  }
}
