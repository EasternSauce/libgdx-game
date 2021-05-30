package com.easternsauce.libgdxgame.ability.hud

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.easternsauce.libgdxgame.screens.PlayScreen
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable

class InventoryWindow(val playScreen: PlayScreen) {

  var visible = false
  var inventoryItemBeingMoved: Option[Int] = None
  var equipmentItemBeingMoved: Option[Int] = None

  private val background: Rectangle = new Rectangle(
    (Gdx.graphics.getWidth * 0.2).toInt,
    (Gdx.graphics.getHeight * 0.3).toInt,
    (Gdx.graphics.getWidth * 0.6).toInt,
    (Gdx.graphics.getHeight * 0.6).toInt
  )

  private val totalRows = 5
  private val totalColumns = 8
  private val inventoryTotalSlots = totalRows * totalColumns
  private val margin = 10
  private val slotSize = 40
  private val spaceBetweenSlots = 5
  private val spaceBeforeEquipment = 100

  private val inventoryWidth = margin + (slotSize + spaceBetweenSlots) * totalColumns
  private val inventoryHeight = margin + (slotSize + spaceBetweenSlots) * totalRows

  private val inventoryRectangles: mutable.Map[Int, Rectangle] = mutable.Map()

  private val equipmentTotalSlots = 6
  private val equipmentRectangles: mutable.Map[Int, Rectangle] = mutable.Map()

  private val equipmentTypes = Map(0 -> "weapon", 1 -> "helmet", 2 -> "body", 3 -> "gloves", 4 -> "ring", 5 -> "boots")

  defineSlotRectangles()

  private def defineSlotRectangles(): Unit = {
    for (i <- 0 until inventoryTotalSlots) {
      inventoryRectangles += (i -> new Rectangle(
        inventorySlotPositionX(i),
        inventorySlotPositionY(i),
        slotSize,
        slotSize
      ))
    }

    for (i <- 0 until equipmentTotalSlots) {
      equipmentRectangles += (i -> new Rectangle(
        equipmentSlotPositionX(i),
        equipmentSlotPositionY(i),
        slotSize,
        slotSize
      ))
    }

  }

  def render(batch: EsBatch): Unit = {
    if (visible) {
      batch.shapeDrawer.filledRectangle(background, Color.LIGHT_GRAY)

      inventoryRectangles.values.foreach(rect => batch.shapeDrawer.filledRectangle(rect, Color.DARK_GRAY))

      equipmentRectangles.foreach {
        case (index, rect) =>
          batch.shapeDrawer.filledRectangle(rect, Color.DARK_GRAY)
          playScreen.defaultFont.draw(
            batch.spriteBatch,
            equipmentTypes(index).capitalize + ":",
            rect.x - slotSize / 2 - 70,
            rect.y + slotSize / 2 + 7
          )
      }

      renderPlayerItems(batch)
    }

  }

  def renderPlayerItems(batch: EsBatch): Unit = {
    val items = playScreen.player.inventoryItems
    val equipment = playScreen.player.equipmentItems

    items
      .filterNot {
        case (index, _) => if (inventoryItemBeingMoved.nonEmpty) inventoryItemBeingMoved.get == index else false
      }
      .foreach {
        case (index, item) =>
          val textureRegion = item.template.textureRegion
          val x = inventorySlotPositionX(index)
          val y = inventorySlotPositionY(index)
          batch.spriteBatch.draw(textureRegion, x, y, slotSize, slotSize)
      }

    equipment
      .filterNot {
        case (index, _) => if (equipmentItemBeingMoved.nonEmpty) equipmentItemBeingMoved.get == index else false
      }
      .foreach {
        case (index, item) =>
          val textureRegion = item.template.textureRegion
          val x = equipmentSlotPositionX(index)
          val y = equipmentSlotPositionY(index)
          batch.spriteBatch.draw(textureRegion, x, y, slotSize, slotSize)
      }

    if (inventoryItemBeingMoved.nonEmpty) {
      batch.spriteBatch.draw(
        items(inventoryItemBeingMoved.get).template.textureRegion,
        Gdx.input.getX - slotSize / 2,
        Gdx.graphics.getHeight - Gdx.input.getY - slotSize / 2,
        slotSize,
        slotSize
      )
    }

    if (equipmentItemBeingMoved.nonEmpty) {
      batch.spriteBatch.draw(
        equipment(equipmentItemBeingMoved.get).template.textureRegion,
        Gdx.input.getX - slotSize / 2,
        Gdx.graphics.getHeight - Gdx.input.getY - slotSize / 2,
        slotSize,
        slotSize
      )
    }
  }

  private def inventorySlotPositionX(index: Int): Float = {
    val currentColumn = index % totalColumns
    background.x + margin + (slotSize + spaceBetweenSlots) * currentColumn
  }

