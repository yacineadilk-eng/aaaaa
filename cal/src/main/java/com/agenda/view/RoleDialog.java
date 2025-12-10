package com.agenda.view;

import com.agenda.auth.RoleManager;

import javax.swing.*;
import java.awt.*;

public class RoleDialog extends JDialog {
    private boolean confirmed = false;

    public RoleDialog(JFrame parent) {
        super(parent, "Choix du rôle", true);
        setSize(520, 380);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JLabel title = new JLabel("🔐 Choix du rôle");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(101, 67, 33));
        title.setBorder(BorderFactory.createEmptyBorder(0,0,8,0));
        panel.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        ButtonGroup group = new ButtonGroup();
        JRadioButton rbVisitor = new JRadioButton("Visiteur (lecture seule)");
        JRadioButton rbAdmin = new JRadioButton("Admin (modification)");
        rbVisitor.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rbAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rbAdmin.setSelected(true);
        group.add(rbVisitor);
        group.add(rbAdmin);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; center.add(rbVisitor, gbc);
        gbc.gridy = 1; center.add(rbAdmin, gbc);

        JLabel lblAdmin = new JLabel("👤 Sélection d'admin :");
        lblAdmin.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JComboBox<String> adminCombo = new JComboBox<>(RoleManager.ADMIN_LIST.toArray(new String[0]));
        adminCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        adminCombo.setEnabled(true);

        JLabel lblCode = new JLabel("🔑 Code admin (prenom123) :");
        lblCode.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JPasswordField codeField = new JPasswordField();
        codeField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        codeField.setEnabled(true);

        gbc.gridy = 2; center.add(lblAdmin, gbc);
        gbc.gridy = 3; center.add(adminCombo, gbc);
        gbc.gridy = 4; center.add(lblCode, gbc);
        gbc.gridy = 5; center.add(codeField, gbc);

        rbAdmin.addActionListener(e -> { adminCombo.setEnabled(true); codeField.setEnabled(true);} );
        rbVisitor.addActionListener(e -> { adminCombo.setEnabled(false); codeField.setEnabled(false);} );

        panel.add(center, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        JButton ok = new JButton("Continuer");
        JButton cancel = new JButton("Annuler");
        ok.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ok.setBackground(new Color(76, 175, 80));
        ok.setForeground(Color.WHITE);
        ok.setFocusPainted(false);
        ok.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(67,160,71),2),
            BorderFactory.createEmptyBorder(6,14,6,14)
        ));
        cancel.setBackground(new Color(244, 67, 54));
        cancel.setForeground(Color.WHITE);
        cancel.setFocusPainted(false);
        cancel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229,57,53),2),
            BorderFactory.createEmptyBorder(6,14,6,14)
        ));
        buttons.add(ok); buttons.add(cancel);
        panel.add(buttons, BorderLayout.SOUTH);

        ok.addActionListener(e -> {
            if (rbVisitor.isSelected()) {
                RoleManager.set(RoleManager.Role.VISITOR, "Visiteur");
                confirmed = true;
            } else {
                String selected = (String) adminCombo.getSelectedItem();
                if (selected == null || selected.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Veuillez sélectionner un admin.");
                    return;
                }
                String code = new String(codeField.getPassword());
                if (code == null || code.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Veuillez entrer le code admin.");
                    codeField.requestFocus();
                    return;
                }
                String prenom = selected.split(" ")[0].toLowerCase();
                String attendu = prenom + "123";
                if (!attendu.equals(code.toLowerCase())) {
                    codeField.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
                    JOptionPane.showMessageDialog(this, "Code invalide. Format attendu: prenom123 (ex: ali123)");
                    codeField.requestFocus();
                    return;
                }
                codeField.setBorder(UIManager.getBorder("TextField.border"));
                RoleManager.set(RoleManager.Role.ADMIN, selected);
                confirmed = true;
            }
            dispose();
        });

        cancel.addActionListener(e -> dispose());
        setContentPane(panel);
    }

    public boolean isConfirmed() { return confirmed; }
}
