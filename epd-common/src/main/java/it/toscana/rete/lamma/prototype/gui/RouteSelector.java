package it.toscana.rete.lamma.prototype.gui;

import dk.dma.epd.common.prototype.model.route.Route;
import org.geotools.ows.wms.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


/**
 * @author kappu72@gmail.com
 *
 */
public class RouteSelector extends JComboBox<String> {

    private static final Logger LOG = LoggerFactory.getLogger(RouteSelector.class);
    private DefaultComboBoxModel<Route> model;
    private static final long serialVersionUID = 1L;


    public RouteSelector(java.util.List<Route> routes) {
        super();
        setRenderer(new LayerRenderer());
        model = new DefaultComboBoxModel(routes.toArray());

        setModel((DefaultComboBoxModel) model);
        setToolTipText("Add a Route");
        setSize(200, 20);
        setSelectedIndex(-1);

    }

    /**
     * Add the ships
     *
     */
    public void addRoutes(java.util.List<Route> routes) {

        model.removeAllElements();
        routes.stream().forEach(model::addElement);
        setSelectedIndex(-1);

    }
    public void addRoute(Route route) {
        model.addElement(route);
    }
    public List<Route> getRoutes() {
        List<Route> l = new ArrayList<Route>();
        for(int i = 0; i < model.getSize(); i++) {
            l.add(model.getElementAt(i));
        }
        return l;
    }
    public void removeRoute(Route r) {
        model.removeElement(r);
        setSelectedIndex(-1);
    }
    private static class LayerRenderer extends BasicComboBoxRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Route) {
                setText(((Route) value).getName());
            }
            if (index == -1 && value == null) setText("Add Routes...");
            return this;
        }
    }

}
