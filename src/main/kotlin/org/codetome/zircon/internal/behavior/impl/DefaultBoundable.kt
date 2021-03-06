package org.codetome.zircon.internal.behavior.impl

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.behavior.Boundable
import org.codetome.zircon.api.behavior.Movable
import java.awt.Point
import java.awt.Rectangle

/**
 * A [DefaultBoundable] has no offset.
 */
class DefaultBoundable(private val size: Size,
                       private var position: Position = Position.DEFAULT_POSITION)
    : Boundable, Movable {

    private var rect = refreshRect()

    override fun getPosition() = position

    override fun getBoundableSize() = size

    override fun moveTo(position: Position) =
            if (this.position == position) {
                false
            } else {
                this.position = position
                this.rect = refreshRect()
                true
            }

    override fun intersects(boundable: Boundable) = rect.intersects(
            Rectangle(
                    boundable.getPosition().column,
                    boundable.getPosition().row,
                    boundable.getBoundableSize().columns,
                    boundable.getBoundableSize().rows))

    override fun containsPosition(position: Position): Boolean {
        return rect.contains(Point(position.column, position.row))
    }

    override fun containsBoundable(boundable: Boundable) = rect.contains(
            Rectangle(
                    boundable.getPosition().column,
                    boundable.getPosition().row,
                    boundable.getBoundableSize().columns,
                    boundable.getBoundableSize().rows))

    private fun refreshRect() =
            Rectangle(position.column, position.row, getBoundableSize().columns, getBoundableSize().rows)
}