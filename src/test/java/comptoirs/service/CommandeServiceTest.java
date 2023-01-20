package comptoirs.service;

import comptoirs.dao.CommandeRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
 // Ce test est basé sur le jeu de données dans "test_data.sql"
class CommandeServiceTest {
    private static final String ID_PETIT_CLIENT = "0COM";
    private static final String ID_GROS_CLIENT = "2COM";
    private static final String VILLE_PETIT_CLIENT = "Berlin";
    private static final BigDecimal REMISE_POUR_GROS_CLIENT = new BigDecimal("0.15");
    private static final int COMMANDE_PAS_LIVREE = 99998;

    @Autowired
    private CommandeService service;
    @Autowired
    private CommandeRepository commandeRepository;

    @Test
    void testCreerCommandePourGrosClient() {
        var commande = service.creerCommande(ID_GROS_CLIENT);
        assertNotNull(commande.getNumero(), "On doit avoir la clé de la commande");
        assertEquals(REMISE_POUR_GROS_CLIENT, commande.getRemise(),
            "Une remise de 15% doit être appliquée pour les gros clients");
    }

    @Test
    void testCreerCommandePourPetitClient() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertNotNull(commande.getNumero());
        assertEquals(BigDecimal.ZERO, commande.getRemise(),
            "Aucune remise ne doit être appliquée pour les petits clients");
    }

    @Test
    void testCreerCommandeInitialiseAdresseLivraison() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertEquals(VILLE_PETIT_CLIENT, commande.getAdresseLivraison().getVille(),
            "On doit recopier l'adresse du client dans l'adresse de livraison");
    }

    @Test
    void testEnregistrementDateCommande(){
        var commande = commandeRepository.findById(COMMANDE_PAS_LIVREE).orElseThrow();
        assertEquals(null,commande.getEnvoyeele(), "la commande devrait avoir une date non définie");
        service.enregistreExpédition(commande.getNumero());
        assertNotNull(commande.getEnvoyeele(), "la commande devrait avoir une date définie");
        service.enregistreExpédition(commande.getNumero());
    }
    @Test
    void testNombreArticlesDansEntrepot() {
        var commande = commandeRepository.findById(COMMANDE_PAS_LIVREE).orElseThrow();
        var ligne = commande.getLignes().get(0);
        assertEquals(100, ligne.getQuantite(), "Il devrait y avoir 100 exemplaires du produit dans la commande");
        var quantite = ligne.getProduit().getUnitesEnStock();
        assertEquals(17, quantite, "la quantité inituale n'est pas la bonne");
        service.enregistreExpédition(commande.getNumero());
        var produit = ligne.getProduit();
        assertEquals(-83, produit.getUnitesEnStock(), "La quantité n'est pas bien décrémentée");


    }
}
