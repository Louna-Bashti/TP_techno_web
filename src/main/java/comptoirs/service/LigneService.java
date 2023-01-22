package comptoirs.service;

import comptoirs.dao.CommandeRepository;
import comptoirs.dao.LigneRepository;
import comptoirs.dao.ProduitRepository;
import comptoirs.entity.Ligne;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated // Les contraintes de validatipn des méthodes sont vérifiées
public class LigneService {
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    private final CommandeRepository commandeDao;
    private final LigneRepository ligneDao;
    private final ProduitRepository produitDao;

    // @Autowired
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    public LigneService(CommandeRepository commandeDao, LigneRepository ligneDao, ProduitRepository produitDao) {
        this.commandeDao = commandeDao;
        this.ligneDao = ligneDao;
        this.produitDao = produitDao;
    }

    /**
     * <pre>
     * Service métier : 
     *     Enregistre une nouvelle ligne de commande pour une commande connue par sa clé,
     *     Incrémente la quantité totale commandée (Produit.unitesCommandees) avec la quantite à commander
     * Règles métier :
     *     - le produit référencé doit exister
     *     - la commande doit exister
     *     - la commande ne doit pas être déjà envoyée (le champ 'envoyeele' doit être null)
     *     - la quantité doit être positive
     *     - On doit avoir une quantite en stock du produit suffisante
     * <pre>
     * 
     *  @param commandeNum la clé de la commande
     *  @param produitRef la clé du produit
     *  @param quantite la quantité commandée (positive)
     *  @return la ligne de commande créée
     */
    @Transactional
    Ligne ajouterLigne(Integer commandeNum, Integer produitRef, @Positive(message = "le montant doit être positif") int quantite) {
        //on récupère la commande et le produit commandé en vérifiant leurs existences
        var commande = commandeDao.findById(commandeNum).orElseThrow();
        var produit = produitDao.findById(produitRef).orElseThrow();
        //on vérifie que la commande n'est pas déjà envoyée
        if (commande.getEnvoyeele() != null){
            throw new IllegalStateException("La commande est déjà envoyée");
        }
        //on vérifie qu'il y a bien assez de produits en stock
        if(produit.getUnitesEnStock() < quantite){
            throw new IllegalArgumentException("la quantité commandée est supérieure à celle disponible");
        }
        //on enregistre la nouvelle ligne
        var ligne = new Ligne(commande, produit, quantite);
        ligneDao.save(ligne);
        return(ligne);
    }
}
