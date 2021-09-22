package com.easternsauce.libgdxgame.ability.other

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.physics.box2d._
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.misc.{Ability, AbilityState, ActiveAnimation}
import com.easternsauce.libgdxgame.ability.parameters.{AbilityParameters, SoundParameters, TimerParameters}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsBatch

case class ExplodeAbility private (
  creature: Creature,
  state: AbilityState = Inactive,
  onCooldown: Boolean = false,
  soundParameters: SoundParameters = SoundParameters(),
  timerParameters: TimerParameters = TimerParameters(),
  b2Body: Body = null, // TODO: body wrapper
  bodyCreated: Boolean = false
) extends Ability
    with ActiveAnimation {
  val id: String = "explode"
  override protected val cooldownTime: Float = 0.8f

  protected val explosionRange: Float = 10f

  val spriteWidth: Int = 64
  val spriteHeight: Int = 64
  val numOfFrames: Int = 21

  override protected val isStoppable: Boolean = false

  override protected lazy val activeTime: Float = 0.9f

  override protected lazy val channelTime: Float = 1.3f

  setupActiveAnimation(
    regionName = "explosion",
    textureWidth = spriteWidth,
    textureHeight = spriteHeight,
    animationFrameCount = numOfFrames,
    frameDuration = activeTime / numOfFrames
  )

  override protected def onActiveStart(): AbilityParameters = {
    super.onActiveStart()

    timerParameters.abilityActiveAnimationTimer.restart()

    // TODO: side effects
    creature.takeStaminaDamage(25f)
    creature.takeLifeDamage(700f, immunityFrames = false, Some(creature), 0, 0, 0)
    Assets.sound(Assets.explosionSound).play(0.07f)

    initBody(creature.pos.x, creature.pos.y)
  }

  override protected def onUpdateActive(): AbilityParameters = {
    val activeTimer = timerParameters.activeTimer

    if (bodyCreated && activeTimer.time > 0.1f) {
      destroyBody(creature.area.get.world)
    } else
      AbilityParameters()
  }

  override def render(batch: EsBatch): AbilityParameters = {
    def renderFrame(image: TextureRegion): Unit = {

      val scale = explosionRange * 2 / image.getRegionWidth

      val scaledWidth = explosionRange * 2
      val scaledHeight = explosionRange * 2

      batch.spriteBatch.draw(
        image,
        creature.pos.x - scaledWidth / 2f,
        creature.pos.y - scaledHeight / 2f,
        0,
        0,
        image.getRegionWidth.toFloat,
        image.getRegionHeight.toFloat,
        scale,
        scale,
        0f
      )

    }

    // TODO: remove side effect
    if (state == AbilityState.Active) {
      renderFrame(currentActiveAnimationFrame)
    }

    AbilityParameters()
  }

  def initBody(x: Float, y: Float): AbilityParameters = {
    // TODO: side effects

    val bodyDef = new BodyDef()
    bodyDef.position.set(x, y)

    bodyDef.`type` = BodyDef.BodyType.StaticBody
    val b2Body = creature.area.get.world.createBody(bodyDef)
    b2Body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(explosionRange)
    fixtureDef.shape = shape
    fixtureDef.isSensor = true
    b2Body.createFixture(fixtureDef)

    copy(b2Body = b2Body, bodyCreated = true)

    AbilityParameters()
  }

  def destroyBody(world: World): AbilityParameters = {
    if (bodyCreated) {
      // TODO: sideeffect?
      world.destroyBody(b2Body)
      AbilityParameters(bodyCreated = Some(false))
    } else
      AbilityParameters()
  }

  override def onCollideWithCreature(otherCreature: Creature): AbilityParameters = {
    // TODO: side effect
    if (!(creature.isEnemy && otherCreature.isEnemy) && otherCreature.isAlive) { // mob can't hurt a mob?
      if (!otherCreature.isImmune) otherCreature.takeLifeDamage(700f, immunityFrames = true, Some(creature), 0, 0, 0)
    }

    AbilityParameters()
  }

  override def applyParams(params: AbilityParameters): ExplodeAbility = {
    copy(
      creature = params.creature.getOrElse(creature),
      state = params.state.getOrElse(state),
      onCooldown = params.onCooldown.getOrElse(onCooldown),
      soundParameters = params.soundParameters.getOrElse(soundParameters),
      timerParameters = params.timerParameters.getOrElse(timerParameters),
      b2Body = params.b2Body.getOrElse(b2Body),
      bodyCreated = params.bodyCreated.getOrElse(bodyCreated)
    )
  }

  override def updateHitbox(): AbilityParameters = {
    AbilityParameters()
  }

  override protected def onUpdateChanneling(): AbilityParameters = {
    AbilityParameters()
  }

  override protected def onStop(): AbilityParameters = {
    AbilityParameters()
  }

}
