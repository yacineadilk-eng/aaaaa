package com.agenda.view;

import com.agenda.controller.AgendaController;
import com.agenda.model.Agenda;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.*;

public class HebdoPanel extends JPanel {
    private final AgendaController controller;
    private final Map<LocalDate, JTextPane> zoneParDate = new HashMap<>();
    private Agenda selectedEvent;

    public HebdoPanel(AgendaController controller) {
        this.controller = controller;
        setLayout(new GridLayout(1, 7, 5, 5));
        setBackground(new Color(245, 245, 245)); // Fond gris clair

        LocalDate aujourd = LocalDate.now();
        LocalDate lundi = aujourd.minusDays(aujourd.getDayOfWeek().getValue() - 1);

        for (int i = 0; i < 7; i++) {
            LocalDate date = lundi.plusDays(i);
            String jourNom = switch (date.getDayOfWeek()) {
                case MONDAY -> "Lundi";
                case TUESDAY -> "Mardi";
                case WEDNESDAY -> "Mercredi";
                case THURSDAY -> "Jeudi";
                case FRIDAY -> "Vendredi";
                case SATURDAY -> "Samedi";
                case SUNDAY -> "Dimanche";
            };

            JPanel jourPanel = new JPanel(new BorderLayout());
            jourPanel.setBackground(Color.WHITE);
            jourPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(208, 208, 208)), jourNom + " " + date));
            jourPanel.setPreferredSize(new Dimension(100, 100));

            JTextPane zone = new JTextPane();
            zone.setEditable(false);
            zone.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            zone.setBackground(new Color(250, 250, 250));
            zone.setForeground(new Color(51, 51, 51));
            zone.setContentType("text/plain");

            JScrollPane scroll = new JScrollPane(zone);
            scroll.setPreferredSize(new Dimension(100, 80));
            scroll.setBorder(null);

            jourPanel.add(scroll, BorderLayout.CENTER);
            zoneParDate.put(date, zone);

            zone.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Clic droit : menu contextuel (supprimer)
                    if (SwingUtilities.isRightMouseButton(e)) {
                        int pos = zone.viewToModel2D(e.getPoint());
                        String texte = zone.getText();
                        String[] blocs = texte.split("\n\n");

                        int blocIndex = -1;
                        int charCount = 0;
                        for (int i = 0; i < blocs.length; i++) {
                            charCount += blocs[i].length() + 2;
                            if (pos <= charCount) {
                                blocIndex = i;
                                break;
                            }
                        }

                        if (blocIndex >= 0 && blocIndex < blocs.length) {
                            String bloc = blocs[blocIndex];
                            String[] lignes = bloc.split("\n");
                            if (lignes.length > 0) {
                                String ligne = lignes[0];
                                String titre = ligne.contains(" - ") ? ligne.split(" - ")[1] : ligne;
                                // trouver l'événement correspondant et définir la sélection
                                Agenda evenementSelectionne = null;
                                for (Agenda evt : controller.getEvenements()) {
                                    if (evt.getDate() != null && evt.getDate().equals(date) && evt.getTitre().equals(titre.trim())) {
                                        evenementSelectionne = evt;
                                        break;
                                    }
                                }
                                HebdoPanel.this.selectedEvent = evenementSelectionne;

                                JPopupMenu menu = new JPopupMenu();
                                JMenuItem supprimer = new JMenuItem("Supprimer");

                                supprimer.addActionListener(ev -> {
                                    controller.supprimerEvenement(date, titre.trim());
                                    SwingUtilities.invokeLater(() -> {
                                        Component root = SwingUtilities.getWindowAncestor(HebdoPanel.this);
                                        if (root instanceof AgendaView view) {
                                            view.actualiserToutesLesVues();
                                            view.notifierAction("Événement supprimé : '" + titre.trim() + "' le " + date);
                                        }
                                    });
                                });

                                menu.add(supprimer);
                                JMenuItem partager = new JMenuItem("🔗 Partager");
                                partager.addActionListener(ev -> {
                                    Component root = SwingUtilities.getWindowAncestor(HebdoPanel.this);
                                    if (root instanceof AgendaView view) {
                                        java.util.List<String> users = java.util.Arrays.asList(
                                            "Ali Paychen", "Ikene Manel", "Yacine Kadaoui", "Zaza Khlifaoui", "Ouri Baouida"
                                        );
                                        PartageDialog pd = new PartageDialog((JFrame) root, users);
                                        pd.setVisible(true);
                                        if (pd.estValide()) {
                                            java.util.List<String> sel = pd.getSelectedUsers();
                                            view.notifierAction("Événement partagé avec " + sel.size() + " utilisateur(s).");
                                        }
                                    }
                                });
                                menu.addSeparator();
                                menu.add(partager);
                                menu.show(zone, e.getX(), e.getY());
                            }
                        }
                    }
                    // Clic gauche simple : si on a cliqué sur un bloc existant afficher menu Modifier/Supprimer, sinon ouvrir formulaire d'ajout
                    else if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                        int pos = zone.viewToModel2D(e.getPoint());
                        String texte = zone.getText();
                        String[] blocs = texte.split("\n\n");

                        int blocIndex = -1;
                        int charCount = 0;
                        for (int i = 0; i < blocs.length; i++) {
                            charCount += blocs[i].length() + 2;
                            if (pos <= charCount) {
                                blocIndex = i;
                                break;
                            }
                        }

                        if (blocIndex >= 0 && blocIndex < blocs.length) {
                            String bloc = blocs[blocIndex];
                            String[] lignes = bloc.split("\n");
                            if (lignes.length > 0) {
                                String ligne = lignes[0];
                                String titre = ligne.contains(" - ") ? ligne.split(" - ")[1] : ligne;

                                JPopupMenu menu = new JPopupMenu();
                                JMenuItem modifier = new JMenuItem("✏️ Modifier");
                                JMenuItem supprimer = new JMenuItem("🗑️ Supprimer");

                                    modifier.addActionListener(ev -> {
                                        for (Agenda evt : controller.getEvenements()) {
                                            if (evt.getDate() != null && evt.getDate().equals(date) && evt.getTitre().equals(titre.trim())) {
                                                java.time.LocalDate ancienneDate = evt.getDate();
                                                String ancienTitre = evt.getTitre();
                                                AgendaForm form = new AgendaForm((JFrame) SwingUtilities.getWindowAncestor(HebdoPanel.this), evt, controller);
                                                form.setVisible(true);
                                                if (form.estValide()) {
                                                    if (form.estSupprime()) {
                                                        controller.supprimerEvenement(ancienneDate, ancienTitre);
                                                        Component root = SwingUtilities.getWindowAncestor(HebdoPanel.this);
                                                        if (root instanceof AgendaView view) {
                                                            view.notifierAction("Événement supprimé : '" + ancienTitre + "' le " + ancienneDate);
                                                        }
                                                    } else {
                                                        boolean ok = controller.modifierEvenement(ancienneDate, ancienTitre, form.getEvenement());
                                                        if (ok) {
                                                            Component root = SwingUtilities.getWindowAncestor(HebdoPanel.this);
                                                            if (root instanceof AgendaView view) {
                                                                view.notifierAction("Événement modifié : '" + form.getEvenement().getTitre() + "' le " + form.getEvenement().getDate());
                                                            }
                                                        }
                                                    }
                                                    SwingUtilities.invokeLater(() -> {
                                                        Component root = SwingUtilities.getWindowAncestor(HebdoPanel.this);
                                                        if (root instanceof AgendaView view) {
                                                            view.actualiserToutesLesVues();
                                                        }
                                                    });
                                                }
                                                break;
                                            }
                                        }
                                });

                                supprimer.addActionListener(ev -> {
                                    int choix = JOptionPane.showConfirmDialog(
                                        HebdoPanel.this,
                                        "Voulez-vous vraiment supprimer l'événement '" + titre.trim() + "' ?",
                                        "Confirmation",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.WARNING_MESSAGE
                                    );
                                    if (choix == JOptionPane.YES_OPTION) {
                                        controller.supprimerEvenement(date, titre.trim());
                                        SwingUtilities.invokeLater(() -> {
                                            Component root = SwingUtilities.getWindowAncestor(HebdoPanel.this);
                                            if (root instanceof AgendaView view) {
                                                view.actualiserToutesLesVues();
                                            }
                                        });
                                    }
                                });

                                menu.add(modifier);
                                menu.add(supprimer);
                                JMenuItem partager = new JMenuItem("🔗 Partager");
                                partager.addActionListener(ev -> {
                                    Component root = SwingUtilities.getWindowAncestor(HebdoPanel.this);
                                    if (root instanceof AgendaView view) {
                                        java.util.List<String> users = java.util.Arrays.asList(
                                            "Ali Paychen", "Ikene Manel", "Yacine Kadaoui", "Zaza Khlifaoui", "Ouri Baouida"
                                        );
                                        PartageDialog pd = new PartageDialog((JFrame) root, users);
                                        pd.setVisible(true);
                                        if (pd.estValide()) {
                                            java.util.List<String> sel = pd.getSelectedUsers();
                                            view.notifierAction("Événement partagé avec " + sel.size() + " utilisateur(s).");
                                        }
                                    }
                                });
                                menu.addSeparator();
                                menu.add(partager);
                                menu.show(zone, e.getX(), e.getY());
                            }
                        } else {
                            AgendaForm form = new AgendaForm((JFrame) SwingUtilities.getWindowAncestor(HebdoPanel.this), controller);
                            form.setVisible(true);
                            if (form.estValide()) {
                                Agenda evt = form.getEvenement();
                                controller.ajouterEvenement(evt);
                                SwingUtilities.invokeLater(() -> {
                                    Component root = SwingUtilities.getWindowAncestor(HebdoPanel.this);
                                    if (root instanceof AgendaView view) {
                                        view.actualiserToutesLesVues();
                                    }
                                });
                            }
                        }
                    }
                }
            });

            add(jourPanel);
        }

        actualiser();
    }

    public void actualiser() {
        for (LocalDate date : zoneParDate.keySet()) {
            afficherEvenements(date);
        }
    }

    public Agenda getSelectedEvent() {
        return selectedEvent;
    }

    private void afficherEvenements(LocalDate date) {
        var evenements = controller.getEvenements();
        var duJour = new ArrayList<Agenda>();

        for (Agenda evt : evenements) {
            if (evt.getDate() != null && evt.getDate().equals(date)) {
                duJour.add(evt);
            }
        }

        duJour.sort(Comparator.comparing(evt -> evt.getHeure() != null ? evt.getHeure() : "00:00"));

        JTextPane zone = zoneParDate.get(date);
        StringBuilder contenu = new StringBuilder();
        for (Agenda evt : duJour) {
            contenu.append(evt.getHeure()).append(" - ").append(evt.getTitre()).append("\n");
            contenu.append(evt.getDescription()).append("\n\n");
        }
        zone.setText(contenu.toString());
    }
}