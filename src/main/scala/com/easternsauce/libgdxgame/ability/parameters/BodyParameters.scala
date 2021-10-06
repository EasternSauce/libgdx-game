package com.easternsauce.libgdxgame.ability.parameters

import com.badlogic.gdx.physics.box2d.Body

case class BodyParameters(
  toBeRemoved: Boolean = false,
  b2Body: Option[Body] = None,
  hitbox: Option[AttackHitbox] = None
)
