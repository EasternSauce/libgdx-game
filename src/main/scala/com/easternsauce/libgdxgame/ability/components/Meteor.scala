package com.easternsauce.libgdxgame.ability.components

import com.badlogic.gdx.physics.box2d.{Body, BodyDef, CircleShape, FixtureDef}
import com.easternsauce.libgdxgame.ability.AbilityState
import com.easternsauce.libgdxgame.ability.AbilityState.AbilityState
import com.easternsauce.libgdxgame.ability.traits.{Ability, ActiveAnimation, WindupAnimation}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsBatch

class Meteor(
  val mainAbility: Ability,
  val startTime: Float,
  val posX: Float,
  val posY: Float,
  val radius: Float,
  speed: Float
) extends AbilityComponent
    with WindupAnimation
    with ActiveAnimation {

  override protected val activeTime: Float = 1.8f / speed
  override protected val channelTime: Float = 1.2f / speed

  override var state: AbilityState = AbilityState.Inactive
  override var started = false
  override var body: Body = _
  override var destroyed = false

  val spriteWidth = 64
  val spriteHeight = 64
  val numOfActiveFrames = 21
  val numOfChannelingFrames = 7

  setupActiveAnimation(
    regionName = "explosion",
    textureWidth = spriteHeight,
    textureHeight = spriteHeight,
    animationFrameCount = numOfActiveFrames,
    frameDuration = activeTime / numOfActiveFrames
  )

  setupWindupAnimation(
    regionName = "explosion_windup",
    textureWidth = spriteHeight,
    textureHeight = spriteHeight,
    animationFrameCount = numOfChannelingFrames,
    frameDuration = channelTime / numOfChannelingFrames
  )

  def start(): Unit = {
    started = true
    state = AbilityState.Channeling
    channelTimer.restart()
    abilityWindupAnimationTimer.restart()
  }

  override def onUpdateActive(): Unit = {
    if (started) {
      if (state == AbilityState.Channeling)
        if (channelTimer.time > channelTime) {
          onActiveStart()
        }
      if (state == AbilityState.Active) {
        if (!destroyed && activeTimer.time >= 0.2f) {
          body.getWorld.destroyBody(body)
          destroyed = true
        }
        if (activeTimer.time > activeTime) {
          // on active stop
          state = AbilityState.Inactive
        }
      }
    }

  }

  private def onActiveStart(): Unit = {
    state = AbilityState.Active
    Assets.sound(Assets.explosionSound).play(0.01f)
    abilityActiveAnimationTimer.restart()
    activeTimer.restart()
    initBody(posX, posY)
  }

  def initBody(x: Float, y: Float): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.position.set(x, y)

    bodyDef.`type` = BodyDef.BodyType.StaticBody
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
    if (state == AbilityState.Channeling) {
      val spriteWidth = 64
      val scale = radius * 2 / spriteWidth
      val image = currentWindupAnimationFrame
      batch.spriteBatch.draw(
        image,
        posX - radius,
        posY - radius,
        0,
        0,
        image.getRegionWidth,
        image.getRegionHeight,
        scale,
        scale,
        0.0f
      )
    }
    if (state == AbilityState.Active) {
      val spriteWidth = 64
      val scale = radius * 2 / spriteWidth
      val image = currentActiveAnimationFrame
      batch.spriteBatch.draw(
        image,
        posX - radius,
        posY - radius,
        0,
        0,
        image.getRegionWidth,
        image.getRegionHeight,
        scale,
        scale,
        0.0f
      )
    }
  }

  override def onCollideWithCreature(creature: Creature): Unit = {
    if (!(mainAbility.creature.isEnemy && creature.isEnemy) && creature.isAlive) {
      if (!creature.isImmune) creature.takeLifeDamage(40f, immunityFrames = true)
    }
  }
}
