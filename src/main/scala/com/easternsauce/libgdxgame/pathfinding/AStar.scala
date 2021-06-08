package com.easternsauce.libgdxgame.pathfinding

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object AStar {

  def aStar(start: AStarNode, target: AStarNode): Option[AStarNode] = {
    var closedList = new mutable.PriorityQueue[AStarNode]
    var openList = new mutable.PriorityQueue[AStarNode]
    start.f = start.g + start.calculateHeuristic(target)
    openList += start
    while (openList.nonEmpty) {
      val n = openList.head
      if (n == target) return Some(n)
      for (edge <- n.neighbors) {
        val m = edge.node
        val totalWeight = n.g + edge.weight
        if (!openList.exists(_ == m) && !closedList.exists(_ == m)) {
          m.parent = Some(n)
          m.g = totalWeight
          m.f = m.g + m.calculateHeuristic(target)
          openList += m
        }
        else{
          if (totalWeight < m.g) {
            m.parent = Some(n)
            m.g = totalWeight
            m.f = m.g + m.calculateHeuristic(target)
            if (closedList.toQueue.contains(m)) {
              closedList = closedList.filter(_ != m)
              openList += m
            }
          }
        }
      }

      openList = openList.filter(_ != n)
      closedList += n
    }
    None
  }

  def getPath(target: Option[AStarNode]): List[AStarNode] = {
    var n = target
    if (n.isEmpty) return List[AStarNode]()
    val ids = ListBuffer[AStarNode]()
    while (n.get.parent.isDefined) {
      ids += new AStarNode(n.get.x, n.get.y, n.get.id)
      n = n.get.parent
    }
    ids += new AStarNode(n.get.x, n.get.y, n.get.id)

    ids.reverse.toList
  }
}
