package pharmacie.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import pharmacie.model.Categorie;
import pharmacie.model.Medicament;

public class MedicamentDAO implements CrudDAO<Medicament> {

    private static final String SELECT =
            "SELECT m.*, c.libelle AS categorie FROM Medicament m "
          + "JOIN Categorie c ON c.id_categorie = m.id_categorie ";

    @Override
    public boolean ajouter(Medicament m) throws SQLException {
        String sql = "INSERT INTO Medicament (code_barre, nom, forme, dosage, photo, prix_unitaire, "
                   + "quantite, seuil_min, date_expiration, ordonnance_requise, id_categorie) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DaoUtil.preparerAvecCle(sql)) {
            remplir(ps, m);
            boolean ok = ps.executeUpdate() == 1;
            m.setId(DaoUtil.cleGeneree(ps));
            return ok;
        }
    }

    @Override
    public Medicament trouverParId(int id) throws SQLException {
        return premier(lister(SELECT + "WHERE m.id_medicament = ?", id));
    }

    @Override
    public List<Medicament> listerTous() throws SQLException {
        return lister(SELECT + "ORDER BY m.nom");
    }

    @Override
    public boolean modifier(Medicament m) throws SQLException {
        String sql = "UPDATE Medicament SET code_barre = ?, nom = ?, forme = ?, dosage = ?, photo = ?, "
                   + "prix_unitaire = ?, quantite = ?, seuil_min = ?, date_expiration = ?, "
                   + "ordonnance_requise = ?, id_categorie = ? WHERE id_medicament = ?";
        try (PreparedStatement ps = DaoUtil.preparer(sql)) {
            remplir(ps, m);
            ps.setInt(12, m.getId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean supprimer(int id) throws SQLException {
        try (PreparedStatement ps = DaoUtil.preparer("DELETE FROM Medicament WHERE id_medicament = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    // ---- Méthodes propres au menu ----

    /** Rubrique « Stocks disponibles » (vue v_StocksDisponibles). */
    public List<Medicament> listerDisponibles() throws SQLException {
        return lister("SELECT * FROM v_StocksDisponibles ORDER BY nom");
    }

    /** Rubrique « Produits indisponibles » (vue v_ProduitsIndisponibles). */
    public List<Medicament> listerIndisponibles() throws SQLException {
        return lister("SELECT * FROM v_ProduitsIndisponibles ORDER BY nom");
    }

    public List<Medicament> listerStockBas() throws SQLException {
        return lister(SELECT + "WHERE m.quantite > 0 AND m.quantite <= m.seuil_min ORDER BY m.quantite");
    }

    public List<Medicament> listerExpirantBientot(int jours) throws SQLException {
        return lister(SELECT + "WHERE m.date_expiration < DATEADD(DAY, ?, CAST(GETDATE() AS DATE)) "
                    + "ORDER BY m.date_expiration", jours);
    }

    /** Recherche parmi les médicaments disponibles, avec filtre facultatif sur la catégorie. */
    public List<Medicament> rechercherDisponibles(String texte, Categorie categorie) throws SQLException {
        if (categorie == null) {
            return lister(SELECT + "WHERE m.quantite > 0 AND m.nom LIKE ? ORDER BY m.nom", DaoUtil.motif(texte));
        }
        return lister(SELECT + "WHERE m.quantite > 0 AND m.nom LIKE ? AND m.id_categorie = ? ORDER BY m.nom",
                DaoUtil.motif(texte), categorie.getId());
    }

    public List<Medicament> rechercherParNom(String texte) throws SQLException {
        return lister(SELECT + "WHERE m.nom LIKE ? ORDER BY m.nom", DaoUtil.motif(texte));
    }

    /** Ajoute delta à la quantité (delta négatif pour une vente). */
    public boolean modifierQuantite(int id, int delta) throws SQLException {
        String sql = "UPDATE Medicament SET quantite = quantite + ? WHERE id_medicament = ?";
        try (PreparedStatement ps = DaoUtil.preparer(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        }
    }

    /** Date de la dernière vente du médicament, ou null s'il n'a jamais été vendu. */
    public LocalDateTime derniereVente(int idMedicament) throws SQLException {
        String sql = "SELECT MAX(v.date_vente) AS derniere FROM LigneVente l "
                   + "JOIN Vente v ON v.id_vente = l.id_vente WHERE l.id_medicament = ?";
        try (PreparedStatement ps = DaoUtil.preparer(sql)) {
            ps.setInt(1, idMedicament);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? DaoUtil.dateHeure(rs, "derniere") : null;
            }
        }
    }

    public int compterDisponibles() throws SQLException {
        return compter("SELECT COUNT(*) FROM Medicament WHERE quantite > 0");
    }

    public int compterIndisponibles() throws SQLException {
        return compter("SELECT COUNT(*) FROM Medicament WHERE quantite = 0");
    }

    // ---- Outils internes ----

    private void remplir(PreparedStatement ps, Medicament m) throws SQLException {
        ps.setString(1, vide(m.getCodeBarre()));
        ps.setString(2, m.getNom());
        ps.setString(3, m.getForme());
        ps.setString(4, m.getDosage());
        DaoUtil.setPhoto(ps, 5, m.getPhoto());
        ps.setBigDecimal(6, m.getPrixUnitaire());
        ps.setInt(7, m.getQuantite());
        ps.setInt(8, m.getSeuilMin());
        DaoUtil.setDate(ps, 9, m.getDateExpiration());
        ps.setBoolean(10, m.isOrdonnanceRequise());
        ps.setInt(11, m.getCategorie().getId());
    }

    /** Un code-barres vide est enregistré NULL pour respecter la contrainte UNIQUE. */
    private String vide(String texte) {
        return texte == null || texte.isBlank() ? null : texte.trim();
    }

    private List<Medicament> lister(String sql, Object... params) throws SQLException {
        List<Medicament> liste = new ArrayList<>();
        try (PreparedStatement ps = DaoUtil.preparer(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(lire(rs));
                }
            }
        }
        return liste;
    }

    private Medicament premier(List<Medicament> liste) {
        return liste.isEmpty() ? null : liste.get(0);
    }

    private int compter(String sql) throws SQLException {
        try (PreparedStatement ps = DaoUtil.preparer(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Medicament lire(ResultSet rs) throws SQLException {
        Medicament m = new Medicament();
        m.setId(rs.getInt("id_medicament"));
        m.setCodeBarre(rs.getString("code_barre"));
        m.setNom(rs.getString("nom"));
        m.setForme(rs.getString("forme"));
        m.setDosage(rs.getString("dosage"));
        m.setPhoto(rs.getBytes("photo"));
        m.setPrixUnitaire(rs.getBigDecimal("prix_unitaire"));
        m.setQuantite(rs.getInt("quantite"));
        m.setSeuilMin(rs.getInt("seuil_min"));
        LocalDate exp = DaoUtil.date(rs, "date_expiration");
        m.setDateExpiration(exp);
        m.setOrdonnanceRequise(rs.getBoolean("ordonnance_requise"));
        m.setCategorie(new Categorie(rs.getInt("id_categorie"), rs.getString("categorie")));
        return m;
    }
}
