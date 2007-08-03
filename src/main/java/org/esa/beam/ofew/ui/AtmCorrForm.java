package org.esa.beam.ofew.ui;

import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.binding.ValueModel;
import com.bc.ceres.binding.swing.SwingBindingContext;
import org.esa.beam.framework.ui.TableLayout;
import org.esa.beam.visat.VisatApp;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;

/**
 * Form for OFEW atmospheric correction dialog.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 */
class AtmCorrForm extends JPanel {

    AtmCorrFormModel model;

    JFormattedTextField[] textFieldsA;
    JFormattedTextField[] textFieldsB;
    private JFormattedTextField targetProductTextField;

    public AtmCorrForm(AtmCorrFormModel model) {
        this.model = model;

        initComponents();
        bindComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        final TableLayout layout1 = new TableLayout(2);
        layout1.setTableAnchor(TableLayout.Anchor.LINE_START);
        layout1.setTableFill(TableLayout.Fill.HORIZONTAL);
        layout1.setColumnWeightX(0, 0.0);
        layout1.setColumnWeightX(1, 1.0);
        layout1.setTablePadding(2, 2);

        final JPanel sourceProductPanel = new JPanel(layout1);
        sourceProductPanel.setBorder(BorderFactory.createTitledBorder(null, "Eingabe",
                                                                      TitledBorder.DEFAULT_JUSTIFICATION,
                                                                      TitledBorder.DEFAULT_POSITION,
                                                                      new Font("Tahoma", 0, 11),
                                                                      new Color(0, 70, 213)));
        sourceProductPanel.add(new JLabel("Eingabe-Produkt:"));
        final JTextField sourceProductTextField = new JTextField(model.getSourceProductName());
        sourceProductTextField.setColumns(35);
        sourceProductTextField.setEditable(false);
        sourceProductPanel.add(sourceProductTextField);
        add(sourceProductPanel, BorderLayout.NORTH);

        final TableLayout layout2 = new TableLayout(3);
        layout2.setTableAnchor(TableLayout.Anchor.LINE_START);
        layout2.setTableFill(TableLayout.Fill.HORIZONTAL);
        layout2.setColumnWeightX(0, 0.0);
        layout2.setColumnWeightX(1, 1.0);
        layout2.setColumnWeightX(2, 1.0);
        layout2.setTablePadding(2, 2);

        final JPanel coefficientPanel = new JPanel(layout2);
        coefficientPanel.setBorder(BorderFactory.createTitledBorder(null, "Koeffizienten",
                                                                    TitledBorder.DEFAULT_JUSTIFICATION,
                                                                    TitledBorder.DEFAULT_POSITION,
                                                                    new Font("Tahoma", 0, 11),
                                                                    new Color(0, 70, 213)));
        coefficientPanel.add(new JLabel(""));
        final JLabel labelA = new JLabel("Multiplikator a");
        coefficientPanel.add(labelA);
        final JLabel labelB = new JLabel("Summand b");
        coefficientPanel.add(labelB);
        textFieldsA = new JFormattedTextField[model.getBandCount()];
        textFieldsB = new JFormattedTextField[model.getBandCount()];

        for (int i = 0; i < model.getBandCount(); i++) {
            coefficientPanel.add(new JLabel(model.getDisplayBandName(i) + ":"));

            textFieldsA[i] = new JFormattedTextField(new DecimalFormat("0.0#######"));
            coefficientPanel.add(textFieldsA[i]);

            textFieldsB[i] = new JFormattedTextField(new DecimalFormat("0.0#######"));
            coefficientPanel.add(textFieldsB[i]);
        }
        add(coefficientPanel, BorderLayout.CENTER);

        final TableLayout layout3 = new TableLayout(2);
        layout3.setTableAnchor(TableLayout.Anchor.LINE_START);
        layout3.setTableFill(TableLayout.Fill.HORIZONTAL);
        layout3.setColumnWeightX(0, 0.0);
        layout3.setColumnWeightX(1, 1.0);
        layout3.setTablePadding(2, 2);

        final JPanel targetProductPanel = new JPanel(layout3);
        targetProductPanel.setBorder(BorderFactory.createTitledBorder(null, "Ausgabe",
                                                                      TitledBorder.DEFAULT_JUSTIFICATION,
                                                                      TitledBorder.DEFAULT_POSITION,
                                                                      new Font("Tahoma", 0, 11),
                                                                      new Color(0, 70, 213)));
        targetProductPanel.add(new JLabel("Ausgabe-Produkt:"));
        targetProductTextField = new JFormattedTextField(model.getTargetProductName());
        sourceProductTextField.setColumns(35);
        targetProductPanel.add(targetProductTextField);
        add(targetProductPanel, BorderLayout.SOUTH);
    }

    private void bindComponents() {
        for (int i = 0; i < model.getBandCount(); i++) {
            final SwingBindingContext bindingContext = new SwingBindingContext(
                    model.getCoefficientPairContainer(i));
            bindingContext.bind(textFieldsA[i], "a");
            bindingContext.bind(textFieldsB[i], "b");
        }

        new SwingBindingContext(model.getTargetProductNameContainer()).
                bind(targetProductTextField, "targetProductName");
    }

    public boolean hasValidValues() {
        try {
            final ValueModel valueModel = model.getTargetProductNameContainer().getModel("targetProductName");
            valueModel.validate(targetProductTextField.getValue());
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(VisatApp.getApp().getMainFrame(),
                                          e.getMessage(), AtmCorrDialog.TITLE, JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

}
