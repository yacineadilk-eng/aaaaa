package com.agenda.view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PartageDialog extends JDialog {
    private final List<JCheckBox> boxes = new ArrayList<>();
    private boolean valide = false;

    public PartageDialog(JFrame parent, List<String> utilisateurs) {
        super(parent, "Partagez l'événement", true);
        setSize(350, 300);
        setLocationRelativeTo(parent);

        JPanel main = new JPanel(new BorderLayout(10,10));
        main.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        for (String u : utilisateurs) {
            JCheckBox cb = new JCheckBox(u);
            boxes.add(cb);
            listPanel.add(cb);
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        main.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("Partager");
        JButton ann = new JButton("Annuler");
        ok.addActionListener(e -> {
            valide = true;
            dispose();
        });
        ann.addActionListener(e -> dispose());
        btnPanel.add(ann);
        btnPanel.add(ok);

        main.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(main);
    }

    public List<String> getSelectedUsers() {
        List<String> sel = new ArrayList<>();
        for (JCheckBox cb : boxes) {
            if (cb.isSelected()) sel.add(cb.getText());
        }
        return sel;
    }

    public boolean estValide() {
        return valide;
    }
}
