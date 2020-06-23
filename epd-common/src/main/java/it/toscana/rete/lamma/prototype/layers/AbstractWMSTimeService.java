package it.toscana.rete.lamma.prototype.layers;

import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.CoordinateReferenceSystem;
import dk.dma.epd.common.prototype.layers.wms.AbstractWMSService;
import it.toscana.rete.lamma.prototype.metocservices.WMSMetocLayers;
import it.toscana.rete.lamma.prototype.model.LammaMetocWMSConfig;


import java.awt.geom.Point2D;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;


/**
    Add the time dimension to a wms layer and the possibility to configure layers
 */
public abstract class AbstractWMSTimeService extends AbstractWMSService {
    private CoordinateReferenceSystem crs = CoordinateReferenceSystem.getForCode("EPSG:900913");
    private LammaMetocWMSConfig wmsParams = new LammaMetocWMSConfig();
    private String wmsServiceURL;
    private static String pattern = "yyyy-MM-dd'T'HH:mm:00.000'Z'";
    private static DateFormat df = new SimpleDateFormat(pattern);

    public AbstractWMSTimeService(String wmsServiceURL) {
        super(wmsServiceURL);
        this.wmsServiceURL = wmsServiceURL;
        df = new SimpleDateFormat(pattern);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public AbstractWMSTimeService(String wmsServiceURL, Projection p) {
        super(wmsServiceURL, p);
        this.wmsServiceURL = wmsServiceURL;
        df = new SimpleDateFormat(pattern);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public LammaMetocWMSConfig getWmsParams() {
        return wmsParams;
    }
    public void setWmsParams(LammaMetocWMSConfig wmsP) {
        this.wmsParams = wmsP;
    }

    private String getRequestTime() {
        Date current = wmsParams.getCurrentTime();
        if(current != null) {
            return "&TIME=" + df.format(current);
        }
        return "";
    }

    private String getRequestLayers() {
        return "&LAYERS=" + wmsParams.getLayers().stream()
                .map(l -> WMSMetocLayers.valueOf(l).layerName() + "_" + wmsParams.getRun())
                .collect(Collectors.joining(","));
    }
    private String getInterpolation() {
        return "&INTERPOLATIONS=" + wmsParams.getLayers().stream()
                .map(l -> {
                    return WMSMetocLayers.valueOf(l).isInterpolate() ? "bicubic" : "";
                })
                .collect(Collectors.joining(","));
    }
    private String getBaseParams() {
        return "SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&FORMAT=image/png&TRANSPARENT=true&EXCEPTIONS=INIMAGE";
    }
    @Override
    protected String getQueryString() {
        return super.getQueryString() + getBaseParams() + getRequestLayers() + getRequestTime();
    }
    // openmap projetta in WGS 84/Pseudo-Mercator (Web Mercator, Google Web Mercator, Spherical Mercator, WGS 84 Web Mercato)
    // per questo il wms deve richiedere i dati in quella proiezione
    protected String getQueryString(Projection prj) {


        String queryString = "";
        String bbox = "undefined";
        String height = "undefined";
        String width = "undefined";

        if (prj != null) {
            Point2D ul = prj.getUpperLeft();
            Point2D lr = prj.getLowerRight();
            Point2D ulP = crs.forward(ul.getY(), ul.getX());
            Point2D lrP = crs.forward(lr.getY(), lr.getX());
            bbox = Double.toString(ulP.getX()) + "," + Double.toString(lrP.getY()) + ","
                    + Double.toString(lrP.getX()) + "," + Double.toString(ulP.getY());
            height = Integer.toString(prj.getHeight());
            width = Integer.toString(prj.getWidth());
        }

        queryString = wmsServiceURL + getBaseParams()+ "&SRS=EPSG:900913&CRS=EPSG:900913" +"&BBOX=" + bbox + "&WIDTH=" + width + "&HEIGHT=" + height + getRequestLayers() + getRequestTime() + getInterpolation();
        LOG.info(queryString);

        return queryString;


    }
}

