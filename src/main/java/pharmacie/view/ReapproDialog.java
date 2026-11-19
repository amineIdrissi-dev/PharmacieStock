package pharmacie.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import pharmacie.dao.FournisseurDAO;
import pharmacie.model.Approvisionnement;
import pharmacie.model.Fournisseur;
import pharmacie.model.Medicament;
import pharmacie.service.StockService;

/** Saisie d'une entrée de stock : fournisseur, quantité, prix d'achat. */
public class ReapproDialog extends JDialog {
    private final Medicament medicament;
    private boolean enregistre;

    private final JComboBox<Fournisseur> fournisseur = new JComboBox<>();
    private final JSpinner quantite = new JSpinner(new SpinnerNumberModel(10, 1, 1_000_000, 1));
    private final JTextField prixAchat = new JTextField(12);

    public ReapproDialog(Window parent, Medicament m) {
        super(parent, "Réapprovisionner", ModalityType.APPLICATION_MODAL);
        this.medicament = m;
        try {
            for (Fournisseur f : new FournisseurDAO().listerTous()) {
                fournisseur.addItem(f);
            }
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }

        JPanel form = UiUtil.formulaire(
                new String[] {"Médicament", "Stock actuel", "Fournisseur *", "Quantité reçue *", "Prix d'achat unitaire"},
                new JComponent[] {new JLabel(m.getNom()), new JLabel(String.valueOf(m.getQuantite())),
                                  fournisseur, quantite, prixAchat});
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(UiUtil.bouton("Valider", e -> valider()));
        actions.add(UiUtil.bouton("Annuler", e -> dispose()));

        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

    public boolean afficher() {
        setVisible(true);
        return enregistre;
    }

    private void valider() {
        try {
            if (fournisseur.getSelectedItem() == null) {
                throw new IllegalArgumentException("Ajoutez d'abord un fournisseur (rubrique Catégories et fournisseurs).");
            }
            Approvisionnement a = new Approvisionnement();
            a.setMedicament(medicament);
            a.setFournisseur((Fournisseur) fournisseur.getSelectedItem());
            a.setQuantite((Integer) quantite.getValue());
            a.setPrixAchat(UiUtil.lireMontant(prixAchat.getText()));
            new StockService().reapprovisionner(a);
            enregistre = true;
            dispose();
        } catch (IllegalArgumentException e) {
            UiUtil.info(this, e.getMessage());
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }
}
