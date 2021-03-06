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
package dk.dma.epd.common.prototype.layers.intendedroute;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import com.bbn.openmap.omGraphics.OMGraphicList;

import dk.dma.enav.model.geometry.Position;
import dk.dma.epd.common.Heading;
import dk.dma.epd.common.prototype.layers.common.WpCircle;
import dk.dma.epd.common.prototype.model.route.IntendedRoute;
import dk.dma.epd.common.prototype.model.route.RouteLeg;
import dk.dma.epd.common.prototype.model.route.RouteWaypoint;
import dk.dma.epd.common.prototype.sensor.pnt.PntTime;

/**
 * Graphic for intended route
 */
public class IntendedRouteGraphic extends OMGraphicList {

    private static final long serialVersionUID = 1L;
    private static final float SCALE = 0.6f; // "Size" of graphics
    private static final int TTL = 600; // Time to live of graphics, i.e. 10
                                        // minutes

    /**
     * Valid colors for intended routes
     */
    public static final Color[] COLORS = { new Color(170, 40, 40), // red'ish
            new Color(40, 170, 40), // green'ish
            new Color(40, 40, 130), // blue'ish
            new Color(170, 170, 40), // yellow'ish
            new Color(100, 100, 100), // gray'ish
            new Color(240, 240, 240), // white'ish
            new Color(20, 20, 20) // black'ish
    };

    private IntendedRoute intendedRoute;
    private IntendedRouteLegGraphic activeWpLine;
    private Color routeColor = COLORS[1];
    private String name;
    private boolean arrowsVisible;
    private Position vesselPos;
    Color highlightColor = new Color(3, 137, 3);

    private List<IntendedRouteLegGraphic> routeLegs = new ArrayList<>();
    private List<WpCircle> routeWps = new ArrayList<>();
    private ActiveIntendedRouteWpCircle activeWpCircle;

    // private PlannedPositionGraphic plannedPositionArea = new
    // PlannedPositionGraphic();

    /**
     * Constructor
     */
    public IntendedRouteGraphic() {
        super();

        // Temp fix for disabling planned position Area
        // add(plannedPositionArea);
    }

    /**
     * Creates a new route leg line
     * 
     * @param index
     *            the route leg index
     * @param start
     *            the start position
     * @param end
     *            the end position
     * @param heading
     *            the heading of the leg
     */
    private IntendedRouteLegGraphic makeLegLine(int index, Position start, Position end, Heading heading) {
        IntendedRouteLegGraphic leg = new IntendedRouteLegGraphic(index, this, false, start, end, heading, routeColor, SCALE, false);
        leg.setArrows(arrowsVisible);
        // routeLegs.add(leg);
        // add(leg);

        return leg;
    }

    /**
     * Creates a new route circle
     * 
     * @param index
     *            the index of the way point
     * @param wp
     *            the way point position
     */
    private void makeWpCircleActiveWP(int index, Position wp) {

        activeWpCircle = new ActiveIntendedRouteWpCircle(this, index, wp.getLatitude(), wp.getLongitude(), routeColor, SCALE);
        // add(activeWpCircle);
        // routeWps.add(activeWpCircle);

        graphics.add(activeWpCircle);

    }

    /**
     * Creates a new route circle
     * 
     * @param index
     *            the index of the way point
     * @param wp
     *            the way point position
     */
    private IntendedRouteWpCircle makeWpCircle(int index, Position wp) {

        // if (intendedRoute.getActiveWpIndex() == index) {
        // ActiveIntendedRouteWpCircle activeWpCircle = new ActiveIntendedRouteWpCircle(this, index, wp.getLatitude(),
        // wp.getLongitude(), routeColor, SCALE);
        // add(activeWpCircle);
        // routeWps.add(activeWpCircle);
        //
        // return activeWpCircle;
        // }

        IntendedRouteWpCircle wpCircle = new IntendedRouteWpCircle(this, index, wp.getLatitude(), wp.getLongitude(), routeColor,
                SCALE);

        // routeWps.add(wpCircle);
        // add(wpCircle);
        return wpCircle;
    }

    /**
     * Flags whether or not to display arrows on the legs
     * 
     * @param show
     *            whether or not to display arrows on the legs
     */
    public void showArrowHeads(boolean show) {
        if (this.arrowsVisible != show) {
            for (IntendedRouteLegGraphic routeLeg : routeLegs) {
                routeLeg.setArrows(show);
            }
            this.arrowsVisible = show;
        }
    }

