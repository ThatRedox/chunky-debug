package dev.thatredox.chunky.debug.renderer;

import dev.thatredox.chunky.debug.DebugException;
import dev.thatredox.chunky.debug.Reflection;
import dev.thatredox.chunky.debug.hash.FnvHasher;
import dev.thatredox.chunky.debug.hash.Hasher;
import se.llbit.chunky.renderer.DefaultRenderManager;
import se.llbit.chunky.renderer.PathTracingRenderer;
import se.llbit.chunky.renderer.WorkerState;
import se.llbit.chunky.renderer.scene.Camera;
import se.llbit.chunky.renderer.scene.RayTracer;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.math.Ray;
import se.llbit.math.Vector3;

import java.util.Random;

public class DebugPathTracingRenderer extends PathTracingRenderer {
    public DebugPathTracingRenderer(PathTracingRenderer renderer) {
        super(
                "Debug" + renderer.getId(),
                "Debug " + renderer.getName(),
                renderer.getDescription(),
                Reflection.getDeclaredField(renderer, "tracer", RayTracer.class)
                        .orElseThrow(RuntimeException::new)
        );
    }

    protected void setSeed(int spp, int x, int y, Random random) {
        Hasher hasher = new FnvHasher();
        hasher.write_int(spp);
        hasher.write_int(x);
        hasher.write_int(y);

        random.setSeed(hasher.finish());
        random.nextLong();
    }

    @Override
    public void render(DefaultRenderManager manager) throws InterruptedException {
        Scene scene = manager.bufferedScene;

        int width = scene.width;
        int height = scene.height;

        int fullWidth = scene.getFullWidth();
        int fullHeight = scene.getFullHeight();
        int cropX = scene.getCropX();
        int cropY = scene.getCropY();

        int sppPerPass = manager.context.sppPerPass();
        Camera cam = scene.camera();
        double halfWidth = fullWidth / (2.0 * fullHeight);
        double invHeight = 1.0 / fullHeight;

        double[] sampleBuffer = scene.getSampleBuffer();

        while (scene.spp < scene.getTargetSpp()) {
            int spp = scene.spp;
            double sinv = 1.0 / (sppPerPass + spp);

            submitTiles(manager, (state, pixel) -> {
                int x = pixel.firstInt();
                int y = pixel.secondInt();

                double sr = 0;
                double sg = 0;
                double sb = 0;

                for (int k = 0; k < sppPerPass; k++) {
                    setSeed(spp + k, x, y, state.random);

                    double ox = state.random.nextDouble();
                    double oy = state.random.nextDouble();

                    cam.calcViewRay(state.ray, state.random,
                            -halfWidth + (x + ox + cropX) * invHeight,
                            -0.5 + (y + oy + cropY) * invHeight);
                    scene.rayTrace(tracer, state);

                    sr += state.ray.color.x;
                    sg += state.ray.color.y;
                    sb += state.ray.color.z;
                }

                int offset = 3 * (y*width + x);
                sampleBuffer[offset + 0] = (sampleBuffer[offset + 0] * spp + sr) * sinv;
                sampleBuffer[offset + 1] = (sampleBuffer[offset + 1] * spp + sg) * sinv;
                sampleBuffer[offset + 2] = (sampleBuffer[offset + 2] * spp + sb) * sinv;
            });

            manager.pool.awaitEmpty();
            scene.spp += sppPerPass;
            if (postRender.getAsBoolean()) break;
        }
    }

    public Vector3 debugTrace(int spp, int x, int y, Scene scene, boolean debug) {
        WorkerState state = new WorkerState();
        state.random = new Random(0);
        state.ray = new Ray();
        setSeed(spp, x, y, state.random);

        int fullWidth = scene.getFullWidth();
        int fullHeight = scene.getFullHeight();
        int cropX = scene.getCropX();
        int cropY = scene.getCropY();

        Camera cam = scene.camera();
        double halfWidth = fullWidth / (2.0 * fullHeight);
        double invHeight = 1.0 / fullHeight;

        if (debug) DebugException.debug();

        double ox = state.random.nextDouble();
        double oy = state.random.nextDouble();

        cam.calcViewRay(state.ray, state.random,
                -halfWidth + (x + ox + cropX) * invHeight,
                -0.5 + (y + oy + cropY) * invHeight);
        scene.rayTrace(tracer, state);

        return new Vector3(state.ray.color.x, state.ray.color.y, state.ray.color.z);
    }
}
