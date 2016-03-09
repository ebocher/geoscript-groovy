package geoscript.layer.io

import geoscript.layer.Pyramid
import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 * The CsvPyramidReader Unit Test
 * @author Jared Erickson
 */
class CsvPyramidReaderTestCase {

    @Test void read() {
        String csv = """EPSG:3857
-2.0036395147881314E7,-2.0037471205137067E7,2.0036395147881314E7,2.003747120513706E7,EPSG:3857
BOTTOM_LEFT
256,256
0,1,1,156412.0,156412.0
1,2,2,78206.0,78206.0
2,4,4,39103.0,39103.0
3,8,8,19551.5,19551.5
4,16,16,9775.75,9775.75
5,32,32,4887.875,4887.875
6,64,64,2443.9375,2443.9375
7,128,128,1221.96875,1221.96875
8,256,256,610.984375,610.984375
9,512,512,305.4921875,305.4921875
10,1024,1024,152.74609375,152.74609375
11,2048,2048,76.373046875,76.373046875
12,4096,4096,38.1865234375,38.1865234375
13,8192,8192,19.09326171875,19.09326171875
14,16384,16384,9.546630859375,9.546630859375
15,32768,32768,4.7733154296875,4.7733154296875
16,65536,65536,2.38665771484375,2.38665771484375
17,131072,131072,1.193328857421875,1.193328857421875
18,262144,262144,0.5966644287109375,0.5966644287109375
19,524288,524288,0.29833221435546875,0.29833221435546875
""".denormalize()
        Pyramid p = new CsvPyramidReader().read(csv)
        assertEquals Pyramid.createGlobalMercatorPyramid(), p
    }
}