    /**
     * Updates the intended route color
     * 
     * @param color
     *            the color to use
     */
    private void updateColor(Color color) {
        int i = 0;
        for (IntendedRouteLegGraphic routeLeg : routeLegs) {
            if (i < intendedRoute.getActiveWpIndex()) {
                routeLeg.setLinePaint(adjustColor(color, 0.3f, 0.9f));
            } else {
                routeLeg.setLinePaint(color);
            }
            i++;
        }
        i = 0;
        for (WpCircle routeWp : routeWps) {
            if (i < intendedRoute.getActiveWpIndex()) {
                routeWp.setLinePaint(adjustColor(color, 0.3f, 0.9f));
            } else {
                routeWp.setLinePaint(color);
            }
            i++;
        }
        if (activeWpLine != null) {
            activeWpLine.setLinePaint(color);
        }
    }

    /**
     * Called when the vessel target has been updated
     * 
     * @param vesselPos
     *            the vessel position
     */
    public synchronized void updateVesselPosition(Position vesselPos) {

        if ((vesselPos == null && this.vesselPos != null) || !vesselPos.equals(this.vesselPos)) {
            this.vesselPos = vesselPos;
            renderIntendedRoute();
        }
        if (!this.isVisible()) {
            // plannedPositionArea.setVisible(false);
        } else {
            // plannedPositionArea.setVisible(true);
        }
    }

    /**
     * Called when the graphics should be updated with the current intended route
     */
    public synchronized void updateIntendedRoute() {
        updateIntendedRoute(intendedRoute);

        // Update planned position
        // plannedPositionArea.moveSymbol(intendedRoute.getPlannedPosition(),
        // intendedRoute.getPlannedPositionBearing(), 1000, 500);
    }

    /**
     * Called when the intended route has been updated
     * 
     * @param intendedRoute
     *            the intended route data
     */
    public synchronized void updateIntendedRoute(IntendedRoute intendedRoute) {

        if (this.name == null) {
            this.name = "ID:" + intendedRoute.getMmsi();
        }

        this.intendedRoute = intendedRoute;
        // Always re-paint, since color may change over time
        renderIntendedRoute();
    }

    private void updateGraphics() {

        int x = 0;
        int legsCreated = 0;
        for (RouteWaypoint wp : intendedRoute.getWaypoints()) {

            WpCircle wpCircle;
            IntendedRouteLegGraphic wpLeg;

            if (x == intendedRoute.getActiveWpIndex()) {

                if (routeWps.size() > x && activeWpCircle != null) {
                    activeWpCircle.setLatLon(wp.getPos().getLatitude(), wp.getPos().getLongitude());
                } else {
                    makeWpCircleActiveWP(x, wp.getPos());
                }
            }

            if (routeWps.size() > x && routeWps.get(x) != null) {
                wpCircle = routeWps.get(x);
                wpCircle.setLatLon(wp.getPos().getLatitude(), wp.getPos().getLongitude());
            } else {

                wpCircle = makeWpCircle(x, wp.getPos());
                routeWps.add(wpCircle);
                graphics.add(wpCircle);
            }

            RouteLeg leg = wp.getOutLeg();
            if (leg != null) {

                legsCreated++;

                if (routeLegs.size() > x && routeWps.get(x) != null) {

                    wpLeg = routeLegs.get(x);

                    Position startPos = leg.getStartWp().getPos();
                    Position endPos = leg.getEndWp().getPos();

                    double[] lls = { startPos.getLatitude(), startPos.getLongitude(), endPos.getLatitude(), endPos.getLongitude() };
                    double[] currentlls = wpLeg.getLL();

                    if (!(lls[0] == currentlls[0] && lls[1] == currentlls[1] && lls[2] == currentlls[2] && lls[3] == currentlls[3])) {
                        wpLeg.setLL(lls);
                    }
                } else {
                    // // Make the leg
                    wpLeg = makeLegLine(x + 1, leg.getStartWp().getPos(), leg.getEndWp().getPos(), leg.getHeading());

                    routeLegs.add(wpLeg);
                    graphics.add(wpLeg);
                }
            }

            x++;
        }

        // Remove excess legs and waypoints incase we are at the start/end of route
        while (intendedRoute.getWaypoints().size() != routeWps.size()) {

            WpCircle toBeRemoved = routeWps.get(routeWps.size() - 1);
            graphics.remove(toBeRemoved);
            routeWps.remove(routeWps.size() - 1);

        }

        while (legsCreated != routeLegs.size()) {
            IntendedRouteLegGraphic toBeRemoved = routeLegs.get(routeLegs.size() - 1);
            graphics.remove(toBeRemoved);
            routeLegs.remove(routeLegs.size() - 1);
        }

        // Adjust the transparency of the color depending on the last-received
        // time for the route
        long secondsSinceReceived = (PntTime.getDate().getTime() - intendedRoute.getReceived().getTime()) / 1000L;

        if (secondsSinceReceived < TTL) {
            float factor = 1.0f - (float) secondsSinceReceived / (float) TTL;
            Color color = adjustColor(routeColor, factor, factor);
            updateColor(color);
            setVisible(intendedRoute.isVisible());
        } else {
            setVisible(false);
        }
    }

