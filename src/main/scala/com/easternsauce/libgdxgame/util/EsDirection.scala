package com.easternsauce.libgdxgame.util

import com.easternsauce.libgdxgame.GameSystem._

object EsDirection extends Enumeration {
  def isHorizontal(value: EsDirection.Value): Boolean = {
    value match {
      case Left  => true
      case Right => true
      case _     => false
    }
  }

  def isVertical(value: EsDirection.Value): Boolean = {
    value match {
      case Up   => true
      case Down => true
      case _    => false
    }
  }

  type WalkDirection = Value
  val Left, Right, Up, Down = Value

  def randomDir(): WalkDirection = {
    Random.nextInt(4) match {
      case 0 => Left
      case 1 => Right
      case 2 => Up
      case 3 => Down
    }
  }
}
