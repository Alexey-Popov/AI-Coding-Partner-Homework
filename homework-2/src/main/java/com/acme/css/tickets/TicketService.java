package com.acme.css.tickets;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class TicketService {

    private final TicketRepository repo;
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();
    private final CsvMapper csvMapper = new CsvMapper();

    public TicketService(TicketRepository repo) {
        this.repo = repo;
    }

    public Ticket create(Ticket ticket) {
        OffsetDateTime now = OffsetDateTime.now();
        if (ticket.getCreated_at() == null) ticket.setCreated_at(now);
        ticket.setUpdated_at(now);
        if (ticket.getStatus() == null) ticket.setStatus(Status.NEW);
        repo.save(ticket);
        return ticket;
    }

    public Optional<Ticket> get(UUID id) {
        return repo.findById(id);
    }

    public List<Ticket> list(Optional<String> category, Optional<String> priority) {
        List<Ticket> all = repo.findAll();
        if (category.isEmpty() && priority.isEmpty()) return all;
        List<Ticket> out = new ArrayList<>();
        for (Ticket t : all) {
            boolean ok = true;
            if (category.isPresent()) {
                try {
                    Category c = Category.forValue(category.get());
                    ok = ok && c.equals(t.getCategory());
                } catch (IllegalArgumentException ignored) { ok = false; }
            }
            if (priority.isPresent()) {
                try {
                    Priority p = Priority.forValue(priority.get());
                    ok = ok && p.equals(t.getPriority());
                } catch (IllegalArgumentException ignored) { ok = false; }
            }
            if (ok) out.add(t);
        }
        return out;
    }

    public Ticket update(UUID id, Ticket update) {
        Ticket existing = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Ticket not found"));
        // apply simple updates
        if (update.getSubject() != null) existing.setSubject(update.getSubject());
        if (update.getDescription() != null) existing.setDescription(update.getDescription());
        if (update.getAssigned_to() != null) existing.setAssigned_to(update.getAssigned_to());
        if (update.getCategory() != null) existing.setCategory(update.getCategory());
        if (update.getPriority() != null) existing.setPriority(update.getPriority());
        if (update.getStatus() != null) existing.setStatus(update.getStatus());
        existing.setUpdated_at(OffsetDateTime.now());
        repo.save(existing);
        return existing;
    }

    public void delete(UUID id) {
        repo.delete(id);
    }

    public ImportResult importFile(MultipartFile file) throws Exception {
        String name = file.getOriginalFilename();
        String lc = name == null ? "" : name.toLowerCase();
        InputStream in = file.getInputStream();
        List<Ticket> parsed = new ArrayList<>();
        if (lc.endsWith(".json") || file.getContentType()!=null && file.getContentType().contains("json")) {
            parsed = jsonMapper.readValue(in, new TypeReference<List<Ticket>>(){});
        } else if (lc.endsWith(".xml") || file.getContentType()!=null && file.getContentType().contains("xml")) {
            // expect a wrapper <tickets><ticket>..</ticket></tickets>
            Map<String,Object> root = xmlMapper.readValue(in, new TypeReference<Map<String,Object>>(){});
            // Try to map 'ticket' or 'tickets' -> list
            Object maybe = root.getOrDefault("tickets", root.get("ticket"));
            if (maybe instanceof List) {
                String asJson = jsonMapper.writeValueAsString(maybe);
                parsed = jsonMapper.readValue(asJson, new TypeReference<List<Ticket>>(){});
            } else if (maybe != null) {
                String asJson = jsonMapper.writeValueAsString(maybe);
                Ticket t = jsonMapper.readValue(asJson, Ticket.class);
                parsed = List.of(t);
            } else {
                // fallback try to map entire document as a single ticket
                in = file.getInputStream();
                Ticket t = xmlMapper.readValue(in, Ticket.class);
                parsed = List.of(t);
            }
        } else if (lc.endsWith(".csv") || file.getContentType()!=null && file.getContentType().contains("csv")) {
            // use CsvMapper
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            List<Map<String,String>> rows = csvMapper.readerFor(new TypeReference<List<Map<String,String>>>(){}).with(schema).readValue(in);
            for (Map<String,String> row : rows) {
                String json = jsonMapper.writeValueAsString(row);
                Ticket t = jsonMapper.readValue(json, Ticket.class);
                parsed.add(t);
            }
        } else {
            throw new IllegalArgumentException("Unsupported file format");
        }

        ImportResult result = new ImportResult();
        result.total = parsed.size();
        for (Ticket t : parsed) {
            try {
                // basic validation via bean validation not wired here; rely on create to set defaults
                create(t);
                result.successful++;
            } catch (Exception e) {
                result.failed++;
                result.errors.add(e.getMessage());
            }
        }
        return result;
    }

    public static class ImportResult {
        public int total = 0;
        public int successful = 0;
        public int failed = 0;
        public List<String> errors = new ArrayList<>();
    }
}
