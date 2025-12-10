package com.agenda.model;

import java.time.LocalDate;
import java.util.ArrayList;

public class Agenda {
    private String titre;
    private LocalDate date;
    private String heure;
    private String responsable;
    private String description;
    private ArrayList<String> participants;

    public Agenda() {
        this.participants = new ArrayList<>();
    }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getHeure() { return heure; }
    public void setHeure(String heure) { this.heure = heure; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ArrayList<String> getParticipants() { return participants; }
    public void setParticipants(ArrayList<String> participants) { this.participants = participants; }
}