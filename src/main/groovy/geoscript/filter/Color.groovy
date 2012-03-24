package geoscript.filter

import org.geotools.brewer.color.ColorBrewer
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.swing.JFrame

/**
 * A Color Expression and a set of Color Utilities used in the Style module.
 * You can create a Color from an RGB String
 * <pre><code>
 * new Color("0,255,0")
 * </code></pre>
 * or from a CSS color name:
 * <pre><code>
 * new Color("silver")
 * </code></pre>
 * or from a hexadecimal:
 * <pre><code>
 * new Color("#00ff00")
 * </code></pre>
 * or from a RGB List:
 * <pre><code>
 * new Color([0,255,0])
 * </code></pre>
 * or from a RGB Map:
 * <pre><code>
 * new Color([r: 0, g: 255, b: 0, a: 125])
 * </code></pre>
 * or from a HSL Map:
 * <pre><code>
 * new Color([h: 0, s: 1.0, l: 0.5])
 * </code></pre>
 * @author Jared Erickson
 */
class Color extends Expression {

    /**
     * Create a new Color from a value
     * @param value The value
     */
    Color(def value) {
        super(Expression.filterFactory.literal(toHex(value)))
    }

    /**
     * Get this Color's hex value
     * @return The hex value
     */
    String getHex() {
        this.value
    }

    /**
     * Get this Color's RGB value
     * @return A List of RGB values
     */
    List getRgb() {
        def color = getColor(this.value)
        [color.red, color.green, color.blue]
    }

    /**
     * Get this Color's HSL value
     * @return A List of HSL values all between 0 and 1
     */
    List getHsl() {
        def color = getColor(this.value)
        double r = color.red / 255.0
        double g = color.green / 255.0
        double b = color.blue / 255.0
        double lo = Math.min(Math.min(r,g),b)
        double hi = Math.max(Math.max(r,g),b)
        def (double h, double s, double l) = [(lo + hi) / 2.0] * 3
        if (lo == hi) {
            (h, s) = [0,0]
        } else {
            float d = (hi - lo) as float
            s = l > 0.5 ? d / (2 - hi - lo) : d / (hi + lo)
            switch (hi) {
                case r:
                    h = (g - b) / d + (g < b ? 6 : 0)
                    break
                case g:
                    h = (b - r) / d + 2
                    break
                case b:
                    h = (r - g) / d + 4
                    break
            }
            h /= 6
        }
        return [h,s,l]
    }

    /**
     * Interpolate a List of Colors between this Color and the given Color
     * @param color The other Color
     * @param n The number of Colors
     * @return A List of Colors
     */
    List interpolate(Color color, int n = 10) {
        List hsl1 = this.hsl
        List hsl2 = color.hsl
        List dhsl = [hsl1, hsl2].transpose().collect{x -> x[1] - x[0] }
        (0..n - 1).collect{r->
            List hsl = (0..hsl1.size()-1).collect{i ->
                double x = hsl1[i] as double
                double y = dhsl[i] as double
                x + (r / (float)n) * y
            }
            new Color([h: hsl[0], s: hsl[1], l: hsl[2]])
        }
    }

    /**
     * Interpolate a List Colors between the start and end Color
     * @param start The start Color
     * @param end The end Color
     * @param n The number of Colors
     * @return A List of Colors
     */
    static interpolate(Color start, Color end, int n = 10) {
        start.interpolate(end, n)
    }

