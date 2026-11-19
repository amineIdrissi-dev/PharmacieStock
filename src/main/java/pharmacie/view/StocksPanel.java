package pharmacie.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import pharmacie.dao.CategorieDAO;
import pharmacie.dao.MedicamentDAO;
import pharmacie.model.Categorie;
import pharmacie.model.Medicament;
import pharmacie.service.AuthService;

/** Rubrique « Stocks disponibles » : médicaments dont la quantité est supérieure à 0. */
public class StocksPanel extends JPanel implements Rafraichissable {
    private static final String TOUTES = "Toutes les catégories";

    private final DefaultTableModel modele = UiUtil.modele(
            "Photo", "Nom", "Catégorie", "Forme", "Dosage", "Quantité", "Prix HT", "Expiration", "Ordonnance");
    private final List<Medicament> medicaments = new ArrayList<>();
    private final JTable table;
    private final JTextField recherche = new JTextField(18);
    private final JComboBox<Object> filtreCategorie = new JComboBox<>();

    private final MedicamentDAO medicamentDAO = new MedicamentDAO();
    private final CategorieDAO categorieDAO = new CategorieDAO();

    public StocksPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        table = UiUtil.tableColoree(modele, this::couleur);
        table.setRowHeight(48);

        JPanel haut = new JPanel(new FlowLayout(FlowLayout.LEFT));
        haut.add(new JLabel("Rechercher :"));
        haut.add(recherche);
        haut.add(filtreCategorie);
        haut.add(UiUtil.bouton("Rechercher", e -> charger()));
        recherche.addActionListener(e -> charger());

        JPanel bas = new JPanel(new BorderLayout());
        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        boutons.add(UiUtil.bouton("Ajouter", e -> editer(null)));
        boutons.add(UiUtil.bouton("Modifier", e -> {
            Medicament m = selection();
            if (m != null) {
                editer(m);
            }
        }));
        JButton supprimer = UiUtil.bouton("Supprimer", e -> supprimer());
        supprimer.setEnabled(AuthService.estAdmin());
        boutons.add(supprimer);
        boutons.add(UiUtil.bouton("Réapprovisionner", e -> reapprovisionner()));
        JLabel legende = new JLabel("Orange : stock bas    Rouge : expire dans moins de 30 jours");
        bas.add(boutons, BorderLayout.WEST);
        bas.add(legende, BorderLayout.EAST);

        add(haut, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bas, BorderLayout.SOUTH);
    }

    @Override
    public void rafraichir() {
        try {
            Object choix = filtreCategorie.getSelectedItem();
            filtreCategorie.removeAllItems();
            filtreCategorie.addItem(TOUTES);
            for (Categorie c : categorieDAO.listerTous()) {
                filtreCategorie.addItem(c);
            }
            if (choix != null) {
                filtreCategorie.setSelectedItem(choix);
            }
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
        charger();
    }

    private void charger() {
        try {
            Object choix = filtreCategorie.getSelectedItem();
            Categorie categorie = choix instanceof Categorie c ? c : null;
            medicaments.clear();
            medicaments.addAll(medicamentDAO.rechercherDisponibles(recherche.getText(), categorie));
            modele.setRowCount(0);
            for (Medicament m : medicaments) {
                modele.addRow(new Object[] {
                    UiUtil.icone(m.getPhoto(), 44), m.getNom(), m.getCategorie().getLibelle(),
                    UiUtil.texte(m.getForme()), UiUtil.texte(m.getDosage()), m.getQuantite(),
                    UiUtil.argent(m.getPrixUnitaire()), UiUtil.date(m.getDateExpiration()),
                    m.isOrdonnanceRequise() ? "Oui" : "Non"
                });
            }
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }

    private Color couleur(int ligne) {
        Medicament m = medicaments.get(ligne);
        if (m.expireBientot()) {
            return UiUtil.ROUGE;
        }
        return m.estStockBas() ? UiUtil.ORANGE : null;
    }

    private Medicament selection() {
        int i = table.getSelectedRow();
        if (i < 0) {
            UiUtil.info(this, "Sélectionnez d'abord un médicament.");
            return null;
        }
        return medicaments.get(i);
    }

    /** m = null : nouveau médicament. */
    private void editer(Medicament m) {
        boolean enregistre = new MedicamentDialog(SwingUtilities.getWindowAncestor(this), m).afficher();
        if (enregistre) {
            charger();
        }
    }

    private void supprimer() {
        Medicament m = selection();
        if (m == null || !UiUtil.confirmer(this, "Supprimer « " + m.getNom() + " » ?")) {
            return;
        }
        try {
            medicamentDAO.supprimer(m.getId());
            charger();
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }

    private void reapprovisionner() {
        Medicament m = selection();
        if (m != null && new ReapproDialog(SwingUtilities.getWindowAncestor(this), m).afficher()) {
            charger();
        }
    }
}
