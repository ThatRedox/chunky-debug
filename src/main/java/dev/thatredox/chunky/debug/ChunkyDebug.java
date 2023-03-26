package dev.thatredox.chunky.debug;

import dev.thatredox.chunky.debug.renderer.DebugPathTracingRenderer;
import javafx.collections.MapChangeListener;
import javafx.scene.control.*;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import se.llbit.chunky.Plugin;
import se.llbit.chunky.main.Chunky;
import se.llbit.chunky.main.ChunkyOptions;
import se.llbit.chunky.renderer.DefaultRenderManager;
import se.llbit.chunky.renderer.PathTracingRenderer;
import se.llbit.chunky.renderer.RenderManager;
import se.llbit.chunky.renderer.Renderer;
import se.llbit.chunky.ui.ChunkyFx;
import se.llbit.log.Log;
import se.llbit.math.Vector2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChunkyDebug implements Plugin {
    @Override
    public void attach(Chunky chunky) {
        PixelTraceManager pixelTraceManager;
        RenderManager renderManager = chunky.getRenderController().getRenderManager();
        if (renderManager instanceof DefaultRenderManager) {
            DefaultRenderManager chunkyRenderManager = (DefaultRenderManager) renderManager;

            List<PathTracingRenderer> renderers = chunkyRenderManager.getRenderers()
                    .stream()
                    .filter(r -> r instanceof PathTracingRenderer)
                    .map(r -> (PathTracingRenderer) r)
                    .collect(Collectors.toList());
            renderers.forEach(r -> Chunky.addRenderer(new DebugPathTracingRenderer(r)));

            pixelTraceManager = new PixelTraceManager(chunkyRenderManager);
        } else {
            Log.error("RenderManager is not a DefaultRenderManager!");
            return;
        }

        ArrayList<MenuItem> items = new ArrayList<>();
        chunky.getRenderContextMenuTransformers().add(contextMenu -> {
            SeparatorMenuItem separator = new SeparatorMenuItem();
            contextMenu.getItems().add(separator);

            contextMenu.getProperties().addListener((MapChangeListener<Object, Object>) change -> {
                if (change.getKey().equals("canvasPosition") && change.wasAdded()) {
                    Vector2 canvasPosition = (Vector2) change.getValueAdded();

                    contextMenu.getItems().removeAll(items);
                    items.clear();

                    int spp = Math.max(1, pixelTraceManager.getSpp());
                    for (int i = spp-1; i >= Math.max(0, spp - 10); i--) {
                        int s = i;
                        MenuItem item = new MenuItem("Sample " + s);
                        items.add(item);

                        item.setOnAction(event -> pixelTraceManager.debugTrace(s, (int) canvasPosition.x, (int) canvasPosition.y));

                        WritableImage color = new WritableImage(1, 1);
                        ImageView sampleColor = new ImageView(color);
                        sampleColor.setFitHeight(16);
                        sampleColor.setFitWidth(16);
                        item.setGraphic(sampleColor);
                        pixelTraceManager.renderTrace(s, (int) canvasPosition.x, (int) canvasPosition.y, c ->
                                color.getPixelWriter().setColor(0, 0, Color.color(c.x, c.y, c.z)));
                    }

                    int index = contextMenu.getItems().indexOf(separator) + 1;
                    contextMenu.getItems().addAll(index, items);
                }
            });
        });
    }

    public static void main(String[] args) {
        Chunky.loadDefaultTextures();
        Chunky chunky = new Chunky(ChunkyOptions.getDefaults());
        new ChunkyDebug().attach(chunky);
        ChunkyFx.startChunkyUI(chunky);
    }
}
