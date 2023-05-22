package ylab.bies.ideaservice.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class Decoder {

    private final JwtDecoder jwtDecoder;

    @Autowired
    public Decoder(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    public UUID getUuidFromToken(String token) {
        return UUID.fromString("a81bc81b-dead-4e5d-abff-90865d1e13b1");
//        UUID userId;
//        if (header != null && header.startsWith("Bearer ")) {
//            String token = header.substring(7);
//            Jwt jwt = jwtDecoder.decode(token);
//            userId = UUID.fromString((String) jwt.getClaims().get("sub"));
//        } else {
//            userId = null; // todo заменить на исключение
//        }
//        return userId;
    }
}
