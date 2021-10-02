package com.easternsauce.libgdxgame.ability.parameters

import com.badlogic.gdx.physics.box2d.Body

case class BodyParameters(
  toRemoveBody: Boolean = false,
  body: Option[Body] = None,
  hitbox: Option[AttackHitbox] = None,
  bodyActive: Boolean = false,
  destroyed: Boolean = false
)
