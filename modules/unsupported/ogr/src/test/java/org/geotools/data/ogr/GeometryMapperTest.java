package org.geotools.data.ogr;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

/**
 * 
 *
 * @source $URL$
 */
public abstract class GeometryMapperTest extends TestCaseSupport {

    GeometryFactory gf = new GeometryFactory();

    protected GeometryMapperTest(OGRDataStoreFactory dataStoreFactory) {
        super(dataStoreFactory);
    }

    public void testLine() throws Exception {
        checkRoundTrip("LINESTRING(0 0, 10 10)");
    }

    public void testPolygon() throws Exception {
        checkRoundTrip("POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))");
    }
    
    public void testPoint() throws Exception {
        checkRoundTrip("POINT(0 0)");
    }
    
    void checkRoundTrip(String geometryWkt) throws Exception {
        checkRoundTrip(geometryWkt, new GeometryMapper.WKB(gf, dataStoreFactory.getOGR()));
        checkRoundTrip(geometryWkt, new GeometryMapper.WKT(gf, dataStoreFactory.getOGR()));
    }

    void checkRoundTrip(String geometryWkt, GeometryMapper mapper) throws Exception {
        Geometry geometry = new WKTReader().read(geometryWkt);

        // to ogr and back
        OGR ogr = dataStoreFactory.getOGR();
        
        Object ogrGeometry = mapper.parseGTGeometry(geometry);
        Geometry remapped = mapper.parseOgrGeometry(ogrGeometry);

        ogr.GeometryDestroy(ogrGeometry);

        assertEquals(geometry, remapped);
    }

   

}
