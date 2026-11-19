package pharmacie.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Vente {
    private int id;
    private LocalDateTime dateVente;
    private BigDecimal montantTotal = BigDecimal.ZERO;   // total HT
    private Acheteur acheteur;                           // null = client de passage
    private Utilisateur utilisateur;
    private List<LigneVente> lignes = new ArrayList<>();
    private int nombreArticles;                          // rempli par VenteDAO pour les listes

    public Vente() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getDateVente() { return dateVente; }
    public void setDateVente(LocalDateTime dateVente) { this.dateVente = dateVente; }
    public BigDecimal getMontantTotal() { return montantTotal; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montantTotal = montantTotal; }
    public Acheteur getAcheteur() { return acheteur; }
    public void setAcheteur(Acheteur acheteur) { this.acheteur = acheteur; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
    public List<LigneVente> getLignes() { return lignes; }
    public void setLignes(List<LigneVente> lignes) { this.lignes = lignes; }
    public int getNombreArticles() { return nombreArticles; }
    public void setNombreArticles(int nombreArticles) { this.nombreArticles = nombreArticles; }

    public void ajouterLigne(LigneVente ligne) {
        lignes.add(ligne);
        calculerTotal();
    }

    public BigDecimal calculerTotal() {
        montantTotal = lignes.stream()
                .map(LigneVente::getSousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return montantTotal;
    }

    public String getNomClient() {
        return acheteur == null ? "Client de passage" : acheteur.getNomComplet();
    }
}
