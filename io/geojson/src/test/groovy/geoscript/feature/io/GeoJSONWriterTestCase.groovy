package geoscript.feature.io

import geoscript.feature.Feature
import geoscript.feature.Field
import geoscript.feature.Schema
import geoscript.geom.Point
import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 * The GeoJSONWriter Unit Test
 * @author Jared Erickson
 */
class GeoJSONWriterTestCase {

    @Test void write() {
        Schema schema = new Schema("houses", [new Field("geom","Point"), new Field("name","string"), new Field("price","float")])
        Feature feature = new Feature([new Point(111,-47), "House", 12.5], "house1", schema)
        GeoJSONWriter writer = new GeoJSONWriter()
        String expected = """{"type":"Feature","geometry":{"type":"Point","coordinates":[111,-47]},"properties":{"name":"House","price":12.5},"id":"house1"}"""
        String actual = writer.write(feature)
        assertEquals expected, actual
    }

    @Test void writeWithOptions() {
        Schema schema = new Schema("houses", [new Field("geom","Point", "EPSG:4326"), new Field("name","string"), new Field("price","float")])
        Feature feature = new Feature([new Point(111.123456,-47.123456), "House", 12.5], "house1", schema)
        GeoJSONWriter writer = new GeoJSONWriter()
        String expected = """{"type":"Feature","crs":{"type":"name","properties":{"name":"EPSG:4326"}},"bbox":[111.123456,-47.123456,111.123456,-47.123456],"geometry":{"type":"Point","coordinates":[111.123456,-47.123456]},"properties":{"name":"House","price":12.5},"id":"house1"}"""
        String actual = writer.write(feature, decimals: 6, encodeFeatureBounds: true, encodeFeatureCRS: true,
                encodeFeatureCollectionBounds: true, encodeFeatureCollectionCRS: true)
        assertEquals expected, actual
    }

}
