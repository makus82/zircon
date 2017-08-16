package org.codetome.zircon.font

import org.codetome.zircon.Modifier
import org.codetome.zircon.TextCharacter
import org.codetome.zircon.extensions.isNotPresent
import org.codetome.zircon.font.cache.DefaultFontRegionCache
import org.codetome.zircon.font.transformer.*
import java.awt.image.BufferedImage

/**
 * Represents a font which is backed by a sprite sheet.
 */
class Java2DFont(private val source: BufferedImage,
                 private val metadata: Map<Char, List<CharacterMetadata>>,
                 private val width: Int,
                 private val height: Int,
                 private val regionTransformers: List<FontRegionTransformer<BufferedImage>>,
                 private val cache: DefaultFontRegionCache<BufferedImage>) : Font<BufferedImage> {

    override fun getWidth() = width

    override fun getHeight() = height

    override fun hasDataForChar(char: Char) = metadata.containsKey(char)

    override fun fetchMetadataForChar(char: Char): List<CharacterMetadata> {
        return metadata[char] ?: listOf()
    }

    override fun fetchRegionForChar(textCharacter: TextCharacter, vararg tags: String): BufferedImage {
        val meta = fetchMetaFor(textCharacter, tags)
        val maybeRegion = cache.retrieveIfPresent(textCharacter)
        
        var region = if(maybeRegion.isNotPresent()) {
            var image = source.getSubimage(meta.x * width, meta.y * height, width, height)
            regionTransformers.forEach {
                image = it.transform(image, textCharacter)
            }
            cache.store(textCharacter, image)
            image
        } else {
            maybeRegion.get()
        }
        textCharacter.getModifiers().forEach {
            region = MODIFIER_TRANSFORMER_LOOKUP[it]!!.transform(region, textCharacter)
        }
        return region
    }

    private fun fetchMetaFor(textCharacter: TextCharacter, tags: Array<out String>): CharacterMetadata {
        require(hasDataForChar(textCharacter.getCharacter())) {
            "No metadata exists for '${textCharacter.getCharacter()}'!"
        }
        val metas = metadata[textCharacter.getCharacter()]!!.filter {
            it.tags.containsAll(tags.toList())
        }
        require(metas.size == 1) {
            "There are more than 1 metadata entries for char: '${textCharacter.getCharacter()}' and tags: '${tags.toList().joinToString()}'"
        }
        return metas.first()
    }

    companion object {
        val MODIFIER_TRANSFORMER_LOOKUP = mapOf(
                Pair(Modifier.UNDERLINE, Java2DUnderlineTransformer()),
                Pair(Modifier.VERTICAL_FLIP, Java2DVerticalFlipper()),
                Pair(Modifier.HORIZONTAL_FLIP, Java2DHorizontalFlipper()),
                Pair(Modifier.CROSSED_OUT, Java2DCrossedOutTransformer()),
                Pair(Modifier.BLINK, NoOpTransformer()),
                Pair(Modifier.HIDDEN, Java2DHiddenTransformer())

        ).toMap()
    }
}