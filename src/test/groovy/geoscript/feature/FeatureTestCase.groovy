package geoscript.feature

import geoscript.filter.Property
import org.junit.Test
import static org.junit.Assert.*
import geoscript.geom.*
import geoscript.AssertUtil
import com.vividsolutions.jts.geom.Geometry as JtsGeometry

/**
 * The Feature UnitTest
 */
class FeatureTestCase {

    @Test void constructors() {

        // The Schema
        Schema s1 = new Schema("houses", [new Field("geom","Point"), new Field("name","string"), new Field("price","float")])

        // Create a Feature from a List of values
        Feature f1 = new Feature([new Point(111,-47), "House", 12.5], "house1", s1)
        assertEquals "houses.house1 geom: POINT (111 -47), name: House, price: 12.5", f1.toString()
        assertTrue(f1.geom instanceof Geometry)
        assertTrue(f1.f.defaultGeometry instanceof JtsGeometry)

        // Create a Feature from a Map of values
        Feature f2 = new Feature(["geom": new Point(111,-47), "name": "House", "price": 12.5], "house1", s1)
        assertEquals "houses.house1 geom: POINT (111 -47), name: House, price: 12.5", f2.toString()
        assertTrue(f2.geom instanceof Geometry)
        assertTrue(f2.f.defaultGeometry instanceof JtsGeometry)

        // Create a Feature from a Map of values with no Schema
        Feature f3 = new Feature(["geom": new Point(111,-47), "name": "House", "price": 12.5], "house1")
        assertEquals "feature.house1 geom: POINT (111 -47), name: House, price: 12.5", f3.toString()
        assertEquals "feature geom: Point, name: String, price: java.math.BigDecimal", f3.schema.toString()
        assertTrue(f3.geom instanceof Geometry)
        assertTrue(f3.f.defaultGeometry instanceof JtsGeometry)

        // Create a feature from a Map of values where some keys don't match the Schema
        Feature f4 = new Feature(["geom": new Point(111,-47), "name": "House", "price": 12.5, "description": "very nice", "rating": 4], "house1", s1)
        assertEquals "houses.house1 geom: POINT (111 -47), name: House, price: 12.5", f4.toString()
    }

    @Test void getId() {
        Schema s1 = new Schema("houses", [new Field("geom","Point"), new Field("name","string"), new Field("price","float")])
        Feature f1 = new Feature([new Point(111,-47), "House", 12.5], "house1", s1)
        assertEquals "house1", f1.id
    }

    @Test void getGeom() {
        Schema s1 = new Schema("houses", [new Field("geom","Point"), new Field("name","string"), new Field("price","float")])
        Feature f1 = new Feature([new Point(111,-47), "House", 12.5], "house1", s1)
        assertEquals "POINT (111 -47)", f1.geom.toString()
        assertTrue(f1.geom instanceof Geometry)
    }

    @Test void setGeom() {
        Schema s1 = new Schema("houses", [new Field("geom","Point"), new Field("name","string"), new Field("price","float")])
        Feature f1 = new Feature([new Point(111,-47), "House", 12.5], "house1", s1)
        assertEquals "POINT (111 -47)", f1.geom.toString()
        f1.geom = new Point(121, -49)
        assertEquals "POINT (121 -49)", f1.geom.toString()
    }

    @Test void getBounds() {
        Schema s1 = new Schema("houses", [new Field("geom","LineString", "EPSG:4326"), new Field("name","string"), new Field("price","float")])
        Feature f1 = new Feature([new LineString([1,1], [10,10]), "House", 12.5], "house1", s1)
        Bounds b = f1.bounds
        assertEquals(1, b.minX, 0.0)
        assertEquals(1, b.minY, 0.0)
        assertEquals(10, b.maxX, 0.0)
        assertEquals(10, b.maxY, 0.0)
        assertEquals("EPSG:4326", b.proj.id)
    }

    @Test void get() {
        Schema s1 = new Schema("houses", [new Field("geom","Point"), new Field("name","string"), new Field("price","float")])
        Feature f1 = new Feature([new Point(111,-47), "House", 12.5], "house1", s1)
        assertEquals "POINT (111 -47)", f1.get("geom").toString()
        assertEquals 12.5, f1.get("price"), 0.1
        assertEquals "House", f1.get("name")
        assertEquals "House", f1.get(s1.get("name"))
    }

    @Test void getAt() {
        Schema s1 = new Schema("houses", [new Field("geom","Point"), new Field("name","string"), new Field("price","float")])
        Feature f1 = new Feature([new Point(111,-47), "House", 12.5], "house1", s1)
        assertEquals "POINT (111 -47)", f1["geom"].toString()
        assertEquals 12.5, f1["price"], 0.1
        assertEquals "House", f1["name"]
        assertEquals "House", f1[s1.get("name")]
    }

    @Test void set() {
        Schema s1 = new Schema("houses", [new Field("geom","Point"), new Field("name","string"), new Field("price","float")])
        Feature f1 = new Feature([new Point(111,-47), "House", 12.5], "house1", s1)

        assertEquals "POINT (111 -47)", f1.get("geom").toString()
        f1.set("geom",  new Point(121, -49))
        assertEquals "POINT (121 -49)", f1.get("geom").toString()

        assertEquals 12.5, f1.get("price"), 0.1
        f1.set("price", 23.9)
        assertEquals 23.9, f1.get("price"), 0.1

        assertEquals "House", f1.get("name")
        f1.set("name", "Work")
        assertEquals "Work", f1.get("name")

        assertEquals "Work", f1.get(s1.get("name"))
        f1.set(s1.get("name"), "Home")
        assertEquals "Home", f1.get(s1.get("name"))

        Feature f2 = s1.feature([geom: new Point(121,-49), price: 15.6, name: "Test"])
        f1.set(f2)
        assertEquals f2['price'], f1['price'], 0.1
        assertEquals f2['name'], f1['name']
        assertEquals f2.geom.wkt, f1.geom.wkt
        // Values from map
        f1.set([price: 1200.5, name: "Car"])
        assertEquals 1200.5, f1['price'], 0.1
        assertEquals "Car", f1['name']
        // Values from named parameters
        f1.set(price: 12.2, name: "Book")
        assertEquals 12.2, f1['price'], 0.1
        assertEquals "Book", f1['name']
    }

