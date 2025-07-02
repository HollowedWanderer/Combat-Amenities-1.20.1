package net.hollowed.backslot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.util.Identifier;

import java.util.List;

public record BackTransformData(
        Identifier item,
        List<Float> scale,
        List<Float> rotation,
        List<Float> translation,
        ModelTransformationMode mode,
        Float sway,
        SecondaryTransformData secondary
) {
    public static final Codec<BackTransformData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("item").forGetter(BackTransformData::item),
            Codec.FLOAT.listOf().fieldOf("scale").orElseGet(() -> List.of(1.0f, 1.0f, 1.0f)).forGetter(BackTransformData::scale),
            Codec.FLOAT.listOf().fieldOf("rotation").orElseGet(() -> List.of(0.0f, 0.0f, 0.0f)).forGetter(BackTransformData::rotation),
            Codec.FLOAT.listOf().fieldOf("translation").orElseGet(() -> List.of(0.0f, 0.0f, 0.0f)).forGetter(BackTransformData::translation),
            Codec.STRING.fieldOf("mode").orElse("FIXED")
                    .xmap(ModelTransformationMode::valueOf, ModelTransformationMode::name)
                    .forGetter(BackTransformData::mode),
            Codec.FLOAT.fieldOf("sway").orElse(1.0F).forGetter(BackTransformData::sway),
            SecondaryTransformData.CODEC.fieldOf("secondary").orElse(new SecondaryTransformData(
                            new Identifier("null"),
                            List.of(1.0f, 1.0f, 1.0f),
                            List.of(0.0f, 0.0f, 0.0f),
                            List.of(0.0f, 0.0f, 0.0f),
                            ModelTransformationMode.NONE
                    ))
                    .forGetter(BackTransformData::secondary)
    ).apply(instance, BackTransformData::new));

    public record SecondaryTransformData(
            Identifier item,
            List<Float> scale,
            List<Float> rotation,
            List<Float> translation,
            ModelTransformationMode mode
    ) {
        public static final Codec<SecondaryTransformData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("model").forGetter(SecondaryTransformData::item),
                Codec.FLOAT.listOf().fieldOf("scale").orElseGet(() -> List.of(1.0f, 1.0f, 1.0f)).forGetter(SecondaryTransformData::scale),
                Codec.FLOAT.listOf().fieldOf("rotation").orElseGet(() -> List.of(0.0f, 0.0f, 0.0f)).forGetter(SecondaryTransformData::rotation),
                Codec.FLOAT.listOf().fieldOf("translation").orElseGet(() -> List.of(0.0f, 0.0f, 0.0f)).forGetter(SecondaryTransformData::translation),
                Codec.STRING.fieldOf("mode").orElse("FIXED")
                        .xmap(ModelTransformationMode::valueOf, ModelTransformationMode::name)
                        .forGetter(SecondaryTransformData::mode)
        ).apply(instance, SecondaryTransformData::new));
    }
}