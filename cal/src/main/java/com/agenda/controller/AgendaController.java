package com.agenda.controller;

import com.agenda.model.Agenda;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AgendaController {
    private final List<Agenda> evenements = new ArrayList<>();

    public void ajouterEvenement(Agenda evt) {
        evenements.add(evt);
    }

    public void supprimerEvenement(LocalDate date, String titre) {
        evenements.removeIf(e -> e.getDate().equals(date) && e.getTitre().equalsIgnoreCase(titre));
    }

    public List<Agenda> getEvenements() {
        return new ArrayList<>(evenements);
    }

    public List<Agenda> getEvenementsDuJour(LocalDate date) {
        List<Agenda> duJour = new ArrayList<>();
        for (Agenda evt : evenements) {
            if (evt.getDate().equals(date)) {
                duJour.add(evt);
            }
        }
        duJour.sort((a, b) -> {
            String h1 = a.getHeure() != null ? a.getHeure() : "00:00";
            String h2 = b.getHeure() != null ? b.getHeure() : "00:00";
            return h1.compareTo(h2);
        });
        return duJour;
    }

    /**
     * Modifie un événement identifié par sa date et son titre.
     * Retourne true si un événement a été trouvé et modifié.
     */
    public boolean modifierEvenement(LocalDate ancienneDate, String ancienTitre, Agenda evenementMisAJour) {
        // 1) Cas nominal: retrouver par ancienne date+titre
        for (Agenda e : evenements) {
            if (e.getDate() != null && e.getDate().equals(ancienneDate) && e.getTitre() != null && e.getTitre().equalsIgnoreCase(ancienTitre)) {
                e.setTitre(evenementMisAJour.getTitre());
                e.setDate(evenementMisAJour.getDate());
                e.setHeure(evenementMisAJour.getHeure());
                e.setResponsable(evenementMisAJour.getResponsable());
                e.setDescription(evenementMisAJour.getDescription());
                e.setParticipants(evenementMisAJour.getParticipants());
                return true;
            }
        }

        // 2) Fallback: si le formulaire a modifié l'objet original (même référence),
        // alors l'événement est déjà à jour; retourner true pour ne pas considérer l'opération échouée
        for (Agenda e : evenements) {
            if (e == evenementMisAJour) {
                return true;
            }
        }

        // 3) Fallback supplémentaire: si l'événement mis à jour correspond exactement à
        // un élément de la liste par valeur (date+titre), considérer réussi
        for (Agenda e : evenements) {
            if (e.getDate() != null && e.getTitre() != null &&
                evenementMisAJour.getDate() != null && evenementMisAJour.getTitre() != null &&
                e.getDate().equals(evenementMisAJour.getDate()) && e.getTitre().equalsIgnoreCase(evenementMisAJour.getTitre())) {
                // Les champs étant déjà modifiés via la référence ou lors du formulaire,
                // on s'assure que les autres attributs sont cohérents
                e.setHeure(evenementMisAJour.getHeure());
                e.setResponsable(evenementMisAJour.getResponsable());
                e.setDescription(evenementMisAJour.getDescription());
                e.setParticipants(evenementMisAJour.getParticipants());
                return true;
            }
        }

        return false;
    }
}