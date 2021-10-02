package com.easternsauce.libgdxgame.ability.other

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import com.easternsauce.libgdxgame.ability.composed.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.misc.{Ability, AbilityState}
import com.easternsauce.libgdxgame.ability.parameters.{
  AnimationParameters,
  BodyParameters,
  SoundParameters,
  TimerParameters
}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsBatch
import com.softwaremill.quicklens._

case class ExplodeAbility private (
  override val creature: Creature,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val animationParameters: AnimationParameters =
    AnimationParameters(textureWidth = 64, textureHeight = 64, activeRegionName = "explosion", activeFrameCount = 21),
  override val soundParameters: SoundParameters = SoundParameters(),
  override val bodyParameters: BodyParameters = BodyParameters(),
  override val components: List[AbilityComponent] = List(),
  override val dirVector: Vector2 = new Vector2(0f, 0f)
) extends Ability {
  type Self = ExplodeAbility

  override val id: String = "explode"
  override protected val cooldownTime: Float = 0.8f

  protected val explosionRange: Float = 10f

  override protected val isStoppable: Boolean = false

  override protected lazy val activeTime: Float = 0.9f

  override protected lazy val channelTime: Float = 1.3f

  override def onActiveStart(): Self = {
    super.onActiveStart()

    timerParameters.abilityActiveAnimationTimer.restart()

    // TODO: side effects
    creature.takeStaminaDamage(25f)
    creature.takeLifeDamage(700f, immunityFrames = false, Some(creature), 0, 0, 0)
    Assets.sound(Assets.explosionSound).play(0.07f)

    initBody(creature.pos.x, creature.pos.y)
  }

  override def onUpdateActive(): Self = {
    val activeTimer = timerParameters.activeTimer

    if (bodyParameters.bodyActive && activeTimer.time > 0.1f) {
      destroyBody(creature.area.get.world)
    } else
      this
  }

  override def render(batch: EsBatch): Self = {
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
      renderFrame(activeAnimation.get.currentFrame(time = timerParameters.activeTimer.time, loop = true))
    }

    this
  }

  def initBody(x: Float, y: Float): Self = {
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

    this
      .modify(_.bodyParameters.body)
      .setTo(Some(b2Body))
      .modify(_.bodyParameters.bodyActive)
      .setTo(true)
  }

  def destroyBody(world: World): Self = {
    if (bodyParameters.bodyActive) {
      // TODO: sideeffect?
      if (bodyParameters.body.nonEmpty) world.destroyBody(bodyParameters.body.get)

      this.modify(_.bodyParameters.bodyActive).setTo(false)
    } else
      this
  }

  override def onCollideWithCreature(otherCreature: Creature): Self = {
    // TODO: side effect
    if (!(creature.isEnemy && otherCreature.isEnemy) && otherCreature.isAlive) { // mob can't hurt a mob?
      if (!otherCreature.isImmune) otherCreature.takeLifeDamage(700f, immunityFrames = true, Some(creature), 0, 0, 0)
    }

    this
  }

  override def copy(
    components: List[AbilityComponent],
    lastComponentFinishTime: Float,
    state: AbilityState,
    onCooldown: Boolean,
    soundParameters: SoundParameters,
    timerParameters: TimerParameters,
    bodyParameters: BodyParameters,
    animationParameters: AnimationParameters,
    dirVector: Vector2
  ): Self =
    ExplodeAbility(
      creature = creature,
      state = state,
      onCooldown = onCooldown,
      soundParameters = soundParameters,
      timerParameters = timerParameters,
      animationParameters = animationParameters,
      bodyParameters = bodyParameters,
      components = components,
      dirVector = dirVector
    )
}
