package com.example.civiclensai.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class GeoLocationResolverTest {

    @Test
    public void testNullOrEmptyAddressReturnsPuneDefault() {
        double[] coordsNull = GeoLocationResolver.resolveCoordinates(null, null);
        assertNotNull(coordsNull);
        assertEquals(2, coordsNull.length);
        assertEquals(GeoLocationResolver.PUNE_LAT, coordsNull[0], 0.001);
        assertEquals(GeoLocationResolver.PUNE_LNG, coordsNull[1], 0.001);

        double[] coordsEmpty = GeoLocationResolver.resolveCoordinates(null, "   ");
        assertNotNull(coordsEmpty);
        assertEquals(2, coordsEmpty.length);
        assertEquals(GeoLocationResolver.PUNE_LAT, coordsEmpty[0], 0.001);
        assertEquals(GeoLocationResolver.PUNE_LNG, coordsEmpty[1], 0.001);
    }

    @Test
    public void testSectorKeywordResolution() {
        double[] kothrud = GeoLocationResolver.resolveCoordinates(null, "Kothrud, Pune");
        assertNotNull(kothrud);
        assertEquals(18.5074, kothrud[0], 0.01);
        assertEquals(73.8077, kothrud[1], 0.01);

        double[] fcRoad = GeoLocationResolver.resolveCoordinates(null, "FC Road, Pune");
        assertNotNull(fcRoad);
        assertEquals(18.5308, fcRoad[0], 0.01);
        assertEquals(73.8474, fcRoad[1], 0.01);

        double[] vimanNagar = GeoLocationResolver.resolveCoordinates(null, "Viman Nagar");
        assertNotNull(vimanNagar);
        assertEquals(18.5679, vimanNagar[0], 0.01);
        assertEquals(73.9143, vimanNagar[1], 0.01);
    }

    @Test
    public void testMetroCitiesKeywordResolution() {
        double[] mumbai = GeoLocationResolver.resolveCoordinates(null, "Mumbai Central");
        assertNotNull(mumbai);
        assertEquals(19.0760, mumbai[0], 0.01);
        assertEquals(72.8777, mumbai[1], 0.01);

        double[] delhi = GeoLocationResolver.resolveCoordinates(null, "Delhi Connaught Place");
        assertNotNull(delhi);
        assertEquals(28.6139, delhi[0], 0.01);
        assertEquals(77.2090, delhi[1], 0.01);
    }
}
