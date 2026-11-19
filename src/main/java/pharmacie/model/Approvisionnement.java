package pharmacie.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Approvisionnement {
    private int id;
    private LocalDateTime dateAppro;
    private int quantite;
    private BigDecimal prixAchat;
    private Medicament medicament;
    private Fournisseur fournisseur;
    private Utilisateur utilisateur;

    public Approvisionnement() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getDateAppro() { return dateAppro; }
    public void setDateAppro(LocalDateTime dateAppro) { this.dateAppro = dateAppro; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public BigDecimal getPrixAchat() { return prixAchat; }
    public void setPrixAchat(BigDecimal prixAchat) { this.prixAchat = prixAchat; }
    public Medicament getMedicament() { return medicament; }
    public void setMedicament(Medicament medicament) { this.medicament = medicament; }
    public Fournisseur getFournisseur() { return fournisseur; }
    public void setFournisseur(Fournisseur fournisseur) { this.fournisseur = fournisseur; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}
