package pharmacie.service;

import java.sql.SQLException;

import pharmacie.dao.UtilisateurDAO;
import pharmacie.model.Utilisateur;
import pharmacie.util.HashUtil;

/** Connexion / déconnexion et utilisateur connecté (session de l'application). */
public class AuthService {
    private static Utilisateur utilisateurConnecte;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    /** Retourne l'utilisateur si les identifiants sont bons, sinon null. */
    public Utilisateur connecter(String login, String motDePasse) throws SQLException {
        Utilisateur u = utilisateurDAO.authentifier(login.trim(), HashUtil.sha256(motDePasse));
        utilisateurConnecte = u;
        return u;
    }

    public static void deconnecter() {
        utilisateurConnecte = null;
    }

    public static Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public static boolean estAdmin() {
        return utilisateurConnecte != null && utilisateurConnecte.estAdmin();
    }
}
