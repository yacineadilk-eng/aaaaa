package com.agenda.view;

import com.agenda.model.Agenda;
import com.agenda.controller.AgendaController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

public class AgendaForm extends JDialog {
    // Utilisateur courant (simulé - dans une vraie application, ce serait après connexion)
    public static final String UTILISATEUR_COURANT = "Yacine Kadaoui";
    
    // Désormais, la liste des admins vient de RoleManager
    
    private JTextField titreField;
    private JTextField heureField;
    private JTextArea descriptionArea;
    private JSpinner dateSpinner;
    private JComboBox<String> responsableCombo;
    private DefaultListModel<String> participantsListModel;
    private JList<String> participantsList;
    private JTextField participantField;
    private boolean valide = false;
    private boolean modeEdition = false;
    private Agenda evenementOriginal;
    private boolean supprime = false;
    private AgendaController controller;

    // Heures disponibles de 08:00 à 20:00
    private static final String[] HEURES = {
        "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
        "11:00", "11:30", "12:00", "12:30", "13:00", "13:30",
        "14:00", "14:30", "15:00", "15:30", "16:00", "16:30",
        "17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00"
    };

    // Constructeur pour AJOUTER un événement
    public AgendaForm(JFrame parent, AgendaController controller) {
        this(parent, null, controller);
    }

