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
package dk.dma.epd.common.prototype.event.mouse;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;

import dk.dma.epd.common.prototype.gui.views.ChartPanelCommon;

public class CommonNavigationMouseMode extends AbstractCoordMouseMode implements KeyListener {

    /**
     * Private fields.
     */
    private static final long serialVersionUID = 1L;
    public final Cursor NAV_CURSOR; // Default cursor for navigation.
    private ChartPanelCommon chartPanel;
    private int maxScale; // The max scaled size which can be zoomed into the
                          // map.
    protected boolean doZoom;
    protected boolean mouseExited;
    protected boolean layerMouseDrag;

    protected boolean mouseDragged;
    protected Point point1, point2;

    /**
     * Creates a new CommonNavigationMouseMode.<br>
     * The object is common behaviour for navigation around the map in ship side and shore side.
     * 
     * @param chartPanel
     *            The chart panel of the object which called this constructor.
     * @param maxScale
     *            The max scale value of the application settings.
     * @param modeid
     */
    public CommonNavigationMouseMode(ChartPanelCommon chartPanel, int maxScale, String modeid) {
        super(modeid, true);
        this.chartPanel = chartPanel;
        this.maxScale = maxScale;

        // Create the cursor for navigation.
        // This cursor can be used in the classes which will inherit from this
        // class.
        // Toolkit tk = Toolkit.getDefaultToolkit();
        // Image cursorIcon =
        // EPD.res().getCachedImageIcon("images/toolbar/zoom_cursor.png").getImage();
        // this.NAV_CURSOR = tk.createCustomCursor(cursorIcon, new Point(5, 5),
        // "zoom");
        this.NAV_CURSOR = new Cursor(Cursor.CROSSHAIR_CURSOR);
    }

    /**
     * Update and returns the scale value.
     * 
     * @param currentScale
     *            The current scale value used on the map.
     * @param scaleFactor
     *            The factor to update the scale value.
     * @return The updated scale factor.
     */
    protected float getNewScale(float currentScale, float scaleFactor) {

        float newScale = currentScale * scaleFactor;

        if (newScale < this.maxScale) {
            newScale = this.maxScale;
        }

        return newScale;
    }

    /**
     * Draws or erases boxes between two screen pixel points. The graphics from the map is set to XOR mode, and this method uses two
     * colors to make the box disappear if on has been drawn at these coordinates, and the box to appear if it hasn't.
     * 
     * @param pt1
     *            one corner of the box to drawn, in window pixel coordinates.
     * @param pt2
     *            the opposite corner of the box.
     */
    protected void paintRectangle(Graphics g, Point point1, Point point2) {

        // Set some colors.
        g.setXORMode(Color.LIGHT_GRAY);
        g.setColor(Color.DARK_GRAY);

        if (point1 != null && point2 != null) {
            int width = Math.abs(point2.x - point1.x);
            int height = Math.abs(point2.y - point1.y);

            if (width == 0) {
                width++;
            }
            if (height == 0) {
                height++;
            }

            g.drawRect(point1.x < point2.x ? point1.x : point2.x, point1.y < point2.y ? point1.y : point2.y, width, height);

            g.drawRect(point1.x < point2.x ? point1.x + (point2.x - point1.x) / 2 - 1 : point2.x + (point1.x - point2.x) / 2 - 1,
                    point1.y < point2.y ? point1.y + (point2.y - point1.y) / 2 - 1 : point2.y + (point1.y - point2.y) / 2 - 1, 2, 2);
        }
    }

    /**
     * Given a MapBean, which provides the projection, and the starting point of a box (pt1), look at pt2 to see if it represents
     * the ratio of the projection map size. If it doesn't, provide a point that does.
     */
    protected Point getRatioPoint(MapBean map, Point pt1, Point pt2) {

        if (map != null && pt1 != null && pt2 != null) {

            Projection proj = map.getProjection();
            float mapRatio = (float) proj.getHeight() / (float) proj.getWidth();

            float boxHeight = pt1.y - pt2.y;
            float boxWidth = pt1.x - pt2.x;
            float boxRatio = Math.abs(boxHeight / boxWidth);
            int isNegative = -1;
            if (boxRatio > mapRatio) {
                // box is too tall, adjust boxHeight
                if (boxHeight < 0) {
                    isNegative = 1;
                }
                boxHeight = Math.abs(mapRatio * boxWidth);
                pt2.y = pt1.y + isNegative * (int) boxHeight;

            } else if (boxRatio < mapRatio) {
                // box is too wide, adjust boxWidth
                if (boxWidth < 0) {
                    isNegative = 1;
                }
                boxWidth = Math.abs(boxHeight / mapRatio);
                pt2.x = pt1.x + isNegative * (int) boxWidth;
            }
        }
        return pt2;
    }

    @Override
    public void setActive(boolean active) {
        doZoom = false;
        mouseExited = false;
        layerMouseDrag = false;

        mouseDragged = false;
        super.setActive(active);

    }

