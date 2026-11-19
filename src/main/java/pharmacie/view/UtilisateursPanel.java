package pharmacie.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import pharmacie.dao.UtilisateurDAO;
import pharmacie.model.Utilisateur;
import pharmacie.service.AuthService;

/** Gestion des comptes (administrateur uniquement). */
public class UtilisateursPanel extends JPanel implements Rafraichissable {
    private final DefaultTableModel modele = UiUtil.modele("Photo", "Nom", "Prénom", "Login", "Rôle", "Actif", "Créé le");
    private final List<Utilisateur> utilisateurs = new ArrayList<>();
    private final JTable table = UiUtil.tableColoree(modele, i -> utilisateurs.get(i).isActif() ? null : UiUtil.ROUGE);
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    public UtilisateursPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        table.setRowHeight(48);

        JPanel bas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bas.add(UiUtil.bouton("Ajouter", e -> editer(null)));
        bas.add(UiUtil.bouton("Modifier", e -> {
            Utilisateur u = selection();
            if (u != null) {
                editer(u);
            }
        }));
        bas.add(UiUtil.bouton("Activer / désactiver", e -> changerStatut()));
        bas.add(UiUtil.bouton("Supprimer", e -> supprimer()));

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bas, BorderLayout.SOUTH);
    }

    @Override
    public void rafraichir() {
        try {
            utilisateurs.clear();
            utilisateurs.addAll(utilisateurDAO.listerTous());
            modele.setRowCount(0);
            for (Utilisateur u : utilisateurs) {
                modele.addRow(new Object[] {
                    UiUtil.icone(u.getPhoto(), 44), u.getNom(), u.getPrenom(), u.getLogin(),
                    u.estAdmin() ? "Administrateur" : "Employé", u.isActif() ? "Oui" : "Non",
                    UiUtil.date(u.getDateCreation())
                });
            }
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }

    private Utilisateur selection() {
        int i = table.getSelectedRow();
        if (i < 0) {
            UiUtil.info(this, "Sélectionnez d'abord un utilisateur.");
            return null;
        }
        return utilisateurs.get(i);
    }

    private boolean estMoi(Utilisateur u) {
        return u.getId() == AuthService.getUtilisateurConnecte().getId();
    }

    private void editer(Utilisateur u) {
        if (new UtilisateurDialog(SwingUtilities.getWindowAncestor(this), u).afficher()) {
            rafraichir();
        }
    }

    private void changerStatut() {
        Utilisateur u = selection();
        if (u == null) {
            return;
        }
        if (estMoi(u)) {
            UiUtil.info(this, "Vous ne pouvez pas désactiver votre propre compte.");
            return;
        }
        try {
            utilisateurDAO.changerStatut(u.getId(), !u.isActif());
            rafraichir();
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }

    private void supprimer() {
        Utilisateur u = selection();
        if (u == null) {
            return;
        }
        if (estMoi(u)) {
            UiUtil.info(this, "Vous ne pouvez pas supprimer votre propre compte.");
            return;
        }
        if (!UiUtil.confirmer(this, "Supprimer le compte " + u.getLogin()
                + " ?\n(Si ce compte a déjà fait des ventes, désactivez-le plutôt.)")) {
            return;
        }
        try {
            utilisateurDAO.supprimer(u.getId());
            rafraichir();
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }
}