    /**
     * Get a Color from an Object.  Handles CSS names (red, wheat),
     * hexadecimals Strings (#00FF00, #FFF), and RGB String ("255,255,0"), list ([0,255,0]), and map ([r: 255, g: 255, b: 0]).
     * @param color A Object convertable to a Color
     * @return a Color or null
     */
    static java.awt.Color getColor(def color) {
        // Color: Color.BLACK, new Color(255,255,255)
        if (color instanceof java.awt.Color) {
            return color
        }
        // RGB as String: "255,255,255"
        else if (color instanceof String && color.split(",").length >= 3) {
            String[] parts = color.split(",")
            int r = parts[0] as int
            int g = parts[1] as int
            int b =  parts[2] as int
            int a = parts.length > 3 ? parts[3] as int : 0
            return new java.awt.Color(r, g, b, a)
        }
        // geoscript.filter.Color
        else if (color instanceof Color) {
            return java.awt.Color.decode(color.toString())
        }
        // Hexadecimal: #00ff00
        else if (color instanceof String && color.startsWith("#")) {
            // #00ff00
            if (color.length() == 7) {
                return java.awt.Color.decode(color)
            }
            // #0f0
            else if (color.length() == 4) {
                String hex = "#" + color[1] * 2 + color[2] * 2 + color[3] * 2
                return java.awt.Color.decode(hex)
            }
            // Oops
            else {
                return null
            }
        }
        // CSS Color Names: wheat, black, navy
        else if (color instanceof String && colorNameMap.containsKey(color.toLowerCase())){
            return java.awt.Color.decode(colorNameMap.get(color.toLowerCase()))
        }
        // RGB as List: [255,255,255,0.1]
        else if (color instanceof List && color.size() >= 3) {
            int r = color[0] as int
            int g = color[1] as int
            int b =  color[2] as int
            int a = color.size > 3 ? color[3] as int : 0
            return new java.awt.Color(r, g, b, a)
        }
        // RGB as Map [r:255,g:255,b:0,a:125]
        else if (color instanceof Map && color.containsKey("r") && color.containsKey("g") && color.containsKey("b")) {
            int r = color.r as int
            int g = color.g as int
            int b =  color.b as int
            int a = color.containsKey('a') ? color.a as int : 0
            return new java.awt.Color(r, g, b, a)
        }
        // HSL as Map [h:0-1,s:0-1,l:0-1]
        else if (color instanceof Map && color.containsKey("h") && color.containsKey("s") && color.containsKey("l")) {
            Map rgb = hsl2rgb([color.h, color.s, color.l])
            return new java.awt.Color(rgb.r, rgb.g, rgb.b)
        }
        else {
            return null
        }
    }

    /**
     * Convert a Color to a hex color string
     * @param color The color value
     * @return A hex color string
     */
    static String toHex(def color) {
        if (color instanceof String && color.startsWith("#")) {
            return color
        } else if (color instanceof java.awt.Color) {
            return "#${Integer.toHexString((color.getRGB() & 0xffffff) | 0x1000000).substring(1)}"
        } else {
            def c = getColor(color)
            if (c != null) {
                return toHex(getColor(color))
            } else {
                return null
            }
        }
    }

    /**
     * Convert a HSL Color (as a List) into a Map of RGB values
     * @param hsl The HSL List
     * @return A Map of RGB values
     */
    private static Map hsl2rgb(List hsl) {
        double r,g,b
        def (double h, double s, double l) = hsl
        if (s == 0) {
            (r,g,b) = [l,l,l]
        } else {
            double q = l < 0.5 ? l * (1 + s) : l + s - l * s
            double p = 2 * l - q
            r = hue2rgb(p, q, h + 1/3)
            g = hue2rgb(p, q, h)
            b = hue2rgb(p, q, h - 1/3)
        }
        [
            r: Math.round(r * 255) as int,
            g: Math.round(g * 255) as int,
            b: Math.round(b * 255) as int
        ]
    }

    /**
     * Convert Hue to RGB
     */
    private static double hue2rgb(double p, double q, double t) {
        if (t < 0) t += 1
        if (t > 1) t -= 1
        if (t < 1/6) return p + (q - p) * 6.0 * t
        if (t < 1/2) return q
        if (t < 2/3) return p + (q - p) * (2/3.0 - t) * 6.0
        return p
    }

    /**
     * Generate a random color
     * @return A Color
     */
    static java.awt.Color getRandom() {
        def random = new java.util.Random()
        int red = random.nextInt(256)
        int green = random.nextInt(256)
        int blue = random.nextInt(256)
        new java.awt.Color(red,green,blue)
    }

    /**
     * Get a random pastel color
     * @return A Color
     */
    static java.awt.Color getRandomPastel() {
        java.util.Random random = new java.util.Random()
        int i = 128
        int r = random.nextInt(i)
        int g = random.nextInt(i)
        int b = random.nextInt(i)
        new java.awt.Color(r,g,b)
    }

    /**
     * The shared ColorBrewer instance
     */
    private static ColorBrewer colorBrewer

    /**
     * Initiate and load ColorBrewer if necessary
     */
    private static void loadColorBrewer() {
        if (colorBrewer == null) {
            colorBrewer = new ColorBrewer()
            colorBrewer.loadPalettes()
        }
    }

