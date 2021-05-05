package indigo.shared.scenegraph

import indigo.shared.datatypes.{Point, Radians, Vector2, Rectangle, Depth, Flip}
import indigo.shared.materials.ShaderData
import indigo.shared.shader.StandardShaders

final case class TextBox(
    text: String,
    maxSize: Point,
    position: Point,
    rotation: Radians,
    scale: Vector2,
    depth: Depth,
    ref: Point,
    flip: Flip
) extends EntityNode
    with SpatialModifiers[TextBox] derives CanEqual:

  def withText(newText: String): TextBox =
    this.copy(text = newText)

  def bounds: Rectangle =
    Rectangle(position, maxSize)

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def moveTo(pt: Point): TextBox =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): TextBox =
    moveTo(Point(x, y))
  def withPosition(newPosition: Point): TextBox =
    moveTo(newPosition)

  def moveBy(pt: Point): TextBox =
    this.copy(position = position + pt)
  def moveBy(x: Int, y: Int): TextBox =
    moveBy(Point(x, y))

  def rotateTo(angle: Radians): TextBox =
    this.copy(rotation = angle)
  def rotateBy(angle: Radians): TextBox =
    rotateTo(rotation + angle)
  def withRotation(newRotation: Radians): TextBox =
    rotateTo(newRotation)

  def scaleBy(amount: Vector2): TextBox =
    this.copy(scale = scale * amount)
  def scaleBy(x: Double, y: Double): TextBox =
    scaleBy(Vector2(x, y))
  def withScale(newScale: Vector2): TextBox =
    this.copy(scale = newScale)

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): TextBox =
    this.copy(position = newPosition, rotation = newRotation, scale = newScale)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): TextBox =
    transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

  def withDepth(newDepth: Depth): TextBox =
    this.copy(depth = newDepth)

  def flipHorizontal(isFlipped: Boolean): TextBox =
    this.copy(flip = flip.withHorizontalFlip(isFlipped))
  def flipVertical(isFlipped: Boolean): TextBox =
    this.copy(flip = flip.withVerticalFlip(isFlipped))
  def withFlip(newFlip: Flip): TextBox =
    this.copy(flip = newFlip)

  def withRef(newRef: Point): TextBox =
    this.copy(ref = newRef)
  def withRef(x: Int, y: Int): TextBox =
    withRef(Point(x, y))

  def toShaderData: ShaderData =
    ShaderData(
      StandardShaders.Bitmap.id,
      Nil,
      None,
      None,
      None,
      None
    )
// material.toShaderData

object TextBox:
  def apply(text: String): TextBox =
    TextBox(text, Point(300), Point.zero, Radians.zero, Vector2.one, Depth.Zero, Point.zero, Flip.default)

  def apply(text: String, maxWidth: Int, maxHeight: Int): TextBox =
    TextBox(
      text,
      Point(maxWidth, maxHeight),
      Point.zero,
      Radians.zero,
      Vector2.one,
      Depth.Zero,
      Point.zero,
      Flip.default
    )