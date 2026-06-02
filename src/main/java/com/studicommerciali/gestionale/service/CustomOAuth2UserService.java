package com.studicommerciali.gestionale.service;

import com.studicommerciali.gestionale.entity.Utente;
import com.studicommerciali.gestionale.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UtenteRepository utenteRepo;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Lasciamo che Spring faccia la chiamata standard a Google/Microsoft per prendere i dati base
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Estraiamo l'email fornita da Google o Microsoft
        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("email_not_found"), "Impossibile recuperare l'email dal provider.");
        }

        // --- IL CUORE DEL SAAS: CERCHIAMO L'UTENTE E LA SUA AZIENDA NEL NOSTRO DB ---
        // (Nota: in UtenteRepository devi creare il metodo findByEmail)
        Utente utente = utenteRepo.findByEmail(email)
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error("user_not_registered"),
                        "Nessun abbonamento aziendale trovato per l'email: " + email)
                );

        if (!utente.isAttivo() || !utente.getAzienda().isAbbonamentoAttivo()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("account_locked"), "Account o abbonamento aziendale bloccato.");
        }

        // Mappiamo le Authorities (I Ruoli) in base al nostro database
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + utente.getRuolo());

        // Restituiamo a Spring Security l'utente autorizzato.
        // Attenzione: usiamo l'USERNAME del nostro DB come identificativo principale (NameAttributeKey)
        // così nel layout.html `sec:authentication="name"` stamperà il nome corretto!
        Map<String, Object> attributes = oAuth2User.getAttributes();

        return new DefaultOAuth2User(
                Collections.singletonList(authority),
                attributes,
                "email" // Diciamo a Spring di usare l'email come chiave principale per mappare l'utente
        );
    }
}