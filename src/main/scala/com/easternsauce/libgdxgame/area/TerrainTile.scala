package com.easternsauce.libgdxgame.area

import com.badlogic.gdx.physics.box2d.Body

case class TerrainTile(pos: (Int, Int, Int), body: Body, flyover: Boolean)
