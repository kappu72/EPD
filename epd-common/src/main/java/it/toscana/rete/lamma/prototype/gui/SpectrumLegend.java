package it.toscana.rete.lamma.prototype.gui;
import dk.dma.epd.common.prototype.EPD;

import java.awt.*;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;


public class SpectrumLegend extends JPanel{

    private Image image;
    public SpectrumLegend() {
        image = EPD.res().getCachedImageIcon("images/legend.png").getImage();
        this.setLayout(new GridLayout(1,1));
        this.setMinimumSize(new Dimension(image.getWidth(null), -1));
        this.setPreferredSize(new Dimension(image.getWidth(null), -1));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawScaledImage(image, this, g);
        //g.drawImage(image, 0, 0, this); // see javadoc for more info on the parameters
    }
    public static void drawScaledImage(Image image, Component canvas, Graphics g) {
        int imgWidth = image.getWidth(null);
        int imgHeight = image.getHeight(null);

        double imgAspect = (double) imgHeight / imgWidth;

        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();



        int x1 = 0; // top left X position
        int y1 = 0; // top left Y position
        int x2 = 0; // bottom right X position
        int y2 = 0; // bottom right Y position

        if ( imgHeight < canvasHeight) {
            // the image is smaller than the canvas
            x1 = 0;
            y1 = (canvasHeight - imgHeight) / 2;
            x2 = imgWidth + x1;
            y2 = imgHeight + y1;

        } else {
            int w = (int)  (canvasHeight / imgAspect);
            x1 = (imgWidth - w )/ 2;
            y2 = canvasHeight;
            x2 = x1 +  w;
        }

        g.drawImage(image, x1, y1, x2, y2, 0, 0, imgWidth, imgHeight, null);
    }

}