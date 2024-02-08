package org.figuramc.figura.model.rendering.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.utils.ResourceUtils;
import org.figuramc.figura.utils.VertexFormatMode;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum RenderTypes {
    NONE(null),

    CUTOUT(RenderType::entityCutoutNoCull),
    CUTOUT_CULL(RenderType::entityCutout),
    CUTOUT_EMISSIVE_SOLID(resourceLocation -> FiguraRenderType.CUTOUT_EMISSIVE_SOLID.apply(resourceLocation, false)),

    TRANSLUCENT(RenderType::entityTranslucent),
    TRANSLUCENT_CULL(RenderType::entityTranslucentCull),

    EMISSIVE(RenderType::eyes),
    EMISSIVE_SOLID(resourceLocation -> RenderType.beaconBeam(resourceLocation, false)),
    EYES(RenderType::eyes),

    END_PORTAL(t -> RenderType.endPortal(0), false), //TODO FIX ME
    END_GATEWAY(t -> RenderType.endPortal(0), false), //TODO FIX ME
    TEXTURED_PORTAL(resourceLocation -> FiguraRenderType.getTexturedPortal(resourceLocation, 0)),  //TODO FIX ME

    GLINT(t -> RenderType.entityGlintDirect(), false, false),
    GLINT2(t -> RenderType.glintDirect(), false, false),
    TEXTURED_GLINT(FiguraRenderType.TEXTURED_GLINT, true, false),

    LINES(t -> RenderType.lines(), false),
    LINES_STRIP(t -> FiguraRenderType.LINE_STRIP, false),
    SOLID(t -> FiguraRenderType.SOLID, false),

    BLURRY(FiguraRenderType.BLURRY);

    private final Function<ResourceLocation, RenderType> func;
    private final boolean texture, offset;

    RenderTypes(Function<ResourceLocation, RenderType> func) {
        this(func, true);
    }

    RenderTypes(Function<ResourceLocation, RenderType> func, boolean texture) {
        this(func, texture, true);
    }

    RenderTypes(Function<ResourceLocation, RenderType> func, boolean texture, boolean offset) {
        this.func = func;
        this.texture = texture;
        this.offset = offset;
    }

    public boolean isOffset() {
        return offset;
    }

    public RenderType get(ResourceLocation id) {
        if (!texture)
            return func.apply(id);

        return id == null || func == null ? null : func.apply(id);
    }

    public static class FiguraRenderType extends RenderType {

        public FiguraRenderType(String name, VertexFormat vertexFormat, VertexFormatMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
            super(name, vertexFormat, drawMode.asGLMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
        }

        public static final RenderType SOLID = create(
                "figura_solid",
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormatMode.QUADS.asGLMode,
                256,
                RenderType.CompositeState.builder()
                        .setLineState(new LineStateShard(OptionalDouble.empty()))
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setOutputState(ITEM_ENTITY_TARGET)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .setCullState(NO_CULL)
                        .createCompositeState(false)
        );

        private static final BiFunction<ResourceLocation, Boolean, RenderType> CUTOUT_EMISSIVE_SOLID = ResourceUtils.memoize(
                (texture, affectsOutline) ->
                        create("figura_cutout_emissive_solid", DefaultVertexFormat.BLOCK, VertexFormatMode.QUADS.asGLMode, 256, true, true,
                                CompositeState.builder()
                                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                                        .setCullState(NO_CULL)
                                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                                        .setOverlayState(NO_OVERLAY)
                                        .setFogState(NO_FOG)
                                        .createCompositeState(affectsOutline)));

        public static RenderType getTexturedPortal(ResourceLocation texture, int i) {
            TextureStateShard textureStateShard;
            TransparencyStateShard transparencyStateShard;
            if (i <= 1) {
                transparencyStateShard = TRANSLUCENT_TRANSPARENCY;
                textureStateShard = new TextureStateShard(texture, false, false);
            } else {
                transparencyStateShard = ADDITIVE_TRANSPARENCY;
                textureStateShard = new TextureStateShard(texture, false, false);
            }
            return create(
                    "figura_textured_portal",
                            DefaultVertexFormat.POSITION_COLOR,
                            VertexFormatMode.QUADS.asGLMode,
                            256,
                            false,
                            false,
                            CompositeState.builder()
                                    .setTextureState(
                                            textureStateShard
                                    ).setTexturingState(new PortalTexturingStateShard(i))
                                    .setTransparencyState(transparencyStateShard)
                                    .createCompositeState(false)
            );
        }

        public static final Function<ResourceLocation, RenderType> BLURRY = ResourceUtils.memoize(
                texture -> create(
                        "figura_blurry",
                        DefaultVertexFormat.NEW_ENTITY,
                        VertexFormatMode.QUADS.asGLMode,
                        256,
                        true,
                        true,
                        CompositeState.builder()
                                .setTextureState(new TextureStateShard(texture, true, false))
                                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                                .setCullState(NO_CULL)
                                .setLightmapState(LIGHTMAP)
                                .setOverlayState(OVERLAY)
                                .createCompositeState(true)
                )
        );

        public static final Function<ResourceLocation, RenderType> TEXTURED_GLINT = ResourceUtils.memoize(
                texture -> create(
                        "figura_textured_glint_direct",
                        DefaultVertexFormat.POSITION_TEX,
                        VertexFormatMode.QUADS.asGLMode,
                        256,
                        false,
                        false,
                        RenderType.CompositeState.builder()
                                .setTextureState(new TextureStateShard(texture, false, false))
                                .setWriteMaskState(COLOR_WRITE)
                                .setCullState(NO_CULL)
                                .setDepthTestState(EQUAL_DEPTH_TEST)
                                .setTransparencyState(GLINT_TRANSPARENCY)
                                .setTexturingState(ENTITY_GLINT_TEXTURING)
                                .createCompositeState(false)
                )
        );
        protected static final OutputStateShard WIREFRAME_ITEM_ENTITY_TARGET = new OutputStateShard("item_entity_target", () -> {
            RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            if (Minecraft.useShaderTransparency()) {
                Minecraft.getInstance().levelRenderer.getItemEntityTarget().bindWrite(false);
            }
        }, () -> {
            RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            if (Minecraft.useShaderTransparency()) {
                Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            }
        });
        public static final RenderType LINE_STRIP = RenderType.create("line_strip", DefaultVertexFormat.POSITION_COLOR, VertexFormatMode.LINE_STRIP.asGLMode, 256, CompositeState.builder().setLineState(new LineStateShard(OptionalDouble.of(0.5))).setLayeringState(VIEW_OFFSET_Z_LAYERING).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(WIREFRAME_ITEM_ENTITY_TARGET).setWriteMaskState(COLOR_DEPTH_WRITE).setCullState(NO_CULL).createCompositeState(false));
        public static final Function<ResourceLocation, RenderType> TEXT_POLYGON_OFFSET = ResourceUtils.memoize(texture -> RenderType.create("text_polygon_offset", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormatMode.QUADS.asGLMode, 256, false, true, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(texture, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).setLayeringState(POLYGON_OFFSET_LAYERING).createCompositeState(false)));
    }
}