    // Constructeur pour ÉDITER un événement
    public AgendaForm(JFrame parent, Agenda evenement, AgendaController controller) {
        super(parent, evenement == null ? "Ajouter un événement" : "Modifier l'événement", true);
        this.evenementOriginal = evenement;
        this.modeEdition = (evenement != null);
        this.controller = controller;
        
        setSize(600, 650);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        // Panel principal avec fond
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(255, 253, 245));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // TITRE STYLISÉ EN HAUT
        JLabel titreDialog = new JLabel(modeEdition ? "📝 MODIFICATION" : "➕ AJOUT RENDEZ-VOUS");
        titreDialog.setFont(new Font("Arial", Font.BOLD, 24));
        titreDialog.setForeground(new Color(101, 67, 33));
        titreDialog.setHorizontalAlignment(SwingConstants.CENTER);
        titreDialog.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // FORMULAIRE
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Titre
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblTitre = new JLabel("📋 INTITULÉ * :");
        lblTitre.setFont(new Font("Arial", Font.BOLD, 13));
        lblTitre.setForeground(new Color(101, 67, 33));
        formPanel.add(lblTitre, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        titreField = new JTextField();
        titreField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titreField.setPreferredSize(new Dimension(300, 30));
        formPanel.add(titreField, gbc);

        // Date
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblDate = new JLabel("Date * :");
        lblDate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblDate, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateSpinner.setPreferredSize(new Dimension(300, 30));
        formPanel.add(dateSpinner, gbc);

        // Heure (saisie libre)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel lblHeure = new JLabel("Heure * :");
        lblHeure.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(lblHeure, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        heureField = new JTextField();
        heureField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        heureField.setPreferredSize(new Dimension(300, 30));
        heureField.setToolTipText("Exemples: 09:15, 9h, 09h30, 09.15");
        formPanel.add(heureField, gbc);

        // Responsable (sélection parmi les admins)
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        JLabel lblResponsable = new JLabel("👤 RESPONSABLE * :");
        lblResponsable.setFont(new Font("Arial", Font.BOLD, 13));
        lblResponsable.setForeground(new Color(101, 67, 33));
        formPanel.add(lblResponsable, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        responsableCombo = new JComboBox<>(com.agenda.auth.RoleManager.ADMIN_LIST.toArray(new String[0]));
        responsableCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        responsableCombo.setBackground(Color.WHITE);
        responsableCombo.setPreferredSize(new Dimension(300, 30));
        // Pré-sélectionner l'utilisateur courant
        responsableCombo.setSelectedItem(UTILISATEUR_COURANT);
        formPanel.add(responsableCombo, gbc);

        // Participants (interface moderne)
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblParticipants = new JLabel("👥 PARTICIPANTS :");
        lblParticipants.setFont(new Font("Arial", Font.BOLD, 13));
        lblParticipants.setForeground(new Color(101, 67, 33));
        formPanel.add(lblParticipants, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        JPanel participantsPanel = new JPanel(new BorderLayout(5, 5));
        participantsPanel.setBackground(Color.WHITE);
        
        // Champ de saisie + bouton ajouter
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBackground(Color.WHITE);
        participantField = new JTextField();
        participantField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        participantField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(5, 8, 5, 8)
        ));
        
        JButton btnAjouterParticipant = new JButton("➕");
        btnAjouterParticipant.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAjouterParticipant.setBackground(new Color(76, 175, 80));
        btnAjouterParticipant.setForeground(Color.WHITE);
        btnAjouterParticipant.setFocusPainted(false);
        btnAjouterParticipant.setPreferredSize(new Dimension(40, 30));
        btnAjouterParticipant.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAjouterParticipant.setToolTipText("Ajouter ce participant");
        
        inputPanel.add(participantField, BorderLayout.CENTER);
        inputPanel.add(btnAjouterParticipant, BorderLayout.EAST);
        
        // Liste des participants
        participantsListModel = new DefaultListModel<>();
        participantsList = new JList<>(participantsListModel);
        participantsList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        participantsList.setBackground(new Color(250, 250, 250));
        participantsList.setBorder(new EmptyBorder(5, 5, 5, 5));
        participantsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane listScroll = new JScrollPane(participantsList);
        listScroll.setPreferredSize(new Dimension(250, 180));
        listScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        // Bouton supprimer
        JButton btnSupprimerParticipant = new JButton("🗑️ Retirer");
        btnSupprimerParticipant.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnSupprimerParticipant.setBackground(new Color(244, 67, 54));
        btnSupprimerParticipant.setForeground(Color.WHITE);
        btnSupprimerParticipant.setFocusPainted(false);
        btnSupprimerParticipant.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSupprimerParticipant.setPreferredSize(new Dimension(80, 25));
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(btnSupprimerParticipant);
        
        participantsPanel.add(inputPanel, BorderLayout.NORTH);
        participantsPanel.add(listScroll, BorderLayout.CENTER);
        participantsPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        formPanel.add(participantsPanel, gbc);
        
        // Actions des boutons
        btnAjouterParticipant.addActionListener(e -> {
            String nom = participantField.getText().trim();
            if (!nom.isEmpty() && !participantsListModel.contains(nom)) {
                participantsListModel.addElement(nom);
                participantField.setText("");
                participantField.requestFocus();
            }
        });
        
        participantField.addActionListener(e -> btnAjouterParticipant.doClick());
        
        btnSupprimerParticipant.addActionListener(e -> {
            int selectedIndex = participantsList.getSelectedIndex();
            if (selectedIndex != -1) {
                participantsListModel.remove(selectedIndex);
            }
        });

        // Description
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblDesc = new JLabel("📝 DESCRIPTION :");
        lblDesc.setFont(new Font("Arial", Font.BOLD, 13));
        lblDesc.setForeground(new Color(101, 67, 33));
        formPanel.add(lblDesc, gbc);

        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setPreferredSize(new Dimension(300, 70));
        formPanel.add(descScroll, gbc);

        // Si mode édition, pré-remplir les champs
        if (modeEdition && evenement != null) {
            titreField.setText(evenement.getTitre());
            
            if (evenement.getDate() != null) {
                Date date = Date.from(evenement.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
                dateSpinner.setValue(date);
            }
            
            if (evenement.getHeure() != null && !evenement.getHeure().isEmpty()) {
                heureField.setText(evenement.getHeure());
            }
            
            if (evenement.getResponsable() != null && !evenement.getResponsable().isEmpty()) {
                responsableCombo.setSelectedItem(evenement.getResponsable());
            }
            
            if (evenement.getDescription() != null) {
                descriptionArea.setText(evenement.getDescription());
            }
            
            // Charger les participants
            if (evenement.getParticipants() != null && !evenement.getParticipants().isEmpty()) {
                for (String participant : evenement.getParticipants()) {
                    participantsListModel.addElement(participant);
                }
            }
        }

        // PANNEAU BOUTONS
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(new Color(245, 245, 245));

        if (modeEdition) {
            // Mode édition : Modifier, Supprimer, Annuler
            JButton btnModifier = creerBouton("✔️ VALIDER", new Color(101, 67, 33), new Color(255, 248, 220));
            btnModifier.addActionListener(e -> validerFormulaire());

            JButton btnSupprimer = creerBouton("🗑️ Supprimer", new Color(244, 67, 54), Color.WHITE);
            btnSupprimer.addActionListener(e -> {
                int choix = JOptionPane.showConfirmDialog(
                    this,
                    "Voulez-vous vraiment supprimer cet événement ?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                if (choix == JOptionPane.YES_OPTION) {
                    supprime = true;
                    valide = true;
                    dispose();
                }
            });

            JButton btnAnnuler = creerBouton("✕ Annuler", new Color(158, 158, 158), Color.WHITE);
            btnAnnuler.addActionListener(e -> dispose());

            btnPanel.add(btnModifier);
            btnPanel.add(btnSupprimer);
            btnPanel.add(btnAnnuler);
        } else {
            // Mode ajout : Ajouter, Annuler
            JButton btnAjouter = creerBouton("➕ AJOUTER", new Color(101, 67, 33), new Color(255, 248, 220));
            btnAjouter.addActionListener(e -> validerFormulaire());

            JButton btnAnnuler = creerBouton("✕ Annuler", new Color(158, 158, 158), Color.WHITE);
            btnAnnuler.addActionListener(e -> dispose());

            btnPanel.add(btnAjouter);
            btnPanel.add(btnAnnuler);
        }

        // Assemblage
        mainPanel.add(titreDialog, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JButton creerBouton(String texte, Color bgColor, Color fgColor) {
        JButton btn = new JButton(texte);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(130, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void validerFormulaire() {
        // Réinitialiser les bordures
        titreField.setBorder(UIManager.getBorder("TextField.border"));
        dateSpinner.setBorder(UIManager.getBorder("Spinner.border"));
        heureField.setBorder(UIManager.getBorder("TextField.border"));

        // Validation
        if (titreField.getText().trim().isEmpty()) {
            titreField.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            JOptionPane.showMessageDialog(this, 
                "Le titre est obligatoire.", 
                "Erreur de validation", 
                JOptionPane.ERROR_MESSAGE);
            titreField.requestFocus();
            return;
        }

        if (heureField.getText().trim().isEmpty()) {
            heureField.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            JOptionPane.showMessageDialog(this, 
                "L'heure est obligatoire.", 
                "Erreur de validation", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (responsableCombo.getSelectedItem() == null) {
            responsableCombo.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            JOptionPane.showMessageDialog(this, 
                "Le responsable est obligatoire.", 
                "Erreur de validation", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Tout est valide
        valide = true;
        dispose();
        
        // Message de succès
        String message = modeEdition ? "Événement modifié avec succès !" : "Événement ajouté avec succès !";
        JOptionPane.showMessageDialog(getParent(), 
            message, 
            "Succès", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    public boolean estValide() {
        return valide;
    }

    public boolean estSupprime() {
        return supprime;
    }

    public Agenda getEvenement() {
        Agenda evt = modeEdition ? evenementOriginal : new Agenda();
        evt.setTitre(titreField.getText().trim());
        
        // Conversion de la date
        Date dateValue = (Date) dateSpinner.getValue();
        LocalDate localDate = dateValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        evt.setDate(localDate);
        
        evt.setHeure(normaliserHeure(heureField.getText().trim()));
        evt.setDescription(descriptionArea.getText().trim());
        
        evt.setResponsable((String) responsableCombo.getSelectedItem());
        
        // Récupérer les participants depuis la liste
        ArrayList<String> participantsSelectionnes = new ArrayList<>();
        for (int i = 0; i < participantsListModel.getSize(); i++) {
            participantsSelectionnes.add(participantsListModel.getElementAt(i));
        }
        evt.setParticipants(participantsSelectionnes);
        
        return evt;
    }

    // Convertit des formats libres (ex: "12.00", "15h15", "15:5", "9") en "HH:mm" si possible
    private String normaliserHeure(String input) {
        if (input == null) return "";
        String s = input.trim();
        if (s.isEmpty()) return s;
        // Uniformiser séparateurs
        s = s.replace('H', 'h');
        s = s.replace('.', ':');
        s = s.replace("h", ":");
        s = s.replace(" ", "");
        // Compléter minutes si absentes
        if (s.matches("^\\d{1,2}$")) {
            s = s + ":00";
        } else if (s.matches("^\\d{1,2}:$")) {
            s = s + "00";
        }
        // Valider et formater
        if (s.matches("^\\d{1,2}:\\d{1,2}$")) {
            try {
                String[] parts = s.split(":");
                int h = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                if (h < 0 || h > 23 || m < 0 || m > 59) return input.trim();
                return String.format("%02d:%02d", h, m);
            } catch (NumberFormatException ex) {
                return input.trim();
            }
        }
        return input.trim();
    }
}
