package org.example.client.consumers;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.example.annotations.Item;
import org.example.annotations.Parameter;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

@Item(name = "InfluxEmitter")
public class InfluxEmitter implements DataDirector {

    public static final String LON = "lon";
    public static final String LAT = "lat";
    public static final String CPM = "cpm";
    public static final double COORD_DEFAULT = 0.0;
    private WriteApiBlocking _writeApiBlocking;

    @Parameter(name = "urlEndpoint", description = "URL of the Influx DB endpoint")
    public String _urlEndpoint;

    @Parameter(name = "token", description = "InfluxDB Auth token")
    public String _token;

    @Parameter(name = "bucket", description = "InfluxDB bucket")
    public String _bucket;

    @Parameter(name = "org", description = "InfluxDB Org name")
    public String _org;

    private InfluxDBClient buildConnection(String url, String token, String bucket, String org) {
        return InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
    }

    WriteApiBlocking getWriteApiBlocking() {
        return _writeApiBlocking;
    }

    private Optional<GeoLocation> getLocation() {
        return Optional.of(new GeoLocation(27.837274, -82.837803));
    }

    private Supplier<GeoLocation> getLocationSupplier() {
        return () -> new GeoLocation(27.837274, -82.837803);
    }

    @Override
    public void direct(int count) {
        GeoLocation geo = getLocationSupplier().get();
        Point point = Point.measurement("GCM-500")
                .addField(LON, geo.getLongitude())
                .addField(LAT, geo.getLatitude())
                .addField(CPM, count)
                .time(Instant.now(), WritePrecision.MS);
        _writeApiBlocking.writePoint(point);
    }

    @Override
    public boolean initialize() {
            InfluxDBClient  influxDBClient = buildConnection(_urlEndpoint,
                _token,
                _bucket,
                _org);
            _writeApiBlocking = influxDBClient.getWriteApiBlocking();
            return true;
    }

    class GeoLocation {
        private double latitude;
        private double longitude;

        public GeoLocation(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }
        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }
}
