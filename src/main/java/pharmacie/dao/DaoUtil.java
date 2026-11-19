package pharmacie.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;

import pharmacie.util.ConnexionBD;

/** Petites fonctions partagées par les DAO. */
final class DaoUtil {

    private DaoUtil() {}

    static Connection cn() throws SQLException {
        return ConnexionBD.getConnexion();
    }

    static PreparedStatement preparer(String sql) throws SQLException {
        return cn().prepareStatement(sql);
    }

    static PreparedStatement preparerAvecCle(String sql) throws SQLException {
        return cn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }

    /** Lit l'identifiant généré par un INSERT (colonne IDENTITY). */
    static int cleGeneree(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    static LocalDateTime dateHeure(ResultSet rs, String colonne) throws SQLException {
        Timestamp t = rs.getTimestamp(colonne);
        return t == null ? null : t.toLocalDateTime();
    }

    static LocalDate date(ResultSet rs, String colonne) throws SQLException {
        Date d = rs.getDate(colonne);
        return d == null ? null : d.toLocalDate();
    }

    static void setDate(PreparedStatement ps, int index, LocalDate valeur) throws SQLException {
        if (valeur == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(valeur));
        }
    }

    static void setPhoto(PreparedStatement ps, int index, byte[] photo) throws SQLException {
        if (photo == null) {
            ps.setNull(index, Types.VARBINARY);
        } else {
            ps.setBytes(index, photo);
        }
    }

    static void setEntierOuNull(PreparedStatement ps, int index, Integer valeur) throws SQLException {
        if (valeur == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, valeur);
        }
    }

    /** Transforme un texte de recherche en motif LIKE. */
    static String motif(String texte) {
        return "%" + (texte == null ? "" : texte.trim()) + "%";
    }
}
