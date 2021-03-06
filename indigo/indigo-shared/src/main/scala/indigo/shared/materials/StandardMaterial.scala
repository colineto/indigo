package indigo.shared.materials

import indigo.shared.assets.AssetName
import indigo.shared.shader.StandardShaders

import indigo.shared.shader.Uniform
import indigo.shared.shader.UniformBlock
import indigo.shared.shader.ShaderPrimitive.{vec3, vec4}
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Fill
import indigo.shared.shader.ShaderPrimitive
import indigo.shared.datatypes.RGB

sealed trait StandardMaterial extends Material

object StandardMaterial {

  final case class Bitmap(diffuse: AssetName) extends StandardMaterial {
    val hash: String =
      diffuse.value

    def toShaderData: ShaderData =
      ShaderData(
        StandardShaders.Bitmap.id,
        None,
        Some(diffuse),
        None,
        None,
        None
      )
  }

  final case class ImageEffects(diffuse: AssetName, alpha: Double, tint: RGBA, overlay: Fill, saturation: Double) extends StandardMaterial {

    def withAlpha(newAlpha: Double): ImageEffects =
      this.copy(alpha = newAlpha)

    def withTint(newTint: RGBA): ImageEffects =
      this.copy(tint = newTint)
    def withTint(newTint: RGB): ImageEffects =
      this.copy(tint = newTint.toRGBA)

    def withOverlay(newOverlay: Fill): ImageEffects =
      this.copy(overlay = newOverlay)

    def withSaturation(newSaturation: Double): ImageEffects =
      this.copy(saturation = newSaturation)

    val hash: String =
      diffuse.value + alpha.toString() + tint.hash + overlay.hash

    def toShaderData: ShaderData = {
      val gradientUniforms: List[(Uniform, ShaderPrimitive)] =
        overlay match {
          case Fill.Color(color) =>
            val c = vec4(color.r, color.g, color.b, color.a)
            List(
              Uniform("GRADIENT_FROM_TO")    -> vec4(0.0d),
              Uniform("GRADIENT_FROM_COLOR") -> c,
              Uniform("GRADIENT_TO_COLOR")   -> c
            )

          case Fill.LinearGradient(fromPoint, fromColor, toPoint, toColor) =>
            List(
              Uniform("GRADIENT_FROM_TO")    -> vec4(fromPoint.x.toDouble, fromPoint.y.toDouble, toPoint.x.toDouble, toPoint.y.toDouble),
              Uniform("GRADIENT_FROM_COLOR") -> vec4(fromColor.r, fromColor.g, fromColor.b, fromColor.a),
              Uniform("GRADIENT_TO_COLOR")   -> vec4(toColor.r, toColor.g, toColor.b, toColor.a)
            )

          case Fill.RadialGradient(fromPoint, fromColor, toPoint, toColor) =>
            List(
              Uniform("GRADIENT_FROM_TO")    -> vec4(fromPoint.x.toDouble, fromPoint.y.toDouble, toPoint.x.toDouble, toPoint.y.toDouble),
              Uniform("GRADIENT_FROM_COLOR") -> vec4(fromColor.r, fromColor.g, fromColor.b, fromColor.a),
              Uniform("GRADIENT_TO_COLOR")   -> vec4(toColor.r, toColor.g, toColor.b, toColor.a)
            )
        }

      val overlayType: Double =
        overlay match {
          case _: Fill.Color          => 0.0
          case _: Fill.LinearGradient => 1.0
          case _: Fill.RadialGradient => 2.0
        }

      ShaderData(
        StandardShaders.ImageEffects.id,
        Some(
          UniformBlock(
            "IndigoImageEffectsData",
            List(
              Uniform("ALPHA_SATURATION_OVERLAYTYPE") -> vec3(alpha, saturation, overlayType),
              Uniform("TINT")                         -> vec4(tint.r, tint.g, tint.b, tint.a)
            ) ++ gradientUniforms
          )
        ),
        Some(diffuse),
        None,
        None,
        None
      )
    }
  }
  object ImageEffects {
    def apply(diffuse: AssetName): ImageEffects =
      ImageEffects(diffuse, 1.0, RGBA.None, Fill.Color.default, 1.0)

