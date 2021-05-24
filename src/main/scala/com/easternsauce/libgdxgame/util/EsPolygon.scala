package com.easternsauce.libgdxgame.util

import com.badlogic.gdx.math.Rectangle

import scala.collection.mutable

class EsPolygon private extends com.badlogic.gdx.math.Polygon {

  def this(vertices: Array[Float]) {
    this()

    setVertices(vertices)
  }

  def this(rect: Rectangle) {
    this()

    val vertices: mutable.ArrayBuffer[Float] = mutable.ArrayBuffer()

    vertices ++= List(rect.x, rect.y)
    vertices ++= List(rect.x, rect.y + rect.height)
    vertices ++= List(rect.x + rect.width, rect.y + rect.height)
    vertices ++= List(rect.x + rect.width, rect.y)

    setVertices(vertices.toArray)
  }

}

object EsPolygon {
  def apply(vertices: Array[Float]) = new EsPolygon(vertices)
  def apply(rect: Rectangle) = new EsPolygon(rect)
}
