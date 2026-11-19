package pharmacie.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Facture {
    public static final String VALIDEE = "VALIDEE";
    public static final String ANNULEE = "ANNULEE";

    private int id;
    private String numero;
    private LocalDateTime dateFacture;
    private BigDecimal montantHt = BigDecimal.ZERO;
    private BigDecimal tauxTva = BigDecimal.ZERO;
    private BigDecimal montantTtc = BigDecimal.ZERO;
    private String modePaiement;                 // ESPECES, CARTE, CHEQUE
    private String statut = VALIDEE;
    private Vente vente;

    public Facture() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public LocalDateTime getDateFacture() { return dateFacture; }
    public void setDateFacture(LocalDateTime dateFacture) { this.dateFacture = dateFacture; }
    public BigDecimal getMontantHt() { return montantHt; }
    public void setMontantHt(BigDecimal montantHt) { this.montantHt = montantHt; }
    public BigDecimal getTauxTva() { return tauxTva; }
    public void setTauxTva(BigDecimal tauxTva) { this.tauxTva = tauxTva; }
    public BigDecimal getMontantTtc() { return montantTtc; }
    public void setMontantTtc(BigDecimal montantTtc) { this.montantTtc = montantTtc; }
    public String getModePaiement() { return modePaiement; }
    public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public Vente getVente() { return vente; }
    public void setVente(Vente vente) { this.vente = vente; }

    public BigDecimal getMontantTva() { return montantTtc.subtract(montantHt); }

    public boolean estAnnulee() { return ANNULEE.equals(statut); }
}
