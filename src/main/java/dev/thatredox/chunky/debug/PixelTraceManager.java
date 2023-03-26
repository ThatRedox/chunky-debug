package dev.thatredox.chunky.debug;

import dev.thatredox.chunky.debug.renderer.DebugPathTracingRenderer;
import se.llbit.chunky.renderer.DefaultRenderManager;
import se.llbit.chunky.renderer.Renderer;
import se.llbit.log.Log;
import se.llbit.math.Vector3;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PixelTraceManager {
    protected final Executor executor = Executors.newFixedThreadPool(1);
    protected final DefaultRenderManager renderManager;

    public PixelTraceManager(DefaultRenderManager renderManager) {
        this.renderManager = renderManager;
    }

    public Optional<DebugPathTracingRenderer> getRenderer() {
        Renderer renderer;
        try {
            Method getRenderer = Reflection.getDeclaredMethod(renderManager, "getRenderer")
                    .orElseThrow(NullPointerException::new);
            renderer = (Renderer) getRenderer.invoke(renderManager);
        } catch (InvocationTargetException | IllegalAccessException | NullPointerException ex) {
            Log.error("Failed to get renderer");
            return Optional.empty();
        }

        if (renderer instanceof DebugPathTracingRenderer) {
            return Optional.of((DebugPathTracingRenderer) renderer);
        } else {
            return Optional.empty();
        }
    }

    public void debugTrace(int spp, int x, int y) {
        Optional<DebugPathTracingRenderer> r = getRenderer();
        if (r.isPresent()) {
            r.get().debugTrace(spp, x, y, renderManager.bufferedScene, true);
        } else {
            Log.error("Selected renderer is not a Debug Renderer!");
        }
    }

    public int getSpp() {
        return renderManager.bufferedScene.spp;
    }

    public void renderTrace(int spp, int x, int y, Consumer<Vector3> onRender) {
        Optional<DebugPathTracingRenderer> r = getRenderer();
        r.ifPresent(pt -> executor.execute(() -> {
            Vector3 color = pt.debugTrace(spp, x, y, renderManager.bufferedScene, false);
            onRender.accept(color);
        }));
    }
}
