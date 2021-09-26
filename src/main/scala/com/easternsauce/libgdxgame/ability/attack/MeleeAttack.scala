package com.easternsauce.libgdxgame.ability.attack

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.{Rectangle, Vector2}
import com.easternsauce.libgdxgame.ability.misc._
import com.easternsauce.libgdxgame.ability.parameters.AttackHitbox
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.{Constants, GameSystem}
import com.easternsauce.libgdxgame.util.{EsBatch, EsPolygon}

trait MeleeAttack extends Ability with PhysicalHitbox with ActiveAnimation with WindupAnimation {

  implicit def toMeleeAttack(ability: Ability): MeleeAttack = ability.asInstanceOf[MeleeAttack]

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

  override def onActiveStart(): MeleeAttack = {
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

    val newBodyParameters = bodyParameters.copy(body = body, toRemoveBody = false, bodyActive = true, hitbox = hitbox)

    ability
      .makeCopy(bodyParameters = newBodyParameters)
  }

  override def render(batch: EsBatch): MeleeAttack = {
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

    if (state == AbilityState.Channeling) renderFrame(currentWindupAnimationFrame)
    if (state == AbilityState.Active) renderFrame(currentActiveAnimationFrame)

    this
  }

  override def onChannellingStart(): MeleeAttack = {
    val ability: MeleeAttack = super.onChannellingStart()

    // TODO: sideeffects

    creature.attackVector = creature.facingVector.cpy()
    timerParameters.abilityWindupAnimationTimer.restart()
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

    ability.makeCopy(bodyParameters = bodyParameters.copy(hitbox = hitbox))
  }

  override def update(): MeleeAttack = {

    val ability: MeleeAttack = super.update()

    if (bodyParameters.body.nonEmpty && bodyParameters.toRemoveBody) {
      bodyParameters.body.get.getWorld.destroyBody(bodyParameters.body.get)

      ability.makeCopy(bodyParameters = bodyParameters.copy(toRemoveBody = false, bodyActive = false))
    } else
      ability

  }

  override def updateHitbox(): MeleeAttack = {
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
        .makeCopy(bodyParameters = bodyParameters.copy(hitbox = newHitbox))

    } else
      this

  }

  override def onStop(): MeleeAttack = {
    creature.isAttacking = false

    // IMPORTANT: ability has to be active
    // if we remove during channeling we could remove it before body is created, causing BOX2D crash
    if (state == AbilityState.Active) {
      makeCopy(bodyParameters = bodyParameters.copy(toRemoveBody = true))
    } else
      this
  }

  override def onCollideWithCreature(otherCreature: Creature): MeleeAttack = {

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

}
