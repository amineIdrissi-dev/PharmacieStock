package pharmacie.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import pharmacie.dao.AcheteurDAO;
import pharmacie.model.Acheteur;
import pharmacie.model.Facture;
import pharmacie.model.Vente;
import pharmacie.service.AuthService;
import pharmacie.service.FactureService;

/** Rubrique « Acheteurs » : tableau des clients et historique de leurs achats. */
public class AcheteursPanel extends JPanel implements Rafraichissable {
    private final DefaultTableModel modele = UiUtil.modele(
            "Nom", "Prénom", "Téléphone", "Email", "Adresse", "Inscription", "Achats");
    private final List<Acheteur> acheteurs = new ArrayList<>();
    private final JTable table = UiUtil.table(modele);
    private final JTextField recherche = new JTextField(18);
    private final AcheteurDAO acheteurDAO = new AcheteurDAO();

    public AcheteursPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel haut = new JPanel(new FlowLayout(FlowLayout.LEFT));
        haut.add(new JLabel("Rechercher (nom, prénom, téléphone) :"));
        haut.add(recherche);
        haut.add(UiUtil.bouton("Rechercher", e -> rafraichir()));
        recherche.addActionListener(e -> rafraichir());

        JPanel bas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bas.add(UiUtil.bouton("Ajouter", e -> {
            if (AcheteurForm.editer(this, null) != null) {
                rafraichir();
            }
        }));
        bas.add(UiUtil.bouton("Modifier", e -> {
            Acheteur a = selection();
            if (a != null && AcheteurForm.editer(this, a) != null) {
                rafraichir();
            }
        }));
        JButton supprimer = UiUtil.bouton("Supprimer", e -> supprimer());
        supprimer.setEnabled(AuthService.estAdmin());
        bas.add(supprimer);
        bas.add(UiUtil.bouton("Historique des achats", e -> historique()));

        add(haut, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bas, BorderLayout.SOUTH);
    }

    @Override
    public void rafraichir() {
        try {
            acheteurs.clear();
            acheteurs.addAll(recherche.getText().isBlank()
                    ? acheteurDAO.listerTous() : acheteurDAO.rechercherParNom(recherche.getText()));
            modele.setRowCount(0);
            for (Acheteur a : acheteurs) {
                modele.addRow(new Object[] {
                    a.getNom(), a.getPrenom(), UiUtil.texte(a.getTelephone()), UiUtil.texte(a.getEmail()),
                    UiUtil.texte(a.getAdresse()), UiUtil.date(a.getDateInscription()), acheteurDAO.nombreAchats(a)
                });
            }
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }

    private Acheteur selection() {
        int i = table.getSelectedRow();
        if (i < 0) {
            UiUtil.info(this, "Sélectionnez d'abord un acheteur.");
            return null;
        }
        return acheteurs.get(i);
    }

    private void supprimer() {
        Acheteur a = selection();
        if (a == null || !UiUtil.confirmer(this, "Supprimer l'acheteur " + a.getNomComplet() + " ?")) {
            return;
        }
        try {
            acheteurDAO.supprimer(a.getId());
            rafraichir();
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }

    /** Fiche acheteur : liste de ses ventes ; double-clic pour ouvrir la facture. */
    private void historique() {
        Acheteur a = selection();
        if (a == null) {
            return;
        }
        try {
            List<Vente> ventes = acheteurDAO.historiqueAchats(a.getId());
            DefaultTableModel m = UiUtil.modele("N° vente", "Date", "Articles", "Montant HT");
            for (Vente v : ventes) {
                m.addRow(new Object[] {v.getId(), UiUtil.date(v.getDateVente()), v.getNombreArticles(),
                                       UiUtil.argent(v.getMontantTotal())});
            }
            JTable t = UiUtil.table(m);
            JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                    "Achats de " + a.getNomComplet(), JDialog.ModalityType.APPLICATION_MODAL);
            t.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int i = t.getSelectedRow();
                    if (e.getClickCount() == 2 && i >= 0) {
                        try {
                            Facture f = new FactureService().chargerParVente(ventes.get(i).getId());
                            if (f != null) {
                                new FactureDialog(d, f).setVisible(true);
                            }
                        } catch (Exception ex) {
                            UiUtil.erreur(d, ex);
                        }
                    }
                }
            });
            d.add(new JLabel("  " + ventes.size() + " achat(s) - double-cliquez pour voir la facture"), BorderLayout.NORTH);
            d.add(new JScrollPane(t), BorderLayout.CENTER);
            d.setSize(new Dimension(560, 380));
            d.setLocationRelativeTo(this);
            d.setVisible(true);
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }
}
