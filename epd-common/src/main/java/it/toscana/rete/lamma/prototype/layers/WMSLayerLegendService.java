package it.toscana.rete.lamma.prototype.layers;

import com.bbn.openmap.image.ImageServerConstants;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Projection;

import dk.dma.epd.common.graphics.CenterRaster;
import dk.dma.epd.common.prototype.status.ComponentStatus;

import it.toscana.rete.lamma.prototype.model.LammaMetocWMSConfig;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;


public final class WMSLayerLegendService extends SingleWMSTimeService {
    private String layer;


    public WMSLayerLegendService(String wmsQuery, Projection p, LammaMetocWMSConfig params, String layer) {
        super(wmsQuery,p, params);
        this.layer = layer;
    }


    @Override
    public OMGraphicList getWmsList(Projection p) {
        java.net.URL url = null;
        OMGraphicList wmsList = new OMGraphicList();

        try {
            url = new java.net.URL(getLegendQueryString(p.getScale(), layer));
            BufferedImage image = ImageIO.read(url);

            if (image == null) {
                // Solo scritta di debug
                LOG.error("Unable to retrieve image from URL, check the WMS URL");
            } else {
                status.markContactSuccess();
                // Image maskedImage = transformWhiteToTransparent(image);
                Mercator pp = (Mercator) p;
                // Va sistemata la posizione dei vari layers
                //Filter list to get other legend heigh
                int  yOffset = 0;
                for(int i = 0; i< wmsList.size(); i++) {
                    OMGraphic g = wmsList.get(i);
                    if(g instanceof LegendRaster)  yOffset += ((LegendRaster) g).getHeight();
                }
                wmsList.add(new LegendRaster(p.getWidth() - image.getWidth(), yOffset, new ImageIcon(image)));
            }

        } catch (IOException ex) {
            status.markContactError(ex);
            LOG.error("Bad URL!");
        }
        // LOG.debug("DONE DOWNLOADING");

        return wmsList;
    }
}
