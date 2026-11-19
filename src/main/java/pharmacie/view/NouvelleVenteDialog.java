package pharmacie.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import pharmacie.dao.AcheteurDAO;
import pharmacie.dao.MedicamentDAO;
import pharmacie.model.Acheteur;
import pharmacie.model.Facture;
import pharmacie.model.LigneVente;
import pharmacie.model.Medicament;
import pharmacie.model.Vente;
import pharmacie.service.ImpressionFacture;
import pharmacie.service.VenteService;

/** Saisie d'une vente : acheteur, lignes (médicament + quantité), mode de paiement. */
public class NouvelleVenteDialog extends JDialog {
    private static final String CLIENT_DE_PASSAGE = "Client de passage";
    private static final String[] PAIEMENTS = {"ESPECES", "CARTE", "CHEQUE"};

    private final Vente vente = new Vente();
    private Facture facture;

    private final JComboBox<Object> acheteur = new JComboBox<>();
    private final JComboBox<Medicament> medicament = new JComboBox<>();
    private final JSpinner quantite = new JSpinner(new SpinnerNumberModel(1, 1, 100_000, 1));
    private final DefaultTableModel modele = UiUtil.modele("Médicament", "Quantité", "P.U. HT", "Sous-total");
    private final JTable table = UiUtil.table(modele);
    private final JLabel total = new JLabel("Total HT : 0.00");
    private final JComboBox<String> paiement = new JComboBox<>(PAIEMENTS);

    public NouvelleVenteDialog(Window parent) {
        super(parent, "Nouvelle vente", ModalityType.APPLICATION_MODAL);
        chargerListes();
        paiement.setRenderer((liste, valeur, index, selection, focus) ->
                new JLabel(ImpressionFacture.libellePaiement(valeur)));

        JPanel client = new JPanel(new FlowLayout(FlowLayout.LEFT));
        client.add(new JLabel("Acheteur :"));
        client.add(acheteur);
        client.add(UiUtil.bouton("Nouvel acheteur...", e -> nouvelAcheteur()));

        JPanel ajout = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ajout.add(new JLabel("Médicament :"));
        ajout.add(medicament);
        ajout.add(new JLabel("Quantité :"));
        ajout.add(quantite);
        ajout.add(UiUtil.bouton("Ajouter la ligne", e -> ajouterLigne()));
        ajout.add(UiUtil.bouton("Retirer la ligne", e -> retirerLigne()));

        JPanel haut = new JPanel(new BorderLayout());
        haut.add(client, BorderLayout.NORTH);
        haut.add(ajout, BorderLayout.SOUTH);

        total.setFont(total.getFont().deriveFont(Font.BOLD, 15f));
        JPanel bas = new JPanel(new BorderLayout());
        JPanel gaucheBas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gaucheBas.add(total);
        gaucheBas.add(new JLabel("    Paiement :"));
        gaucheBas.add(paiement);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton valider = UiUtil.bouton("Valider la vente", e -> valider());
        actions.add(valider);
        actions.add(UiUtil.bouton("Annuler", e -> dispose()));
        bas.add(gaucheBas, BorderLayout.WEST);
        bas.add(actions, BorderLayout.EAST);

        JPanel contenu = new JPanel(new BorderLayout(8, 8));
        contenu.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contenu.add(haut, BorderLayout.NORTH);
        contenu.add(new JScrollPane(table), BorderLayout.CENTER);
        contenu.add(bas, BorderLayout.SOUTH);
        setContentPane(contenu);
        setSize(new Dimension(820, 520));
        setLocationRelativeTo(parent);
    }

    /** Affiche la fenêtre ; retourne la facture créée, ou null si la vente est annulée. */
    public Facture afficher() {
        setVisible(true);
        return facture;
    }

    private void chargerListes() {
        try {
            acheteur.addItem(CLIENT_DE_PASSAGE);
            for (Acheteur a : new AcheteurDAO().listerTous()) {
                acheteur.addItem(a);
            }
            for (Medicament m : new MedicamentDAO().listerDisponibles()) {
                medicament.addItem(m);
            }
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }

    private void nouvelAcheteur() {
        Acheteur a = AcheteurForm.editer(this, null);
        if (a != null) {
            acheteur.addItem(a);
            acheteur.setSelectedItem(a);
        }
    }

    private void ajouterLigne() {
        Medicament m = (Medicament) medicament.getSelectedItem();
        if (m == null) {
            UiUtil.info(this, "Aucun médicament disponible.");
            return;
        }
        int qte = (Integer) quantite.getValue();
        int dejaDansLaVente = vente.getLignes().stream()
                .filter(l -> l.getMedicament().getId() == m.getId())
                .mapToInt(LigneVente::getQuantite).sum();
        if (qte + dejaDansLaVente > m.getQuantite()) {
            UiUtil.info(this, "Stock insuffisant : " + m.getQuantite() + " disponible(s).");
            return;
        }
        if (m.isOrdonnanceRequise()
                && !UiUtil.confirmer(this, m.getNom() + " nécessite une ordonnance.\nLe client a-t-il présenté son ordonnance ?")) {
            return;
        }
        LigneVente l = new LigneVente(m, qte);
        vente.ajouterLigne(l);
        modele.addRow(new Object[] {m.getNom(), qte, UiUtil.argent(l.getPrixUnitaire()), UiUtil.argent(l.getSousTotal())});
        majTotal();
    }

    private void retirerLigne() {
        int i = table.getSelectedRow();
        if (i >= 0) {
            vente.getLignes().remove(i);
            vente.calculerTotal();
            modele.removeRow(i);
            majTotal();
        }
    }

    private void majTotal() {
        total.setText("Total HT : " + UiUtil.argent(vente.getMontantTotal()));
    }

    private void valider() {
        if (vente.getLignes().isEmpty()) {
            UiUtil.info(this, "Ajoutez au moins un médicament.");
            return;
        }
        Object choix = acheteur.getSelectedItem();
        vente.setAcheteur(choix instanceof Acheteur a ? a : null);
        try {
            facture = new VenteService().enregistrerVente(vente, (String) paiement.getSelectedItem());
            dispose();
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }
}
