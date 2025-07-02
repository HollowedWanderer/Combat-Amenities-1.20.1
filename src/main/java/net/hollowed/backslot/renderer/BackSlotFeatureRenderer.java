package net.hollowed.backslot.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.backslot.BackTransformData;
import net.hollowed.backslot.BackTransformResourceReloadListener;
import net.hollowed.backslot.Backslot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.List;

@Environment(EnvType.CLIENT)
public class BackSlotFeatureRenderer extends HeldItemFeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

	private final HeldItemRenderer heldItemRenderer;
	private ItemStack lastSavedItem = ItemStack.EMPTY;

    public BackSlotFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context, HeldItemRenderer heldItemRenderer) {
		super(context, heldItemRenderer);
		this.heldItemRenderer = heldItemRenderer;
	}

	@Override
	public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, AbstractClientPlayerEntity playerEntity, float limbAngle, float limbDistance, float tickDelta, float age, float headYaw, float headPitch) {
		ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

		ItemStack backSlotStack;
		if (playerEntity.getMainHandStack().isIn(TagKey.of(Registries.ITEM.getKey(), new Identifier(Backslot.MOD_ID, "whitelist")))) {
			lastSavedItem = playerEntity.getMainHandStack();
		}
		backSlotStack = lastSavedItem;
		if (backSlotStack.isOf(playerEntity.getMainHandStack().getItem()) || backSlotStack.isOf(playerEntity.getOffHandStack().getItem())) {
			return;
		}
		if (!backSlotStack.isEmpty()) {
			matrixStack.push();

			BackTransformData transformData = BackTransformResourceReloadListener.getTransform(Registries.ITEM.getId(backSlotStack.getItem()));
			BackTransformData.SecondaryTransformData secondaryTransformData = transformData.secondary();

			ModelPart bodyPart = this.getContextModel().body;
			bodyPart.rotate(matrixStack);

			matrixStack.translate(0, -0.3, 0);

			// Default transforms
			matrixStack.translate(0D, 0.3D, 0.15D);
			matrixStack.scale(1F, 1F, 1F);

			float h = MinecraftClient.getInstance().getTickDelta();

			double d = MathHelper.lerp(h, playerEntity.prevCapeX, playerEntity.capeX) - MathHelper.lerp(h, playerEntity.prevX, playerEntity.getX());
			double e = MathHelper.lerp(h, playerEntity.prevCapeY, playerEntity.capeY) - MathHelper.lerp(h, playerEntity.prevY, playerEntity.getY());
			double m = MathHelper.lerp(h, playerEntity.prevCapeZ, playerEntity.capeZ) - MathHelper.lerp(h, playerEntity.prevZ, playerEntity.getZ());
			float n = MathHelper.lerpAngleDegrees(h, playerEntity.prevBodyYaw, playerEntity.bodyYaw);
			double o = MathHelper.sin(n * 0.017453292F);
			double p = -MathHelper.cos(n * 0.017453292F);
			float q = (float) e * 10.0F;
			q = MathHelper.clamp(q, -6.0F, 32.0F);
			float r = (float) (d * o + m * p) * 40.0F;
			r = MathHelper.clamp(r, 0.0F, 150.0F);
			float s = (float) (d * p - m * o) * 40.0F;
			s = MathHelper.clamp(s, -20.0F, 20.0F);
			if (r < 0.0F) {
				r = 0.0F;
			}

			float t = MathHelper.lerp(h, playerEntity.prevStrideDistance, playerEntity.strideDistance);
			q += MathHelper.sin(MathHelper.lerp(h, playerEntity.prevHorizontalSpeed, playerEntity.horizontalSpeed) * 6.0F) * 32.0F * t;
			if (playerEntity.isInSneakingPose()) {
				q += 2.5F;
			}

			matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0F + r / 2.0F + q));
			matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(s / 2.0F));
			matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - s / 2.0F));

			matrixStack.push();
			List<Float> scale = transformData.scale();
			matrixStack.scale(scale.get(0), scale.get(1), scale.get(2)); // Scale

			List<Float> translation = transformData.translation();
			matrixStack.translate(translation.get(0), translation.get(1), translation.get(2)); // Translation

			List<Float> rotation = transformData.rotation();
			matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotation.get(0))); // Rotation X
			matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation.get(1))); // Rotation Y
			matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotation.get(2))); // Rotation Z

			matrixStack.translate(0, 0.3, 0);
			heldItemRenderer.renderItem(playerEntity, backSlotStack, transformData.mode(), false, matrixStack, vertexConsumerProvider, light);

			if (Registries.ITEM.getId(backSlotStack.getItem()).equals(new Identifier("prominent:ash")) && !Registries.ITEM.getId(playerEntity.getOffHandStack().getItem()).equals(new Identifier("prominent:edar"))) {
				matrixStack.translate(0, 0, -0.05);
				matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
				heldItemRenderer.renderItem(playerEntity, new ItemStack(Registries.ITEM.get(new Identifier("prominent:edar"))), transformData.mode(), false, matrixStack, vertexConsumerProvider, light);
			}

			if (Registries.ITEM.getId(backSlotStack.getItem()).equals(new Identifier("prominent:edar")) && !Registries.ITEM.getId(playerEntity.getOffHandStack().getItem()).equals(new Identifier("prominent:ash"))) {
				matrixStack.translate(0, 0, -0.05);
				matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
				heldItemRenderer.renderItem(playerEntity, new ItemStack(Registries.ITEM.get(new Identifier("prominent:ash"))), transformData.mode(), false, matrixStack, vertexConsumerProvider, light);
			}
			matrixStack.pop();

			matrixStack.push();
			matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));

			List<Float> secondaryScale = secondaryTransformData.scale();
			matrixStack.scale(secondaryScale.get(0), secondaryScale.get(1), secondaryScale.get(2)); // Scale

			List<Float> secondaryTranslation = secondaryTransformData.translation();
			matrixStack.translate(secondaryTranslation.get(0), secondaryTranslation.get(1), secondaryTranslation.get(2)); // Translation

			List<Float> secondaryRotation = secondaryTransformData.rotation();
			matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(secondaryRotation.get(0))); // Rotation X
			matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(secondaryRotation.get(1))); // Rotation Y
			matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(secondaryRotation.get(2))); // Rotation Z

			matrixStack.translate(0, 0.3, 0);

			BakedModel secondaryModel = itemRenderer.getModels().getModelManager().getModel(new ModelIdentifier(secondaryTransformData.item(), "inventory"));
			if (!secondaryTransformData.item().equals(new Identifier("null"))) {
				itemRenderer.renderItem(Items.APPLE.getDefaultStack(), secondaryTransformData.mode(), false, matrixStack, vertexConsumerProvider, light, OverlayTexture.DEFAULT_UV, secondaryModel);
			}
			matrixStack.pop();

			matrixStack.pop();
		}
	}
}
