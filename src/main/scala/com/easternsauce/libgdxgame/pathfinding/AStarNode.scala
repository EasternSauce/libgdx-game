package com.easternsauce.libgdxgame.pathfinding

import scala.collection.mutable.ListBuffer

class AStarNode(val x: Int, val y: Int, val id: String) extends Comparable[AStarNode] {
  val neighbors: ListBuffer[AStarEdge] = ListBuffer()

  var parent: Option[AStarNode] = None

  var f: Double = Double.MaxValue
  var g: Double = Double.MaxValue

  override def compareTo(n: AStarNode): Int = f.compare(n.f)
  def addEdge(weight: Int, node: AStarNode): Unit = {
    val newEdge = AStarEdge(weight, node)
    neighbors += newEdge
  }
  def calculateHeuristic(target: AStarNode): Double = {
    (Math.abs(target.x - x) + Math.abs(target.y - y)) * 10
  }

  override def toString(): String = id
}

case class AStarEdge(weight: Int, node: AStarNode)
