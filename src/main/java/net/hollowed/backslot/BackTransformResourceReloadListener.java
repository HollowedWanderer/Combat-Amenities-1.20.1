package net.hollowed.backslot;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BackTransformResourceReloadListener implements SimpleSynchronousResourceReloadListener {
    private static final Map<Identifier, BackTransformData> transforms = new HashMap<>();
    private static BackTransformData defaultTransforms;

    @Override
    public Identifier getFabricId() {
        return new Identifier(Backslot.MOD_ID, "backslot_transforms");
    }

    @Override
    public void reload(ResourceManager manager) {
        transforms.clear();
        System.out.println("Reloading transform data...");

        manager.findResources("backslot_transforms", path -> path.getPath().endsWith(".json")).keySet().forEach(id -> {
            if (manager.getResource(id).isPresent()) {
                try (InputStream stream = manager.getResource(id).get().getInputStream()) {
                    var json = JsonHelper.deserialize(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    DataResult<BackTransformData> result = BackTransformData.CODEC.parse(JsonOps.INSTANCE, json);

                    result.resultOrPartial(Backslot.LOGGER::error).ifPresent(data -> {
                        Backslot.LOGGER.info("Loaded transform for: {}", data.item());
                        if (Objects.equals(data.item(), Identifier.of("beltslot", "default"))) {
                            defaultTransforms = data;
                        } else {
                            transforms.put(data.item(), data);
                        }
                    });
                } catch (Exception e) {
                    Backslot.LOGGER.error("Failed to load transform for {}: {}", id, e.getMessage());
                }
            }
        });

        Backslot.LOGGER.info("Loaded transforms: {}", transforms);
    }

    public static BackTransformData getTransform(Identifier itemId) {
        BackTransformData baseTransform = transforms.getOrDefault(itemId, defaultTransforms);

        if (baseTransform != null) {
            return baseTransform;
        }

        // Fallback to a fully default transform if no data is available
        return new BackTransformData(
                itemId,
                List.of(1.0f, 1.0f, 1.0f), // Default scale
                List.of(0.0f, 0.0f, 0.0f), // Default rotation
                List.of(0.0f, 0.0f, 0.0f), // Default translation
                ModelTransformationMode.FIXED, // Default mode
                1.0F, // Default sway
                new BackTransformData.SecondaryTransformData(
                        new Identifier("null"),
                        List.of(1.0f, 1.0f, 1.0f), // Default scale
                        List.of(0.0f, 0.0f, 0.0f), // Default rotation
                        List.of(0.0f, 0.0f, 0.0f), // Default translation
                        ModelTransformationMode.FIXED // Default mode
                )
        );
    }
}
