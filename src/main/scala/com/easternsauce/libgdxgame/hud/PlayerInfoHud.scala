package com.easternsauce.libgdxgame.hud

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.easternsauce.libgdxgame.GameSystem._
import com.easternsauce.libgdxgame.util.EsBatch

class PlayerInfoHud {

  private var maxHealthRect = new Rectangle(10, 40, 100, 10)
  private var healthRect =
    new Rectangle(10, 40, 100 * player.healthPoints / player.maxHealthPoints, 10)
  private var maxStaminaRect = new Rectangle(10, 25, 100, 10)
  private var staminaRect =
    new Rectangle(10, 25, 100 * player.staminaPoints / player.maxStaminaPoints, 10)

  private val slotSize = 40

  val currentWeaponRect = new Rectangle(20f, maxHealthRect.y + 30f, slotSize, slotSize)

  val swapWeaponRect = new Rectangle(70f, maxHealthRect.y + 30f, slotSize / 2f, slotSize / 2f)

  val consumableRect =
    new Rectangle(maxStaminaRect.x + maxStaminaRect.width + 20f, maxStaminaRect.y, slotSize, slotSize)

  def render(batch: EsBatch): Unit = {
    batch.shapeDrawer.filledRectangle(maxHealthRect, Color.ORANGE)
    batch.shapeDrawer.filledRectangle(healthRect, Color.RED)
    batch.shapeDrawer.filledRectangle(maxStaminaRect, Color.ORANGE)
    batch.shapeDrawer.filledRectangle(staminaRect, Color.GREEN)

    batch.shapeDrawer.filledRectangle(
      currentWeaponRect.x - 3,
      currentWeaponRect.y - 3,
      currentWeaponRect.width + 6,
      currentWeaponRect.height + 6,
      Color.BROWN
    )
    batch.shapeDrawer.filledRectangle(currentWeaponRect, Color.BLACK)

    if (player.equipmentItems.contains(primaryWeaponIndex)) {

      val weapon = player.equipmentItems(primaryWeaponIndex)

      val textureRegion = weapon.template.textureRegion
      batch.spriteBatch.draw(
        textureRegion,
        currentWeaponRect.x,
        currentWeaponRect.y,
        currentWeaponRect.width,
        currentWeaponRect.height
      )

    }

    batch.shapeDrawer.filledRectangle(
      swapWeaponRect.x - 3,
      swapWeaponRect.y - 3,
      swapWeaponRect.width + 6,
      swapWeaponRect.height + 6,
      Color.BROWN
    )
    batch.shapeDrawer.filledRectangle(swapWeaponRect, Color.BLACK)

    if (player.equipmentItems.contains(secondaryWeaponIndex)) {

      val swapWeapon = player.equipmentItems(secondaryWeaponIndex)
      val swapTextureRegion = swapWeapon.template.textureRegion
      batch.spriteBatch.draw(
        swapTextureRegion,
        swapWeaponRect.x,
        swapWeaponRect.y,
        swapWeaponRect.width,
        swapWeaponRect.height
      )

    }

    batch.shapeDrawer.filledRectangle(
      consumableRect.x - 3,
      consumableRect.y - 3,
      consumableRect.width + 6,
      consumableRect.height + 6,
      Color.BROWN
    )
    batch.shapeDrawer.filledRectangle(consumableRect, Color.BLACK)

    if (player.equipmentItems.contains(consumableIndex)) {

      val consumable = player.equipmentItems(consumableIndex)
      val consumableRegion = consumable.template.textureRegion
      batch.spriteBatch.draw(
        consumableRegion,
        consumableRect.x,
        consumableRect.y,
        consumableRect.width,
        consumableRect.height
      )

      if (consumable.quantity > 1) {
        defaultFont.setColor(Color.WHITE)
        defaultFont.draw(batch.spriteBatch, consumable.quantity.toString, consumableRect.x, consumableRect.y + 15)
      }
    }

  }

  def update(): Unit = {
    maxHealthRect = new Rectangle(10, 40, 100, 10)
    healthRect = new Rectangle(
      10,
      40,
      100 * (if (player.healthPoints > player.maxHealthPoints) 1f
             else player.healthPoints / player.maxHealthPoints),
      10
    )
    maxStaminaRect = new Rectangle(10, 25, 100, 10)
    staminaRect = new Rectangle(10, 25, 100 * player.staminaPoints / player.maxStaminaPoints, 10)
  }
}
