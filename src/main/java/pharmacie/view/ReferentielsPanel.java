package pharmacie.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import pharmacie.dao.CategorieDAO;
import pharmacie.dao.FournisseurDAO;
import pharmacie.model.Categorie;
import pharmacie.model.Fournisseur;
import pharmacie.service.AuthService;

/** Listes de référence : catégories de médicaments et fournisseurs. */
public class ReferentielsPanel extends JPanel implements Rafraichissable {
    private final DefaultListModel<Categorie> modeleCategories = new DefaultListModel<>();
    private final JList<Categorie> listeCategories = new JList<>(modeleCategories);
    private final DefaultTableModel modeleFournisseurs = UiUtil.modele("Nom", "Téléphone", "Email", "Adresse");
    private final List<Fournisseur> fournisseurs = new ArrayList<>();
    private final JTable tableFournisseurs = UiUtil.table(modeleFournisseurs);

    private final CategorieDAO categorieDAO = new CategorieDAO();
    private final FournisseurDAO fournisseurDAO = new FournisseurDAO();

    public ReferentielsPanel() {
        super(new GridLayout(1, 2, 12, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        listeCategories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel categories = new JPanel(new BorderLayout(4, 4));
        categories.setBorder(BorderFactory.createTitledBorder("Catégories"));
        categories.add(new JScrollPane(listeCategories), BorderLayout.CENTER);
        JPanel bc = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bc.add(UiUtil.bouton("Ajouter", e -> editerCategorie(null)));
        bc.add(UiUtil.bouton("Renommer", e -> {
            Categorie c = listeCategories.getSelectedValue();
            if (c != null) {
                editerCategorie(c);
            }
        }));
        bc.add(UiUtil.bouton("Supprimer", e -> supprimerCategorie()));
        categories.add(bc, BorderLayout.SOUTH);

        JPanel panneauFournisseurs = new JPanel(new BorderLayout(4, 4));
        panneauFournisseurs.setBorder(BorderFactory.createTitledBorder("Fournisseurs"));
        panneauFournisseurs.add(new JScrollPane(tableFournisseurs), BorderLayout.CENTER);
        JPanel bf = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bf.add(UiUtil.bouton("Ajouter", e -> editerFournisseur(null)));
        bf.add(UiUtil.bouton("Modifier", e -> {
            int i = tableFournisseurs.getSelectedRow();
            if (i >= 0) {
                editerFournisseur(fournisseurs.get(i));
            }
        }));
        bf.add(UiUtil.bouton("Supprimer", e -> supprimerFournisseur()));
        panneauFournisseurs.add(bf, BorderLayout.SOUTH);

        add(categories);
        add(panneauFournisseurs);
    }

    @Override
    public void rafraichir() {
        try {
            modeleCategories.clear();
            for (Categorie c : categorieDAO.listerTous()) {
                modeleCategories.addElement(c);
            }
            fournisseurs.clear();
            fournisseurs.addAll(fournisseurDAO.listerTous());
            modeleFournisseurs.setRowCount(0);
            for (Fournisseur f : fournisseurs) {
                modeleFournisseurs.addRow(new Object[] {f.getNom(), UiUtil.texte(f.getTelephone()),
                        UiUtil.texte(f.getEmail()), UiUtil.texte(f.getAdresse())});
            }
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }

    private void editerCategorie(Categorie c) {
        String libelle = JOptionPane.showInputDialog(this, "Libellé de la catégorie :", c == null ? "" : c.getLibelle());
        if (libelle == null || libelle.isBlank()) {
            return;
        }
        try {
            if (c == null) {
                categorieDAO.ajouter(new Categorie(0, libelle.trim()));
            } else {
                c.setLibelle(libelle.trim());
                categorieDAO.modifier(c);
            }
            rafraichir();
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }

    private void supprimerCategorie() {
        Categorie c = listeCategories.getSelectedValue();
        if (c == null || !droitSuppression()
                || !UiUtil.confirmer(this, "Supprimer la catégorie « " + c.getLibelle() + " » ?")) {
            return;
        }
        try {
            categorieDAO.supprimer(c.getId());
            rafraichir();
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }

    private void editerFournisseur(Fournisseur existant) {
        Fournisseur f = existant == null ? new Fournisseur() : existant;
        JTextField nom = new JTextField(UiUtil.texte(f.getNom()), 22);
        JTextField telephone = new JTextField(UiUtil.texte(f.getTelephone()), 22);
        JTextField email = new JTextField(UiUtil.texte(f.getEmail()), 22);
        JTextField adresse = new JTextField(UiUtil.texte(f.getAdresse()), 22);
        int choix = JOptionPane.showConfirmDialog(this,
                UiUtil.formulaire(new String[] {"Nom *", "Téléphone", "Email", "Adresse"},
                        new JComponent[] {nom, telephone, email, adresse}),
                existant == null ? "Nouveau fournisseur" : "Modifier le fournisseur",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choix != JOptionPane.OK_OPTION) {
            return;
        }
        if (nom.getText().isBlank()) {
            UiUtil.info(this, "Le nom est obligatoire.");
            return;
        }
        f.setNom(nom.getText().trim());
        f.setTelephone(telephone.getText().trim());
        f.setEmail(email.getText().trim());
        f.setAdresse(adresse.getText().trim());
        try {
            if (existant == null) {
                fournisseurDAO.ajouter(f);
            } else {
                fournisseurDAO.modifier(f);
            }
            rafraichir();
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }

    private void supprimerFournisseur() {
        int i = tableFournisseurs.getSelectedRow();
        if (i < 0 || !droitSuppression()) {
            return;
        }
        Fournisseur f = fournisseurs.get(i);
        if (!UiUtil.confirmer(this, "Supprimer le fournisseur « " + f.getNom() + " » ?")) {
            return;
        }
        try {
            fournisseurDAO.supprimer(f.getId());
            rafraichir();
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }

    private boolean droitSuppression() {
        if (!AuthService.estAdmin()) {
            UiUtil.info(this, "Seul l'administrateur peut supprimer.");
            return false;
        }
        return true;
    }
}