    /**
     * Re-renders the intended route
     */
    private void renderIntendedRoute() {

        updateGraphics();

        // Handle empty route
        if (intendedRoute == null || !intendedRoute.hasRoute()) {

//            System.out.println("No route, it has been deactivated?");

            // Clear the graphics
            clear();
            routeLegs = new ArrayList<>();
            routeWps = new ArrayList<>();
            activeWpCircle = null;
            activeWpLine = null;
            setVisible(false);

            return;
        }

        // Update leg to first way point
        if (vesselPos != null) {

            // its null, make it
            if (activeWpLine == null) {
                // Attempt to set the heading of this leg to that
                // of the in-leg of the active way point
                Heading heading = Heading.RL;
                if (intendedRoute.getActiveWaypoint().getInLeg() != null) {
                    heading = intendedRoute.getActiveWaypoint().getInLeg().getHeading();
                }
                Position activeWpPos = intendedRoute.getActiveWaypoint().getPos();
                activeWpLine = new IntendedRouteLegGraphic(0, this, true, vesselPos, activeWpPos, heading, routeColor, SCALE, true);

                add(activeWpLine);

                // It already exists, but do we need to update it
            } else {

                // Position activeWpPos = intendedRoute.getActiveWaypoint().getPos();

                double[] lls = { vesselPos.getLatitude(), vesselPos.getLongitude(),
                        intendedRoute.getActiveWaypoint().getPos().getLatitude(),
                        intendedRoute.getActiveWaypoint().getPos().getLongitude() };
                double[] currentlls = activeWpLine.getLL();

                if (!(lls[0] == currentlls[0] && lls[1] == currentlls[1] && lls[2] == currentlls[2] && lls[3] == currentlls[3])) {
                    activeWpLine.setLL(lls);
                }
            }

        } else {
            // Remove wpLine
            if (activeWpLine != null) {
                remove(activeWpLine);
                activeWpLine = null;
            }
        }

        // Instead of clearing all graphics, update each waypoint and leg

        // Update legs (Remove old ones if less than new)

        // Update waypoints (Remove old ones if less than new)

    }

    /**
     * Adjusts the saturation and opacity of the color according to the parameters
     * 
     * @param col
     *            the color to adjust
     * @param saturation
     *            the change to saturation
     * @param opacity
     *            the change to opacity
     * @return the result
     */
    private Color adjustColor(Color col, float saturation, float opacity) {
        float[] hsb = Color.RGBtoHSB(col.getRed(), col.getGreen(), col.getBlue(), null);
        hsb[1] = (float) Math.max(Math.min(hsb[1] * saturation, 1.0), 0.0);
        col = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
        int alpha = (int) Math.max(Math.min(255.0 * opacity, 255.0), 0.0);
        return new Color(col.getRed(), col.getGreen(), col.getBlue(), alpha);
    }

    /**
     * Turn on anti-aliasing
     */
    @Override
    public void render(Graphics g) {
        Graphics2D image = (Graphics2D) g;
        image.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.render(image);
    }

    /**
     * Returns a reference to the associated intended route
     * 
     * @return a reference to the associated intended route
     */
    public IntendedRoute getIntendedRoute() {
        return intendedRoute;
    }

    /**
     * Returns the name of the associated vessel
     * 
     * @return the name of the associated vessel
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the associated vessel
     * 
     * @param name
     *            the name of the associated vessel
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the color for the intended route
     * 
     * @return the color for the intended route
     */
    public Color getRouteColor() {
        return routeColor;
    }

