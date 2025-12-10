package com.agenda.view;

import com.agenda.controller.AgendaController;
import com.agenda.model.Agenda;
import com.agenda.auth.RoleManager;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MensuelPanel extends JPanel {
    private final AgendaController controller;
    private final Map<LocalDate, JTextPane> zoneParDate = new HashMap<>();
    private JPanel gridPanel;
    private Agenda selectedEvent;

    public MensuelPanel(AgendaController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(new Color(0xF5F2E8));

        // Header jours
        JPanel headerPanel = new JPanel(new GridLayout(1, 7));
        headerPanel.setBackground(new Color(0xC28A3D));
        String[] jours = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (String jour : jours) {
            JLabel label = new JLabel(jour, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setForeground(Color.WHITE);
            headerPanel.add(label);
        }

        gridPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        gridPanel.setBackground(new Color(0xF5F2E8));
        // Tailles raisonnables pour afficher le mois sans scroll
        gridPanel.setPreferredSize(new Dimension(7 * 220, 6 * 180));

        add(headerPanel, BorderLayout.NORTH);
        // Afficher tout le mois sans scroll
        add(gridPanel, BorderLayout.CENTER);

        construireMois(YearMonth.now());
    }

    private void construireMois(YearMonth mois) {
        gridPanel.removeAll();
        zoneParDate.clear();

        LocalDate premierJour = mois.atDay(1);
        int decalage = (premierJour.getDayOfWeek().getValue() + 6) % 7; // pour que Lundi=0
        int nbJours = mois.lengthOfMonth();

        for (int i = 0; i < decalage; i++) {
            gridPanel.add(new JPanel());
        }

        for (int i = 1; i <= nbJours; i++) {
            LocalDate date = mois.atDay(i);
            JPanel caseJour = new JPanel(new BorderLayout());
            caseJour.setBackground(Color.WHITE);
            caseJour.setBorder(BorderFactory.createLineBorder(new Color(208, 208, 208)));

            // En-tête jour avec bouton + pour ajout rapide
            JPanel headerJour = new JPanel(new BorderLayout());
            headerJour.setOpaque(false);
            JLabel labelJour = new JLabel(String.valueOf(i));
            labelJour.setHorizontalAlignment(SwingConstants.LEFT);
            labelJour.setFont(new Font("Segoe UI", Font.BOLD, 12));
            labelJour.setForeground(new Color(51, 51, 51));
            JButton btnAjouterJour = new JButton("+");
            btnAjouterJour.setMargin(new Insets(0, 6, 0, 6));
            btnAjouterJour.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnAjouterJour.setFocusable(false);
            btnAjouterJour.setBackground(new Color(0x8B5E34));
            btnAjouterJour.setForeground(Color.WHITE);
            btnAjouterJour.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            btnAjouterJour.setToolTipText("Ajouter un événement");
            btnAjouterJour.addActionListener(ev -> {
                Component root = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                if (root instanceof AgendaView view) {
                    if (RoleManager.isVisitor()) {
                        JOptionPane.showMessageDialog(MensuelPanel.this, "Mode visiteur: modification désactivée.");
                        return;
                    }
                    AgendaForm form = new AgendaForm((JFrame) root, view.getController());
                    form.setVisible(true);
                    if (form.estValide()) {
                        Agenda evt = form.getEvenement();
                        evt.setDate(date);
                        view.getController().ajouterEvenement(evt);
                        view.actualiserToutesLesVues();
                        view.notifierAction("Nouvel événement ajouté avec succès.");
                    }
                }
            });
            headerJour.add(labelJour, BorderLayout.WEST);
            headerJour.add(btnAjouterJour, BorderLayout.EAST);

            JTextPane zone = new JTextPane();
            zone.setEditable(false);
            zone.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            zone.setBackground(new Color(0xFFF9EF));
            zone.setForeground(new Color(0x4A2F1B));
            zone.setContentType("text/html");
            zone.setOpaque(true);
            zone.setPreferredSize(new Dimension(220, 160));

            // DropTarget pour réceptionner les déplacements
            new DropTarget(zone, new DropTargetAdapter() {
                @Override
                public void dragEnter(DropTargetDragEvent dtde) {
                    zone.setBackground(new Color(200, 230, 255));
                }

                @Override
                public void dragExit(DropTargetEvent dte) {
                    zone.setBackground(new Color(250, 250, 250));
                }

                @Override
                public void drop(DropTargetDropEvent dtde) {
                    try {
                        dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                        Transferable transferable = dtde.getTransferable();
                        String data = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                        String[] parts = data.split("\\|");
                        if (parts.length >= 3) {
                            String titre = parts[0];
                            LocalDate ancienneDate = LocalDate.parse(parts[1]);
                            String ancienneHeure = parts[2];

                            Agenda evtADeplacer = null;
                            for (Agenda evt : controller.getEvenements()) {
                                if (evt.getTitre().equals(titre) && evt.getDate().equals(ancienneDate)
                                        && evt.getHeure() != null && evt.getHeure().equals(ancienneHeure)) {
                                    evtADeplacer = evt;
                                    break;
                                }
                            }

                            if (evtADeplacer != null) {
                                evtADeplacer.setDate(date);
                                actualiser();
                                SwingUtilities.invokeLater(() -> {
                                    Component root = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                    if (root instanceof AgendaView view) view.actualiserToutesLesVues();
                                });
                                Component root = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                if (root instanceof AgendaView view) view.notifierAction("L'événement a été déplacé.");
                            }
                        }
                        dtde.dropComplete(true);
                    } catch (Exception ex) {
                        dtde.dropComplete(false);
                    } finally {
                        zone.setBackground(new Color(250, 250, 250));
                    }
                }
            });

            // Drag (initier)
            zone.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    String texte = zone.getText();
                    if (texte == null || texte.trim().isEmpty()) return;

                    String plain = texte.replaceAll("<[^>]*>", "");
                    String[] blocs = plain.split("\\n\\n");
                    if (blocs.length == 0) return;
                    String bloc = blocs[0];
                    String[] lignes = bloc.split("\\n");
                    if (lignes.length == 0) return;
                    String ligne = lignes[0];
                    String titre = ligne.contains(" - ") ? ligne.split(" - ")[1] : ligne;

                    for (Agenda evt : controller.getEvenements()) {
                        if (evt.getDate().equals(date) && evt.getTitre().equals(titre.trim())) {
                            if (evt.getResponsable() != null && evt.getResponsable().equals(AgendaForm.UTILISATEUR_COURANT)) {
                                zone.setBackground(new Color(255, 200, 150));
                                TransferHandler handler = new TransferHandler("text") {
                                    @Override
                                    protected Transferable createTransferable(JComponent c) {
                                        return new StringSelection(evt.getTitre() + "|" + evt.getDate() + "|" + evt.getHeure());
                                    }

                                    @Override
                                    public int getSourceActions(JComponent c) {
                                        return MOVE;
                                    }

                                    @Override
                                    protected void exportDone(JComponent source, Transferable data, int action) {
                                        zone.setBackground(new Color(250, 250, 250));
                                    }
                                };
                                zone.setTransferHandler(handler);
                                handler.exportAsDrag(zone, e, TransferHandler.MOVE);
                            } else {
                                JOptionPane.showMessageDialog(MensuelPanel.this,
                                        "⚠️ Seul le responsable (" + evt.getResponsable() + ") peut déplacer cet événement.",
                                        "Déplacement non autorisé", JOptionPane.WARNING_MESSAGE);
                            }
                            break;
                        }
                    }
                }
            });

            // Clics et menu contextuel
            zone.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // Clic droit sur case vide : proposer Ajouter (via modèle, pas HTML)
                    if (SwingUtilities.isRightMouseButton(e)) {
                        List<Agenda> events = controller.getEvenementsDuJour(date);
                        if (events == null || events.isEmpty()) {
                            JPopupMenu menuVide = new JPopupMenu();
                            JMenuItem ajouterItem = new JMenuItem("➕ Ajouter événement");
                            ajouterItem.addActionListener(ae -> {
                                Component root = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                if (root instanceof AgendaView view) {
                                    if (RoleManager.isVisitor()) {
                                        JOptionPane.showMessageDialog(MensuelPanel.this, "Mode visiteur: modification désactivée.");
                                        return;
                                    }
                                    AgendaForm form = new AgendaForm((JFrame) root, view.getController());
                                    form.setVisible(true);
                                    if (form.estValide()) {
                                        Agenda evt = form.getEvenement();
                                        evt.setDate(date);
                                        view.getController().ajouterEvenement(evt);
                                        view.actualiserToutesLesVues();
                                        view.notifierAction("Nouvel événement ajouté avec succès.");
                                    }
                                }
                            });
                            menuVide.add(ajouterItem);
                            menuVide.show(zone, e.getX(), e.getY());
                            return;
                        }
                    }
                    // Clic gauche sur case vide : ajouter directement
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        List<Agenda> events = controller.getEvenementsDuJour(date);
                        if (events == null || events.isEmpty()) {
                            Component root = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                            if (root instanceof AgendaView view) {
                                if (RoleManager.isVisitor()) {
                                    JOptionPane.showMessageDialog(MensuelPanel.this, "Mode visiteur: modification désactivée.");
                                    return;
                                }
                                AgendaForm form = new AgendaForm((JFrame) root, view.getController());
                                form.setVisible(true);
                                if (form.estValide()) {
                                    Agenda evt = form.getEvenement();
                                    evt.setDate(date);
                                    view.getController().ajouterEvenement(evt);
                                    view.actualiserToutesLesVues();
                                    view.notifierAction("Nouvel événement ajouté avec succès.");
                                }
                            }
                            return;
                        }
                    }
                    
                    // Certaines configurations de JTextPane n'envoient pas mouseClicked pour les single-clicks
                    // Utiliser mousePressed pour afficher le menu rapide au clic gauche.
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                        String texteSel = zone.getText();
                        if (texteSel != null && !texteSel.trim().isEmpty()) {
                            String plainSel = texteSel.replaceAll("<[^>]*>", "");
                            String[] blocsSel = plainSel.split("\\n\\n");
                            if (blocsSel.length > 0) {
                                String blocSel = blocsSel[0];
                                String[] lignesSel = blocSel.split("\\n");
                                if (lignesSel.length > 0) {
                                    String ligneSel = lignesSel[0];
                                    String titreSel = ligneSel.contains(" - ") ? ligneSel.split(" - ")[1] : ligneSel;
                                    for (Agenda evt : controller.getEvenements()) {
                                        if (evt.getDate().equals(date) && evt.getTitre().equals(titreSel.trim())) {
                                            selectedEvent = evt;
                                            Component root = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                            if (root instanceof AgendaView view) view.setStatus("Événement sélectionné : " + evt.getTitre());

                                            // Construire et afficher le menu rapide
                                            JPopupMenu menuRapide = new JPopupMenu();
                                            JMenuItem modifierRapide = new JMenuItem("✏️ Modifier");
                                            JMenuItem supprimerRapide = new JMenuItem("🗑️ Supprimer");
                                            JMenuItem partagerRapide = new JMenuItem("🔗 Partager");

                                            modifierRapide.addActionListener(ae -> {
                                                if (RoleManager.isVisitor()) {
                                                    JOptionPane.showMessageDialog(MensuelPanel.this, "Mode visiteur: modification désactivée.");
                                                    return;
                                                }
                                                AgendaForm form = new AgendaForm((JFrame) SwingUtilities.getWindowAncestor(MensuelPanel.this), evt, controller);
                                                form.setVisible(true);
                                                if (form.estValide()) {
                                                    if (form.estSupprime()) {
                                                        if (RoleManager.isVisitor()) return;
                                                        controller.supprimerEvenement(date, evt.getTitre());
                                                        Component r = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                                        if (r instanceof AgendaView v) v.notifierAction("Événement supprimé avec succès.");
                                                    } else {
                                                        boolean ok = controller.modifierEvenement(date, evt.getTitre(), form.getEvenement());
                                                        if (ok) {
                                                            Component r = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                                            if (r instanceof AgendaView v) v.notifierAction("Événement modifié avec succès.");
                                                        }
                                                    }
                                                    SwingUtilities.invokeLater(() -> {
                                                        Component r = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                                        if (r instanceof AgendaView v) v.actualiserToutesLesVues();
                                                    });
                                                }
                                            });

                                            supprimerRapide.addActionListener(ae -> {
                                                if (RoleManager.isVisitor()) {
                                                    JOptionPane.showMessageDialog(MensuelPanel.this, "Mode visiteur: suppression désactivée.");
                                                    return;
                                                }
                                                int choix = JOptionPane.showConfirmDialog(MensuelPanel.this,
                                                        "Voulez-vous vraiment supprimer cet événement ?",
                                                        "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                                                if (choix == JOptionPane.YES_OPTION) {
                                                    controller.supprimerEvenement(date, evt.getTitre());
                                                    Component r = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                                    if (r instanceof AgendaView v) v.notifierAction("Événement supprimé avec succès.");
                                                    SwingUtilities.invokeLater(() -> {
                                                        Component r2 = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                                        if (r2 instanceof AgendaView v2) v2.actualiserToutesLesVues();
                                                    });
                                                }
                                            });

                                            partagerRapide.addActionListener(ae -> {
                                                Component r = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                                if (r instanceof AgendaView v) {
                                                    java.util.List<String> users = java.util.Arrays.asList(
                                                            "Ali Paychen", "Ikene Manel", "Yacine Kadaoui", "Zaza Khlifaoui", "Ouri Baouida"
                                                    );
                                                    PartageDialog pd = new PartageDialog((JFrame) r, users);
                                                    pd.setVisible(true);
                                                    if (pd.estValide()) {
                                                        java.util.List<String> sel = pd.getSelectedUsers();
                                                        String names = sel.isEmpty() ? "(aucun)" : String.join(", ", sel);
                                                        v.notifierAction("Événement partagé avec : " + names);
                                                    }
                                                }
                                            });

                                            menuRapide.add(modifierRapide);
                                            menuRapide.add(supprimerRapide);
                                            menuRapide.addSeparator();
                                            menuRapide.add(partagerRapide);
                                            menuRapide.show(zone, e.getX(), e.getY());

                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Single left-click: menu rapide sur premier événement du jour (détection modèle)
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                        List<Agenda> events = controller.getEvenementsDuJour(date);
                        if (events != null && !events.isEmpty()) {
                            Agenda evt = events.get(0);
                                            selectedEvent = evt;
                                            Component root = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                            if (root instanceof AgendaView view) view.setStatus("Événement sélectionné : " + evt.getTitre());

                                            // Menu rapide sur clic gauche (Modifier / Supprimer / Partager)
                                            JPopupMenu menuRapide = new JPopupMenu();
                                            JMenuItem modifierRapide = new JMenuItem("✏️ Modifier");
                                            JMenuItem supprimerRapide = new JMenuItem("🗑️ Supprimer");
                                            JMenuItem partagerRapide = new JMenuItem("🔗 Partager");

                                            modifierRapide.addActionListener(ae -> {
                                                AgendaForm form = new AgendaForm((JFrame) SwingUtilities.getWindowAncestor(MensuelPanel.this), evt, controller);
                                                form.setVisible(true);
                                                if (form.estValide()) {
                                                    if (form.estSupprime()) {
                                                        controller.supprimerEvenement(date, evt.getTitre());
                                                        Component r = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                                        if (r instanceof AgendaView v) v.notifierAction("Événement supprimé avec succès.");
                                                    } else {
                                                        boolean ok = controller.modifierEvenement(date, evt.getTitre(), form.getEvenement());
                                                        if (ok) {
                                                            Component r = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                                            if (r instanceof AgendaView v) v.notifierAction("Événement modifié avec succès.");
                                                        }
                                                    }
                                                    SwingUtilities.invokeLater(() -> {
                                                        Component r = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                                        if (r instanceof AgendaView v) v.actualiserToutesLesVues();
                                                    });
                                                }
                                            });

                                            supprimerRapide.addActionListener(ae -> {
                                                int choix = JOptionPane.showConfirmDialog(MensuelPanel.this,
                                                        "Voulez-vous vraiment supprimer cet événement ?",
                                                        "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                                                if (choix == JOptionPane.YES_OPTION) {
                                                    controller.supprimerEvenement(date, evt.getTitre());
                                                    Component r = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                                    if (r instanceof AgendaView v) v.notifierAction("Événement supprimé avec succès.");
                                                    SwingUtilities.invokeLater(() -> {
                                                        Component r2 = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                                        if (r2 instanceof AgendaView v2) v2.actualiserToutesLesVues();
                                                    });
                                                }
                                            });

                                            partagerRapide.addActionListener(ae -> {
                                                Component r = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                                if (r instanceof AgendaView v) {
                                                    java.util.List<String> users = java.util.Arrays.asList(
                                                            "Ali Paychen", "Ikene Manel", "Yacine Kadaoui", "Zaza Khlifaoui", "Ouri Baouida"
                                                    );
                                                    PartageDialog pd = new PartageDialog((JFrame) r, users);
                                                    pd.setVisible(true);
                                                    if (pd.estValide()) {
                                                        java.util.List<String> sel = pd.getSelectedUsers();
                                                        String names = sel.isEmpty() ? "(aucun)" : String.join(", ", sel);
                                                        v.notifierAction("Événement partagé avec : " + names);
                                                    }
                                                }
                                            });

                                            menuRapide.add(modifierRapide);
                                            menuRapide.add(supprimerRapide);
                                            menuRapide.addSeparator();
                                            menuRapide.add(partagerRapide);
                                            menuRapide.show(zone, e.getX(), e.getY());
                        }
                    }
                    String texte = zone.getText();
                    // Double-clic pour éditer le premier événement affiché
                    if (e.getClickCount() == 2 && texte != null && !texte.trim().isEmpty()) {
                        String plain = texte.replaceAll("<[^>]*>", "");
                        String[] blocs = plain.split("\\n\\n");
                        if (blocs.length == 0) return;
                        String bloc = blocs[0];
                        String[] lignes = bloc.split("\\n");
                        if (lignes.length == 0) return;
                        String ligne = lignes[0];
                        String titre = ligne.contains(" - ") ? ligne.split(" - ")[1] : ligne;

                        for (Agenda evt : controller.getEvenements()) {
                            if (evt.getDate().equals(date) && evt.getTitre().equals(titre.trim())) {
                                AgendaForm form = new AgendaForm((JFrame) SwingUtilities.getWindowAncestor(MensuelPanel.this), evt, controller);
                                form.setVisible(true);
                                if (form.estValide()) {
                                    if (form.estSupprime()) {
                                        controller.supprimerEvenement(date, titre.trim());
                                        Component root = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                        if (root instanceof AgendaView view) view.notifierAction("Événement supprimé avec succès.");
                                    } else {
                                        boolean ok = controller.modifierEvenement(date, titre.trim(), form.getEvenement());
                                        if (ok) {
                                            Component root = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                            if (root instanceof AgendaView view) view.notifierAction("Événement modifié avec succès.");
                                        }
                                    }
                                    SwingUtilities.invokeLater(() -> {
                                        Component root = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                        if (root instanceof AgendaView view) view.actualiserToutesLesVues();
                                    });
                                }
                                break;
                            }
                        }
                    }

                    // Clic droit : menu listant tous les événements du jour
                    else if (SwingUtilities.isRightMouseButton(e)) {
                        List<Agenda> events = controller.getEvenementsDuJour(date);
                        if (events == null || events.isEmpty()) return;
                        JPopupMenu menu = new JPopupMenu();
                        for (Agenda evt : events) {
                            JMenu sous = new JMenu((evt.getHeure() != null ? evt.getHeure() + " - " : "") + evt.getTitre());
                            JMenuItem modifier = new JMenuItem("✏️ Modifier");
                            JMenuItem supprimer = new JMenuItem("🗑️ Supprimer");
                            JMenuItem partager = new JMenuItem("🔗 Partager");

                                modifier.addActionListener(ev -> {
                                    if (RoleManager.isVisitor()) {
                                        JOptionPane.showMessageDialog(MensuelPanel.this, "Mode visiteur: modification désactivée.");
                                        return;
                                    }
                                    AgendaForm form = new AgendaForm((JFrame) SwingUtilities.getWindowAncestor(MensuelPanel.this), evt, controller);
                                    form.setVisible(true);
                                    if (form.estValide()) {
                                        if (form.estSupprime()) {
                                            controller.supprimerEvenement(date, evt.getTitre().trim());
                                            Component root = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                            if (root instanceof AgendaView view) view.notifierAction("Événement supprimé avec succès.");
                                        } else {
                                            boolean ok = controller.modifierEvenement(date, evt.getTitre().trim(), form.getEvenement());
                                            if (ok) {
                                                Component root = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                                if (root instanceof AgendaView view) view.notifierAction("Événement modifié avec succès.");
                                            }
                                        }
                                        SwingUtilities.invokeLater(() -> {
                                            Component root = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                            if (root instanceof AgendaView view) view.actualiserToutesLesVues();
                                        });
                                    }
                                });

                                supprimer.addActionListener(ev -> {
                                    if (RoleManager.isVisitor()) {
                                        JOptionPane.showMessageDialog(MensuelPanel.this, "Mode visiteur: suppression désactivée.");
                                        return;
                                    }
                                    int choix = JOptionPane.showConfirmDialog(MensuelPanel.this,
                                            "Voulez-vous vraiment supprimer cet événement ?",
                                            "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                                    if (choix == JOptionPane.YES_OPTION) {
                                        controller.supprimerEvenement(date, evt.getTitre().trim());
                                        Component root = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                        if (root instanceof AgendaView view) view.notifierAction("Événement supprimé avec succès.");
                                        SwingUtilities.invokeLater(() -> {
                                            Component root2 = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                            if (root2 instanceof AgendaView view2) view2.actualiserToutesLesVues();
                                        });
                                    }
                                });

                                partager.addActionListener(ev -> {
                                    Component root = SwingUtilities.getWindowAncestor(MensuelPanel.this);
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

                            sous.add(modifier);
                            sous.add(supprimer);
                            sous.addSeparator();
                            sous.add(partager);
                            menu.add(sous);
                        }
                        menu.show(zone, e.getX(), e.getY());
                    }
                }
            });

            // Construire texte et tooltip pour la zone
            List<Agenda> duJour = controller.getEvenementsDuJour(date);
            StringBuilder contenu = new StringBuilder("<html><body style='width: 180px; padding: 5px;'>");
            StringBuilder tooltip = new StringBuilder("<html><body style='width: 250px; padding: 5px;'>");
            tooltip.append("<h3 style='color: #654321; margin: 0;'>📅 Événements du ").append(date.getDayOfMonth()).append("/").append(date.getMonthValue()).append("</h3>");
            tooltip.append("<hr style='border: 1px solid #DDD;'>");

            for (Agenda evt : duJour) {
                contenu.append("<div style='background-color: #E8F5E9; padding: 4px; margin: 2px; border-left: 3px solid #4CAF50; border-radius: 3px;'>");
                contenu.append("<b style='color: #2E7D32;'>").append(evt.getHeure() != null ? evt.getHeure() : "").append(" - ").append(evt.getTitre()).append("</b><br>");
                if (evt.getDescription() != null && !evt.getDescription().isEmpty()) {
                    contenu.append("<span style='color: #555;'>").append(evt.getDescription()).append("</span>");
                }
                contenu.append("</div><br>");

                tooltip.append("<div style='background-color: #FFF8DC; padding: 8px; margin: 6px 0; border-left: 4px solid #4CAF50; border-radius: 4px;'>");
                tooltip.append("<b style='color: #2E7D32; font-size: 12px;'>📋 ").append(evt.getTitre()).append("</b><br>");
                tooltip.append("🕒 <b>Heure:</b> ").append(evt.getHeure() != null ? evt.getHeure() : "").append("<br>");
                tooltip.append("👤 <b>Responsable:</b> ").append(evt.getResponsable() != null ? evt.getResponsable() : "Non assigné").append("<br>");
                if (evt.getParticipants() != null && !evt.getParticipants().isEmpty()) {
                    tooltip.append("👥 <b>Participants:</b> ").append(String.join(", ", evt.getParticipants())).append("<br>");
                }
                if (evt.getDescription() != null && !evt.getDescription().isEmpty()) {
                    String desc = evt.getDescription();
                    if (desc.length() > 80) desc = desc.substring(0, 80) + "...";
                    tooltip.append("📝 <b>Description:</b><br><i>").append(desc).append("</i><br>");
                }
                tooltip.append("<span style='color: #888; font-size: 9px;'>💡 Double-clic pour modifier | Glisser pour déplacer</span>");
                tooltip.append("</div>");
            }

            if (duJour.isEmpty()) {
                tooltip.append("<p style='color: #888; font-style: italic;'>Aucun événement prévu</p>");
            }

            tooltip.append("</body></html>");
            contenu.append("</body></html>");
            zone.setText(contenu.toString());
            String tip = tooltip.toString();
            zone.setToolTipText(tip);
            // Propager l'infobulle au conteneur et à l'en-tête pour un survol fiable
            caseJour.setToolTipText(tip);
            headerJour.setToolTipText(tip);
            labelJour.setToolTipText(tip);

            // Ajout d'un menu contextuel sur le panel parent (caseJour)
            caseJour.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        List<Agenda> evenementsDuJour = controller.getEvenementsDuJour(date);
                        boolean estVide = evenementsDuJour == null || evenementsDuJour.isEmpty();
                        if (estVide) {
                            JPopupMenu menuVide = new JPopupMenu();
                            JMenuItem ajouterItem = new JMenuItem("➕ Ajouter événement");
                            ajouterItem.addActionListener(ae -> {
                                Component root = SwingUtilities.getWindowAncestor(MensuelPanel.this);
                                if (root instanceof AgendaView view) {
                                    AgendaForm form = new AgendaForm((JFrame) root, view.getController());
                                    form.setVisible(true);
                                    if (form.estValide()) {
                                        Agenda evt = form.getEvenement();
                                        evt.setDate(date);
                                        view.getController().ajouterEvenement(evt);
                                        view.actualiserToutesLesVues();
                                        view.notifierAction("Nouvel événement ajouté avec succès.");
                                    }
                                }
                            });
                            menuVide.add(ajouterItem);
                            menuVide.show(caseJour, e.getX(), e.getY());
                        }
                    }
                }
            });

            caseJour.add(headerJour, BorderLayout.NORTH);
            // Ajouter directement la zone sans scroll pour afficher tout
            caseJour.add(zone, BorderLayout.CENTER);
            zoneParDate.put(date, zone);
            gridPanel.add(caseJour);
        }

        revalidate();
        repaint();
    }

    public void actualiser() {
        for (Map.Entry<LocalDate, JTextPane> e : zoneParDate.entrySet()) {
            LocalDate date = e.getKey();
            JTextPane zone = e.getValue();
            List<Agenda> duJour = controller.getEvenementsDuJour(date);
            StringBuilder contenu = new StringBuilder("<html><body style='width: 180px; padding: 5px;'>");
            StringBuilder tooltip = new StringBuilder("<html><body style='width: 250px; padding: 5px;'>");
            tooltip.append("<h3 style='color: #654321; margin: 0;'>📅 Événements du ").append(date.getDayOfMonth()).append("/").append(date.getMonthValue()).append("</h3>");
            tooltip.append("<hr style='border: 1px solid #DDD;'>");

            for (Agenda evt : duJour) {
                contenu.append("<div style='background-color: #E8F5E9; padding: 4px; margin: 2px; border-left: 3px solid #4CAF50; border-radius: 3px;'>");
                contenu.append("<b style='color: #2E7D32;'>").append(evt.getHeure() != null ? evt.getHeure() : "").append(" - ").append(evt.getTitre()).append("</b><br>");
                if (evt.getDescription() != null && !evt.getDescription().isEmpty()) {
                    contenu.append("<span style='color: #555;'>").append(evt.getDescription()).append("</span>");
                }
                contenu.append("</div><br>");

                tooltip.append("<div style='background-color: #FFF8DC; padding: 8px; margin: 6px 0; border-left: 4px solid #4CAF50; border-radius: 4px;'>");
                tooltip.append("<b style='color: #2E7D32; font-size: 12px;'>📋 ").append(evt.getTitre()).append("</b><br>");
                tooltip.append("🕒 <b>Heure:</b> ").append(evt.getHeure() != null ? evt.getHeure() : "").append("<br>");
                tooltip.append("👤 <b>Responsable:</b> ").append(evt.getResponsable() != null ? evt.getResponsable() : "Non assigné").append("<br>");
                if (evt.getParticipants() != null && !evt.getParticipants().isEmpty()) {
                    tooltip.append("👥 <b>Participants:</b> ").append(String.join(", ", evt.getParticipants())).append("<br>");
                }
                if (evt.getDescription() != null && !evt.getDescription().isEmpty()) {
                    String desc = evt.getDescription();
                    if (desc.length() > 80) desc = desc.substring(0, 80) + "...";
                    tooltip.append("📝 <b>Description:</b><br><i>").append(desc).append("</i><br>");
                }
                tooltip.append("<span style='color: #888; font-size: 9px;'>💡 Double-clic pour modifier | Glisser pour déplacer</span>");
                tooltip.append("</div>");
            }

            if (duJour.isEmpty()) {
                tooltip.append("<p style='color: #888; font-style: italic;'>Aucun événement prévu</p>");
            }

            tooltip.append("</body></html>");
            contenu.append("</body></html>");
            zone.setText(contenu.toString());
            String tip = tooltip.toString();
            zone.setToolTipText(tip);
            // Mettre à jour aussi le panel jour et son en-tête si accessibles
            Container parent = zone.getParent();
            if (parent instanceof JComponent jp) {
                jp.setToolTipText(tip);
                for (Component c : jp.getComponents()) {
                    if (c instanceof JComponent jc && !(jc instanceof JButton)) {
                        jc.setToolTipText(tip);
                    }
                }
            }
        }
        revalidate();
        repaint();
    }

    // Retourne l'événement sélectionné dans la vue mensuelle (ou null)
    public Agenda getSelectedEvent() {
        return selectedEvent;
    }
}
