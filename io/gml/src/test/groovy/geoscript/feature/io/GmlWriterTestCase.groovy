package geoscript.feature.io

import geoscript.AssertUtil
import geoscript.feature.Feature
import geoscript.feature.Field
import geoscript.feature.Schema
import geoscript.geom.Point
import org.junit.Test

/**
 * The GmlWriter UnitTest
 */
class GmlWriterTestCase {

    @Test void write() {

        // The Schema
        Schema schema = new Schema("houses", [new Field("geom","Point"), new Field("name","string"), new Field("price","float")])

        // Create a Feature from a List of values
        Feature feature = new Feature([new Point(111,-47), "House", 12.5], "house1", schema)

        // Write the Feature to a GML String
        GmlWriter writer = new GmlWriter()
        String expected = """<gsf:houses xmlns:gsf="http://geoscript.org/feature" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml" fid="house1">
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
"""
        String actual = writer.write(feature)
        AssertUtil.assertStringsEqual expected, actual, removeXmlNS: true, trim: true
    }

}