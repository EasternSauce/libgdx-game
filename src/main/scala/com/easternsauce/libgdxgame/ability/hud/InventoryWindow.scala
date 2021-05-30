package com.easternsauce.libgdxgame.ability.hud

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.easternsauce.libgdxgame.creature.Player
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable

class InventoryWindow(val player: Player) {

  var visible = false
  var itemBeingMoved: Option[Int] = None

  private val background: Rectangle = new Rectangle(
    (Gdx.graphics.getWidth * 0.2).toInt,
    (Gdx.graphics.getHeight * 0.3).toInt,
    (Gdx.graphics.getWidth * 0.6).toInt,
    (Gdx.graphics.getHeight * 0.6).toInt
  )

  private val totalRows = 4
  private val totalColumns = 6
  private val totalSlots = totalRows * totalColumns
  private val margin = 10

  private val slotSize = 40

  private val spaceBetweenSlots = 5

  private val slotRectangles: mutable.Map[Int, Rectangle] = mutable.Map()

  defineSlotRectangles()

  private def defineSlotRectangles(): Unit = {
    for (i <- 0 until totalSlots) {
      slotRectangles += (i -> new Rectangle(slotPositionX(i), slotPositionY(i), slotSize, slotSize))
    }
  }

  def render(batch: EsBatch): Unit = {
    if (visible) {
      batch.shapeDrawer.filledRectangle(background, Color.LIGHT_GRAY)

      slotRectangles.values.foreach(rect => batch.shapeDrawer.filledRectangle(rect, Color.DARK_GRAY))

      renderPlayerItems(batch)
    }

  }

  def renderPlayerItems(batch: EsBatch): Unit = {
    val items = player.inventoryItems

    items.filterNot { case (index, _) => if (itemBeingMoved.nonEmpty) itemBeingMoved.get == index else false }.foreach {
      case (index, item) =>
        val textureRegion = item.template.textureRegion
        val x = slotPositionX(index)
        val y = slotPositionY(index)
        batch.spriteBatch.draw(textureRegion, x, y, slotSize, slotSize)
    }

    if (itemBeingMoved.nonEmpty) {
      batch.spriteBatch.draw(
        items(itemBeingMoved.get).template.textureRegion,
        Gdx.input.getX - slotSize / 2,
        Gdx.graphics.getHeight - Gdx.input.getY - slotSize / 2,
        slotSize,
        slotSize
      )
    }
  }

  private def slotPositionX(index: Int) = {
    val currentColumn = index % totalColumns
    background.x + margin + (slotSize + spaceBetweenSlots) * currentColumn
  }

  private def slotPositionY(index: Int) = {
    val currentRow = index / totalColumns
    background.y + background.height - (slotSize + margin + (slotSize + spaceBetweenSlots) * currentRow)
  }

  def handleMouseClick(x: Int, y: Int): Unit = {
    var slotClicked: Option[Int] = None

    slotRectangles
      .filter { case (_, v) => v.contains(x, Gdx.graphics.getHeight - y) }
      .foreach { case (k, _) => slotClicked = Some(k) }

    if (itemBeingMoved.isEmpty && slotClicked.nonEmpty) {
      if (player.inventoryItems.contains(slotClicked.get)) itemBeingMoved = slotClicked
    } else {
      if (slotClicked.nonEmpty) {
        swapSlotContent(itemBeingMoved.get, slotClicked.get)
      }
      itemBeingMoved = None
    }

  }

  def swapSlotContent(fromIndex: Int, toIndex: Int): Unit = {
    val itemFrom = player.inventoryItems.get(fromIndex)
    val itemTo = player.inventoryItems.get(toIndex)

    val temp = itemTo

    if (itemFrom.nonEmpty) player.inventoryItems(toIndex) = itemFrom.get
    else player.inventoryItems.remove(toIndex)
    if (temp.nonEmpty) player.inventoryItems(fromIndex) = temp.get
    else player.inventoryItems.remove(fromIndex)

  }

}
