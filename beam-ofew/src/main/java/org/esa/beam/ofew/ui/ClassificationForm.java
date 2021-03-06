package org.esa.beam.ofew.ui;

import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.BindingContext;
import org.esa.beam.visat.VisatApp;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;

class ClassificationForm extends JPanel {

    private JFormattedTextField[] variablesTextFields;
	private PropertySet[] variablesVC;
	private PropertySet modelVC;
	private JComboBox roiCombo;
	private JCheckBox roiCheckBox;
	private JFormattedTextField indexProductName;
	private JFormattedTextField endmemberProductName;
	private JFormattedTextField classifyProductName;
    

	ClassificationForm(ClassificationModel model) {
		variablesVC = model.getVariableValueContainers();
		modelVC = model.getModelValueContainer();
		
		initComponents(model);
		bindComponents();
    }
	
    private void bindComponents() {
    	for (int i = 0; i < variablesVC.length; i++) {
            PropertySet container = variablesVC[i];
    		BindingContext bindingContext = new BindingContext(container);
    		bindingContext.bind("value", variablesTextFields[i]);
		}
    	BindingContext bindingContext = new BindingContext(modelVC);
		bindingContext.bind("classification", classifyProductName);
		bindingContext.bind("index", indexProductName);
		bindingContext.bind("endmember", endmemberProductName);
		
		bindingContext.bind("maskName", roiCombo);
		bindingContext.bind("useRoi", roiCheckBox);
    }

    private void initComponents(ClassificationModel model) {
    	FocusListener focusListener =  new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                final JTextComponent tc = ((JTextComponent) e.getComponent());
                if (tc instanceof JFormattedTextField) {
                    final JFormattedTextField ftf = (JFormattedTextField)tc;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            ftf.selectAll();
                        }
                    });
                } else {
                	tc.selectAll();
                }
            }
        };
        
        TableLayout tableLayout = new TableLayout(1);
		setLayout(tableLayout);
        tableLayout.setTableAnchor(TableLayout.Anchor.LINE_START);
        tableLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        tableLayout.setColumnWeightX(0, 1);
        tableLayout.setTablePadding(2, 2);
        
        TableLayout inputTableLayout = new TableLayout(2);
        inputTableLayout.setTableAnchor(TableLayout.Anchor.LINE_START);
        inputTableLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        inputTableLayout.setColumnWeightX(0, 0.1);
        inputTableLayout.setColumnWeightX(1, 1);
        inputTableLayout.setTablePadding(2, 2);
		JPanel inputPanel = new JPanel(inputTableLayout);

        inputPanel.setBorder(BorderFactory.createTitledBorder(null, "Eingabe",
                                                                TitledBorder.DEFAULT_JUSTIFICATION,
                                                                TitledBorder.DEFAULT_POSITION,
                                                                new Font("Tahoma", 0, 11),
                                                                new Color(0, 70, 213)));
        inputPanel.add(new JLabel("Eingabe-Produkt:"));
        JTextField inputProductTextField = new JTextField(model.getInputProduct().getName(), 40);
        inputProductTextField.setEditable(false);
		inputPanel.add(inputProductTextField);
		
		roiCheckBox = new JCheckBox("Nur in der Maske:", true);
		inputPanel.add(roiCheckBox);
		roiCombo = new JComboBox();
		inputPanel.add(roiCombo);

		if (model.useRoi()) {
			roiCheckBox.addActionListener(new ActionListener() {
				@Override
                public void actionPerformed(ActionEvent e) {
					roiCombo.setEnabled(roiCheckBox.isSelected());
				}
			});
		} else {
			roiCombo.setEnabled(false);
			roiCheckBox.setEnabled(false);
		}
		add(inputPanel);
		
        if (variablesVC.length != 0) {
        	TableLayout paramTableLayout = new TableLayout(2);
        	paramTableLayout.setTableAnchor(TableLayout.Anchor.LINE_START);
        	paramTableLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        	paramTableLayout.setColumnWeightX(0, 0.1);
        	paramTableLayout.setColumnWeightX(1, 1);
        	paramTableLayout.setTablePadding(2, 2);
        	JPanel paramPanel = new JPanel(paramTableLayout);

        	paramPanel.setBorder(BorderFactory.createTitledBorder(null, "Einstellungen",
                                                                TitledBorder.DEFAULT_JUSTIFICATION,
                                                                TitledBorder.DEFAULT_POSITION,
                                                                new Font("Tahoma", 0, 11),
                                                                new Color(0, 70, 213)));
        	variablesTextFields = new JFormattedTextField[variablesVC.length];
        	final DecimalFormat format = new DecimalFormat("0.000#####");
        	for (int i = 0; i < variablesVC.length; i++) {
        		paramPanel.add(new JLabel(variablesVC[i].getValue("description") + ":"));
        		
        		JFormattedTextField textField = new JFormattedTextField(format);
        		textField.setHorizontalAlignment(JTextField.RIGHT);
        		textField.addFocusListener(focusListener);
        		paramPanel.add(textField);
        		variablesTextFields[i] = textField;
			}
		
        	add(paramPanel);
        }
        
        
        TableLayout outputTableLayout = new TableLayout(2);
        outputTableLayout.setTableAnchor(TableLayout.Anchor.LINE_START);
        outputTableLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        outputTableLayout.setColumnWeightX(0, 0.1);
        outputTableLayout.setColumnWeightX(1, 1);
        outputTableLayout.setTablePadding(2, 2);
		JPanel outputPanel = new JPanel(outputTableLayout);

		outputPanel.setBorder(BorderFactory.createTitledBorder(null, "Ausgabe",
                                                                TitledBorder.DEFAULT_JUSTIFICATION,
                                                                TitledBorder.DEFAULT_POSITION,
                                                                new Font("Tahoma", 0, 11),
                                                                new Color(0, 70, 213)));
		outputPanel.add(new JLabel("Klassifikations-Produkt:"));
		classifyProductName = new JFormattedTextField();
		classifyProductName.addFocusListener(focusListener);
		outputPanel.add(classifyProductName);
		outputPanel.add(new JLabel("Entmischungs-Produkt:"));
		endmemberProductName = new JFormattedTextField();
		endmemberProductName.addFocusListener(focusListener);
		outputPanel.add(endmemberProductName);
		outputPanel.add(new JLabel("Index-Produkt:"));
		indexProductName = new JFormattedTextField();
		indexProductName.addFocusListener(focusListener);
		outputPanel.add(indexProductName);
        add(outputPanel);
    }

	public boolean hasValidValues() {
		try {
			modelVC.getProperty("classification").validate(classifyProductName.getValue());
			modelVC.getProperty("index").validate(indexProductName.getValue());
			modelVC.getProperty("endmember").validate(endmemberProductName.getValue());
		} catch (ValidationException e) {
			JOptionPane.showMessageDialog(VisatApp.getApp().getMainFrame(),
        			e.getMessage(), ClassificationDialog.TITLE, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
}
