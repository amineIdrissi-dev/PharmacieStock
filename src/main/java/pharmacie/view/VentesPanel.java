package pharmacie.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import pharmacie.dao.VenteDAO;
import pharmacie.model.Facture;
import pharmacie.model.Vente;
import pharmacie.service.FactureService;

/** Rubrique « Produits vendus » : historique des ventes, nouvelle vente, facture. */
public class VentesPanel extends JPanel implements Rafraichissable {
    private static final String[] PERIODES = {"Aujourd'hui", "7 derniers jours", "30 derniers jours", "Tout"};

    private final DefaultTableModel modele = UiUtil.modele(
            "N° vente", "Date", "Acheteur", "Vendeur", "Articles", "Montant HT");
    private final List<Vente> ventes = new ArrayList<>();
    private final JTable table = UiUtil.table(modele);
    private final JComboBox<String> periode = new JComboBox<>(PERIODES);
    private final JLabel total = new JLabel();
    private final VenteDAO venteDAO = new VenteDAO();

    public VentesPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel haut = new JPanel(new BorderLayout());
        JPanel filtres = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtres.add(new JLabel("Période :"));
        filtres.add(periode);
        periode.addActionListener(e -> rafraichir());
        haut.add(filtres, BorderLayout.WEST);
        haut.add(total, BorderLayout.EAST);

        JPanel bas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bas.add(UiUtil.bouton("Nouvelle vente", e -> nouvelleVente()));
        bas.add(UiUtil.bouton("Voir / imprimer la facture", e -> ouvrirFacture()));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ouvrirFacture();
                }
            }
        });

        add(haut, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bas, BorderLayout.SOUTH);
    }

    @Override
    public void rafraichir() {
        LocalDate fin = LocalDate.now();
        LocalDate debut = switch (periode.getSelectedIndex()) {
            case 0 -> fin;
            case 1 -> fin.minusDays(6);
            case 2 -> fin.minusDays(29);
            default -> null;
        };
        if (debut == null) {
            fin = null;
        }
        try {
            ventes.clear();
            ventes.addAll(venteDAO.listerParPeriode(debut, fin));
            modele.setRowCount(0);
            for (Vente v : ventes) {
                modele.addRow(new Object[] {
                    v.getId(), UiUtil.date(v.getDateVente()), v.getNomClient(),
                    v.getUtilisateur().getNomComplet(), v.getNombreArticles(), UiUtil.argent(v.getMontantTotal())
                });
            }
            total.setText("Total HT (hors factures annulées) : " + UiUtil.argent(venteDAO.totalParPeriode(debut, fin)) + "   ");
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }

    private void nouvelleVente() {
        Facture f = new NouvelleVenteDialog(SwingUtilities.getWindowAncestor(this)).afficher();
        rafraichir();
        if (f != null) {
            ouvrirFacture(f.getVente().getId());
        }
    }

    private void ouvrirFacture() {
        int i = table.getSelectedRow();
        if (i < 0) {
            UiUtil.info(this, "Sélectionnez d'abord une vente.");
            return;
        }
        ouvrirFacture(ventes.get(i).getId());
    }

    private void ouvrirFacture(int idVente) {
        try {
            Facture f = new FactureService().chargerParVente(idVente);
            if (f == null) {
                UiUtil.info(this, "Aucune facture pour cette vente.");
                return;
            }
            new FactureDialog(SwingUtilities.getWindowAncestor(this), f).setVisible(true);
            rafraichir();
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }
}
