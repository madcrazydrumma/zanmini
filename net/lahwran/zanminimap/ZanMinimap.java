package net.lahwran.zanminimap;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;

/**
 * Main Zanminimap class where everything happens
 * 
 * @author lahwran
 */
public class ZanMinimap {

    /** Current build version */
    public static final String zmodver = "v0.10.5";

    /** Minecraft version */
    public static final String mcvers = "1.7.3";

    /** World loaded, so we can detect when the world changes */
    public String worldname = "";

    /** ObfHub instance, public for things that want to plug into the minimap */
    public ObfHub obfhub;

    /**
     * Config instance, public for things that want to plug into the minimap
     */
    public Config conf;

    /**
     * MapCalculator instance, public for things that want to plug into the minimap
     */
    public MapCalculator mapcalc;

    /**
     * MapRenderer instance, public for things that want to plug into the minimap
     */
    public MapRenderer renderer;

    /**
     * Menu instance, public for things that want to plug into the minimap
     */
    public Menu menu;

    public Map map;

    public TextureManager texman;

    /**
     * Instance, mainly for things that want to plug into the minimap
     */
    public static ZanMinimap instance;

    /**
     * Some things dealing with screen size seem to mysteriously shift by
     * five for no apparent reason. this was a test to see what changing it did.
     * it screwed up rendering pretty bad to change it.
     */
    public static final double mysteriousFivePointO = 0.0d;
    /**
     * @see mysteriousFivePointO
     */
    public static final int heightOffset = (int)mysteriousFivePointO;
    /**
     * 
     */
    public ZanMinimap() {

        map = new Map();
        obfhub = new ObfHub(this);
        texman = new TextureManager(this);
        conf = new Config(this);
        mapcalc = new MapCalculator(this);
        menu = new Menu(this);
        renderer = new MapRenderer(this);

        conf.initializeEverything();
        mapcalc.start();

        instance = this;
    }

    /**
     * Heartbeat function called each render by whatever is managing the minimap.
     * 
     * @param mc Minecraft instance to initialize obfhub.game with
     */
    public void onRenderTick(Minecraft mc) {
        if (obfhub.game == null)
            obfhub.game = mc;

        if (!obfhub.safeToRun())
            return;

        int dim = obfhub.getCurrentDimension();
        if (dim != obfhub.lastdim) {
            map.timer = 400;
            conf.cavemap = dim < 0;
            conf.lightmap = true;
            conf.heightmap = !conf.cavemap;
            conf.netherpoints = conf.cavemap;
            obfhub.lastdim = dim;
            conf.saveConfig();
            if (conf.cavemap) {
                conf.full = false;
                conf.zoom = 1;
                menu.error = "Cavemap zoom (2.0x)";
            }
        }
        String worldName = obfhub.getWorldName();

        if (!worldname.equals(worldName)) {
            worldname = worldName;
            menu.iMenu = 1;
            conf.loadWaypoints();
        }

        int[] scSize = obfhub.getScreenSize();
        int scWidth = scSize[0];
        int scHeight = scSize[1];

        if (!obfhub.isMenuShowing()) {
            if (Keyboard.isKeyDown(conf.menuKey)) {
                menu.iMenu = 2;
                obfhub.showPlaceholderGui();
            }
            if (Keyboard.isKeyDown(conf.zoomKey)) {
                conf.nextZoom();
            }
        }

        if (obfhub.isIngameMenuUp() ^ Keyboard.isKeyDown(Keyboard.KEY_F6))
            conf.enabled = false;
        else
            conf.enabled = true;

        scWidth -= ZanMinimap.heightOffset;
        scHeight -= ZanMinimap.heightOffset;

        obfhub.onRenderTick();
        mapcalc.onRenderTick();
        menu.onRenderTick(scWidth, scHeight);
        renderer.onRenderTick(scWidth, scHeight);
    }

    
}
