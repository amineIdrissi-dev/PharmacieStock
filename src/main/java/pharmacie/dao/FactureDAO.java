package pharmacie.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import pharmacie.model.Facture;
import pharmacie.model.Vente;

public class FactureDAO implements CrudDAO<Facture> {

    @Override
    public boolean ajouter(Facture f) throws SQLException {
        String sql = "INSERT INTO Facture (numero_facture, montant_ht, taux_tva, montant_ttc, mode_paiement, statut, id_vente) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DaoUtil.preparerAvecCle(sql)) {
            ps.setString(1, f.getNumero());
            ps.setBigDecimal(2, f.getMontantHt());
            ps.setBigDecimal(3, f.getTauxTva());
            ps.setBigDecimal(4, f.getMontantTtc());
            ps.setString(5, f.getModePaiement());
            ps.setString(6, f.getStatut());
            ps.setInt(7, f.getVente().getId());
            boolean ok = ps.executeUpdate() == 1;
            f.setId(DaoUtil.cleGeneree(ps));
            return ok;
        }
    }

    @Override
    public Facture trouverParId(int id) throws SQLException {
        return premier(lister("SELECT * FROM Facture WHERE id_facture = ?", id));
    }

    @Override
    public List<Facture> listerTous() throws SQLException {
        return lister("SELECT * FROM Facture ORDER BY date_facture DESC");
    }

    /** Seuls le mode de paiement et le statut sont modifiables : le numéro et les montants sont figés. */
    @Override
    public boolean modifier(Facture f) throws SQLException {
        try (PreparedStatement ps = DaoUtil.preparer(
                "UPDATE Facture SET mode_paiement = ?, statut = ? WHERE id_facture = ?")) {
            ps.setString(1, f.getModePaiement());
            ps.setString(2, f.getStatut());
            ps.setInt(3, f.getId());
            return ps.executeUpdate() == 1;
        }
    }

    /** Une facture ne se supprime pas (règle de gestion) : utiliser annuler(). */
    @Override
    public boolean supprimer(int id) throws SQLException {
        throw new UnsupportedOperationException("Une facture ne se supprime pas : elle s'annule.");
    }

    public Facture trouverParVente(int idVente) throws SQLException {
        return premier(lister("SELECT * FROM Facture WHERE id_vente = ?", idVente));
    }

    public Facture trouverParNumero(String numero) throws SQLException {
        return premier(lister("SELECT * FROM Facture WHERE numero_facture = ?", numero));
    }

    /** Numéro suivant pour l'année : FAC-2026-00001, FAC-2026-00002... */
    public String genererNumero(int annee) throws SQLException {
        String prefixe = "FAC-" + annee + "-";
        try (PreparedStatement ps = DaoUtil.preparer(
                "SELECT MAX(numero_facture) FROM Facture WITH (UPDLOCK, HOLDLOCK) WHERE numero_facture LIKE ?")) {
            ps.setString(1, prefixe + "%");
            try (ResultSet rs = ps.executeQuery()) {
                int dernier = 0;
                if (rs.next() && rs.getString(1) != null) {
                    dernier = Integer.parseInt(rs.getString(1).substring(prefixe.length()));
                }
                return String.format("%s%05d", prefixe, dernier + 1);
            }
        }
    }

    public List<Facture> listerParPeriode(LocalDate debut, LocalDate fin) throws SQLException {
        return lister("SELECT * FROM Facture WHERE date_facture >= ? AND date_facture < ? ORDER BY date_facture DESC",
                Timestamp.valueOf(debut.atStartOfDay()), Timestamp.valueOf(fin.plusDays(1).atStartOfDay()));
    }

    public boolean annuler(int id) throws SQLException {
        try (PreparedStatement ps = DaoUtil.preparer("UPDATE Facture SET statut = 'ANNULEE' WHERE id_facture = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private Facture premier(List<Facture> liste) {
        return liste.isEmpty() ? null : liste.get(0);
    }

    private List<Facture> lister(String sql, Object... params) throws SQLException {
        List<Facture> liste = new ArrayList<>();
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

    private Facture lire(ResultSet rs) throws SQLException {
        Facture f = new Facture();
        f.setId(rs.getInt("id_facture"));
        f.setNumero(rs.getString("numero_facture"));
        f.setDateFacture(DaoUtil.dateHeure(rs, "date_facture"));
        f.setMontantHt(rs.getBigDecimal("montant_ht"));
        f.setTauxTva(rs.getBigDecimal("taux_tva"));
        f.setMontantTtc(rs.getBigDecimal("montant_ttc"));
        f.setModePaiement(rs.getString("mode_paiement"));
        f.setStatut(rs.getString("statut"));
        Vente v = new Vente();
        v.setId(rs.getInt("id_vente"));
        f.setVente(v);
        return f;
    }
}
