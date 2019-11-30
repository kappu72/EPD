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
package dk.dma.epd.common.prototype.gui.metoc;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import dk.dma.epd.common.prototype.communication.webservice.ShoreServiceException;
import dk.dma.epd.common.prototype.model.route.Route;
import dk.dma.epd.common.prototype.model.route.RoutesUpdateEvent;
import dk.dma.epd.common.prototype.route.RouteManagerCommon;
import dk.frv.enav.common.xml.metoc.MetocForecast;

/**
 * Dialog shown when requesting METOC
 */
public class MetocRequestDialog extends JDialog implements Runnable, ActionListener {
    
    private static final long serialVersionUID = 1L;
    
    private RouteManagerCommon routeManager;
    private Route route;
    private Window parent;
    private JLabel statusLbl;
    private JButton cancelBtn;
    private Boolean cancelReq = false;
    private Boolean rawMetoc = false;
    
    public MetocRequestDialog(Window parent, RouteManagerCommon routeManager, Route route) {
        super(parent, "Request METOC");
        this.routeManager = routeManager;
        this.route = route;
        this.parent = parent;
        
        initGui();        
    }
    
    public static void requestMetoc(Window parent, RouteManagerCommon routeManager, Route route) {
        MetocRequestDialog metocRequestDialog = new MetocRequestDialog(parent, routeManager, route);
        metocRequestDialog.doRequestMetoc();
        metocRequestDialog = null;
    }
    
    private void doRequestMetoc() {
        // Start thread
        new Thread(this).start();
        
        // Set dialog visible
        setVisible(true);
    }
    
    @Override
    public void run() {
        // Vanno differenziti i servizi qui olo possiamo fare dopo a monte
        ShoreServiceException error = null;
        try {
            routeManager.requestRouteMetoc(route);
        } catch (ShoreServiceException e) {
            error = e;
        }
        
        if (isCancelReq()) {
            route.removeMetoc();
            routeManager.notifyListeners(RoutesUpdateEvent.ROUTE_METOC_CHANGED);
            return;
        }
        
        if (error == null) {
            routeManager.notifyListeners(RoutesUpdateEvent.ROUTE_METOC_CHANGED);
        }
        
        // Close dialog        
        setVisible(false);        
        
        // Give response        
        if (error != null) {
            String text = error.getMessage();
            if (error.getExtraMessage() != null) {
                text += ": " + error.getExtraMessage();
            }
            JOptionPane.showMessageDialog(parent, text, "Shore service error",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            MetocForecast metocForecast = route.getMetocForecast();
            JOptionPane.showMessageDialog(parent, "Received " + metocForecast.getForecasts().size() + " METOC forecast points", "Shore service result",
                    JOptionPane.INFORMATION_MESSAGE);

        }
        
    }
    
    private void initGui() {
        setSize(280, 130);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);
        getContentPane().setLayout(null);
        
        cancelBtn = new JButton("Cancel");
        cancelBtn.setBounds(96, 58, 80, 23);
        getContentPane().add(cancelBtn);
        cancelBtn.addActionListener(this);
        
        statusLbl = new JLabel("Getting METOC from shore server ...");
        statusLbl.setHorizontalAlignment(SwingConstants.CENTER);
        statusLbl.setBounds(10, 23, 244, 14);
        getContentPane().add(statusLbl);
    }
    
    private boolean isCancelReq() {
        synchronized (cancelReq) {
            return cancelReq.booleanValue();
        }
    }
    
    private void setCancelReq(boolean cancel) {
        synchronized (cancelReq) {
            this.cancelReq = cancel;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelBtn) {
            setCancelReq(true);
            setVisible(false);
            route.removeMetoc();
            routeManager.notifyListeners(RoutesUpdateEvent.METOC_SETTINGS_CHANGED);
        }
    }

    public Boolean getRawMetoc() {
        return rawMetoc;
    }

    public void setRawMetoc(Boolean rawMetoc) {
        this.rawMetoc = rawMetoc;
    }

}
