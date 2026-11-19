package pharmacie.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import pharmacie.dao.CategorieDAO;
import pharmacie.dao.MedicamentDAO;
import pharmacie.model.Categorie;
import pharmacie.model.Medicament;

/** Formulaire d'ajout ou de modification d'un médicament (avec photo). */
public class MedicamentDialog extends JDialog {
    private final Medicament medicament;
    private final boolean nouveau;
    private boolean enregistre;
    private byte[] photo;

    private final JTextField codeBarre = new JTextField(20);
    private final JTextField nom = new JTextField(20);
    private final JComboBox<String> forme = new JComboBox<>(
            new String[] {"Comprimé", "Gélule", "Sirop", "Crème", "Pommade", "Injectable", "Sachet", "Gouttes"});
    private final JTextField dosage = new JTextField(20);
    private final JTextField prix = new JTextField(20);
    private final JSpinner quantite = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 1));
    private final JSpinner seuil = new JSpinner(new SpinnerNumberModel(10, 0, 1_000_000, 1));
    private final JTextField expiration = new JTextField(20);
    private final JCheckBox ordonnance = new JCheckBox("Vente sur ordonnance");
    private final JComboBox<Categorie> categorie = new JComboBox<>();
    private final JLabel apercuPhoto = new JLabel("Aucune photo", SwingConstants.CENTER);

    public MedicamentDialog(Window parent, Medicament m) {
        super(parent, m == null ? "Nouveau médicament" : "Modifier le médicament", ModalityType.APPLICATION_MODAL);
        this.nouveau = m == null;
        this.medicament = m == null ? new Medicament() : m;
        forme.setEditable(true);
        apercuPhoto.setPreferredSize(new Dimension(110, 110));

        try {
            for (Categorie c : new CategorieDAO().listerTous()) {
                categorie.addItem(c);
            }
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
        if (!nouveau) {
            remplir();
        }

        JPanel form = UiUtil.formulaire(
                new String[] {"Code-barres", "Nom *", "Forme", "Dosage", "Prix unitaire HT *", "Quantité",
                              "Seuil d'alerte", "Expiration (jj/mm/aaaa)", "Catégorie *", ""},
                new JComponent[] {codeBarre, nom, forme, dosage, prix, quantite, seuil, expiration, categorie,
                                  ordonnance});

        JPanel panneauPhoto = new JPanel(new BorderLayout(4, 4));
        panneauPhoto.add(apercuPhoto, BorderLayout.CENTER);
        JPanel boutonsPhoto = new JPanel(new FlowLayout());
        boutonsPhoto.add(UiUtil.bouton("Choisir...", e -> choisirPhoto()));
        boutonsPhoto.add(UiUtil.bouton("Retirer", e -> afficherPhoto(null)));
        panneauPhoto.add(boutonsPhoto, BorderLayout.SOUTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton valider = UiUtil.bouton("Enregistrer", e -> enregistrer());
        actions.add(valider);
        actions.add(UiUtil.bouton("Annuler", e -> dispose()));
        getRootPane().setDefaultButton(valider);

        add(form, BorderLayout.CENTER);
        add(panneauPhoto, BorderLayout.EAST);
        add(actions, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

    /** Affiche la fenêtre et retourne true si le médicament a été enregistré. */
    public boolean afficher() {
        setVisible(true);
        return enregistre;
    }

    private void remplir() {
        codeBarre.setText(UiUtil.texte(medicament.getCodeBarre()));
        nom.setText(medicament.getNom());
        forme.setSelectedItem(medicament.getForme());
        dosage.setText(UiUtil.texte(medicament.getDosage()));
        prix.setText(UiUtil.argent(medicament.getPrixUnitaire()));
        quantite.setValue(medicament.getQuantite());
        seuil.setValue(medicament.getSeuilMin());
        expiration.setText(UiUtil.date(medicament.getDateExpiration()));
        ordonnance.setSelected(medicament.isOrdonnanceRequise());
        categorie.setSelectedItem(medicament.getCategorie());
        afficherPhoto(medicament.getPhoto());
    }

    private void choisirPhoto() {
        try {
            byte[] donnees = UiUtil.choisirPhoto(this);
            if (donnees != null) {
                afficherPhoto(donnees);
            }
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }

    private void afficherPhoto(byte[] donnees) {
        photo = donnees;
        apercuPhoto.setIcon(UiUtil.icone(donnees, 100));
        apercuPhoto.setText(donnees == null ? "Aucune photo" : "");
    }

    private void enregistrer() {
        try {
            if (nom.getText().isBlank()) {
                throw new IllegalArgumentException("Le nom est obligatoire.");
            }
            if (categorie.getSelectedItem() == null) {
                throw new IllegalArgumentException("Créez d'abord une catégorie (rubrique Catégories et fournisseurs).");
            }
            if (UiUtil.lireMontant(prix.getText()) == null) {
                throw new IllegalArgumentException("Le prix est obligatoire.");
            }
            medicament.setCodeBarre(codeBarre.getText());
            medicament.setNom(nom.getText().trim());
            Object f = forme.getSelectedItem();
            medicament.setForme(f == null ? null : f.toString());
            medicament.setDosage(dosage.getText().trim());
            medicament.setPrixUnitaire(UiUtil.lireMontant(prix.getText()));
            medicament.setQuantite((Integer) quantite.getValue());
            medicament.setSeuilMin((Integer) seuil.getValue());
            medicament.setDateExpiration(UiUtil.lireDate(expiration.getText()));
            medicament.setOrdonnanceRequise(ordonnance.isSelected());
            medicament.setCategorie((Categorie) categorie.getSelectedItem());
            medicament.setPhoto(photo);

            MedicamentDAO dao = new MedicamentDAO();
            if (nouveau) {
                dao.ajouter(medicament);
            } else {
                dao.modifier(medicament);
            }
            enregistre = true;
            dispose();
        } catch (IllegalArgumentException e) {
            UiUtil.info(this, e.getMessage());
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }
}
