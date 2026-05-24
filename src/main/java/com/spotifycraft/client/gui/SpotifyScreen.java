package com.spotifycraft.client.gui;

import com.spotifycraft.music.MusicManager;
import com.spotifycraft.music.MusicTrack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SpotifyScreen extends Screen {

    private final Screen parent;
    private final MusicManager mm = MusicManager.getInstance();

    private static final int PANEL_W = 640;
    private static final int PANEL_H = 400;
    private static final int SIDEBAR_W = 190;
    private static final int TOPBAR_H = 36;
    private static final int PLAYER_H = 68;

    // Purple theme
    private static final int BG          = 0xFF0e0e12;
    private static final int BG_DARK     = 0xFF09090d;
    private static final int BG_SIDEBAR  = 0xFF0b0b0f;
    private static final int BG_CARD     = 0xFF13131d;
    private static final int BG_ACTIVE   = 0xFF16162a;
    private static final int BORDER      = 0xFF1a1a24;
    private static final int BORDER_DARK = 0xFF141420;

    private static final int PURPLE      = 0xFFa78bfa;
    private static final int PURPLE_DARK = 0xFF6d28d9;
    private static final int PURPLE_LIGHT= 0xFFc4b5fd;
    private static final int PURPLE_BG   = 0xFF2d1b5a;

    private static final int WHITE       = 0xFFe2e8f0;
    private static final int TEXT_MID    = 0xFFc0c0e0;
    private static final int TEXT_DIM    = 0xFF5a5a7a;
    private static final int TEXT_DARK   = 0xFF3a3a5a;
    private static final int TEXT_DARKER = 0xFF2a2a3a;

    private int listScrollOffset = 0;
    private static final int TRACK_ROW_H = 44;
    private static final int VISIBLE_ROWS = 5;

    private boolean draggingVolume = false;
    private boolean draggingProgress = false;

    private int px, py;

    public SpotifyScreen(Screen parent) {
        super(Component.literal("SpotifyCraft"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        px = (this.width - PANEL_W) / 2;
        py = (this.height - PANEL_H) / 2;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        // Dim backdrop
        g.fill(0, 0, this.width, this.height, 0xBB000000);

        // Shadow
        g.fill(px + 6, py + 6, px + PANEL_W + 6, py + PANEL_H + 6, 0x55000000);

        // Main panel
        g.fill(px, py, px + PANEL_W, py + PANEL_H, BG);

        renderTopbar(g, mx, my);
        renderSidebar(g, mx, my);
        renderMain(g, mx, my);
        renderPlayer(g, mx, my);

        super.render(g, mx, my, delta);
    }

    private void renderTopbar(GuiGraphics g, int mx, int my) {
        int x = px, y = py;
        g.fill(x, y, x + PANEL_W, y + TOPBAR_H, BG_DARK);
        g.fill(x, y + TOPBAR_H - 1, x + PANEL_W, y + TOPBAR_H, BORDER);

        // Logo box
        g.fill(x + 12, y + 5, x + 36, y + 29, PURPLE_DARK);
        g.drawString(font, "♪", x + 17, y + 11, PURPLE_LIGHT, false);

        // Name
        g.drawString(font, "SpotifyCraft", x + 42, y + 7, WHITE, false);
        g.drawString(font, "Minecraft 1.21.1", x + 42, y + 20, TEXT_DARK, false);

        // Key hints
        String[] hints = {"M — відкрити", "Space — пауза", "← →"};
        int hx = x + PANEL_W - 10;
        for (int i = hints.length - 1; i >= 0; i--) {
            int w = font.width(hints[i]) + 10;
            hx -= w + 4;
            g.fill(hx, y + 8, hx + w, y + 27, BG_CARD);
            g.fill(hx, y + 8, hx + w, y + 9, BORDER);
            g.fill(hx, y + 26, hx + w, y + 27, BORDER);
            g.fill(hx, y + 8, hx + 1, y + 27, BORDER);
            g.fill(hx + w - 1, y + 8, hx + w, y + 27, BORDER);
            g.drawString(font, hints[i], hx + 5, y + 14, TEXT_DIM, false);
        }

        // Close button
        int cx = x + PANEL_W - 8;
        boolean ch = isIn(mx, my, cx - 14, y + 8, 14, 18);
        g.drawString(font, "✕", cx - 12, y + 11, ch ? 0xFFff6b6b : TEXT_DARK, false);
    }

    private void renderSidebar(GuiGraphics g, int mx, int my) {
        int x = px, y = py + TOPBAR_H;
        int h = PANEL_H - TOPBAR_H - PLAYER_H;

        g.fill(x, y, x + SIDEBAR_W, y + h, BG_SIDEBAR);
        g.fill(x + SIDEBAR_W - 1, y, x + SIDEBAR_W, y + h, BORDER);

        // Section: Menu
        g.drawString(font, "МЕНЮ", x + 14, y + 10, TEXT_DARKER, false);

        String[][] items = {
            {"♬", "Your Library"},
            {"♡", "Favourites"},
            {"⟳", "History"},
        };
        for (int i = 0; i < items.length; i++) {
            int iy = y + 22 + i * 28;
            boolean hov = isIn(mx, my, x + 6, iy, SIDEBAR_W - 12, 24);
            boolean active = (i == 0);
            if (active) g.fill(x + 6, iy, x + SIDEBAR_W - 6, iy + 24, BG_ACTIVE);
            else if (hov) g.fill(x + 6, iy, x + SIDEBAR_W - 6, iy + 24, BG_CARD);
            int col = active ? PURPLE : (hov ? TEXT_MID : TEXT_DIM);
            g.drawString(font, items[i][0], x + 14, iy + 7, col, false);
            g.drawString(font, items[i][1], x + 28, iy + 7, col, false);
        }

        // Section: Actions
        g.drawString(font, "ДІЇ", x + 14, y + 108, TEXT_DARKER, false);

        String[][] actions = {
            {"⊕", "Add Music"},
            {"↺", "Refresh"},
            {"⚙", "Settings"},
        };
        for (int i = 0; i < actions.length; i++) {
            int iy = y + 120 + i * 28;
            boolean hov = isIn(mx, my, x + 6, iy, SIDEBAR_W - 12, 24);
            if (hov) g.fill(x + 6, iy, x + SIDEBAR_W - 6, iy + 24, BG_CARD);
            int col = hov ? TEXT_MID : TEXT_DIM;
            g.drawString(font, actions[i][0], x + 14, iy + 7, col, false);
            g.drawString(font, actions[i][1], x + 28, iy + 7, col, false);
        }

        // Footer
        int fy = y + h - 52;
        g.fill(x, fy, x + SIDEBAR_W - 1, fy + 1, BORDER_DARK);
        g.drawString(font, "Папка з музикою:", x + 10, fy + 6, TEXT_DARKER, false);
        g.drawString(font, ".minecraft/", x + 10, fy + 19, PURPLE_DARK, false);
        g.drawString(font, "spotifycraft_music/", x + 10, fy + 31, PURPLE_DARK, false);
        g.drawString(font, "Формати: .wav .au .aiff", x + 10, fy + 43, TEXT_DARKER, false);
    }

    private void renderMain(GuiGraphics g, int mx, int my) {
        int x = px + SIDEBAR_W;
        int y = py + TOPBAR_H;
        int w = PANEL_W - SIDEBAR_W;
        int h = PANEL_H - TOPBAR_H - PLAYER_H;

        g.fill(x, y, px + PANEL_W, y + h, BG);

        // Header
        g.fill(x, y, px + PANEL_W, y + 44, BG);
        g.drawString(font, "Your Library", x + 16, y + 10, WHITE, false);
        List<MusicTrack> tracks = mm.getTracks();
        g.drawString(font, tracks.size() + " треків", x + 16, y + 26, TEXT_DARK, false);
        g.fill(x, y + 43, px + PANEL_W, y + 44, BORDER_DARK);

        // Column headers
        int chY = y + 46;
        g.fill(x, chY, px + PANEL_W, chY + 18, BG);
        g.drawString(font, "#", x + 14, chY + 4, TEXT_DARK, false);
        g.drawString(font, "НАЗВА", x + 34, chY + 4, TEXT_DARK, false);
        g.drawString(font, "ТРИВАЛІСТЬ", px + PANEL_W - 72, chY + 4, TEXT_DARK, false);
        g.fill(x, chY + 17, px + PANEL_W, chY + 18, BORDER_DARK);

        if (tracks.isEmpty()) {
            drawCentered(g, "Немає музики!", x + w / 2, y + h / 2 - 12, WHITE);
            drawCentered(g, "Додай .wav файли у .minecraft/spotifycraft_music/", x + w / 2, y + h / 2 + 4, TEXT_DIM);
            drawCentered(g, "Потім натисни Refresh у меню зліва", x + w / 2, y + h / 2 + 18, TEXT_DARK);
            return;
        }

        int listY = chY + 20;
        for (int i = 0; i < VISIBLE_ROWS; i++) {
            int idx = i + listScrollOffset;
            if (idx >= tracks.size()) break;
            MusicTrack track = tracks.get(idx);
            int ry = listY + i * TRACK_ROW_H;
            boolean active = (idx == mm.getCurrentIndex());
            boolean hov = isIn(mx, my, x, ry, w, TRACK_ROW_H - 2);

            if (active) g.fill(x, ry, px + PANEL_W, ry + TRACK_ROW_H - 2, BG_ACTIVE);
            else if (hov) g.fill(x, ry, px + PANEL_W, ry + TRACK_ROW_H - 2, BG_CARD);

            // Number / play indicator
            if (active && mm.isPlaying())
                g.drawString(font, "▶", x + 12, ry + 16, PURPLE, false);
            else
                g.drawString(font, String.valueOf(idx + 1), x + 14, ry + 16, TEXT_DARK, false);

            // Name
            String name = truncate(track.getDisplayName(), w - 100);
            g.drawString(font, name, x + 34, ry + 10, active ? PURPLE : TEXT_MID, false);
            g.drawString(font, "Local File", x + 34, ry + 24, TEXT_DARK, false);

            // Duration placeholder
            g.drawString(font, "—:——", px + PANEL_W - 62, ry + 16, TEXT_DARK, false);
        }

        // Scrollbar
        if (tracks.size() > VISIBLE_ROWS) {
            int sbX = px + PANEL_W - 4;
            int sbH = (int)((float) VISIBLE_ROWS / tracks.size() * (VISIBLE_ROWS * TRACK_ROW_H));
            int sbY = listY + (int)((float) listScrollOffset / tracks.size() * (VISIBLE_ROWS * TRACK_ROW_H));
            g.fill(sbX, listY, sbX + 3, listY + VISIBLE_ROWS * TRACK_ROW_H, BG_CARD);
            g.fill(sbX, sbY, sbX + 3, sbY + sbH, TEXT_DARKER);
        }
    }

    private void renderPlayer(GuiGraphics g, int mx, int my) {
        int y = py + PANEL_H - PLAYER_H;
        g.fill(px, y, px + PANEL_W, py + PANEL_H, BG_DARK);
        g.fill(px, y, px + PANEL_W, y + 1, BORDER);

        MusicTrack cur = mm.getCurrentTrack();

        // Cover + track info
        int cx = px + 10;
        g.fill(cx, y + 12, cx + 38, y + 50, PURPLE_BG);
        g.fill(cx, y + 12, cx + 38, y + 13, PURPLE_DARK);
        g.fill(cx, y + 49, cx + 38, y + 50, PURPLE_DARK);
        g.fill(cx, y + 12, cx + 1, y + 50, PURPLE_DARK);
        g.fill(cx + 37, y + 12, cx + 38, y + 50, PURPLE_DARK);
        g.drawString(font, "♫", cx + 12, y + 24, PURPLE, false);

        if (cur != null) {
            String n = truncate(cur.getDisplayName(), 90);
            g.drawString(font, n, cx + 46, y + 18, 0xFFd0d0f0, false);
            g.drawString(font, "Local File", cx + 46, y + 32, TEXT_DIM, false);
        } else {
            g.drawString(font, "Нічого не грає", cx + 46, y + 25, TEXT_DIM, false);
        }

        // Center controls
        int cc = px + PANEL_W / 2;

        // Shuffle
        boolean sh = mm.isShuffling();
        boolean shH = isIn(mx, my, cc - 84, y + 12, 18, 16);
        g.drawString(font, "⇄", cc - 84, y + 12, sh ? PURPLE : (shH ? TEXT_MID : TEXT_DIM), false);

        // Prev
        boolean prevH = isIn(mx, my, cc - 56, y + 11, 18, 18);
        g.drawString(font, "⏮", cc - 58, y + 11, prevH ? WHITE : TEXT_DIM, false);

        // Play button (circle)
        int bx = cc - 14, by = y + 8;
        boolean playH = isIn(mx, my, bx, by, 28, 28);
        int btnBg = playH ? PURPLE_LIGHT : PURPLE;
        g.fill(bx, by, bx + 28, by + 28, btnBg);
        // circle corners (fake round)
        g.fill(bx, by, bx + 3, by + 3, BG_DARK);
        g.fill(bx + 25, by, bx + 28, by + 3, BG_DARK);
        g.fill(bx, by + 25, bx + 3, by + 28, BG_DARK);
        g.fill(bx + 25, by + 25, bx + 28, by + 28, BG_DARK);
        if (mm.isPlaying()) {
            g.fill(bx + 9, by + 7, bx + 12, by + 21, 0xFF0e0e12);
            g.fill(bx + 16, by + 7, bx + 19, by + 21, 0xFF0e0e12);
        } else {
            g.drawString(font, "▶", bx + 9, by + 9, 0xFF0e0e12, false);
        }

        // Next
        boolean nextH = isIn(mx, my, cc + 40, y + 11, 18, 18);
        g.drawString(font, "⏭", cc + 38, y + 11, nextH ? WHITE : TEXT_DIM, false);

        // Loop
        boolean lp = mm.isLooping();
        boolean lpH = isIn(mx, my, cc + 68, y + 12, 18, 16);
        g.drawString(font, "↺", cc + 68, y + 12, lp ? PURPLE : (lpH ? TEXT_MID : TEXT_DIM), false);

        // Progress bar
        int progX = cc - 120, progY = y + 44, progW = 240;
        float prog = mm.getProgress();
        g.fill(progX, progY, progX + progW, progY + 3, 0xFF1e1e2e);
        int filled = (int)(prog * progW);
        boolean progH = isIn(mx, my, progX, progY - 4, progW, 10);
        g.fill(progX, progY, progX + filled, progY + 3, progH ? PURPLE_LIGHT : PURPLE);
        // Knob
        g.fill(progX + filled - 5, progY - 3, progX + filled + 5, progY + 7, PURPLE_LIGHT);
        g.fill(progX + filled - 3, progY - 1, progX + filled + 3, progY + 5, PURPLE);

        g.drawString(font, mm.getCurrentTimeStr(), progX - 28, progY - 1, TEXT_DIM, false);
        g.drawString(font, mm.getTotalTimeStr(), progX + progW + 4, progY - 1, TEXT_DIM, false);

        // Volume
        int vx = px + PANEL_W - 110, vy = y + 44, vw = 64;
        g.drawString(font, "♪", vx - 14, vy - 2, TEXT_DIM, false);
        g.fill(vx, vy, vx + vw, vy + 3, 0xFF1e1e2e);
        int vf = (int)(mm.getVolume() * vw);
        g.fill(vx, vy, vx + vf, vy + 3, PURPLE);
        g.fill(vx + vf - 4, vy - 3, vx + vf + 4, vy + 6, PURPLE_LIGHT);
        g.fill(vx + vf - 2, vy - 1, vx + vf + 2, vy + 4, PURPLE);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int x = (int) mx, y = (int) my;
        int barY = py + PANEL_H - PLAYER_H;
        int cc = px + PANEL_W / 2;

        // Close
        if (isIn(x, y, px + PANEL_W - 20, py + 9, 14, 18)) { onClose(); return true; }

        // Sidebar: Refresh (index 1 of actions)
        int sbY = py + TOPBAR_H + 120 + 28;
        if (isIn(x, y, px + 6, sbY, SIDEBAR_W - 12, 24)) { mm.scanMusic(); return true; }

        // Track list
        int listY = py + TOPBAR_H + 84;
        if (x >= px + SIDEBAR_W && x <= px + PANEL_W && y >= listY && y < barY) {
            int idx = (y - listY) / TRACK_ROW_H + listScrollOffset;
            if (idx >= 0 && idx < mm.getTracks().size()) { mm.play(idx); return true; }
        }

        // Play
        if (isIn(x, y, cc - 14, barY + 8, 28, 28)) { mm.playOrPause(); return true; }
        if (isIn(x, y, cc - 56, barY + 11, 18, 18)) { mm.previous(); return true; }
        if (isIn(x, y, cc + 40, barY + 11, 18, 18)) { mm.next(); return true; }
        if (isIn(x, y, cc - 84, barY + 12, 18, 16)) { mm.setShuffling(!mm.isShuffling()); return true; }
        if (isIn(x, y, cc + 68, barY + 12, 18, 16)) { mm.setLooping(!mm.isLooping()); return true; }

        // Progress
        int progX = cc - 120, progY = barY + 44, progW = 240;
        if (isIn(x, y, progX, progY - 4, progW, 10)) {
            mm.seekTo((float)(x - progX) / progW);
            draggingProgress = true;
            return true;
        }

        // Volume
        int vx = px + PANEL_W - 110, vy = barY + 44, vw = 64;
        if (isIn(x, y, vx, vy - 4, vw, 10)) {
            mm.setVolume((float)(x - vx) / vw);
            draggingVolume = true;
            return true;
        }

        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        int x = (int) mx;
        int cc = px + PANEL_W / 2;
        if (draggingProgress) {
            mm.seekTo(Math.max(0f, Math.min(1f, (float)(x - (cc - 120)) / 240)));
            return true;
        }
        if (draggingVolume) {
            mm.setVolume(Math.max(0f, Math.min(1f, (float)(x - (px + PANEL_W - 110)) / 64)));
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        draggingVolume = false; draggingProgress = false;
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hd, double vd) {
        if ((int)mx >= px + SIDEBAR_W) {
            listScrollOffset = Math.max(0, Math.min(
                listScrollOffset - (int) vd,
                Math.max(0, mm.getTracks().size() - VISIBLE_ROWS)
            ));
            return true;
        }
        return super.mouseScrolled(mx, my, hd, vd);
    }

    @Override
    public boolean keyPressed(int kc, int sc, int mod) {
        if (kc == 256) { onClose(); return true; }
        if (kc == 32)  { mm.playOrPause(); return true; }
        if (kc == 262) { mm.next(); return true; }
        if (kc == 263) { mm.previous(); return true; }
        return super.keyPressed(kc, sc, mod);
    }

    @Override public void onClose() { this.minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }

    private boolean isIn(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
    private void drawCentered(GuiGraphics g, String t, int cx, int y, int col) {
        g.drawString(font, t, cx - font.width(t) / 2, y, col, false);
    }
    private String truncate(String s, int maxW) {
        if (font.width(s) <= maxW) return s;
        while (font.width(s + "...") > maxW && s.length() > 1) s = s.substring(0, s.length() - 1);
        return s + "...";
    }
}
