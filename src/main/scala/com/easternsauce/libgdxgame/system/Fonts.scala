package com.easternsauce.libgdxgame.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.{Batch, BitmapFont}

import scala.language.implicitConversions

object Fonts {
  var defaultFont: BitmapFont = _
  var hugeFont: BitmapFont = _

  class EnrichedBitmapFont(val font: BitmapFont) {
    def draw(batch: Batch, str: CharSequence, x: Float, y: Float, color: Color): Unit = {
      font.setColor(color)
      font.draw(batch, str, x, y)
    }
  }

  def loadFonts(): Unit = {
    def loadFont(assetPath: String, size: Int): BitmapFont = {
      val generator = new FreeTypeFontGenerator(Gdx.files.internal(assetPath))
      val parameter = new FreeTypeFontGenerator.FreeTypeFontParameter
      parameter.size = size
      val font: BitmapFont = generator.generateFont(parameter)
      font.getRegion.getTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear)

      generator.dispose()
      font
    }

    defaultFont = loadFont(Assets.youngSerifFont, 16)
    hugeFont = loadFont(Assets.youngSerifFont, 64)
  }

}