    /**
     * Get a PaletteType by a string
     * @param type The string type
     * @return A org.geotools.brewer.color.PaletteType
     */
    private static org.geotools.brewer.color.PaletteType getPaletteType(String type) {
        if (type.equalsIgnoreCase("diverging")) {
            return ColorBrewer.DIVERGING
        } else if (type.equalsIgnoreCase("qualitative")) {
            return ColorBrewer.QUALITATIVE
        } else if (type.equalsIgnoreCase("sequential")) {
            return ColorBrewer.SEQUENTIAL
        } else {
            return ColorBrewer.ALL
        }
    }

    /**
     * Get a List of Palette names by type (defaults to All)
     * @param type The type (all, diverging, qualitative, sequential)
     * @return A List of Palette names
     */
    static List getPaletteNames(String type = "all") {
        loadColorBrewer()
        colorBrewer.getPalettes(getPaletteType(type)).collect{palette ->
            palette.name
        }
    }

    /**
     * Get a List of Colors from a Palette by name
     * @param name The Palette name
     * @param count The number of colors
     * @return A List of Colors.  The List will be empty if the Palette can't
     * be found.  The number of Colors is constrained by the number of maximum
     * colors for the given palette.
     */
    static List getPaletteColors(String name, int count = -1) {
        loadColorBrewer()
        def colors = []
        def palette = colorBrewer.getPalette(name)
        if (count == -1) {
            count = palette.maxColors
        }
        if (palette != null) {
            colors.addAll(palette.getColors(Math.min(palette.maxColors, count)).toList())
        }
        colors
    }

    /**
     * CSS Color names
     */
    static final Map colorNameMap = [
        aliceblue: "#f0f8ff",
        antiquewhite: "#faebd7",
        aqua: "#00ffff",
        aquamarine: "#7fffd4",
        azure: "#f0ffff",
        beige: "#f5f5dc",
        bisque: "#ffe4c4",
        black: "#000000",
        blanchedalmond: "#ffebcd",
        blue: "#0000ff",
        blueviolet: "#8a2be2",
        brown: "#a52a2a",
        burlywood: "#deb887",
        cadetblue: "#5f9ea0",
        chartreuse: "#7fff00",
        chocolate: "#d2691e",
        coral: "#ff7f50",
        cornflowerblue: "#6495ed",
        cornsilk: "#fff8dc",
        crimson: "#dc143c",
        cyan: "#00ffff",
        darkblue: "#00008b",
        darkcyan: "#008b8b",
        darkgoldenrod: "#b8860b",
        darkgray: "#a9a9a9",
        darkgreen: "#006400",
        darkkhaki: "#bdb76b",
        darkmagenta: "#8b008b",
        darkolivegreen: "#556b2f",
        darkorange: "#ff8c00",
        darkorchid: "#9932cc",
        darkred: "#8b0000",
        darksalmon: "#e9967a",
        darkseagreen: "#8fbc8f",
        darkslateblue: "#483d8b",
        darkslategray: "#2f4f4f",
        darkturquoise: "#00ced1",
        darkviolet: "#9400d3",
        deeppink: "#ff1493",
        deepskyblue: "#00bfff",
        dimgray: "#696969",
        dodgerblue: "#1e90ff",
        firebrick: "#b22222",
        floralwhite: "#fffaf0",
        forestgreen: "#228b22",
        fuchsia: "#ff00ff",
        gainsboro: "#dcdcdc",
        ghostwhite: "#f8f8ff",
        gold: "#ffd700",
        goldenrod: "#daa520",
        gray: "#808080",
        green: "#008000",
        greenyellow: "#adff2f",
        honeydew: "#f0fff0",
        hotpink: "#ff69b4",
        indianred: "#cd5c5c",
        indigo: "#4b0082",
        ivory: "#fffff0",
        khaki: "#f0e68c",
        lavender: "#e6e6fa",
        lavenderblush: "#fff0f5",
        lawngreen: "#7cfc00",
        lemonchiffon: "#fffacd",
        lightblue: "#add8e6",
        lightcoral: "#f08080",
        lightcyan: "#e0ffff",
        lightgoldenrodyellow: "#fafad2",
        lightgrey: "#d3d3d3",
        lightgreen: "#90ee90",
        lightpink: "#ffb6c1",
        lightsalmon: "#ffa07a",
        lightseagreen: "#20b2aa",
        lightskyblue: "#87cefa",
        lightslategray: "#778899",
        lightsteelblue: "#b0c4de",
        lightyellow: "#ffffe0",
        lime: "#00ff00",
        limegreen: "#32cd32",
        linen: "#faf0e6",
        magenta: "#ff00ff",
        maroon: "#800000",
        mediumaquamarine: "#66cdaa",
        mediumblue: "#0000cd",
        mediumorchid: "#ba55d3",
        mediumpurple: "#9370d8",
        mediumseagreen: "#3cb371",
        mediumslateblue: "#7b68ee",
        mediumspringgreen: "#00fa9a",
        mediumturquoise: "#48d1cc",
        mediumvioletred: "#c71585",
        midnightblue: "#191970",
        mintcream: "#f5fffa",
        mistyrose: "#ffe4e1",
        moccasin: "#ffe4b5",
        navajowhite: "#ffdead",
        navy: "#000080",
        oldlace: "#fdf5e6",
        olive: "#808000",
        olivedrab: "#6b8e23",
        orange: "#ffa500",
        orangered: "#ff4500",
        orchid: "#da70d6",
        palegoldenrod: "#eee8aa",
        palegreen: "#98fb98",
        paleturquoise: "#afeeee",
        palevioletred: "#d87093",
        papayawhip: "#ffefd5",
        peachpuff: "#ffdab9",
        peru: "#cd853f",
        pink: "#ffc0cb",
        plum: "#dda0dd",
        powderblue: "#b0e0e6",
        purple: "#800080",
        red: "#ff0000",
        rosybrown: "#bc8f8f",
        royalblue: "#4169e1",
        saddlebrown: "#8b4513",
        salmon: "#fa8072",
        sandybrown: "#f4a460",
        seagreen: "#2e8b57",
        seashell: "#fff5ee",
        sienna: "#a0522d",
        silver: "#c0c0c0",
        skyblue: "#87ceeb",
        slateblue: "#6a5acd",
        slategray: "#708090",
        snow: "#fffafa",
        springgreen: "#00ff7f",
        steelblue: "#4682b4",
        tan: "#d2b48c",
        teal: "#008080",
        thistle: "#d8bfd8",
        tomato: "#ff6347",
        turquoise: "#40e0d0",
        violet: "#ee82ee",
        wheat: "#f5deb3",
        white: "#ffffff",
        whitesmoke: "#f5f5f5",
        yellow: "#ffff00",
        yellowgreen: "#9acd32"
    ]