    /**
     * If the mouse is pressed twice right after each other, this mouse event handler method will update the location on the map by
     * the position of the mouse. If the control button is pushed down when this method is called, a new scale value will be
     * calculated so that a zoom to the new position will be done too. If the control and shift button are both down at when called
     * a zoom out from the point will be done.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.getSource() instanceof MapBean && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2 && !e.isConsumed()) {

            // Fire the mouse support.
            super.mouseSupport.fireMapMouseClicked(e);

            // Get the map and a new location from the clicked position.
            MapBean map = (MapBean) e.getSource();
            Projection projection = map.getProjection();
            Proj proj = (Proj) projection;
            LatLonPoint llp = projection.inverse(e.getPoint());

            // Update the scale factor.
            if (e.isControlDown() && e.isShiftDown()) {
                proj.setScale(this.getNewScale(proj.getScale(), 2.0f));
            } else if (e.isControlDown()) {
                proj.setScale(this.getNewScale(proj.getScale(), 0.5f));
            }

            // Reset the points.
            this.point1 = null;
            this.point2 = null;

            // Update location on map.
            proj.setCenter(llp);
            map.setProjection(proj);
            this.doZoom = false;
        } else {
            if (e.getSource() instanceof MapBean && SwingUtilities.isRightMouseButton(e)) {
                doZoom = false;
                mouseExited = false;
                layerMouseDrag = false;
                mouseDragged = false;
            }
        }
    }

    /**
     * If the mouse is entered on a MapBean object, the doZoom boolean will be set to true.
     */
    @Override
    public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
        // this.doZoom = true;
        doZoom = false;
        mouseExited = false;
        layerMouseDrag = false;
        mouseDragged = false;
    }

    /**
     * If the mouse is exiting a MapBean object, the doZoom boolean will be set to false, which will prevent the map from zoom to
     * selected area if the mouse is released.
     */
    @Override
    public void mouseExited(MouseEvent e) {

        super.mouseExited(e);
        if (e.getSource() instanceof MapBean) {

            // Set to false to prevent zoom to selected area.
            this.doZoom = false;
            // Clear the drawn rectangle
            this.paintRectangle(((MapBean) e.getSource()).getGraphics(), this.point1, this.point2);
            // Reset point.
            this.point2 = null;

            this.mouseExited = true;
        }
        this.doZoom = false;
    }

    /**
     * If the the mouse is pressed down, the first point will be saved, the second point will be reset, and the doZoom boolean will
     * be set to true, so that if the mouse is releasted, after being dragged, a zoom to that selected area will be executed.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        if (e.getSource() instanceof MapBean && SwingUtilities.isLeftMouseButton(e) && !super.mouseSupport.fireMapMousePressed(e)) {
            // Set the first point.
            this.point1 = e.getPoint();
            // Reset the second point.
            this.point2 = null;
            // Prepare to zoom is now true.
            this.doZoom = true;
        }
    }

    /**
     * If the mouse is dragged, a test will be done if it is a layer related element. If it isn't, a rectangle will be drawn from
     * the first point, to the point of the mouse. If control is down when mouse is dragged the rectangle will follow the mouse.
     * Else the mouse will draw a rectangle fitted in ratio to the map frame.
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.getSource() instanceof MapBean && SwingUtilities.isLeftMouseButton(e) && this.doZoom) {

            super.mouseDragged(e);

            if (!this.mouseDragged) {
                this.layerMouseDrag = super.mouseSupport.fireMapMouseDragged(e);
            }

            if (this.layerMouseDrag && this.mouseExited) {

                this.mouseReleased(e);
                this.mouseExited = false;

            } else if (!this.layerMouseDrag) {

                this.mouseDragged = true;

                // Clear up the old point.
                this.paintRectangle(((MapBean) e.getSource()).getGraphics(), this.point1, this.point2);

                if (e.isControlDown()) {
                    this.point2 = e.getPoint();
                } else {
                    this.point2 = this.getRatioPoint((MapBean) e.getSource(), this.point1, e.getPoint());
                }

                // Clear up the old point.
                this.paintRectangle(((MapBean) e.getSource()).getGraphics(), this.point1, this.point2);

                // Repaint new rectangle.
                ((MapBean) e.getSource()).repaint();
            }
        }
    }

    /**
     * When the mouse is released, this event will check if the control button was held down, when the mouse was released. If it
     * was, it will find the best rectangle in ratio to fit the selected area into. If the control button was not held down, the
     * method will zoom to the selected rectangle.
     */
    @Override
    public void mouseReleased(MouseEvent e) {

        super.mouseReleased(e);
        if (e.getSource() instanceof MapBean && SwingUtilities.isLeftMouseButton(e) && this.point2 != null) {

            if (this.layerMouseDrag) {
                super.mouseSupport.fireMapMouseReleased(e);
            } else if (this.mouseDragged && this.doZoom) {

                // Reset boolean to enable dragging of other elements.
                this.mouseDragged = false;
                this.layerMouseDrag = false;

                // Get map projection.
                MapBean map = (MapBean) e.getSource();
                Projection projection = map.getProjection();
                Proj proj = (Proj) projection;

                synchronized (this) {

                    int centerX = 0;
                    int centerY = 0;
                    float scale = 0;
                    double selectedAreaWidth = 0;
                    double selectedAreaHeight = 0;

                    /*
                     * If control are held down, the code inside the statement will try to make a rectangle in ratio of the frame
                     * size (much like when control are not held down), where the selected area can fit into.
                     */
                    if (e.isControlDown()) {

                        Point fakeRatioPoint = null;
                        Point offsetPoint = null;
                        double rectangleRatio = 0;
                        selectedAreaWidth = Math.abs(e.getPoint().x - this.point1.x);
                        selectedAreaHeight = Math.abs(e.getPoint().y - this.point1.y);

                        // If the selected area is more wider than higher.
                        if (selectedAreaWidth > selectedAreaHeight) {

                            // Calculate the hight of the selected area.
                            double frameWidth = this.chartPanel.getWidth();
                            selectedAreaWidth = e.getPoint().x - this.point1.x;
                            rectangleRatio = frameWidth / selectedAreaWidth;
                            selectedAreaHeight = this.chartPanel.getMap().getHeight() / rectangleRatio;

                            // Create the fake ratio rectangle opposite point.
                            fakeRatioPoint = this.getRatioPoint(map, this.point1, new Point(e.getPoint().x,
                                    (int) (this.point1.y + selectedAreaWidth)));

                            // Create the off set point of the rectangle.
                            offsetPoint = new Point((int) (this.point1.x - selectedAreaWidth / this.point2.x),
                                    (int) (e.getPoint().y - selectedAreaHeight / 2));

                            // If the selected area is more higher than wider.
                        } else if (selectedAreaWidth < selectedAreaHeight) {

                            // Calculate the height of the selected area.
                            double frameHeight = this.chartPanel.getMap().getHeight();
                            selectedAreaHeight = e.getPoint().y - this.point1.y;
                            rectangleRatio = frameHeight / selectedAreaHeight;
                            selectedAreaWidth = this.chartPanel.getMap().getWidth() / rectangleRatio;

                            // Create the fake ratio rectangle opposite point.
                            fakeRatioPoint = this.getRatioPoint(map, this.point1, new Point(
                                    (int) (this.point1.x + selectedAreaWidth), e.getPoint().y));

                            // Create the off set point of the rectangle.
                            offsetPoint = new Point((int) (this.point1.x - selectedAreaWidth / 2),
                                    (int) (e.getPoint().y - selectedAreaHeight));
                        }

                        centerX = (int) (offsetPoint.x + selectedAreaWidth / 2);
                        centerY = (int) (offsetPoint.y + selectedAreaHeight / 2);
                        scale = ProjMath.getScale(offsetPoint, fakeRatioPoint, projection);

                    } else {

                        this.point2 = this.getRatioPoint(map, this.point1, e.getPoint());

                        selectedAreaWidth = Math.abs(this.point2.x - this.point1.x);
                        selectedAreaHeight = Math.abs(this.point2.y - this.point1.y);

                        scale = ProjMath.getScale(this.point1, this.point2, projection);

                        centerX = (int) (Math.min(this.point1.x, this.point2.x) + selectedAreaWidth / 2);
                        centerY = (int) (Math.min(this.point1.y, this.point2.y) + selectedAreaHeight / 2);
                    }

                    // If the selected area is too small, reset and dont zoom.
                    if (Math.abs(selectedAreaWidth) < 10 || Math.abs(selectedAreaHeight) < 10) {

                        // Reset rectangle.
                        paintRectangle(((MapBean) e.getSource()).getGraphics(), this.point1, this.point2);

                        // Reset points and zoom.
                        this.point1 = null;
                        this.point2 = null;
                        this.doZoom = false;

                        return;
                    }

                    // Ensure that the scale level wont zoom to far into
                    // the map.
                    if (scale < this.maxScale) {
                        scale = this.maxScale;
                    }

                    // Go to new projection.
                    LatLonPoint llp = projection.inverse(centerX, centerY);
                    proj.setScale(scale);
                    proj.setCenter(llp);
                    map.setProjection(proj);

                    // Reset points and zoom.
                    this.point1 = null;
                    this.point2 = null;
                    this.doZoom = false;
                }
            }
        }
    }

    /**
     * Called by the MapBean when it repaints, to let the MouseMode know when to update itself on the map. PaintListener interface.
     */
    @Override
    public void listenerPaint(Graphics g) {
        if (doZoom) {
            paintRectangle(g, this.point1, this.point2);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void keyTyped(KeyEvent e) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void keyPressed(KeyEvent e) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void keyReleased(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && this.point2 != null) {

            this.paintRectangle(((MapBean) e.getSource()).getGraphics(), this.point1, this.point2);
            this.mouseDragged = false;
            this.doZoom = false;
            this.point1 = null;
            this.point2 = null;
        }
    }

}