    @Test void putAt() {
        Schema s1 = new Schema("houses", [new Field("geom","Point"), new Field("name","string"), new Field("price","float")])
        Feature f1 = new Feature([new Point(111,-47), "House", 12.5], "house1", s1)

        assertEquals "POINT (111 -47)", f1["geom"].toString()
        f1["geom"] = new Point(121, -49)
        assertEquals "POINT (121 -49)", f1["geom"].toString()

        assertEquals 12.5, f1["price"], 0.1
        f1["price"] =  23.9
        assertEquals 23.9, f1["price"], 0.1

        assertEquals "House", f1["name"]
        f1["name"] = "Work"
        assertEquals "Work", f1["name"]

        def fld = s1.get("name")
        assertEquals "Work", f1[fld]
        f1[fld] = "Home"
        assertEquals "Home", f1[fld]
    }

    @Test void getAttributes() {
        Schema s1 = new Schema("houses", [new Field("geom","Point"), new Field("name","string"), new Field("price","float")])
        Feature f1 = new Feature([new Point(111,-47), "House", 12.5], "house1", s1)
        Map attributes = f1.attributes
        assertEquals "POINT (111 -47)", attributes['geom'].toString()
        assertTrue(attributes['geom'] instanceof Geometry)
        assertEquals "House", attributes['name']
        assertEquals 12.5, attributes['price'], 0.1
    }

    @Test void toStringTest() {
        Schema s1 = new Schema("houses", [new Field("geom","Point"), new Field("name","string"), new Field("price","float")])
        Feature f1 = new Feature([new Point(111,-47), "House", 12.5], "house1", s1)
        assertEquals "houses.house1 geom: POINT (111 -47), name: House, price: 12.5", f1.toString()
    }

    @Test void getGeoJSON() {
        Schema s1 = new Schema("houses", [new Field("geom","Point"), new Field("name","string"), new Field("price","float")])
        Feature f1 = new Feature([new Point(111,-47), "House", 12.5], "house1", s1)
        assertEquals """{"type":"Feature","geometry":{"type":"Point","coordinates":[111,-47]},"properties":{"name":"House","price":12.5},"id":"house1"}""", f1.geoJSON
    }

    @Test void getGeoRSS() {
        Schema s1 = new Schema("houses", [new Field("geom","Point"), new Field("name","string"), new Field("price","float")])
        Feature f1 = new Feature([new Point(111,-47), "House", 12.5], "house1", s1)
        AssertUtil.assertStringsEqual "<entry xmlns:georss='http://www.georss.org/georss' xmlns='http://www.w3.org/2005/Atom'>" +
                "<title>house1</title>" +
                "<summary>[geom:POINT (111 -47), name:House, price:12.5]</summary>" +
                "<updated>12/7/2013</updated>" +
                "<georss:point>-47.0 111.0</georss:point>" +
                "</entry>", f1.getGeoRSS(feedType: "atom", geometryType: "simple", itemDate: "12/7/2013")
    }

    @Test void getGml() {
        Schema s1 = new Schema("houses", [new Field("geom","Point"), new Field("name","string"), new Field("price","float")])
        Feature f1 = new Feature([new Point(111,-47), "House", 12.5], "house1", s1)
        AssertUtil.assertStringsEqual """<gsf:houses xmlns:gsf="http://geoscript.org/feature" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml" fid="house1">
<gml:name>House</gml:name>
<gsf:geom>
<gml:Point>
<gml:coord>
<gml:X>111.0</gml:X>
<gml:Y>-47.0</gml:Y>
</gml:coord>
</gml:Point>
</gsf:geom>
<gsf:price>12.5</gsf:price>
</gsf:houses>
""", f1.gml
    }

    @Test void getKml() {
        Schema s1 = new Schema("houses", [new Field("geom","Point"), new Field("name","string"), new Field("price","float")])
        Feature f1 = new Feature([new Point(111,-47), "House", 12.5], "house1", s1)
        AssertUtil.assertStringsEqual """<kml:Placemark xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:kml="http://earth.google.com/kml/2.1" id="house1">
<kml:name>House</kml:name>
<kml:Point>
<kml:coordinates>111.0,-47.0</kml:coordinates>
</kml:Point>
</kml:Placemark>
""", f1.kml
    }

    @Test void getGpx() {
        Schema s1 = new Schema("houses", [new Field("geom","Point"), new Field("name","string"), new Field("price","float")])
        Feature f1 = new Feature([new Point(111,-47), "House", 12.5], "house1", s1)
        AssertUtil.assertStringsEqual "<wpt lat='-47.0' lon='111.0' xmlns='http://www.topografix.com/GPX/1/1'>" +
                "<name>House</name><desc>House costs \$12.5</desc></wpt>",
                f1.getGpx(name: new Property("name"), description: {Feature f -> "${f['name']} costs \$${f['price']}"})
    }
}

