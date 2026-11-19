package pharmacie.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import pharmacie.dao.AcheteurDAO;
import pharmacie.dao.MedicamentDAO;
import pharmacie.dao.VenteDAO;
import pharmacie.model.Medicament;

/** Accueil : 4 compteurs et les alertes (stock bas, expiration proche). */
public class DashboardPanel extends JPanel implements Rafraichissable {
    private final JLabel nbStock = compteur();
    private final JLabel nbVentesJour = compteur();
    private final JLabel nbIndisponibles = compteur();
    private final JLabel nbAcheteurs = compteur();
    private final DefaultTableModel modeleStockBas = UiUtil.modele("Médicament", "Quantité", "Seuil");
    private final DefaultTableModel modeleExpiration = UiUtil.modele("Médicament", "Quantité", "Expiration");

    private final MedicamentDAO medicamentDAO = new MedicamentDAO();
    private final VenteDAO venteDAO = new VenteDAO();
    private final AcheteurDAO acheteurDAO = new AcheteurDAO();

    public DashboardPanel() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel cartes = new JPanel(new GridLayout(1, 4, 12, 0));
        cartes.add(carte("Produits en stock", nbStock, UiUtil.VERT));
        cartes.add(carte("Ventes du jour", nbVentesJour, new Color(187, 222, 251)));
        cartes.add(carte("Produits à quantité 0", nbIndisponibles, UiUtil.ROUGE));
        cartes.add(carte("Acheteurs", nbAcheteurs, new Color(225, 190, 231)));

        JPanel alertes = new JPanel(new GridLayout(1, 2, 12, 0));
        alertes.add(bloc("Stock bas (orange)", modeleStockBas));
        alertes.add(bloc("Expire dans moins de 30 jours (rouge)", modeleExpiration));

        add(cartes, BorderLayout.NORTH);
        add(alertes, BorderLayout.CENTER);
    }

    @Override
    public void rafraichir() {
        try {
            nbStock.setText(String.valueOf(medicamentDAO.compterDisponibles()));
            nbVentesJour.setText(String.valueOf(venteDAO.compterVentesDuJour()));
            nbIndisponibles.setText(String.valueOf(medicamentDAO.compterIndisponibles()));
            nbAcheteurs.setText(String.valueOf(acheteurDAO.listerTous().size()));

            modeleStockBas.setRowCount(0);
            for (Medicament m : medicamentDAO.listerStockBas()) {
                modeleStockBas.addRow(new Object[] {m.getNom(), m.getQuantite(), m.getSeuilMin()});
            }
            modeleExpiration.setRowCount(0);
            for (Medicament m : medicamentDAO.listerExpirantBientot(30)) {
                modeleExpiration.addRow(new Object[] {m.getNom(), m.getQuantite(), UiUtil.date(m.getDateExpiration())});
            }
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }

    private static JLabel compteur() {
        JLabel l = new JLabel("-", SwingConstants.CENTER);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 32f));
        return l;
    }

    private static JPanel carte(String titre, JLabel valeur, Color fond) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(fond);
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        p.add(new JLabel(titre, SwingConstants.CENTER), BorderLayout.NORTH);
        p.add(valeur, BorderLayout.CENTER);
        return p;
    }

    private static JPanel bloc(String titre, DefaultTableModel modele) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(titre));
        p.add(new JScrollPane(UiUtil.table(modele)), BorderLayout.CENTER);
        return p;
    }
}
