package pharmacie.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import pharmacie.dao.MedicamentDAO;
import pharmacie.model.Medicament;

/** Rubrique « Produits indisponibles » : médicaments dont la quantité est égale à 0. */
public class IndisponiblesPanel extends JPanel implements Rafraichissable {
    private final DefaultTableModel modele = UiUtil.modele("Photo", "Nom", "Catégorie", "Dosage", "Dernière vente");
    private final List<Medicament> medicaments = new ArrayList<>();
    private final JTable table = UiUtil.tableColoree(modele, i -> UiUtil.ROUGE);
    private final MedicamentDAO medicamentDAO = new MedicamentDAO();

    public IndisponiblesPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        table.setRowHeight(48);

        JPanel bas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bas.add(UiUtil.bouton("Réapprovisionner", e -> reapprovisionner()));
        bas.add(new JLabel("Après réapprovisionnement, le produit repasse dans les stocks disponibles."));

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bas, BorderLayout.SOUTH);
    }

    @Override
    public void rafraichir() {
        try {
            medicaments.clear();
            medicaments.addAll(medicamentDAO.listerIndisponibles());
            modele.setRowCount(0);
            for (Medicament m : medicaments) {
                modele.addRow(new Object[] {
                    UiUtil.icone(m.getPhoto(), 44), m.getNom(), m.getCategorie().getLibelle(),
                    UiUtil.texte(m.getDosage()), UiUtil.date(medicamentDAO.derniereVente(m.getId()))
                });
            }
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }

    private void reapprovisionner() {
        int i = table.getSelectedRow();
        if (i < 0) {
            UiUtil.info(this, "Sélectionnez d'abord un médicament.");
            return;
        }
        if (new ReapproDialog(SwingUtilities.getWindowAncestor(this), medicaments.get(i)).afficher()) {
            rafraichir();
        }
    }
}
