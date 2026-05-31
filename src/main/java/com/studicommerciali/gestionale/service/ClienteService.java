package com.studicommerciali.gestionale.service;

import com.studicommerciali.gestionale.entity.Cliente;
import com.studicommerciali.gestionale.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository repo;

    public List<Cliente> tutti() {
        return repo.findByAttivoTrueOrderByRagioneSocialeAsc();
    }

    public List<Cliente> cerca(String q) {
        return q == null || q.isBlank() ? tutti() : repo.cerca(q.trim());
    }

    public Cliente trovaPerId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente non trovato: " + id));
    }

    @Transactional
    public Cliente salva(Cliente c) { return repo.save(c); }

    @Transactional
    public void elimina(Long id) {
        Cliente c = trovaPerId(id);
        c.setAttivo(false);   // soft delete
        repo.save(c);
    }
}