package com.agenda;

import com.agenda.view.AgendaView;
import com.agenda.view.RoleDialog;
import javax.swing.SwingUtilities;

public class AgendaCollaboratif {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RoleDialog dlg = new RoleDialog(null);
            dlg.setVisible(true);
            // Continuer même si annulation: défaut VISITOR
            new AgendaView();
        });
    }
}