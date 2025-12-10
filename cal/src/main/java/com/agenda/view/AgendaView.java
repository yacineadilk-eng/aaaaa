package com.agenda.view;

import com.agenda.controller.AgendaController;
import com.agenda.model.Agenda;
import com.agenda.auth.RoleManager;

import javax.swing.*;
import java.awt.*;

public class AgendaView extends JFrame {
    private final AgendaController controller;
    private final AgendaPanel panelListe;
    private final HebdoPanelNew panelHebdo;
    private final MensuelPanel panelMensuel;
    private final javax.swing.JLabel statusLabel;
    private final java.util.Set<String> reminded = new java.util.HashSet<>();
    private final javax.swing.Timer reminderTimer;
    // Barre de rappels (distincte de la barre de statut)
    private final JPanel reminderBar = new JPanel(new BorderLayout());
    private final DefaultListModel<String> reminderListModel = new DefaultListModel<>();
    private final JList<String> reminderList = new JList<>(reminderListModel);

    public AgendaView() {
        setTitle("🗓️ PlannerPro - Organisateur Personnel");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Couleur de fond fenêtre (beige clair)
        getContentPane().setBackground(new Color(0xF5F2E8));

        controller = new AgendaController();
        panelListe = new AgendaPanel(controller.getEvenements());
        panelHebdo = new HebdoPanelNew(controller);
        panelMensuel = new MensuelPanel(controller);

        JTabbedPane tabs = new JTabbedPane();
        // Style des onglets
        tabs.setBackground(new Color(0xE8D8C3));
        tabs.setForeground(new Color(0x4A2F1B));
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.addTab("⚡ VUE RAPIDE", panelListe);
        tabs.addTab("📊 PLANNING 7J", panelHebdo);
        tabs.addTab("🗓️ CALENDRIER", panelMensuel);

        JButton ajouterBtn = new JButton("➕ NOUVEAU RENDEZ-VOUS");
        // Style bouton principal
        ajouterBtn.setBackground(new Color(0x8B5E34));
        ajouterBtn.setForeground(Color.WHITE);
        ajouterBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ajouterBtn.setFocusPainted(false);
        ajouterBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        ajouterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ajouterBtn.addActionListener(e -> {
            if (RoleManager.isVisitor()) {
                JOptionPane.showMessageDialog(this, "Mode visiteur: modification désactivée.");
                return;
            }
            AgendaForm form = new AgendaForm(this, controller);
            form.setVisible(true);
            if (form.estValide()) {
                Agenda evt = form.getEvenement();
                // Attribuer l'utilisateur courant si admin
                evt.setResponsable(RoleManager.getCurrentUser());
                controller.ajouterEvenement(evt);
                actualiserToutesLesVues();
            }
        });

        JButton aideBtn = new JButton("📖 MANUEL");
        aideBtn.setBackground(new Color(0x8B5E34));
        aideBtn.setForeground(Color.WHITE);
        aideBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        aideBtn.setFocusPainted(false);
        aideBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        aideBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        aideBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "📖 Guide d'utilisation - Gestionnaire d'Événements Pro\n\n" +
                "🎯 Créer un événement :\n" +
                "- Cliquez sur « ➕ Ajouter un événement ».\n" +
                "- Remplissez le formulaire et validez.\n\n" +
                "*️ Vue Mensuelle :\n" +
                "- Clic gauche : ajouter un événement.\n" +
                "- Clic droit : supprimer un événement.\n\n" +
                "* Vue Hebdomadaire :\n" +
                "- Clic gauche : ajouter un événement.\n" +
                "- Clic droit : supprimer un événement.\n\n" +
                "* Vue Liste :\n" +
                "- Affiche les événements du jour.\n\n" +
                "* Astuce :\n" +
                "- Les événements sont triés par heure.",
                "Aide", JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel topPanel = new JPanel(new BorderLayout(12, 12));
        // Barre supérieure: panneau d'action + barre de rappels
        topPanel.setBackground(new Color(0xC28A3D));
        topPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Sous-panneau actions (gauche + droite)
        JPanel actionsPanel = new JPanel(new BorderLayout(12, 0));
        actionsPanel.setOpaque(false);
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(aideBtn);
        actionsPanel.add(leftPanel, BorderLayout.WEST);
        actionsPanel.add(ajouterBtn, BorderLayout.EAST);

        // Barre de rappels distincte (au-dessus des onglets)
        construireReminderBar();

        topPanel.add(actionsPanel, BorderLayout.NORTH);
        topPanel.add(reminderBar, BorderLayout.SOUTH);

        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(tabs, BorderLayout.CENTER);

        // Barre de statut en bas
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(0xF5F2E8));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220,220,220)));
        statusLabel = new JLabel("Prêt");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        statusBar.add(statusLabel, BorderLayout.WEST);
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        // Raccourcis clavier
        registerShortcuts();

        // Timer de rappels (vérification chaque minute)
        reminderTimer = new javax.swing.Timer(60_000, e -> checkReminders());
        reminderTimer.setInitialDelay(5_000);
        reminderTimer.start();

        // Désactiver le bouton d'ajout en mode visiteur
        if (RoleManager.isVisitor()) {
            ajouterBtn.setEnabled(false);
            setStatus("Mode visiteur: lecture seule");
        } else {
            setStatus("Connecté en tant que: " + RoleManager.getCurrentUser());
        }
        setVisible(true);
    }

    public void actualiserToutesLesVues() {
        panelListe.mettreAJour(controller.getEvenements());
        panelHebdo.actualiser();
        panelMensuel.actualiser();
    }
    
    public AgendaController getController() {
        return controller;
    }

    // Notifier une action (affiche message modal et met à jour la barre de statut)
    public void notifierAction(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message);
            setStatus("Dernière action : " + message);
        });
    }

    public void setStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    private void registerShortcuts() {
        var pane = getRootPane();
        var im = pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        var am = pane.getActionMap();

        // Ctrl+N -> nouveau
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK), "nouveau");
        am.put("nouveau", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (RoleManager.isVisitor()) {
                    JOptionPane.showMessageDialog(AgendaView.this, "Mode visiteur: modification désactivée.");
                    return;
                }
                AgendaForm form = new AgendaForm(AgendaView.this, controller);
                form.setVisible(true);
                if (form.estValide()) {
                    controller.ajouterEvenement(form.getEvenement());
                    actualiserToutesLesVues();
                    notifierAction("Nouvel événement ajouté avec succès.");
                }
            }
        });

        // Delete -> supprimer sélection
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0), "supprimerSelection");
        am.put("supprimerSelection", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Agenda sel = getSelectedEventFromActiveTab();
                // fallback: try each panel directly if nothing found on active tab
                if (sel == null) {
                    try {
                        Agenda a = panelListe.getSelectedEvent();
                        if (a != null) sel = a;
                    } catch (Exception ignored) {}
                    if (sel == null) {
                        try {
                            Agenda a = panelHebdo.getSelectedEvent();
                            if (a != null) sel = a;
                        } catch (Exception ignored) {}
                    }
                    if (sel == null) {
                        try {
                            Agenda a = panelMensuel.getSelectedEvent();
                            if (a != null) sel = a;
                        } catch (Exception ignored) {}
                    }
                }

                if (sel != null) {
                    if (RoleManager.isVisitor()) {
                        JOptionPane.showMessageDialog(AgendaView.this, "Mode visiteur: suppression désactivée.");
                        return;
                    }
                    int choix = JOptionPane.showConfirmDialog(AgendaView.this,
                            "Voulez-vous vraiment supprimer cet événement ?",
                            "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (choix == JOptionPane.YES_OPTION) {
                        controller.supprimerEvenement(sel.getDate(), sel.getTitre());
                        actualiserToutesLesVues();
                        notifierAction("Événement supprimé avec succès.");
                    }
                } else {
                    setStatus("Aucun événement sélectionné");
                }
            }
        });
    }

    // Retourne l'événement sélectionné selon l'onglet actif (ou null)
    public Agenda getSelectedEventFromActiveTab() {
        Component c = null;
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JTabbedPane) {
                c = ((JTabbedPane) comp).getSelectedComponent();
                break;
            }
        }
        if (c == null) return null;

        Agenda sel = null;
        if (c instanceof AgendaPanel) sel = ((AgendaPanel) c).getSelectedEvent();
        else if (c instanceof HebdoPanelNew) sel = ((HebdoPanelNew) c).getSelectedEvent();
        else if (c instanceof MensuelPanel) sel = ((MensuelPanel) c).getSelectedEvent();

        return sel;
    }

    private void checkReminders() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        // Rafraîchir la liste visuelle des rappels
        reminderListModel.clear();
        java.util.List<String> upcoming = new java.util.ArrayList<>();
        for (Agenda evt : controller.getEvenements()) {
            try {
                if (evt.getDate() == null || evt.getHeure() == null) continue;
                java.time.LocalTime lt = parseFlexibleTime(evt.getHeure());
                if (lt == null) continue; // heure illisible: ignorer pour le rappel
                java.time.LocalDateTime when = java.time.LocalDateTime.of(evt.getDate(), lt);
                long minutes = java.time.Duration.between(now, when).toMinutes();
                String key = evt.getDate() + "|" + lt.toString() + "|" + evt.getTitre();
                if (minutes > 0 && minutes <= 60 && !reminded.contains(key)) {
                    reminded.add(key);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(AgendaView.this,
                                "Rappel : L'événement '" + evt.getTitre() + "' commence dans " + minutes + " min.",
                                "Rappel", JOptionPane.INFORMATION_MESSAGE);
                        setStatus("Rappel : '" + evt.getTitre() + "' dans " + minutes + " min");
                    });
                }
                if (minutes > 0 && minutes <= 60) {
                    upcoming.add(String.format("%s • %s (%d min)", evt.getHeure(), evt.getTitre(), minutes));
                }
            } catch (Exception ignored) {}
        }
        // Mettre à jour la barre des rappels avec les événements à venir triés par minutes
        upcoming.sort((a, b) -> {
            int ma = extraireMinutes(a);
            int mb = extraireMinutes(b);
            return Integer.compare(ma, mb);
        });
        for (String s : upcoming) reminderListModel.addElement(s);
    }

    private int extraireMinutes(String item) {
        try {
            int idx1 = item.lastIndexOf('(');
            int idx2 = item.lastIndexOf(')');
            if (idx1 != -1 && idx2 != -1 && idx2 > idx1) {
                String inside = item.substring(idx1 + 1, idx2).replace(" min", "").trim();
                return Integer.parseInt(inside);
            }
        } catch (Exception ignored) {}
        return Integer.MAX_VALUE;
    }

    private void construireReminderBar() {
        reminderBar.setBackground(new Color(0xF8EFE0)); // beige plus clair que la barre du haut
        reminderBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(0xD0C4B2)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JLabel icon = new JLabel("⏰ Rappels (≤ 60 min)");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        icon.setForeground(new Color(0x4A2F1B));
        left.add(icon);

        reminderList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reminderList.setBackground(new Color(0xFFF9EF));
        reminderList.setForeground(new Color(0x4A2F1B));
        reminderList.setVisibleRowCount(1);
        reminderList.setFixedCellHeight(22);
        reminderList.setSelectionBackground(new Color(0xEAD2B8));
        reminderList.setSelectionForeground(new Color(0x4A2F1B));

        JScrollPane scroller = new JScrollPane(reminderList,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroller.setBorder(BorderFactory.createEmptyBorder());
        scroller.getViewport().setBackground(new Color(0xFFF9EF));

        reminderBar.add(left, BorderLayout.WEST);
        reminderBar.add(scroller, BorderLayout.CENTER);
    }

    // Parsing tolérant pour différents formats d'heure (ex: "09:15", "9h", "09h30", "09.15", "9:5", "9")
    private java.time.LocalTime parseFlexibleTime(String input) {
        if (input == null) return null;
        String s = input.trim();
        if (s.isEmpty()) return null;
        try {
            // Tentative directe ISO (HH:mm, HH:mm:ss)
            return java.time.LocalTime.parse(s);
        } catch (Exception ignored) {}

        // Normaliser séparateurs et compléter minutes si absentes
        s = s.replace('H', 'h');
        s = s.replace('.', ':');
        s = s.replace("h", ":");
        s = s.replace(" ", "");
        if (s.matches("^\\d{1,2}$")) {
            s = s + ":00";
        } else if (s.matches("^\\d{1,2}:$")) {
            s = s + "00";
        }

        if (s.matches("^\\d{1,2}:\\d{1,2}$")) {
            try {
                String[] parts = s.split(":");
                int h = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                if (h < 0 || h > 23 || m < 0 || m > 59) return null;
                return java.time.LocalTime.of(h, m);
            } catch (Exception ignored) {}
        }
        return null;
    }
}