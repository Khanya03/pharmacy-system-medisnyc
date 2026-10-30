package za.ac.cput.medisnyc.security;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AppBootId {
    private final String id = UUID.randomUUID().toString();

    public String getId() {
        return id;
    }
}
