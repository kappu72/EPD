package it.toscana.rete.lamma.prototype.layers;

import com.bbn.openmap.proj.Proj;
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
        if(wmsServiceURL.endsWith("?") || wmsServiceURL.contains("?")){
            this.wmsServiceURL = wmsServiceURL;
        }else {
            this.wmsServiceURL = wmsServiceURL.concat("?");
        }
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public AbstractWMSTimeService(String wmsServiceURL, Projection p) {
        super(wmsServiceURL, p);
        if(wmsServiceURL.endsWith("?") || wmsServiceURL.contains("?")){
            this.wmsServiceURL = wmsServiceURL;
        }else {
            this.wmsServiceURL = wmsServiceURL.concat("?");
        }
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public String getWmsServiceURL() {
        return wmsServiceURL;
    }

    public void setWmsServiceURL(String wmsServiceURL) {
        this.wmsServiceURL = wmsServiceURL;
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
                .collect(Collectors.joining(","));
    }
    private String getRequestLayer(String layer) {
        return WMSMetocLayers.get(layer).layerName();
    }
    private String getInterpolation() {
        return "&INTERPOLATIONS=" + wmsParams.getLayers().stream()
                .map(l -> {
                    return WMSMetocLayers.get(l).isInterpolate() ? "bilinear" : " ";
                })
                .collect(Collectors.joining(",")).replace(" ", "");
    }
    private String getBaseParams() {
        return "SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&FORMAT=image/png&TRANSPARENT=true&EXCEPTIONS=INIMAGE";
    }
    // https://geoportale.lamma.rete.toscana.it/geoserver/WW3_MEDIT_RUN00/ows?SERVICE=WMS&EXCEPTIONS=application%2Fvnd.ogc.se_xml&TRANSPARENT=TRUE&VERSION=1.3.0&REQUEST=GetLegendGraphic&ELEVATION=0.0&CRS=EPSG%3A900913&LAYER=ww3_medit_Sig_height_of_wind_waves_and_swell_surface_20200626T000000000Z&STYLE=&LEGEND_OPTIONS=forceLabels%3Aon%3BfontSize%3A10&WIDTH=12&HEIGHT=12&FORMAT=image%2Fgif&SCALE=17471284.63743896
    private String getLegendBaseParams() {
        return "SERVICE=WMS&VERSION=1.3.0&REQUEST=GetLegendGraphic&ELEVATION=0.0&CRS=EPSG:900913&STYLES=&FORMAT=image/png&EXCEPTIONS=application/vnd.ogc.se_xml&&LEGEND_OPTIONS=forceLabels:on;fontSize:10;border:true";
    }
    private String getLegend() {
        String l = "";
        if(this.wmsParams.getLegend()){
          l = "&format_options=layout:legend&legend_options=fontAntiAliasing:true";
        }
        return l;
    }
    @Override
    protected String getQueryString() {
        return super.getQueryString() + getBaseParams() + getRequestLayers() + getRequestTime();
    }
    // openmap projetta in WGS 84/Pseudo-Mercator (Web Mercator, Google Web Mercator, Spherical Mercator, WGS 84 Web Mercator)
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

        queryString = wmsServiceURL + getBaseParams()+ "&SRS=EPSG:900913&CRS=EPSG:900913" +"&BBOX=" + bbox + "&WIDTH=" + width + "&HEIGHT=" + height + getRequestLayers() + getInterpolation() + getRequestTime();


        LOG.info("STRINGA RICHIESTA: " + queryString);

        return queryString;


    }

    /**
     *
     * @return legend query string
     * https://geoportale.lamma.rete.toscana.it/geoserver/WW3_MEDIT_RUN00/ows?SERVICE=WMS&EXCEPTIONS=application%2Fvnd.ogc.se_xml&TRANSPARENT=TRUE&VERSION=1.3.0&REQUEST=GetLegendGraphic&ELEVATION=0.0&CRS=EPSG%3A900913&LAYER=ww3_medit_Sig_height_of_wind_waves_and_swell_surface_20200626T000000000Z&STYLE=&LEGEND_OPTIONS=forceLabels%3Aon%3BfontSize%3A10&WIDTH=12&HEIGHT=12&FORMAT=image%2Fgif&SCALE=17471284.63743896
     */
    protected String getLegendQueryString(float scale, String layer ) {
        String queryString = wmsServiceURL + getLegendBaseParams() + "&LAYER=" + getRequestLayer(layer) + "&SCALE=" + scale + "&WIDTH=100";
        LOG.info("STRINGA LEGENDA RICHIESTA: " + queryString);
        return queryString;
    }
}

