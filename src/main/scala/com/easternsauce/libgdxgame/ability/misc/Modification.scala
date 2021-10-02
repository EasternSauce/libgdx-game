package com.easternsauce.libgdxgame.ability.misc

trait Modification {
  type Self >: this.type <: Modification

  def modifyIf(condition: => Boolean)(codeBlock: => Self): Self = {
    if (condition) {
      codeBlock
    } else {
      this
    }
  }
}
