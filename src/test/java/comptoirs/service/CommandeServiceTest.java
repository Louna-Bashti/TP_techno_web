package comptoirs.service;

import comptoirs.dao.CommandeRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
 // Ce test est basé sur le jeu de données dans "test_data.sql"
class CommandeServiceTest {
    private static final String ID_PETIT_CLIENT = "0COM";
    private static final String ID_GROS_CLIENT = "2COM";
    private static final String VILLE_PETIT_CLIENT = "Berlin";
    private static final BigDecimal REMISE_POUR_GROS_CLIENT = new BigDecimal("0.15");
    private static final Integer COMMANDE_PAS_LIVREE = 99998;
    private static final Integer COMMANDE_LIVREE = 99999;
    private static final Integer COMMANDE_NON_EXISTANTE = 10;


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

    @Test void miseAJourDateEnvoi(){
        var commandeEnvoyee = service.enregistreExpédition(COMMANDE_PAS_LIVREE);
        assertEquals(LocalDate.now(), commandeEnvoyee.getEnvoyeele(),
                "la date d'envoi n'est pas celle du jour actuel");
    }

    @Test
    void testEnregistrementDateCommande(){
        var commande = commandeRepository.findById(COMMANDE_PAS_LIVREE).orElseThrow();
        service.enregistreExpédition(commande.getNumero());
        assertEquals(LocalDate.now(), commande.getEnvoyeele(), "la commande devrait avoir une date définie");
    }
    @Test
    void commandeDejaEnvoyee(){
        assertThrows(Exception.class, () -> service.enregistreExpédition(COMMANDE_LIVREE),
                "la commande est déjà enregistrée");
    }


    @Test
    void commandeEnregistreeExiste(){
        assertThrows(Exception.class, () -> service.enregistreExpédition(COMMANDE_NON_EXISTANTE),
                "l'id de commande n'existe pas");

    }


}
