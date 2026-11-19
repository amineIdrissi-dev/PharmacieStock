package pharmacie.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import pharmacie.model.Role;
import pharmacie.model.Utilisateur;

public class UtilisateurDAO implements CrudDAO<Utilisateur> {

    @Override
    public boolean ajouter(Utilisateur u) throws SQLException {
        String sql = "INSERT INTO Utilisateur (nom, prenom, login, mot_de_passe_hash, photo, role, actif) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DaoUtil.preparerAvecCle(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getLogin());
            ps.setString(4, u.getMotDePasseHash());
            DaoUtil.setPhoto(ps, 5, u.getPhoto());
            ps.setString(6, u.getRole().name());
            ps.setBoolean(7, u.isActif());
            boolean ok = ps.executeUpdate() == 1;
            u.setId(DaoUtil.cleGeneree(ps));
            return ok;
        }
    }

    @Override
    public Utilisateur trouverParId(int id) throws SQLException {
        try (PreparedStatement ps = DaoUtil.preparer("SELECT * FROM Utilisateur WHERE id_utilisateur = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? lire(rs) : null;
            }
        }
    }

    @Override
    public List<Utilisateur> listerTous() throws SQLException {
        List<Utilisateur> liste = new ArrayList<>();
        try (PreparedStatement ps = DaoUtil.preparer("SELECT * FROM Utilisateur ORDER BY nom, prenom");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(lire(rs));
            }
        }
        return liste;
    }

    /** Met à jour les informations ; le mot de passe n'est changé que s'il est renseigné. */
    @Override
    public boolean modifier(Utilisateur u) throws SQLException {
        boolean avecMotDePasse = u.getMotDePasseHash() != null && !u.getMotDePasseHash().isBlank();
        String sql = "UPDATE Utilisateur SET nom = ?, prenom = ?, login = ?, photo = ?, role = ?, actif = ?"
                   + (avecMotDePasse ? ", mot_de_passe_hash = ?" : "")
                   + " WHERE id_utilisateur = ?";
        try (PreparedStatement ps = DaoUtil.preparer(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getLogin());
            DaoUtil.setPhoto(ps, 4, u.getPhoto());
            ps.setString(5, u.getRole().name());
            ps.setBoolean(6, u.isActif());
            int i = 7;
            if (avecMotDePasse) {
                ps.setString(i++, u.getMotDePasseHash());
            }
            ps.setInt(i, u.getId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean supprimer(int id) throws SQLException {
        try (PreparedStatement ps = DaoUtil.preparer("DELETE FROM Utilisateur WHERE id_utilisateur = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    /** Retourne l'utilisateur actif correspondant, ou null si les identifiants sont faux. */
    public Utilisateur authentifier(String login, String motDePasseHash) throws SQLException {
        String sql = "SELECT * FROM Utilisateur WHERE login = ? AND mot_de_passe_hash = ? AND actif = 1";
        try (PreparedStatement ps = DaoUtil.preparer(sql)) {
            ps.setString(1, login);
            ps.setString(2, motDePasseHash);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? lire(rs) : null;
            }
        }
    }

    public boolean changerStatut(int id, boolean actif) throws SQLException {
        try (PreparedStatement ps = DaoUtil.preparer("UPDATE Utilisateur SET actif = ? WHERE id_utilisateur = ?")) {
            ps.setBoolean(1, actif);
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        }
    }

    private Utilisateur lire(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id_utilisateur"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setLogin(rs.getString("login"));
        u.setMotDePasseHash(rs.getString("mot_de_passe_hash"));
        u.setPhoto(rs.getBytes("photo"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setActif(rs.getBoolean("actif"));
        u.setDateCreation(DaoUtil.dateHeure(rs, "date_creation"));
        return u;
    }
}
