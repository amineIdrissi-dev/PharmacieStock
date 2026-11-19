package pharmacie.dao;

import java.sql.SQLException;
import java.util.List;

/** Les 5 opérations CRUD communes à toutes les classes DAO. */
public interface CrudDAO<T> {
    boolean ajouter(T objet) throws SQLException;      // Create
    T trouverParId(int id) throws SQLException;        // Read (un)
    List<T> listerTous() throws SQLException;          // Read (tous)
    boolean modifier(T objet) throws SQLException;     // Update
    boolean supprimer(int id) throws SQLException;     // Delete
}
