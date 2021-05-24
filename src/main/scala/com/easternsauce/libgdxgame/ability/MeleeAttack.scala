package com.easternsauce.libgdxgame.ability

import com.badlogic.gdx.math.{Polygon, Rectangle, Vector2}
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, FixtureDef, PolygonShape}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.{EsBatch, EsPolygon}

trait MeleeAttack extends Attack {
  var scale: Float
  var attackRange: Float
  var body: Body = _
  var hitbox: AttackHitbox = _
  var toRemoveBody = false

  var bodyActive = false
  // IMPORTANT: do NOT use body after already destroyed (otherwise weird behavior occurs, because, for some reason,
  // the reference can STILL be attached to some other random body after destruction, like arrow bodies)

  protected var aimed: Boolean
  protected var width: Float
  protected var height: Float
  protected var knockbackPower: Float
  override protected val isAttack = true

  implicit def rectConversion(s: com.badlogic.gdx.math.Rectangle): Rectangle =
    new Rectangle(s.x, s.y, s.width, s.height)

  override def onActiveStart(): Unit = {
    super.onActiveStart()

    abilityActiveAnimationTimer.restart()

    creature.takeStaminaDamage(15f)

    if (abilitySound.nonEmpty) abilitySound.get.play(0.1f)

    var attackVector = creature.attackVector
    val theta = new Vector2(attackVector.x, attackVector.y).angleDeg()

    if (attackVector.len() > 0f) {
      attackVector = new Vector2(attackVector.x / attackVector.len(), attackVector.y / attackVector.len())
    }

    val attackShiftX = attackVector.x * attackRange
    val attackShiftY = attackVector.y * attackRange

    val attackRectX = attackShiftX + creature.pos.x
    val attackRectY = attackShiftY + creature.pos.y

    val poly = new EsPolygon(new Rectangle(0, 0, width, height))

    poly.setOrigin(0, height / 2)
    poly.setRotation(theta)
    //poly.setPosition(attackRectX, attackRectY)
    poly.translate(0, -height / 2)
    poly.setScale(scale, scale)

    hitbox = AttackHitbox(attackRectX, attackRectY, poly)

    initBody(hitbox)
    bodyActive = true

    toRemoveBody = false
  }

  def initBody(hitbox: AttackHitbox): Unit = {
    val bodyDef = new BodyDef()
    bodyDef.position.set(hitbox.x, hitbox.y)

    bodyDef.`type` = BodyDef.BodyType.KinematicBody
    body = creature.area.get.world.createBody(bodyDef)
    body.setUserData(this)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: PolygonShape = new PolygonShape()
    shape.set(hitbox.polygon.getTransformedVertices)
    fixtureDef.shape = shape
    fixtureDef.isSensor = true
    body.createFixture(fixtureDef)
  }

  override def onUpdateActive(): Unit = {
    super.onUpdateActive()

  }

  override def render(batch: EsBatch): Unit = {
    super.render(batch)

    if (state == AbilityState.Channeling) {
      val image = currentWindupAnimationFrame

      val attackVector = creature.attackVector
      val theta = new Vector2(attackVector.x, attackVector.y).angleDeg()

      batch.spriteBatch.draw(
        image,
        hitbox.x,
        hitbox.y - height / 2,
        0,
        height / 2,
        image.getRegionWidth,
        image.getRegionHeight,
        scale,
        scale,
        theta
      )
    }
    if (state == AbilityState.Active) {
      val image = currentActiveAnimationFrame

      val attackVector = creature.attackVector
      val theta = new Vector2(attackVector.x, attackVector.y).angleDeg()

      batch.spriteBatch.draw(
        image,
        hitbox.x,
        hitbox.y - height / 2,
        0,
        height / 2,
        image.getRegionWidth,
        image.getRegionHeight,
        scale,
        scale,
        theta
      )
    }
  }

  override def onChannellingStart(): Unit = {
    creature.attackVector = creature.facingVector
    abilityWindupAnimationTimer.restart()
    creature.isAttacking = true

    var attackVector = creature.attackVector
    val theta = new Vector2(attackVector.x, attackVector.y).angleDeg()

    if (attackVector.len() > 0f) {
      attackVector = new Vector2(attackVector.x / attackVector.len(), attackVector.y / attackVector.len())
    }

    val attackShiftX = attackVector.x * attackRange
    val attackShiftY = attackVector.y * attackRange

    val attackRectX = attackShiftX + creature.pos.x
    val attackRectY = attackShiftY + creature.pos.y

    val poly = new EsPolygon(new Rectangle(0, 0, width, height))

    poly.setOrigin(0, height / 2)
    poly.setRotation(theta)
    //poly.setPosition(attackRectX, attackRectY)
    poly.translate(0, -height / 2)
    poly.setScale(scale, scale)

    hitbox = AttackHitbox(attackRectX, attackRectY, poly)

  }

  override def update(): Unit = {
    super.update()

    if (body != null && toRemoveBody) {
      body.getWorld.destroyBody(body)
      toRemoveBody = false
      bodyActive = false
    }
  }

  override def updateHitbox(): Unit = {
    super.updateHitbox()

    if (hitbox != null) {
      var attackVector = creature.attackVector

      if (attackVector.len() > 0f) {
        attackVector = new Vector2(attackVector.x / attackVector.len(), attackVector.y / attackVector.len())
      }

      val attackShiftX = attackVector.x * attackRange
      val attackShiftY = attackVector.y * attackRange

      hitbox.x = attackShiftX + creature.pos.x
      hitbox.y = attackShiftY + creature.pos.y

      if (bodyActive) {
        body.setTransform(hitbox.x, hitbox.y, 0f)
      }
    }

  }

  override def onStop() {
    super.onStop()

    creature.isAttacking = false

    if (state == AbilityState.Active)
      toRemoveBody = true // IMPORTANT: ability has to be active
    // if we remove during channeling we could remove it before body is created, causing BOX2D crash

  }

  override def onCollideWithCreature(otherCreature: Creature): Unit = {
    super.onCollideWithCreature(otherCreature)
    if (!(creature.isEnemy && otherCreature.isEnemy)) {
      if (creature != otherCreature && state == AbilityState.Active && !otherCreature.isImmune) {
        otherCreature.takeHealthDamage(
          creature.weaponDamage,
          immunityFrames = true,
          knockbackPower * 100f,
          creature.pos.x,
          creature.pos.y
        )
      }
    }
  }
}

case class AttackHitbox private (var x: Float, var y: Float, polygon: Polygon)
