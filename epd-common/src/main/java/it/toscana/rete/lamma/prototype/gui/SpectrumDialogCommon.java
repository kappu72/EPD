package it.toscana.rete.lamma.prototype.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import dk.dma.epd.common.prototype.EPD;
import it.toscana.rete.lamma.prototype.gui.route.RouteFuelConsumptionPropertiesDialogCommon;
import it.toscana.rete.lamma.prototype.model.MetocPointForecast;
import it.toscana.rete.lamma.prototype.model.Wave;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class SpectrumDialogCommon extends JDialog {
    private MetocPanelCommon metocPanel;
    private MetocPointForecast mpf;
    private JPanel contentPane;
    private JButton setBtn;
    private JFormattedTextField hWind;
    private JFormattedTextField hSwell3;
    private JFormattedTextField hSwell2;
    private JFormattedTextField pWind;
    private JFormattedTextField dWind;
    private JFormattedTextField hSwell1;
    private JFormattedTextField hSwell4;
    private JFormattedTextField hSwell5;
    private JFormattedTextField pSwell1;
    private JFormattedTextField pSwell2;
    private JFormattedTextField pSwell3;
    private JFormattedTextField pSwell4;
    private JFormattedTextField pSwell5;
    private JFormattedTextField dSwell1;
    private JFormattedTextField dSwell2;
    private JFormattedTextField dSwell3;
    private JFormattedTextField dSwell4;
    private JFormattedTextField dSwell5;
    private JFormattedTextField hTWave;
    private JFormattedTextField pTWave;
    private JFormattedTextField dTWave;
    private NumberFormatter heightFormatter;
    private NumberFormatter periodFormatter;
    private NumberFormatter dirFormatter;
    private JFormattedTextField[][] swellInputs = new JFormattedTextField[5][3];


    public SpectrumDialogCommon(MetocPointForecast mpf, MetocPanelCommon metocPanel) {
        super(EPD.getInstance().getMainFrame(), "Set Wave spectrum panel", ModalityType.MODELESS);
        this.mpf = mpf;
        this.metocPanel = metocPanel;
        $$$setupUI$$$();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // Rimuovi listeners e altri riferimenti se li hai!!
                ((SpectrumDialogCommon) e.getSource()).clean();
            }
        });
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(setBtn);
        setLocationRelativeTo(EPD.getInstance().getMainFrame());
        initValues();
        setBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // va aggiornato il punto
                metocPanel.drawMetoc(getMetoc());
            }
        });
    }

    private void initValues() {
        // int tot.wave values
        Wave tWave = mpf.getMeanWave();
        if (tWave != null && tWave.isValid()) {
            hTWave.setValue(tWave.getHeight());
            pTWave.setValue(tWave.getPeriod());
            dTWave.setValue(tWave.getDirection());
        }
        if (!mpf.getHasPartitions()) {
            return;
        }
        Wave w = mpf.getWindWave();
        if (w != null) {
            hWind.setValue(w.getHeight());
            pWind.setValue(w.getPeriod());
            dWind.setValue(w.getDirection());
        }
        java.util.List<Wave> swell = mpf.getSwellWave();
        for (int i = 0; i < swell.size(); i++) {
            w = swell.get(i);
            swellInputs[i][0].setValue(w.getHeight());
            swellInputs[i][1].setValue(w.getPeriod());
            swellInputs[i][2].setValue(w.getDirection());
        }
    }

    private void clean() {
        this.mpf = null;
        metocPanel = null;
    }

    public MetocPointForecast getMetoc() {
        if (hTWave.getValue() != null && pTWave.getValue() != null && dTWave.getValue() != null) {
            mpf.setMeanWave(new Wave((double) hTWave.getValue(), (double) dTWave.getValue(), (double) pTWave.getValue()));
        } else {
            mpf.setWindWave(null);
        }

        if (hWind.getValue() != null && pWind.getValue() != null && dWind.getValue() != null) {
            mpf.setWindWave(new Wave((double) hWind.getValue(), (double) dWind.getValue(), (double) pWind.getValue()));
        } else {
            mpf.setWindWave(null);
        }
        java.util.List<Wave> l = new ArrayList<Wave>();
        for (int i = 0; i < 5; i++) {
            if (swellInputs[i][0].getValue() != null && swellInputs[i][1].getValue() != null && swellInputs[i][2].getValue() != null) {
                l.add(new Wave((double) swellInputs[i][0].getValue(), (double) swellInputs[i][2].getValue(), (double) swellInputs[i][1].getValue()));
            }
        }
        mpf.setSwellWave(l);
        return mpf;
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        setBtn = new JButton();
        setBtn.setText("Set wave spectrum values");
        panel2.add(setBtn, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(8, 4, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Swell 3");
        panel3.add(label1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Swell 2");
        panel3.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel3.add(hSwell3, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 20), null, 0, false));
        panel3.add(hSwell2, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 20), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Height");
        panel3.add(label3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 16), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Dir");
        panel3.add(label4, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Period");
        panel3.add(label5, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 16), null, 0, false));
        panel3.add(hWind, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(60, 20), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Wind ");
        panel3.add(label6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Swell 1");
        panel3.add(label7, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel3.add(hSwell1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 20), null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Swell 4");
        panel3.add(label8, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Swell 5");
        panel3.add(label9, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel3.add(hSwell4, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 20), null, 0, false));
        panel3.add(hSwell5, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 20), null, 0, false));
        panel3.add(pSwell1, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 20), null, 0, false));
        panel3.add(pSwell2, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 20), null, 0, false));
        panel3.add(pSwell3, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 20), null, 0, false));
        pSwell4.setText("");
        panel3.add(pSwell4, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 20), null, 0, false));
        panel3.add(pSwell5, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 20), null, 0, false));
        panel3.add(pWind, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(60, 20), null, 0, false));
        panel3.add(dWind, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(60, 20), null, 0, false));
        panel3.add(dSwell1, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 20), null, 0, false));
        panel3.add(dSwell2, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 20), null, 0, false));
        panel3.add(dSwell3, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 20), null, 0, false));
        panel3.add(dSwell4, new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 20), null, 0, false));
        panel3.add(dSwell5, new GridConstraints(6, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 20), null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Tot.Wave");
        panel3.add(label10, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel3.add(hTWave, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 20), null, 0, false));
        panel3.add(pTWave, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 20), null, 0, false));
        panel3.add(dTWave, new GridConstraints(7, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, 20), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private void createUIComponents() {
        DecimalFormat dec = new DecimalFormat();
        dec.setMaximumFractionDigits(2);

        dirFormatter = new NumberFormatter(dec);
        dirFormatter.setMinimum(0.0);
        dirFormatter.setMaximum(360.0);


        heightFormatter = new NumberFormatter(dec);
        heightFormatter.setMinimum(0.0);
        heightFormatter.setMaximum(20.0);

        periodFormatter = new NumberFormatter(dec);
        periodFormatter.setMaximum(20.0);
        periodFormatter.setMinimum(0.1);

        // height
        hWind = new JFormattedTextField(heightFormatter);

        hSwell1 = new JFormattedTextField(heightFormatter);
        swellInputs[0][0] = hSwell1;
        hSwell2 = new JFormattedTextField(heightFormatter);
        swellInputs[1][0] = hSwell2;
        hSwell3 = new JFormattedTextField(heightFormatter);
        swellInputs[2][0] = hSwell3;
        hSwell4 = new JFormattedTextField(heightFormatter);
        swellInputs[3][0] = hSwell4;
        hSwell5 = new JFormattedTextField(heightFormatter);
        swellInputs[4][0] = hSwell5;
        hTWave = new JFormattedTextField(heightFormatter);

        // directions
        dWind = new JFormattedTextField(dirFormatter);

        dSwell1 = new JFormattedTextField(dirFormatter);
        swellInputs[0][2] = dSwell1;
        dSwell2 = new JFormattedTextField(dirFormatter);
        swellInputs[1][2] = dSwell2;
        dSwell3 = new JFormattedTextField(dirFormatter);
        swellInputs[2][2] = dSwell3;
        dSwell4 = new JFormattedTextField(dirFormatter);
        swellInputs[3][2] = dSwell4;
        dSwell5 = new JFormattedTextField(dirFormatter);
        swellInputs[4][2] = dSwell5;

        dTWave = new JFormattedTextField(dirFormatter);

        // period
        pWind = new JFormattedTextField(periodFormatter);

        pSwell1 = new JFormattedTextField(periodFormatter);
        swellInputs[0][1] = pSwell1;
        pSwell2 = new JFormattedTextField(periodFormatter);
        swellInputs[1][1] = pSwell2;
        pSwell3 = new JFormattedTextField(periodFormatter);
        swellInputs[2][1] = pSwell3;
        pSwell4 = new JFormattedTextField(periodFormatter);
        swellInputs[3][1] = pSwell4;
        pSwell5 = new JFormattedTextField(periodFormatter);
        swellInputs[4][1] = pSwell5;

        pTWave = new JFormattedTextField(periodFormatter);

    }

}