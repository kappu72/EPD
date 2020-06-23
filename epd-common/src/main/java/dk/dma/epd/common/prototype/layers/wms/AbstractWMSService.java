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
package dk.dma.epd.common.prototype.layers.wms;

import java.util.Observable;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;

import dk.dma.epd.common.prototype.event.WMSEvent;
import dk.dma.epd.common.prototype.event.WMSEventListener;
import dk.dma.epd.common.prototype.status.WMSStatus;

public abstract class AbstractWMSService extends Observable {
    protected Logger LOG;

    protected String wmsQuery = "";
    protected String width;
    protected String height;
    protected Double upperLeftLon;
    protected Double upperLeftLat;
    protected Double lowerRightLon;
    protected Double lowerRightLat;

    protected int wmsWidth;
    protected int wmsHeight;
    protected Double wmsullon;
    protected Double wmsullat;
    
    private Double deltaX = 0.000000;
    private Double deltaY = 0.000000;
    protected WMSStatus status = new WMSStatus();
    protected float zoomLevel = -1;
    protected final CopyOnWriteArrayList<WMSEventListener> listeners;

    public AbstractWMSService(String wmsQuery) {

        this.LOG = LoggerFactory.getLogger(this.getClass());
        this.wmsQuery = wmsQuery;
        this.listeners = new CopyOnWriteArrayList<>();
    }

    public AbstractWMSService(String wmsQuery, Projection p) {
        this(wmsQuery);
        this.setWMSPosition(p);
        this.setZoomLevel(p);

    }

    protected void setZoomLevel(Projection p) {
        setZoomLevel(p.getScale());
    }

    protected void setWMSPosition(Projection p) {
        setWMSPosition(p.getUpperLeft().getX(), p.getUpperLeft().getY(), p.getUpperLeft().getX(), p.getUpperLeft().getY(), p
                .getLowerRight().getX(), p.getLowerRight().getY(), p.getWidth(), p.getHeight());
    }

    protected void setZoomLevel(float zoom) {
        zoomLevel = zoom;
    }

    /**
     * Set the position of the WMS image and what area we wish to display
     * 
     * @param ullon
     * @param ullat
     * @param upperLeftLon
     * @param upperLeftLat
     * @param lowerRightLon
     * @param lowerRightLat
     * @param w
     * @param h
     */
    protected void setWMSPosition(Double ullon, Double ullat, Double upperLeftLon, Double upperLeftLat, Double lowerRightLon,
            Double lowerRightLat, int w, int h) {

        this.wmsWidth = w;
        this.wmsHeight = h;
        this.wmsullon = ullon;
        this.wmsullat = ullat + 1;
        this.width = Integer.toString(w);
        this.height = Integer.toString(h);

        this.upperLeftLon = upperLeftLon;
        this.upperLeftLat = upperLeftLat + 1;
        this.lowerRightLon = lowerRightLon ;
        this.lowerRightLat = lowerRightLat ;

    }

    /**
     * Get the generated WMS query
     * 
     * @author David A. Camre (davidcamre@gmail.com)
     * @return
     */
    protected String getQueryString() {
        String queryString = "";
        queryString = wmsQuery + "&BBOX=" + getBbox() + "&WIDTH=" + width + "&HEIGHT=" + height;
        return queryString;
    }

    public void setWMSString(String wmsString) {
        this.wmsQuery = wmsString;
    }

    /**
     * After the query has been generated this completes it and returns a OMGraphiclist of the graphics
     * 
     * @return
     */
    public abstract OMGraphicList getWmsList(Projection p);

    public String getBbox() {
        // @author Renoud Because finished education and 10 years of experince
        // we know to add the delta values
        return Double.toString(upperLeftLon + deltaX) + "," + Double.toString(lowerRightLat + deltaY) + ","
                + Double.toString(lowerRightLon + deltaX) + "," + Double.toString(upperLeftLat + deltaY);

    }

    public static Projection normalizeProjection(Projection p) {
        // TODO: implement
        return (Projection) p.makeClone();

    }

    public void addWMSEventListener(WMSEventListener l) {
        this.listeners.add(l);
    }

    public void removeMyChangeListener(WMSEventListener l) {
        this.listeners.remove(l);
    }

    // Event firing method. Called internally by other class methods.
    protected void fireWMSEvent() {
        WMSEvent evt = new WMSEvent(this);

        for (WMSEventListener l : listeners) {
            l.changeEventReceived(evt);
        }
    }

}
