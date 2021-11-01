package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.physics.box2d.{Body, Fixture}
import com.easternsauce.libgdxgame.util.EsDirection

case class CreatureParameters(
  body: Option[Body] = None, // mutable!
  fixture: Option[Fixture] = None, // mutable!
  areaId: Option[String] = None,
  bodyCreated: Boolean = false,
  isInitialized: Boolean = false,
  currentDirection: EsDirection.Value = EsDirection.Down
)
