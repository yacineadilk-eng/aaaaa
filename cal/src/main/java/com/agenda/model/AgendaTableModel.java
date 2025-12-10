package com.agenda.model;

import com.agenda.controller.AgendaController;
import com.agenda.view.AgendaView;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.util.List;

public class AgendaTableModel extends AbstractTableModel {
    private final String[] colonnes = {"Titre", "Date", "Heure", "Responsable", "Participants", "Description"};
    private List<Agenda> evenements;
    private final AgendaController controller;
    private final AgendaView view;

    // Constructeur avec controller et view pour synchronisation
    public AgendaTableModel(List<Agenda> evenements, AgendaController controller, AgendaView view) {
        this.evenements = evenements;
        this.controller = controller;
        this.view = view;
    }

    // Constructeur simple pour AgendaPanel
    public AgendaTableModel(List<Agenda> evenements) {
        this.evenements = evenements;
        this.controller = null;
        this.view = null;
    }

    @Override
    public int getRowCount() {
        return evenements.size();
    }

    @Override
    public int getColumnCount() {
        return colonnes.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Agenda evt = evenements.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> evt.getTitre();
            case 1 -> evt.getDate();
            case 2 -> evt.getHeure();
            case 3 -> evt.getResponsable();
            case 4 -> {
                // Formater la liste des participants
                if (evt.getParticipants() == null || evt.getParticipants().isEmpty()) {
                    yield "Aucun";
                }
                yield String.join(", ", evt.getParticipants());
            }
            case 5 -> evt.getDescription();
            default -> null;
        };
    }

    @Override
    public String getColumnName(int column) {
        return colonnes[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // La colonne Participants (index 4) n'est pas éditable directement
        return columnIndex != 4;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Agenda evt = evenements.get(rowIndex);
        switch (columnIndex) {
            case 0 -> evt.setTitre(aValue.toString());
            case 1 -> evt.setDate(LocalDate.parse(aValue.toString()));
            case 2 -> evt.setHeure(aValue.toString());
            case 3 -> evt.setResponsable(aValue.toString());
            case 4 -> {} // Participants non-éditable directement depuis le tableau
            case 5 -> evt.setDescription(aValue.toString());
        }
        fireTableCellUpdated(rowIndex, columnIndex);
        if (view != null) {
            view.actualiserToutesLesVues();
        }
    }

    public void actualiser(List<Agenda> nouvelleListe) {
        this.evenements = nouvelleListe;
        fireTableDataChanged();
    }
    
    public List<Agenda> getEvenements() {
        return evenements;
    }
}