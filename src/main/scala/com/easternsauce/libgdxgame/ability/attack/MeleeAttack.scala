package com.easternsauce.libgdxgame.ability.attack

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.{Rectangle, Vector2}
import com.easternsauce.libgdxgame.ability.composed.components.AbilityComponent
import com.easternsauce.libgdxgame.ability.misc.AbilityState.{AbilityState, Inactive}
import com.easternsauce.libgdxgame.ability.misc.{Ability, _}
import com.easternsauce.libgdxgame.ability.parameters._
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.{Constants, GameSystem}
import com.easternsauce.libgdxgame.util.{EsBatch, EsPolygon}
import com.softwaremill.quicklens.ModifyPimp

abstract class MeleeAttack(
  override val creature: Creature,
  override val state: AbilityState = Inactive,
  override val onCooldown: Boolean = false,
  override val components: List[AbilityComponent] = List(),
  override val lastComponentFinishTime: Float = 0,
  override val timerParameters: TimerParameters = TimerParameters(),
  override val soundParameters: SoundParameters = SoundParameters(),
  override val bodyParameters: BodyParameters = BodyParameters(),
  override val animationParameters: AnimationParameters = AnimationParameters(),
  override val dirVector: Vector2 = new Vector2(0f, 0f)
) extends Ability(
      creature = creature,
      state = state,
      onCooldown = onCooldown,
      components = components,
      lastComponentFinishTime = lastComponentFinishTime,
      timerParameters = timerParameters,
      soundParameters = soundParameters,
      bodyParameters = bodyParameters,
      animationParameters = animationParameters,
      dirVector = dirVector
    )
    with PhysicalHitbox {
  type Self >: this.type <: MeleeAttack

  implicit def toMeleeAttack(ability: Ability): Self = ability.asInstanceOf[Self]

  val attackRange: Float

  // IMPORTANT: do NOT use body after already destroyed (otherwise weird behavior occurs, because, for some reason,
  // the reference can STILL be attached to some other random body after destruction, like arrow bodies)

  protected val aimed: Boolean
  protected val spriteWidth: Int
  protected val spriteHeight: Int

  protected def width: Float = spriteWidth.toFloat / Constants.PPM

  protected def height: Float = spriteHeight.toFloat / Constants.PPM

  protected val knockbackVelocity: Float
  override protected val isAttack = true

  protected val baseChannelTime: Float
  protected val baseActiveTime: Float

  override protected lazy val activeTime: Float = baseActiveTime
  override protected lazy val channelTime: Float = baseChannelTime / attackSpeed

  def attackSpeed: Float =
    if (creature.isWeaponEquipped) creature.currentWeapon.template.attackSpeed.get
    else 1.4f

  def attackScale: Float =
    if (creature.isWeaponEquipped) creature.currentWeapon.template.attackScale.get
    else 1.4f

  override def onActiveStart(): Self = {
    val ability = super.onActiveStart()

    // TODO: clean up sideeffects

    timerParameters.abilityActiveAnimationTimer.restart()

    creature.takeStaminaDamage(15f)

    val attackVector = creature.attackVector

    val normalizedAttackVector =
      if (attackVector.len() > 0f) {
        new Vector2(attackVector.x / attackVector.len(), attackVector.y / attackVector.len())
      } else attackVector

    val theta = new Vector2(attackVector.x, attackVector.y).angleDeg()

    val attackShiftX = normalizedAttackVector.x * attackRange
    val attackShiftY = normalizedAttackVector.y * attackRange

    val attackRectX = attackShiftX + creature.pos.x
    val attackRectY = attackShiftY + creature.pos.y

    val poly = new EsPolygon(new Rectangle(0, 0, width, height))

    poly.setOrigin(0, height / 2)
    poly.setRotation(theta)
    poly.translate(0, -height / 2)
    poly.setScale(attackScale, attackScale)

    val hitbox = Some(AttackHitbox(attackRectX, attackRectY, poly))

    val body = if (creature.area.nonEmpty) {
      initBody(creature.area.get.world, bodyParameters.hitbox.get)
    } else None

    ability
      .modify(_.bodyParameters.body)
      .setTo(body)
      .modify(_.bodyParameters.toRemoveBody)
      .setTo(false)
      .modify(_.bodyParameters.bodyActive)
      .setTo(true)
      .modify(_.bodyParameters.hitbox)
      .setTo(hitbox)
  }

  override def render(batch: EsBatch): Self = {
    // TODO: remove side effect
    def renderFrame(image: TextureRegion): Unit = {
      val attackVector = creature.attackVector
      val theta = new Vector2(attackVector.x, attackVector.y).angleDeg()

      if (bodyParameters.hitbox.nonEmpty) {
        batch.spriteBatch.draw(
          image,
          bodyParameters.hitbox.get.x,
          bodyParameters.hitbox.get.y - height / 2,
          0,
          height / 2,
          width,
          height,
          attackScale,
          attackScale,
          theta
        )
      }
    }

    if (state == AbilityState.Channeling && channelAnimation.nonEmpty)
      renderFrame(
        channelAnimation.get.currentFrame(time = timerParameters.abilityChannelAnimationTimer.time, loop = true)
      )
    if (state == AbilityState.Active && activeAnimation.nonEmpty)
      renderFrame(
        activeAnimation.get.currentFrame(time = timerParameters.abilityActiveAnimationTimer.time, loop = true)
      )

    this
  }

  override def onChannellingStart(): Self = {
    val ability: Self = super.onChannellingStart()

    // TODO: sideeffects

    creature.attackVector = creature.facingVector.cpy()
    timerParameters.abilityChannelAnimationTimer.restart()
    creature.isAttacking = true

    val attackVector = creature.attackVector
    val theta = new Vector2(attackVector.x, attackVector.y).angleDeg()

    val normalizedAttackVector =
      if (attackVector.len() > 0f) {
        new Vector2(attackVector.x / attackVector.len(), attackVector.y / attackVector.len())
      } else attackVector

    val attackShiftX = normalizedAttackVector.x * attackRange
    val attackShiftY = normalizedAttackVector.y * attackRange

    val attackRectX = attackShiftX + creature.pos.x
    val attackRectY = attackShiftY + creature.pos.y

    val poly = new EsPolygon(new Rectangle(0, 0, width, height))

    poly.setOrigin(0, height / 2)
    poly.setRotation(theta)
    poly.translate(0, -height / 2)
    poly.setScale(attackScale, attackScale)

    val hitbox = Some(AttackHitbox(attackRectX, attackRectY, poly))

    ability.modify(_.bodyParameters.hitbox).setTo(hitbox)
  }

  override def update(): Self = {

    val ability: Self = super.update()

    if (bodyParameters.body.nonEmpty && bodyParameters.toRemoveBody) {
      bodyParameters.body.get.getWorld.destroyBody(bodyParameters.body.get)

      ability
        .modify(_.bodyParameters.toRemoveBody)
        .setTo(false)
        .modify(_.bodyParameters.bodyActive)
        .setTo(false)
    } else
      ability

  }

  override def updateHitbox(): Self = {
    if (bodyParameters.hitbox.nonEmpty) {

      val attackVector = creature.attackVector

      val normalizedAttackVector =
        if (attackVector.len() > 0f) {
          new Vector2(attackVector.x / attackVector.len(), attackVector.y / attackVector.len())
        } else attackVector

      val attackShiftX = normalizedAttackVector.x * attackRange
      val attackShiftY = normalizedAttackVector.y * attackRange

      val newHitbox = Some(
        AttackHitbox(attackShiftX + creature.pos.x, attackShiftY + creature.pos.y, bodyParameters.hitbox.get.polygon)
      )

      // TODO: remove sideeffect
      if (bodyParameters.bodyActive) {
        bodyParameters.body.get.setTransform(bodyParameters.hitbox.get.x, bodyParameters.hitbox.get.y, 0f)
      }

      this
        .modify(_.bodyParameters.hitbox)
        .setTo(newHitbox)

    } else
      this

  }

  override def onStop(): Self = {
    creature.isAttacking = false

    // IMPORTANT: ability has to be active
    // if we remove during channeling we could remove it before body is created, causing BOX2D crash
    if (state == AbilityState.Active) {
      this
        .modify(_.bodyParameters.toRemoveBody)
        .setTo(true)
    } else
      this
  }

  override def onCollideWithCreature(otherCreature: Creature): Self = {

    // TODO: remove sideeffect
    if (!(creature.isEnemy && otherCreature.isEnemy)) {
      if (creature != otherCreature && !otherCreature.isImmune) {
        otherCreature.takeLifeDamage(
          damage = creature.weaponDamage,
          immunityFrames = true,
          dealtBy = Some(creature),
          attackKnockbackVelocity = knockbackVelocity,
          sourceX = creature.pos.x,
          sourceY = creature.pos.y
        )
        val random = GameSystem.randomGenerator.nextFloat()

        if (creature.isWeaponEquipped && random < creature.currentWeapon.template.poisonChance.get) {
          otherCreature.activateEffect("poisoned", 10f)
          otherCreature.poisonTickTimer.restart()
        }
      }
    }

    this
  }

  override def copy(
    creature: Creature = creature,
    state: AbilityState = state,
    onCooldown: Boolean = onCooldown,
    components: List[AbilityComponent] = components,
    lastComponentFinishTime: Float = lastComponentFinishTime,
    soundParameters: SoundParameters = soundParameters,
    timerParameters: TimerParameters = timerParameters,
    bodyParameters: BodyParameters = bodyParameters,
    animationParameters: AnimationParameters = animationParameters,
    dirVector: Vector2 = dirVector
  ): Self
}
