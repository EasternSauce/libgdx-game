package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.physics.box2d.{Body, Fixture}

case class CreatureParameters(body: Option[Body] = None, fixture: Option[Fixture] = None, areaId: Option[String] = None)