  private def inventorySlotPositionY(index: Int): Float = {
    val currentRow = index / totalColumns
    background.y + background.height - (slotSize + margin + (slotSize + spaceBetweenSlots) * currentRow)
  }

  private def equipmentSlotPositionX(index: Int): Float = {
    background.x + inventoryWidth + margin + spaceBeforeEquipment
  }

  private def equipmentSlotPositionY(index: Int): Float = {
    background.y + background.height - (slotSize + margin + (slotSize + spaceBetweenSlots) * index)
  }

  def handleMouseClick(x: Int, y: Int): Unit = {
    var inventorySlotClicked: Option[Int] = None
    var equipmentSlotClicked: Option[Int] = None

    inventoryRectangles
      .filter { case (_, v) => v.contains(x, Gdx.graphics.getHeight - y) }
      .foreach { case (k, _) => inventorySlotClicked = Some(k) }

    equipmentRectangles
      .filter { case (_, v) => v.contains(x, Gdx.graphics.getHeight - y) }
      .foreach { case (k, _) => equipmentSlotClicked = Some(k) }

    (inventoryItemBeingMoved, equipmentItemBeingMoved, inventorySlotClicked, equipmentSlotClicked) match {
      case (Some(from), _, Some(to), _) => swapInventorySlotContent(from, to)
      case (Some(from), _, _, Some(to)) => swapBetweenInventoryAndEquipment(from, to)
      case (_, Some(from), Some(to), _) => swapBetweenInventoryAndEquipment(to, from)
      case (_, Some(from), _, Some(to)) => swapEquipmentSlotContent(from, to)
      case (_, _, Some(index), _) =>
        if (playScreen.player.inventoryItems.contains(index)) inventoryItemBeingMoved = Some(index)
      case (_, _, _, Some(index)) =>
        if (playScreen.player.equipmentItems.contains(index)) equipmentItemBeingMoved = Some(index)
      case _ =>
        inventoryItemBeingMoved = None
        equipmentItemBeingMoved = None
    }

  }

  def swapInventorySlotContent(fromIndex: Int, toIndex: Int): Unit = {
    val itemFrom = playScreen.player.inventoryItems.get(fromIndex)
    val itemTo = playScreen.player.inventoryItems.get(toIndex)

    val temp = itemTo

    if (itemFrom.nonEmpty) playScreen.player.inventoryItems(toIndex) = itemFrom.get
    else playScreen.player.inventoryItems.remove(toIndex)
    if (temp.nonEmpty) playScreen.player.inventoryItems(fromIndex) = temp.get
    else playScreen.player.inventoryItems.remove(fromIndex)

    inventoryItemBeingMoved = None
    equipmentItemBeingMoved = None
  }

  def swapEquipmentSlotContent(fromIndex: Int, toIndex: Int): Unit = {
    val itemFrom = playScreen.player.equipmentItems.get(fromIndex)
    val itemTo = playScreen.player.equipmentItems.get(toIndex)

    val temp = itemTo

    val fromEquipmentTypeMatches =
      itemFrom.nonEmpty && itemFrom.get.template.parameters("equipableType").stringValue.get == equipmentTypes(toIndex)
    val toEquipmentTypeMatches =
      itemTo.nonEmpty && itemTo.get.template.parameters("equipableType").stringValue.get == equipmentTypes(fromIndex)

    if (fromEquipmentTypeMatches && toEquipmentTypeMatches) {
      if (itemFrom.nonEmpty) playScreen.player.equipmentItems(toIndex) = itemFrom.get
      else playScreen.player.equipmentItems.remove(toIndex)
      if (temp.nonEmpty) playScreen.player.equipmentItems(fromIndex) = temp.get
      else playScreen.player.equipmentItems.remove(fromIndex)
    }

    inventoryItemBeingMoved = None
    equipmentItemBeingMoved = None
  }

  def swapBetweenInventoryAndEquipment(inventoryIndex: Int, equipmentIndex: Int): Unit = {
    val inventoryItem = playScreen.player.inventoryItems.get(inventoryIndex)
    val equipmentItem = playScreen.player.equipmentItems.get(equipmentIndex)

    val temp = equipmentItem

    val equipmentTypeMatches =
      inventoryItem.nonEmpty && inventoryItem.get.template
        .parameters("equipableType")
        .stringValue
        .get == equipmentTypes(equipmentIndex)

    if (inventoryItem.isEmpty || equipmentTypeMatches) {
      if (temp.nonEmpty) playScreen.player.inventoryItems(inventoryIndex) = temp.get
      else playScreen.player.inventoryItems.remove(inventoryIndex)
      if (inventoryItem.nonEmpty) playScreen.player.equipmentItems(equipmentIndex) = inventoryItem.get
      else playScreen.player.equipmentItems.remove(equipmentIndex)
    }

    inventoryItemBeingMoved = None
    equipmentItemBeingMoved = None
  }
}
