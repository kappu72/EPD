/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.epd.common.prototype.settings;

import java.io.Serializable;
import java.util.Properties;

import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.PropUtils;

/**
 * Map/chart settings
 */
public class MapSettings implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String PREFIX = "map.";

    private LatLonPoint center = new LatLonPoint.Double(56, 11);
    private float scale = 10000000;
    private boolean useEnc;
    private boolean useWms = true; //default = false
    private boolean encVisible;
    private boolean wmsVisible;
    private int maxScale = 5000;
    private boolean s52ShowText;
    private boolean s52ShallowPattern;
    private int s52ShallowContour = 6;
    private int s52SafetyDepth = 8;
    private int s52SafetyContour = 8;
    private int s52DeepContour = 10;
    private boolean useSimplePointSymbols = true;
    private boolean usePlainAreas;
    private boolean s52TwoShades;
    private String color = "Day";
    private String wmsQuery = "";
    private boolean multipleBackgrounds;
    private String lammaWMSservice = "https://geoportale.lamma.rete.toscana.it/geoserver_mare/ows";
    
    public MapSettings() {
    }

    public static String getPrefix() {
        return PREFIX;
    }

    public void readProperties(Properties props) {
        center.setLatitude(PropUtils.doubleFromProperties(props, PREFIX + "center_lat", center.getLatitude()));
        center.setLongitude(PropUtils.doubleFromProperties(props, PREFIX + "center_lon", center.getLongitude()));
        scale = PropUtils.floatFromProperties(props, PREFIX + "scale", scale);
        useEnc = PropUtils.booleanFromProperties(props, PREFIX + "useEnc", useEnc);
        useWms = PropUtils.booleanFromProperties(props, PREFIX + "useWms", useWms);
        encVisible = PropUtils.booleanFromProperties(props, PREFIX + "encVisible", encVisible);
        wmsVisible = PropUtils.booleanFromProperties(props, PREFIX + "wmsVisible", wmsVisible);
        maxScale = PropUtils.intFromProperties(props, PREFIX + "maxScale", maxScale);
        multipleBackgrounds = PropUtils.booleanFromProperties(props, PREFIX + "multipleBackgrounds", multipleBackgrounds);
        
        //settings for wms
        wmsQuery = props.getProperty(PREFIX + "wmsQuery", "");
        lammaWMSservice = props.getProperty(PREFIX + "lammaWMSservice", "https://geoportale.lamma.rete.toscana.it/geoserver_mare/ows");
        
        // settings for S52 layer
        s52ShowText = PropUtils.booleanFromProperties(props, PREFIX + "s52ShowText", s52ShowText);
        s52ShallowPattern = PropUtils.booleanFromProperties(props, PREFIX + "s52ShallowPattern", s52ShallowPattern);
        s52ShallowContour = PropUtils.intFromProperties(props, PREFIX + "s52ShallowContour", s52ShallowContour);
        s52SafetyDepth = PropUtils.intFromProperties(props, PREFIX + "s52SafetyDepth", s52SafetyDepth);
        s52SafetyContour = PropUtils.intFromProperties(props, PREFIX + "s52SafetyContour", s52SafetyContour);
        s52DeepContour = PropUtils.intFromProperties(props, PREFIX + "s52DeepContour", s52DeepContour);
        useSimplePointSymbols = PropUtils.booleanFromProperties(props, PREFIX + "useSimplePointSymbols", useSimplePointSymbols);
        usePlainAreas = PropUtils.booleanFromProperties(props, PREFIX + "usePlainAreas", usePlainAreas);
        s52TwoShades = PropUtils.booleanFromProperties(props, PREFIX + "s52TwoShades", s52TwoShades);
        color = props.getProperty(PREFIX + "color", color);

            }

    public void setProperties(Properties props) {
        props.put(PREFIX + "center_lat", Double.toString(center.getLatitude()));
        props.put(PREFIX + "center_lon", Double.toString(center.getLongitude()));
        props.put(PREFIX + "scale", Double.toString(scale));
        props.put(PREFIX + "useEnc", Boolean.toString(useEnc));
        props.put(PREFIX + "useWms", Boolean.toString(useWms));
        props.put(PREFIX + "encVisible", Boolean.toString(encVisible));
        props.put(PREFIX + "wmsVisible", Boolean.toString(wmsVisible));
        props.put(PREFIX + "maxScale", Integer.toString(maxScale));
        props.put(PREFIX + "multipleBackgrounds", Boolean.toString(multipleBackgrounds));
        
        props.put(PREFIX + "wmsQuery", wmsQuery);
        props.put(PREFIX + "lammaWMSservice", lammaWMSservice);
        
        // settings for S52 layer
        props.put(PREFIX + "s52ShowText", Boolean.toString(s52ShowText));
        props.put(PREFIX + "s52ShallowPattern", Boolean.toString(s52ShallowPattern));
        props.put(PREFIX + "s52ShallowContour", Integer.toString(s52ShallowContour));
        props.put(PREFIX + "s52SafetyDepth", Integer.toString(s52SafetyDepth));
        props.put(PREFIX + "s52SafetyContour", Integer.toString(s52SafetyContour));
        props.put(PREFIX + "s52DeepContour", Integer.toString(s52DeepContour));
        props.put(PREFIX + "useSimplePointSymbols", Boolean.toString(useSimplePointSymbols));
        props.put(PREFIX + "usePlainAreas", Boolean.toString(usePlainAreas));
        props.put(PREFIX + "s52TwoShades", Boolean.toString(s52TwoShades));
        props.put(PREFIX + "color", color);
    }
    
    /******************** Getters and setters **************************/
    
    public LatLonPoint getCenter() {
        return center;
    }

    public void setCenter(LatLonPoint center) {
        this.center = center;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public boolean isUseEnc() {
        return useEnc;
    }

    public void setUseEnc(boolean useEnc) {
        this.useEnc = useEnc;
    }
    
    public boolean isEncVisible() {
        return encVisible;
    }
    
    public boolean isUseWms() {
        return useWms;
    }

    public void setUseWms(boolean useWms) {
        this.useWms = useWms;
    }    
    
    public void setEncVisible(boolean encVisible) {
        this.encVisible = encVisible;
    }
    
    public boolean isWmsVisible() {
        return wmsVisible;
    }

    public void setWmsVisible(boolean wmsVisible) {
        this.wmsVisible = wmsVisible;
    }

    public int getMaxScale() {
        return maxScale;
    }
    
    public void setMaxScale(int maxScale) {
        this.maxScale = maxScale;
    }

    public boolean isS52ShowText() {
        return s52ShowText;
    }

    public void setS52ShowText(boolean s52ShowText) {
        this.s52ShowText = s52ShowText;
    }

    public boolean isS52ShallowPattern() {
        return s52ShallowPattern;
    }

    public void setS52ShallowPattern(boolean s52ShallowPattern) {
        this.s52ShallowPattern = s52ShallowPattern;
    }

    public String getWmsQuery() {
        return wmsQuery;
    }

    public void setWmsQuery(String wmsQuery) {
        this.wmsQuery = wmsQuery;
    }

    public int getS52ShallowContour() {
        return s52ShallowContour;
    }

    public void setS52ShallowContour(int s52ShallowContour) {
        this.s52ShallowContour = s52ShallowContour;
    }

    public int getS52SafetyDepth() {
        return s52SafetyDepth;
    }

    public void setS52SafetyDepth(int s52SafetyDepth) {
        this.s52SafetyDepth = s52SafetyDepth;
    }

    public int getS52SafetyContour() {
        return s52SafetyContour;
    }

    public void setS52SafetyContour(int s52SafetyContour) {
        this.s52SafetyContour = s52SafetyContour;
    }

    public int getS52DeepContour() {
        return s52DeepContour;
    }

    public void setS52DeepContour(int s52DeepContour) {
        this.s52DeepContour = s52DeepContour;
    }

    public boolean isUseSimplePointSymbols() {
        return useSimplePointSymbols;
    }

    public void setUseSimplePointSymbols(boolean useSimplePointSymbols) {
        this.useSimplePointSymbols = useSimplePointSymbols;
    }

    public boolean isUsePlainAreas() {
        return usePlainAreas;
    }

    public void setUsePlainAreas(boolean usePlainAreas) {
        this.usePlainAreas = usePlainAreas;
    }

    public boolean isS52TwoShades() {
        return s52TwoShades;
    }

    public void setS52TwoShades(boolean s52TwoShades) {
        this.s52TwoShades = s52TwoShades;
    }

    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public boolean isMultipleBackgrounds() {
        return multipleBackgrounds;
    }
    
    public void setMultipleBackgrounds(boolean multipleBackgrounds) {
        this.multipleBackgrounds = multipleBackgrounds;
    }

    public String getLammaWMSservice() {
        return lammaWMSservice;
    }

    public void setLammaWMSservice(String lammaWMSservice) {
        this.lammaWMSservice = lammaWMSservice;
    }
}
