package com.easternsauce.libgdxgame.ability.composed.components

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, CircleShape, FixtureDef}
import com.easternsauce.libgdxgame.ability.misc.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.misc.{Ability, AbilityState}
import com.easternsauce.libgdxgame.ability.parameters.{AnimationParameters, TimerParameters}
import com.easternsauce.libgdxgame.animation.Animation
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}
import com.easternsauce.libgdxgame.util.EsBatch

class Bubble(
  val mainAbility: Ability,
  var startX: Float,
  var startY: Float,
  val radius: Float,
  val speed: Float,
  val startTime: Float,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val animationParameters: AnimationParameters =
    AnimationParameters(textureWidth = 64, textureHeight = 64, activeRegionName = "bubble", activeFrameCount = 2)
) extends AbilityComponent {
  override lazy val activeTime: Float = 1.5f
  override lazy val channelTime: Float = 0.6f

  val loopTime = 0.2f

  var dirVector = new Vector2(1, 0)

  override var state: AbilityState = AbilityState.Inactive
  override var started = false
  override var body: Body = _
  override var destroyed = false

  override val activeAnimation: Option[Animation] = Some(
    Animation.activeAnimationFromParameters(animationParameters, activeTime)
  )
  override val channelAnimation: Option[Animation] = None

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
    timerParameters.abilityActiveAnimationTimer.restart()
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
      val spriteWidth = 64
      val scale = radius * 2 / spriteWidth
      val image = activeAnimation.get.currentFrame(time = timerParameters.activeTimer.time, loop = true)
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
        0.0f
      )
    }
  }

  override def onCollideWithCreature(creature: Creature): Unit = {
    if (!(mainAbility.creature.isEnemy && creature.isEnemy) && creature.isAlive) {
      if (!creature.isImmune) creature.takeLifeDamage(70f, immunityFrames = true)
    }
  }

}
