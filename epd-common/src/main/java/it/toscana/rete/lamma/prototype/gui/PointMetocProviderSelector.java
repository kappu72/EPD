package it.toscana.rete.lamma.prototype.gui;


import it.toscana.rete.lamma.prototype.metocservices.PointMetocProvider;
import org.geotools.ows.wms.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class PointMetocProviderSelector extends JComboBox<PointMetocProvider> {

    private DefaultComboBoxModel<PointMetocProvider> model;
    private static final long serialVersionUID = 1L;

    private static String pattern = "dd/MM/yyyy HH:mm";
    private static DateFormat df;


    public PointMetocProviderSelector() {
        this(new ArrayList<>());
    }

    public PointMetocProviderSelector(List<PointMetocProvider> providers) {
        super();
        df = new SimpleDateFormat(pattern);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        setRenderer(new ProviderRenderer());
        model = new DefaultComboBoxModel(providers.toArray());

        setModel((DefaultComboBoxModel) model);
        setToolTipText("Select a source for metoc");
    }

    /**
     * Add the ships
     *
     */
    public void addProviders(List<PointMetocProvider> providers) {
        model.removeAllElements();
        for (PointMetocProvider p : providers) {
            model.addElement(p);
        }
    }
    private static class ProviderRenderer extends BasicComboBoxRenderer {


            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof PointMetocProvider) {
                    setText(((PointMetocProvider) value).getName());
                }
                return this;
            }

    }


}
