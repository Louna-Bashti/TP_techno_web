package comptoirs.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Null;
import org.springframework.stereotype.Service;

import comptoirs.dao.ClientRepository;
import comptoirs.dao.CommandeRepository;
import comptoirs.entity.Commande;
import jakarta.transaction.Transactional;

@Service
public class CommandeService {
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    private final CommandeRepository commandeDao;
    private final ClientRepository clientDao;
    

    // @Autowired
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    public CommandeService(CommandeRepository commandeDao, ClientRepository clientDao) {
        this.commandeDao = commandeDao;
        this.clientDao = clientDao;
    }
    /**
     * Service métier : Enregistre une nouvelle commande pour un client connu par sa clé
     * Règles métier :
     * - le client doit exister
     * - On initialise l'adresse de livraison avec l'adresse du client
     * - Si le client a déjà commandé plus de 100 articles, on lui offre une remise de 15%
     * @param clientCode la clé du client
     * @return la commande créée
     */
    @Transactional
    public Commande creerCommande(String clientCode) {
        // On vérifie que le client existe
        var client = clientDao.findById(clientCode).orElseThrow();
        // On crée une commande pour ce client
        var nouvelleCommande = new Commande(client);
        // On initialise l'adresse de livraison avec l'adresse du client
        nouvelleCommande.setAdresseLivraison(client.getAdresse());
        // Si le client a déjà commandé plus de 100 articles, on lui offre une remise de 15%
        // La requête SQL nécessaire est définie dans l'interface ClientRepository
        var nbArticles = clientDao.nombreArticlesCommandesPar(clientCode);
        if (nbArticles >= 100) {
            nouvelleCommande.setRemise(new BigDecimal("0.15"));
        }
        // On enregistre la commande (génère la clé)
        commandeDao.save(nouvelleCommande);
        return nouvelleCommande;
    }

    /**
     * Service métier : Enregistre l'expédition d'une commande connue par sa clé
     * Règles métier :
     * - la commande doit exister
     * - la commande ne doit pas être déjà envoyée (le champ 'envoyeele' doit être null)
     * - On met à jour la date d'expédition (envoyeele) avec la date du jour
     * - Pour chaque produit commandé, décrémente la quantité en stock (Produit.unitesEnStock)
     *   de la quantité commandée
     * @param commandeNum la clé de la commande
     * @return la commande mise à jour
     */
    @Transactional
    public Commande enregistreExpédition(Integer commandeNum) {

        //on vérifie que la commande existe
        var commande = commandeDao.findById(commandeNum).orElseThrow();
        //on vérifie que la commande n'a pas déjà été envoyée
        if(commande.getEnvoyeele() != null)
        {
            throw new IllegalArgumentException("la date d'envoi est déjà enregistrée");
        }
        // On met à jour la date de livraison
        commande.setEnvoyeele(LocalDate.now());

        // On met à jour le nombre d'articles dans l'entrepôt
        var listeLignes = commande.getLignes();
        for (int i = 0; i < listeLignes.size(); i++) {
            var ligne = listeLignes.get(i);
            var produit = ligne.getProduit();
            var produitsEnStock = produit.getUnitesEnStock() - ligne.getQuantite()  ;
            produit.setUnitesEnStock(produitsEnStock);
        }
        return commande;
    }
}