    def apply(diffuse: AssetName, alpha: Double): ImageEffects =
      ImageEffects(diffuse, alpha, RGBA.None, Fill.Color.default, 1.0)
  }

  // final case class Textured(diffuse: AssetName, isLit: Boolean) extends StandardMaterial {

  //   def withDiffuse(newDiffuse: AssetName): Textured =
  //     this.copy(diffuse = newDiffuse)

  //   def lit: Textured =
  //     this.copy(isLit = true)

  //   def unlit: Textured =
  //     this.copy(isLit = false)

  //   def toShaderData: ShaderData =
  //     ShaderData(
  //       StandardShaders.Basic,
  //       Map(),
  //       Some(diffuse),
  //       None,
  //       None,
  //       None
  //     )

  //   lazy val hash: String =
  //     diffuse.value + (if (isLit) "1" else "0")
  // }
  // object Textured {
  //   def apply(diffuse: AssetName): Textured =
  //     new Textured(diffuse, false)

  //   def unapply(t: Textured): Option[(AssetName, Boolean)] =
  //     Some((t.diffuse, t.isLit))
  // }

  // final case class Lit(
  //     albedo: AssetName,
  //     emissive: Option[Texture],
  //     normal: Option[Texture],
  //     specular: Option[Texture],
  //     isLit: Boolean
  // ) extends StandardMaterial {

  //   def withAlbedo(newAlbedo: AssetName): Lit =
  //     this.copy(albedo = newAlbedo)

  //   def withEmission(emissiveAssetName: AssetName, amount: Double): Lit =
  //     this.copy(emissive = Some(Texture(emissiveAssetName, amount)))

  //   def withNormal(normalAssetName: AssetName, amount: Double): Lit =
  //     this.copy(normal = Some(Texture(normalAssetName, amount)))

  //   def withSpecular(specularAssetName: AssetName, amount: Double): Lit =
  //     this.copy(specular = Some(Texture(specularAssetName, amount)))

  //   def lit: Lit =
  //     this.copy(isLit = true)

  //   def unlit: Lit =
  //     this.copy(isLit = false)

  //   lazy val hash: String =
  //     albedo.value +
  //       emissive.map(_.hash).getOrElse("_") +
  //       normal.map(_.hash).getOrElse("_") +
  //       specular.map(_.hash).getOrElse("_") +
  //       (if (isLit) "1" else "0")

  //   def toShaderData: ShaderData =
  //     ShaderData(
  //       StandardShaders.Basic,
  //       Map(),
  //       Some(albedo),
  //       emissive.map(_.assetName),
  //       normal.map(_.assetName),
  //       specular.map(_.assetName)
  //     )
  // }
  // object Lit {
  //   def apply(
  //       albedo: AssetName,
  //       emissive: Option[Texture],
  //       normal: Option[Texture],
  //       specular: Option[Texture]
  //   ): Lit =
  //     new Lit(albedo, emissive, normal, specular, true)

  //   def apply(
  //       albedo: AssetName
  //   ): Lit =
  //     new Lit(albedo, None, None, None, true)

  //   def apply(
  //       albedo: AssetName,
  //       emissive: AssetName
  //   ): Lit =
  //     new Lit(
  //       albedo,
  //       Some(Texture(emissive, 1.0d)),
  //       None,
  //       None,
  //       true
  //     )

  //   def apply(
  //       albedo: AssetName,
  //       emissive: AssetName,
  //       normal: AssetName
  //   ): Lit =
  //     new Lit(
  //       albedo,
  //       Some(Texture(emissive, 1.0d)),
  //       Some(Texture(normal, 1.0d)),
  //       None,
  //       true
  //     )

  //   def apply(
  //       albedo: AssetName,
  //       emissive: AssetName,
  //       normal: AssetName,
  //       specular: AssetName
  //   ): Lit =
  //     new Lit(
  //       albedo,
  //       Some(Texture(emissive, 1.0d)),
  //       Some(Texture(normal, 1.0d)),
  //       Some(Texture(specular, 1.0d)),
  //       true
  //     )

  //   def fromAlbedo(albedo: AssetName): Lit =
  //     new Lit(albedo, None, None, None, true)
  // }

}

// final case class Texture(assetName: AssetName, amount: Double) {
//   def hash: String =
//     assetName.value + amount.toString()
// }
