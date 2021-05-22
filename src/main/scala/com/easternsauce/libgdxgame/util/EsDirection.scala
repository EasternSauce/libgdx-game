package com.easternsauce.libgdxgame.util

import com.easternsauce.libgdxgame.LibgdxGame

object EsDirection extends Enumeration {
  type WalkDirection = Value
  val Left, Right, Up, Down = Value

  def randomDir(): WalkDirection = {
    LibgdxGame.Random.nextInt(4) match {
      case 0 => Left
      case 1 => Right
      case 2 => Up
      case 3 => Down
    }
  }
}
