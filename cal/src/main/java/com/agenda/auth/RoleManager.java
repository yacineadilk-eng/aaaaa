package com.agenda.auth;

import java.util.Arrays;
import java.util.List;

public class RoleManager {
    public enum Role { VISITOR, ADMIN }

    private static Role currentRole = Role.VISITOR;
    private static String currentUser = "Visiteur";

    // Liste des admins autorisés
        public static final List<String> ADMIN_LIST = Arrays.asList(
            "Ali Paychen",
            "Yacine Kadaoui",
            "Manel Ikene",
            "Zaza Khlifaoui"
        );

    public static void set(Role role, String user) {
        currentRole = role;
        currentUser = user != null ? user : "Visiteur";
    }

    public static boolean isAdmin() {
        return currentRole == Role.ADMIN;
    }

    public static boolean isVisitor() {
        return currentRole == Role.VISITOR;
    }

    public static String getCurrentUser() {
        return currentUser;
    }
}
