package pharmacie.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import pharmacie.model.LigneVente;
import pharmacie.model.Medicament;

public class LigneVenteDAO implements CrudDAO<LigneVente> {

    private static final String SELECT =
            "SELECT l.*, m.nom, m.dosage FROM LigneVente l "
          + "JOIN Medicament m ON m.id_medicament = l.id_medicament ";

    @Override
    public boolean ajouter(LigneVente l) throws SQLException {
        String sql = "INSERT INTO LigneVente (id_vente, id_medicament, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DaoUtil.preparerAvecCle(sql)) {
            ps.setInt(1, l.getIdVente());
            ps.setInt(2, l.getMedicament().getId());
            ps.setInt(3, l.getQuantite());
            ps.setBigDecimal(4, l.getPrixUnitaire());
            boolean ok = ps.executeUpdate() == 1;
            l.setId(DaoUtil.cleGeneree(ps));
            return ok;
        }
    }

    @Override
    public LigneVente trouverParId(int id) throws SQLException {
        List<LigneVente> liste = lister(SELECT + "WHERE l.id_ligne = ?", id);
        return liste.isEmpty() ? null : liste.get(0);
    }

    @Override
    public List<LigneVente> listerTous() throws SQLException {
        return lister(SELECT + "ORDER BY l.id_vente, l.id_ligne");
    }

    @Override
    public boolean modifier(LigneVente l) throws SQLException {
        String sql = "UPDATE LigneVente SET quantite = ?, prix_unitaire = ? WHERE id_ligne = ?";
        try (PreparedStatement ps = DaoUtil.preparer(sql)) {
            ps.setInt(1, l.getQuantite());
            ps.setBigDecimal(2, l.getPrixUnitaire());
            ps.setInt(3, l.getId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean supprimer(int id) throws SQLException {
        try (PreparedStatement ps = DaoUtil.preparer("DELETE FROM LigneVente WHERE id_ligne = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    public List<LigneVente> listerParVente(int idVente) throws SQLException {
        return lister(SELECT + "WHERE l.id_vente = ? ORDER BY l.id_ligne", idVente);
    }

    private List<LigneVente> lister(String sql, Object... params) throws SQLException {
        List<LigneVente> liste = new ArrayList<>();
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

    private LigneVente lire(ResultSet rs) throws SQLException {
        Medicament m = new Medicament();
        m.setId(rs.getInt("id_medicament"));
        m.setNom(rs.getString("nom"));
        m.setDosage(rs.getString("dosage"));

        LigneVente l = new LigneVente();
        l.setId(rs.getInt("id_ligne"));
        l.setIdVente(rs.getInt("id_vente"));
        l.setMedicament(m);
        l.setQuantite(rs.getInt("quantite"));
        l.setPrixUnitaire(rs.getBigDecimal("prix_unitaire"));
        return l;
    }
}