    /**
     * Sets the color for the intended route
     * 
     * @param routeColor
     *            the color for the intended route
     */
    public void setRouteColor(Color routeColor) {
        this.routeColor = routeColor;
    }

    /**
     * Returns the current vessel position
     * 
     * @return the current vessel position
     * @return
     */
    public Position getVesselPostion() {
        return vesselPos;
    }

    public void highlightRoute() {

        int i = 0;
        for (IntendedRouteLegGraphic routeLeg : routeLegs) {
            if (i < intendedRoute.getActiveWpIndex()) {
                routeLeg.setLinePaint(adjustColor(highlightColor, 0.3f, 0.9f));

                routeLeg.setStroke(new BasicStroke(4.0f * SCALE, // Width
                        BasicStroke.CAP_SQUARE, // End cap
                        BasicStroke.JOIN_MITER, // Join style
                        10.0f * SCALE, // Miter limit
                        new float[] { 3.0f * SCALE, 10.0f * SCALE }, // Dash
                                                                     // pattern
                        0.0f)); // Dash phase)

            } else {
                routeLeg.setLinePaint(highlightColor);

                routeLeg.setStroke(new BasicStroke(4.0f * SCALE, // Width
                        BasicStroke.CAP_SQUARE, // End cap
                        BasicStroke.JOIN_MITER, // Join style
                        10.0f * SCALE, // Miter limit
                        new float[] { 10.0f * SCALE, 8.0f * SCALE }, // Dash
                                                                     // pattern
                        0.0f)); // Dash phase)
            }

            i++;
        }
        i = 0;
        for (WpCircle routeWp : routeWps) {
            if (i < intendedRoute.getActiveWpIndex()) {
                routeWp.setLinePaint(adjustColor(highlightColor, 0.3f, 0.9f));
            } else {
                routeWp.setLinePaint(highlightColor);
            }
            i++;
        }
        if (activeWpLine != null) {
            activeWpLine.setLinePaint(highlightColor);

            activeWpLine.setStroke(new BasicStroke(4.0f * SCALE, // Width
                    BasicStroke.CAP_SQUARE, // End cap
                    BasicStroke.JOIN_MITER, // Join style
                    10.0f * SCALE, // Miter limit
                    new float[] { 3.0f * SCALE, 10.0f * SCALE }, // Dash pattern
                    0.0f)); // Dash phase)
        }

    }

    public void unHightlightRoute() {

        int i = 0;
        for (IntendedRouteLegGraphic routeLeg : routeLegs) {
            if (i < intendedRoute.getActiveWpIndex()) {
                routeLeg.setLinePaint(adjustColor(routeColor, 0.3f, 0.9f));

                routeLeg.setStroke(new BasicStroke(2.0f * SCALE, // Width
                        BasicStroke.CAP_SQUARE, // End cap
                        BasicStroke.JOIN_MITER, // Join style
                        10.0f * SCALE, // Miter limit
                        new float[] { 3.0f * SCALE, 10.0f * SCALE }, // Dash
                                                                     // pattern
                        0.0f)); // Dash phase)

            } else {
                routeLeg.setLinePaint(routeColor);

                routeLeg.setStroke(new BasicStroke(2.0f * SCALE, // Width
                        BasicStroke.CAP_SQUARE, // End cap
                        BasicStroke.JOIN_MITER, // Join style
                        10.0f * SCALE, // Miter limit
                        new float[] { 10.0f * SCALE, 8.0f * SCALE }, // Dash
                                                                     // pattern
                        0.0f)); // Dash phase)
            }

            i++;
        }
        i = 0;
        for (WpCircle routeWp : routeWps) {
            if (i < intendedRoute.getActiveWpIndex()) {
                routeWp.setLinePaint(adjustColor(routeColor, 0.3f, 0.9f));
            } else {
                routeWp.setLinePaint(routeColor);
            }
            i++;
        }
        if (activeWpLine != null) {
            activeWpLine.setLinePaint(routeColor);

            activeWpLine.setStroke(new BasicStroke(2.0f * SCALE, // Width
                    BasicStroke.CAP_SQUARE, // End cap
                    BasicStroke.JOIN_MITER, // Join style
                    10.0f * SCALE, // Miter limit
                    new float[] { 3.0f * SCALE, 10.0f * SCALE }, // Dash pattern
                    0.0f)); // Dash phase)
        }

    }

}
