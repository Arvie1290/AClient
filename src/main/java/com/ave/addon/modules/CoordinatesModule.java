package com.ave.addon.modules;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.renderer.Renderer2D;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CoordinatesModule extends Module {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.000", DecimalFormatSymbols.getInstance(Locale.US));

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // Slider Posisi & Ukuran Layout
    private final Setting<Integer> posX = sgGeneral.add(new IntSetting.Builder().name("x-position").defaultValue(20).sliderMax(1920).build());
    private final Setting<Integer> posY = sgGeneral.add(new IntSetting.Builder().name("y-position").defaultValue(20).sliderMax(1080).build());
    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder().name("size-scale").defaultValue(1.0).sliderMin(0.5).sliderMax(3.0).build());

    // Template Teks Kustom
    private final Setting<String> templateText = sgGeneral.add(new StringSetting.Builder()
        .name("text-template")
        .description("Template for coordinates text. Use {x}, {y}, {z}")
        .defaultValue("XYZ: {x} / {y} / {z}")
        .build()
    );

    // Kosmetik Skema Warna & Background
    private final Setting<Boolean> drawBackground = sgGeneral.add(new BoolSetting.Builder().name("draw-background").defaultValue(true).build());
    private final Setting<SettingColor> textColor = sgGeneral.add(new ColorSetting.Builder().name("text-color").defaultValue(new SettingColor(255, 255, 255)).build());
    private final Setting<SettingColor> background = sgGeneral.add(new ColorSetting.Builder().name("background-color").defaultValue(new SettingColor(0, 0, 0, 200)).build());
    private final Setting<SettingColor> border = sgGeneral.add(new ColorSetting.Builder().name("border-color").defaultValue(new SettingColor(180, 0, 255)).build());

    public CoordinatesModule(Category category) {
        super(category, "coordinates-hud", "Displays coordinates HUD.");
    }

    @SuppressWarnings("unused")
    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mc.player == null) return;

        // Ambil data posisi real-time player
        String x = FORMAT.format(mc.player.getX());
        String y = FORMAT.format(mc.player.getY());
        String z = FORMAT.format(mc.player.getZ());

        double s = scale.get();
        double hPad = 6;
        double vPad = 4;

        event.drawContext.getMatrices().pushMatrix();
        event.drawContext.getMatrices().scale((float) s, (float) s);

        double startX = posX.get() / s;
        double startY = posY.get() / s;

        String text = templateText.get().replace("{x}", x).replace("{y}", y).replace("{z}", z);
        double w = TextRenderer.get().getWidth(text) + hPad * 2;
        double h = TextRenderer.get().getHeight() + vPad * 2;

        if (drawBackground.get()) {
            drawRoundedBox(startX, startY, w, h);
            TextRenderer.get().render(text, startX + hPad, startY + vPad, textColor.get(), false);
        } else {
            TextRenderer.get().render(text, startX, startY, textColor.get(), false);
        }

        event.drawContext.getMatrices().popMatrix();
    }

    private void drawRoundedBox(double x, double y, double w, double h) {
        Color bg = background.get();
        Color out = border.get();

        // Background Hitam
        Renderer2D.COLOR.quad(x + 2, y, w - 4, h, bg);
        Renderer2D.COLOR.quad(x, y + 2, 2, h - 4, bg);
        Renderer2D.COLOR.quad(x + w - 2, y + 2, 2, h - 4, bg);
        Renderer2D.COLOR.quad(x + 1, y + 1, 1, 1, bg);
        Renderer2D.COLOR.quad(x + w - 2, y + 1, 1, 1, bg);
        Renderer2D.COLOR.quad(x + 1, y + h - 2, 1, 1, bg);
        Renderer2D.COLOR.quad(x + w - 2, y + h - 2, 1, 1, bg);

        // Garis Tepi (Outline)
        Renderer2D.COLOR.quad(x + 2, y, w - 4, 1, out);
        Renderer2D.COLOR.quad(x + 2, y + h - 1, w - 4, 1, out);
        Renderer2D.COLOR.quad(x, y + 2, 1, h - 4, out);
        Renderer2D.COLOR.quad(x + w - 1, y + 2, 1, h - 4, out);

        // Pixel Sudut
        Renderer2D.COLOR.quad(x + 1, y + 1, 1, 1, out);
        Renderer2D.COLOR.quad(x + w - 2, y + 1, 1, 1, out);
        Renderer2D.COLOR.quad(x + 1, y + h - 2, 1, 1, out);
        Renderer2D.COLOR.quad(x + w - 2, y + h - 2, 1, 1, out);
    }
}
