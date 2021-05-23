package com.easternsauce.libgdxgame.area

import com.badlogic.gdx.physics.box2d.Body

case class AreaTile(pos: (Int, Int, Int), body: Body, traversable: Boolean, flyover: Boolean)
