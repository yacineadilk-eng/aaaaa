package com.agenda.view;

import com.agenda.model.Agenda;
import com.agenda.model.AgendaTableModel;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class AgendaPanel extends JPanel {
    private JTable tableau;
    private AgendaTableModel modele;
    private TableRowSorter<AgendaTableModel> sorter;
    private JTextField champRecherche;

    public AgendaPanel(List<Agenda> evenements) {
        setLayout(new BorderLayout());
        setBackground(new Color(255, 253, 245)); // Fond crème

        // Panneau de recherche
        JPanel panelRecherche = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        panelRecherche.setBackground(new Color(0xF5F2E8));
        panelRecherche.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel labelRecherche = new JLabel("🔍 RECHERCHE RAPIDE :");
        labelRecherche.setFont(new Font("Arial", Font.BOLD, 15));
        labelRecherche.setForeground(new Color(101, 67, 33));
        
        champRecherche = new JTextField(45);
        champRecherche.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Style champ de recherche
        champRecherche.setBackground(new Color(0xFFF9EF));
        champRecherche.setForeground(new Color(0x4A2F1B));
        champRecherche.setBorder(BorderFactory.createLineBorder(new Color(0x8B5E34), 2));
        
        panelRecherche.add(labelRecherche);
        panelRecherche.add(champRecherche);
        
        // Bouton Ajouter
        JButton btnAjouter = new JButton("➕ AJOUTER");
        btnAjouter.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAjouter.setBackground(new Color(0x8B5E34));
        btnAjouter.setForeground(Color.WHITE);
        btnAjouter.setFocusPainted(false);
        btnAjouter.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnAjouter.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAjouter.addActionListener(e -> {
            Component root = SwingUtilities.getWindowAncestor(this);
            if (root instanceof AgendaView view) {
                AgendaForm form = new AgendaForm((JFrame) root, view.getController());
                form.setVisible(true);
                if (form.estValide()) {
                    Agenda evt = form.getEvenement();
                    view.getController().ajouterEvenement(evt);
                    view.actualiserToutesLesVues();
                    view.notifierAction("Nouvel événement ajouté avec succès.");
                }
            }
        });
        panelRecherche.add(btnAjouter);

        // Création du tableau
        modele = new AgendaTableModel(evenements);
        tableau = new JTable(modele);
        tableau.setFillsViewportHeight(true);
        tableau.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        // Style du tableau
        tableau.setBackground(Color.WHITE);
        tableau.setForeground(new Color(0x4A2F1B));
        tableau.setGridColor(new Color(0x8B5E34));
        tableau.setRowHeight(28);

        // Activation du tri automatique
        tableau.setAutoCreateRowSorter(true);
        sorter = new TableRowSorter<>(modele);
        tableau.setRowSorter(sorter);

        // En-tête
        tableau.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableau.getTableHeader().setBackground(new Color(0x8B5E34));
        tableau.getTableHeader().setForeground(Color.WHITE);

        // Couleurs des cellules déjà définies ci-dessus

        // Surlignage de la ligne sélectionnée
        tableau.setSelectionBackground(new Color(184, 134, 11)); // Marron caramel
        tableau.setSelectionForeground(Color.WHITE);

        // Recherche en temps réel
        champRecherche.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filtrer();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filtrer();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filtrer();
            }

            private void filtrer() {
                String texte = champRecherche.getText();
                if (texte.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texte));
                }
            }
        });

        // Double-clic pour éditer
        tableau.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableau.getSelectedRow();
                    if (row != -1) {
                        int modelRow = tableau.convertRowIndexToModel(row);
                        List<Agenda> evenements = modele.getEvenements();
                        if (modelRow < evenements.size()) {
                            Agenda evt = evenements.get(modelRow);
                            ouvrirFormulaire(evt);
                        }
                    }
                }
                // Single left-click: afficher menu rapide Modifier / Supprimer
                else if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    int row = tableau.rowAtPoint(e.getPoint());
                    if (row != -1) {
                        tableau.setRowSelectionInterval(row, row);
                        int modelRow = tableau.convertRowIndexToModel(row);
                        List<Agenda> evenements = modele.getEvenements();
                        if (modelRow < evenements.size()) {
                            Agenda evt = evenements.get(modelRow);
                            JPopupMenu menu = new JPopupMenu();
                            JMenuItem modifier = new JMenuItem("✏️ Modifier");
                            JMenuItem supprimer = new JMenuItem("🗑️ Supprimer");

                            modifier.addActionListener(ev -> ouvrirFormulaire(evt));

                            supprimer.addActionListener(ev -> {
                                Component root = SwingUtilities.getWindowAncestor(AgendaPanel.this);
                                if (root instanceof AgendaView view) {
                                    int choix = JOptionPane.showConfirmDialog(
                                        AgendaPanel.this,
                                        "Voulez-vous vraiment supprimer l'événement '" + evt.getTitre() + "' ?",
                                        "Confirmation",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.WARNING_MESSAGE
                                    );
                                    if (choix == JOptionPane.YES_OPTION) {
                                        view.getController().supprimerEvenement(evt.getDate(), evt.getTitre());
                                        view.actualiserToutesLesVues();
                                        view.notifierAction("Événement supprimé avec succès.");
                                    }
                                }
                            });

                            menu.add(modifier);
                            menu.add(supprimer);
                            menu.show(tableau, e.getX(), e.getY());
                        }
                    }
                }
            }
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = tableau.rowAtPoint(e.getPoint());
                    if (row != -1) {
                        tableau.setRowSelectionInterval(row, row);
                        afficherMenuContextuel(e, row);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableau);
        scrollPane.getViewport().setBackground(new Color(0xF5F2E8));
        
        add(panelRecherche, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void ouvrirFormulaire(Agenda evt) {
        Component root = SwingUtilities.getWindowAncestor(this);
        if (root instanceof AgendaView view) {
            // Capturer l'identifiant actuel (date + titre) avant édition
            java.time.LocalDate ancienneDate = evt.getDate();
            String ancienTitre = evt.getTitre();

            AgendaForm form = new AgendaForm((JFrame) root, evt, view.getController());
            form.setVisible(true);
            if (form.estValide()) {
                if (form.estSupprime()) {
                    view.getController().supprimerEvenement(ancienneDate, ancienTitre);
                    view.notifierAction("Événement supprimé avec succès.");
                } else {
                    // Appliquer la modification via le controller
                    boolean ok = view.getController().modifierEvenement(ancienneDate, ancienTitre, form.getEvenement());
                    if (ok) {
                        view.notifierAction("Événement modifié avec succès.");
                    }
                }
                view.actualiserToutesLesVues();
            }
        }
    }
    
    private void afficherMenuContextuel(java.awt.event.MouseEvent e, int row) {
        int modelRow = tableau.convertRowIndexToModel(row);
        List<Agenda> evenements = modele.getEvenements();
        if (modelRow < evenements.size()) {
            Agenda evt = evenements.get(modelRow);
            JPopupMenu menu = new JPopupMenu();
            JMenuItem modifier = new JMenuItem("✏️ Modifier");
            JMenuItem supprimer = new JMenuItem("🗑️ Supprimer");
            JMenuItem partager = new JMenuItem("🔗 Partager");

            modifier.addActionListener(ev -> {
                AgendaForm form = new AgendaForm((JFrame) SwingUtilities.getWindowAncestor(AgendaPanel.this), evt, null);
                form.setVisible(true);
                if (form.estValide()) {
                    Component root = SwingUtilities.getWindowAncestor(AgendaPanel.this);
                    if (root instanceof AgendaView view) {
                        if (form.estSupprime()) {
                            view.getController().supprimerEvenement(evt.getDate(), evt.getTitre());
                            view.notifierAction("Événement supprimé avec succès.");
                        } else {
                            boolean ok = view.getController().modifierEvenement(evt.getDate(), evt.getTitre(), form.getEvenement());
                            if (ok) {
                                view.notifierAction("Événement modifié avec succès.");
                            }
                        }
                        view.actualiserToutesLesVues();
                    }
                }
            });

            supprimer.addActionListener(ev -> {
                Component root = SwingUtilities.getWindowAncestor(AgendaPanel.this);
                if (root instanceof AgendaView view) {
                    int choix = JOptionPane.showConfirmDialog(
                        AgendaPanel.this,
                        "Voulez-vous vraiment supprimer cet événement ?",
                        "Confirmation",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );
                    if (choix == JOptionPane.YES_OPTION) {
                        view.getController().supprimerEvenement(evt.getDate(), evt.getTitre());
                        view.actualiserToutesLesVues();
                        view.notifierAction("L'événement a été supprimé avec succès.");
                    }
                }
            });

            partager.addActionListener(ev -> {
                Component root = SwingUtilities.getWindowAncestor(AgendaPanel.this);
                if (root instanceof AgendaView view) {
                    java.util.List<String> users = java.util.Arrays.asList(
                        "Ali Paychen", "Ikene Manel", "Yacine Kadaoui", "Zaza Khlifaoui", "Ouri Baouida"
                    );
                    PartageDialog pd = new PartageDialog((JFrame) root, users);
                    pd.setVisible(true);
                    if (pd.estValide()) {
                        java.util.List<String> sel = pd.getSelectedUsers();
                        String names = sel.isEmpty() ? "(aucun)" : String.join(", ", sel);
                        view.notifierAction("Événement partagé avec : " + names);
                    }
                }
            });

            menu.add(modifier);
            menu.add(supprimer);
            menu.addSeparator();
            menu.add(partager);
            menu.show(tableau, e.getX(), e.getY());
        }
    }

    public void mettreAJour(List<Agenda> evenements) {
        modele.actualiser(evenements);
        sorter.setModel(modele);
    }

    // Retourne l'événement sélectionné dans le tableau (ou null)
    public Agenda getSelectedEvent() {
        int row = tableau.getSelectedRow();
        if (row == -1) return null;
        int modelRow = tableau.convertRowIndexToModel(row);
        List<Agenda> evts = modele.getEvenements();
        if (modelRow < 0 || modelRow >= evts.size()) return null;
        return evts.get(modelRow);
    }
}