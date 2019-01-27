package kappu.openmap.test;

import java.awt.HeadlessException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.MultipleSoloMapComponentException;
import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.event.OMMouseMode;
import com.bbn.openmap.gui.OverlayMapPanel;
import com.bbn.openmap.gui.time.TimeSliderPanel;
import com.bbn.openmap.gui.OpenMapFrame;
import com.bbn.openmap.layer.GraticuleLayer;
import com.bbn.openmap.layer.shape.MultiShapeLayer;
import com.bbn.openmap.proj.coords.LatLonPoint;
import dk.dma.epd.ship.layers.background.CoastalOutlineLayer;

public class MapFrame extends OpenMapFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7458702941036371988L;
	private MapBean mapBean;
	private OverlayMapPanel mapPanel;

	public MapFrame() throws HeadlessException {
		super("Simple Map");
		initComponents();
		initMap();

	}
	
	
	private void initComponents() {
		 mapPanel = new OverlayMapPanel();
		 //TimeSliderPanel timeslider = new com.bbn.openmap.gui.time.TimeSliderPanel(true);
		 setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		 getContentPane().add(mapPanel, java.awt.BorderLayout.CENTER);
		 //getContentPane().add(timeslider, java.awt.BorderLayout.NORTH);
		 pack();
	}
	private void initMap() {
		try {
			// Get the default MapHandler the BasicMapPanel created.
		      MapHandler mapHandler = mapPanel.getMapHandler();
		      mapHandler.add(new MouseDelegator());     
		      // Add OMMouseMode, which handles how the map reacts to mouse
		      // movements
		      mapHandler.add(new OMMouseMode());
		      // Set the map's center
		      mapPanel.getMapBean().setCenter(new LatLonPoint.Double(38.0, 24.5));
		      // Set the map's scale 1:120 million
		      mapPanel.getMapBean().setScale(120000000f);
		      /*
		       * Create and add a LayerHandler to the MapHandler. The LayerHandler
		       * manages Layers, whether they are part of the map or not.
		       * layer.setVisible(true) will add it to the map. The LayerHandler
		       * has methods to do this, too. The LayerHandler will find the
		       * MapBean in the MapHandler.
		       */
		       mapHandler.add(new LayerHandler());
		       CompletableFuture.supplyAsync(() -> getShapeLayer())
	            .thenAcceptAsync(
	                 shapeLayer -> {
	                    // Add the political layer to the map
	                	mapHandler.add(shapeLayer);
	                    mapHandler.add(new GraticuleLayer());
	                    MapFrame.this.revalidate();
	       });
		       mapHandler.add(this);
		}catch (MultipleSoloMapComponentException msmce) {
			
		}
		
		 

	}
	private CoastalOutlineLayer getShapeLayer() {
	    PropertyHandler propertyHandler = null;
	    try {
	        propertyHandler = new PropertyHandler("./openmap.properties");
	        propertyHandler.setPropertyPrefix("background");
	        
	    } catch (IOException ex) {
	        Logger.getLogger(MapFrame.class.getName()).log(Level.SEVERE, null, ex);
	    }
	    CoastalOutlineLayer shapeLayer = new CoastalOutlineLayer();
	    if (propertyHandler != null) {
	        shapeLayer.setProperties(propertyHandler.getPropertyPrefix(),propertyHandler.getProperties());
	    }
	    return shapeLayer;
	}
	

}