    /**
     * Draw a List of Colors to an Image
     * @param colors The List of Colors
     * @param orientation The orientation (vertical or horizontal)
     * @param size The size of each Color swatch
     * @return A BufferedImage
     */
    static BufferedImage drawToImage(List colors, String orientation = "vertical", int size = 50) {
        int w = orientation == "vertical" ? size : colors.size() * size
        int h = orientation == "vertical" ? colors.size() * size : size
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        Graphics2D g2d = image.createGraphics()
        colors.eachWithIndex{color,i ->
            def c = Color.getColor(color)
            g2d.color = c
            int x = orientation == "vertical" ? 0 : i * size
            int y = orientation == "vertical" ? i * size : 0
            g2d.fillRect(x, y, w, size)
        }
        g2d.dispose()
        image
    }

    /**
     * Draw a List of Colors to a JFrame
     * @param colors The List of Colors
     * @param orientation The orientation (vertical or horizontal)
     * @param size The size of each Color swatch
     */
    static void draw(List colors, String orientation = "vertical", int size = 50) {
        def frame = new javax.swing.JFrame("GeoScript Colors")
        // If we are opening Windows from the GroovyConsole, we can't use EXIT_ON_CLOSE because the GroovyConsole
        // itself will exit
        if (java.awt.Frame.frames.find{it.title.contains("GroovyConsole")}) {
            frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        } else {
            // The Groovy Shell has a special SecurityManager that doesn't allow EXIT_ON_CLOSE
            try { frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE } catch (SecurityException ex) {frame.defaultCloseOperation = JFrame.HIDE_ON_CLOSE}
        }
        def panel = new javax.swing.JPanel()
        panel.add(new javax.swing.JLabel(new javax.swing.ImageIcon(drawToImage(colors, orientation, size))))
        int w = orientation == "vertical" ? size : colors.size() * size
        int h = orientation == "vertical" ? colors.size() * size : size
        panel.preferredSize = new java.awt.Dimension(w,h)
        frame.layout = new java.awt.BorderLayout()
        frame.add(panel, java.awt.BorderLayout.CENTER)
        frame.pack()
        frame.visible = true
    }

}
