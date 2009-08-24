package com.bc.ceres.binding.swing;

import com.bc.ceres.binding.ValueContainer;
import com.bc.ceres.binding.ValueRange;
import com.bc.ceres.binding.ValueSet;
import com.bc.ceres.binding.Validator;
import com.bc.ceres.binding.ValueModel;
import com.bc.ceres.binding.ValidationException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.DefaultComboBoxModel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class BindingContextUI {

    String name;
    int age;
    double height;
    String eyeColor;
    Gender gender;

    String country;
    boolean healthy;

    JPanel controlPanel;
    BindingContext bindingContext;

    enum Gender {
        male,
        female
    }


    public BindingContextUI() {
        name = "Urte";
        age = 20;
        height = 1.50;
        eyeColor = "brown";
        gender = Gender.female;
        country = "Germany";
        healthy = true;

        final ValueContainer vc = ValueContainer.createObjectBacked(this);

        vc.getModel("name").getDescriptor().setNotEmpty(true);
        vc.getModel("age").getDescriptor().setValueRange(new ValueRange(1, 150));
        vc.getModel("height").getDescriptor().setValueRange(new ValueRange(0.5, 2.5));
        vc.getModel("gender").getDescriptor().setValueSet(new ValueSet(new Object[]{Gender.female, Gender.male}));
        vc.getModel("eyeColor").getDescriptor().setNotEmpty(true);
        vc.getModel("eyeColor").getDescriptor().setValidator(new LettersOnlyValidator());
        vc.getModel("country").getDescriptor().setValueSet(new ValueSet(new Object[]{"France", "Italy", "Spain", "Germany", "United Kingdom", "United States"}));

        bindingContext = new BindingContext(vc);

        bindingContext.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println(this + ": '" + evt.getPropertyName() +
                        "' changed from '" + evt.getOldValue() +
                        "' to '" + evt.getNewValue() + "'");
            }
        });
        bindingContext.addProblemListener(new BindingProblemListener() {
            @Override
            public void problemReported(BindingProblem newProblem, BindingProblem oldProblem) {
                System.out.println("problemReported: newProblem = " + newProblem);
                System.out.println("                 oldProblem = " + oldProblem);
            }

            @Override
            public void problemCleared(BindingProblem oldProblem) {
                System.out.println("problemCleared: oldProblem = " + oldProblem);
            }
        });

        bindingContext.bind("name", new JTextField(16));
        bindingContext.bind("age", new JTextField(4));
        bindingContext.bind("height", new JTextArea(1, 7));

        final JComboBox comboBox = new JComboBox(new DefaultComboBoxModel(new String[]{"brown", "blue", "green"}));
        comboBox.setEditable(true);
        bindingContext.bind("eyeColor", comboBox);

        final JRadioButton r1 = new JRadioButton("Female");
        final JRadioButton r2 = new JRadioButton("Male");
        final ButtonGroup bg = new ButtonGroup();
        bg.add(r1);
        bg.add(r2);
        bindingContext.bind("gender", bg);
        bindingContext.bind("country", new JComboBox());
        bindingContext.bind("healthy", new JCheckBox("Yes, I am not ill"));

        final GridBagConstraints gbc = new GridBagConstraints();
        controlPanel = new JPanel(new GridBagLayout());

        gbc.anchor = GridBagConstraints.BASELINE;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0;
        gbc.gridx = 0;
        controlPanel.add(new JLabel("Name: "), gbc);
        gbc.gridx = 1;
        controlPanel.add(getPrimaryComponent(bindingContext, "name"), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        controlPanel.add(r1, gbc);
        gbc.gridx = 1;
        controlPanel.add(r2, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        controlPanel.add(new JLabel("Age: "), gbc);
        gbc.gridx = 1;
        controlPanel.add(getPrimaryComponent(bindingContext, "age"), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        controlPanel.add(new JLabel("Height: "), gbc);
        gbc.gridx = 1;
        controlPanel.add(getPrimaryComponent(bindingContext, "height"), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        controlPanel.add(new JLabel("Eye colour: "), gbc);
        gbc.gridx = 1;
        controlPanel.add(getPrimaryComponent(bindingContext, "eyeColor"), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        controlPanel.add(new JLabel("Country: "), gbc);
        gbc.gridx = 1;
        controlPanel.add(getPrimaryComponent(bindingContext, "country"), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        controlPanel.add(getPrimaryComponent(bindingContext, "healthy"), gbc);

    }

    private static JComponent getPrimaryComponent(BindingContext ctx, String s) {
        return ctx.getBinding(s).getComponents()[0];
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // ok
        }

        final JFrame frame = new JFrame(BindingContextUI.class.getSimpleName() + " - Main");

        final JButton modalButton = new JButton("Modal");
        modalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createDlg(frame, true);
            }
        });
        final JButton modelessButton = new JButton("Modeless");
        modelessButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createDlg(frame, false);
            }
        });

        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        buttonPanel.add(modalButton);
        buttonPanel.add(modelessButton);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(buttonPanel);
        frame.pack();
        frame.setVisible(true);
    }

    private static void createDlg(JFrame frame, final boolean modal) {
        final JButton applyButton = new JButton(modal ? "OK" : "Apply");
        final JButton closeButton = new JButton(modal ? "Cancel" : "Close");

        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        buttonPanel.add(applyButton);
        buttonPanel.add(closeButton);

        final BindingContextUI ui = new BindingContextUI();
        final JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(new EmptyBorder(4, 4, 4, 4));
        panel.add(ui.controlPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        final JDialog dlg = new JDialog(frame, BindingContextUI.class.getSimpleName() + " - " + (modal ? "Modal" : "Modeless"), modal);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.setContentPane(panel);
        dlg.pack();
        dlg.setLocation(frame.getX() + frame.getWidth(), frame.getY());
        dlg.getRootPane().setDefaultButton(applyButton);

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final BindingProblem[] bindingProblems = ui.bindingContext.getProblems();
                if (bindingProblems.length > 0) {
                    for (int i = 0; i < bindingProblems.length; i++) {
                        BindingProblem bindingProblem = bindingProblems[i];
                        System.out.println("bindingProblem[" + i + "] = " + bindingProblem.getCause().getMessage());
                    }
                    JOptionPane.showMessageDialog(dlg, "Problems!");
                } else {
                    System.out.println("Apply!");
                    if (modal) {
                        dlg.dispose();
                    }
                }
            }
        });

        ui.bindingContext.preventPropertyChanges(closeButton);

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Close!");
                dlg.dispose();
            }
        });

        dlg.setVisible(true);
    }

    private static class LettersOnlyValidator implements Validator {
        @Override
            public void validateValue(ValueModel valueModel, Object value) throws ValidationException {
            for (char c : ((String) value).toCharArray()) {
                if (!Character.isLetter(c)) {
                    throw new ValidationException("Only letters!");
                }
            }
        }
    }
}