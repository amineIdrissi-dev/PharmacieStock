package pharmacie.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import pharmacie.model.Facture;
import pharmacie.model.LigneVente;
import pharmacie.model.Vente;
import pharmacie.service.AuthService;
import pharmacie.service.FactureService;
import pharmacie.service.ImpressionFacture;
import pharmacie.util.Config;

/** Aperçu de la facture, avec impression et annulation (administrateur). */
public class FactureDialog extends JDialog {
    private final Facture facture;
    private final JEditorPane apercu = new JEditorPane("text/html", "");
    private final JButton annuler;

    public FactureDialog(Window parent, Facture facture) {
        super(parent, "Facture " + facture.getNumero(), ModalityType.APPLICATION_MODAL);
        this.facture = facture;
        apercu.setEditable(false);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(UiUtil.bouton("Imprimer", e -> imprimer()));
        annuler = UiUtil.bouton("Annuler la facture", e -> annulerFacture());
        annuler.setVisible(AuthService.estAdmin());
        actions.add(annuler);
        actions.add(UiUtil.bouton("Fermer", e -> dispose()));

        add(new JScrollPane(apercu), BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        majApercu();
        setSize(new Dimension(640, 640));
        setLocationRelativeTo(parent);
    }

    private void majApercu() {
        annuler.setEnabled(!facture.estAnnulee());
        apercu.setText(html());
        apercu.setCaretPosition(0);
    }

    private String html() {
        Vente v = facture.getVente();
        StringBuilder sb = new StringBuilder("<html><body style='font-family:sans-serif;padding:10px'>");
        sb.append("<h2>").append(echapper(Config.get("pharmacie.nom"))).append("</h2>")
          .append(echapper(Config.get("pharmacie.adresse"))).append(" - Tél. ")
          .append(echapper(Config.get("pharmacie.telephone"))).append("<hr>");
        sb.append("<h3>Facture n° ").append(facture.getNumero());
        if (facture.estAnnulee()) {
            sb.append(" <span style='color:red'>(ANNULÉE)</span>");
        }
        sb.append("</h3>")
          .append("Date : ").append(UiUtil.date(facture.getDateFacture())).append("<br>")
          .append("Client : ").append(echapper(v.getNomClient())).append("<br>")
          .append("Vendeur : ").append(echapper(v.getUtilisateur().getNomComplet())).append("<br><br>");
        sb.append("<table width='100%' border='1' cellspacing='0' cellpadding='4'>")
          .append("<tr><th align='left'>Médicament</th><th>Qté</th><th align='right'>P.U. HT</th>")
          .append("<th align='right'>Sous-total</th></tr>");
        for (LigneVente l : v.getLignes()) {
            sb.append("<tr><td>").append(echapper(l.getMedicament().getNom()))
              .append(l.getMedicament().getDosage() == null ? "" : " " + echapper(l.getMedicament().getDosage()))
              .append("</td><td align='center'>").append(l.getQuantite())
              .append("</td><td align='right'>").append(UiUtil.argent(l.getPrixUnitaire()))
              .append("</td><td align='right'>").append(UiUtil.argent(l.getSousTotal())).append("</td></tr>");
        }
        sb.append("</table><br><table align='right'>")
          .append("<tr><td>Total HT :</td><td align='right'>").append(UiUtil.argent(facture.getMontantHt())).append("</td></tr>")
          .append("<tr><td>TVA (").append(facture.getTauxTva().stripTrailingZeros().toPlainString()).append(" %) :</td>")
          .append("<td align='right'>").append(UiUtil.argent(facture.getMontantTva())).append("</td></tr>")
          .append("<tr><td><b>Total TTC :</b></td><td align='right'><b>").append(UiUtil.argent(facture.getMontantTtc()))
          .append("</b></td></tr></table><br clear='all'>")
          .append("Paiement : ").append(ImpressionFacture.libellePaiement(facture.getModePaiement()))
          .append("</body></html>");
        return sb.toString();
    }

    private static String echapper(String texte) {
        return texte == null ? "" : texte.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void imprimer() {
        try {
            new ImpressionFacture(facture).imprimer();
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }

    private void annulerFacture() {
        if (!UiUtil.confirmer(this, "Annuler la facture " + facture.getNumero()
                + " ?\nLes quantités vendues seront remises en stock.")) {
            return;
        }
        try {
            new FactureService().annulerFacture(facture);
            majApercu();
        } catch (Exception e) {
            UiUtil.erreur(this, e);
        }
    }
}
