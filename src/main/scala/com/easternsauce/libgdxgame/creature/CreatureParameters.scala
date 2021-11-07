package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.physics.box2d.{Body, Fixture}

case class CreatureParameters(
  body: Option[Body] = None, // mutable!
  fixture: Option[Fixture] = None, // mutable!
  areaId: Option[String] = None,
  bodyCreated: Boolean = false,
  isInitialized: Boolean = false,
  maxStaminaPoints: Float = 100f,
  staminaPoints: Float = 100f,
  staminaOveruse: Boolean = false
)
