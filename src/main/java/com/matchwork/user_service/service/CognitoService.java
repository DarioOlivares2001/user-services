package com.matchwork.user_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class CognitoService {
    
    private final CognitoIdentityProviderClient cognitoClient;
    
    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;
    
    @Value("${aws.cognito.clientId}")
    private String clientId;
    
    // Si tienes Client Secret, agrégalo aquí
    @Value("${aws.cognito.clientSecret:}")
    private String clientSecret;
    
    public CognitoService(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }
    
    // Registro en Cognito
    public void signUp(String email, String password, String nombre, String rol) {
        try {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("email", email);
            attributes.put("name", nombre);
            attributes.put("custom:rol", rol); // Atributo personalizado
            
            SignUpRequest request = SignUpRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .password(password)
                    .secretHash(calculateSecretHash(email))
                    .userAttributes(
                        attributes.entrySet().stream()
                            .map(entry -> AttributeType.builder()
                                .name(entry.getKey())
                                .value(entry.getValue())
                                .build())
                            .toList()
                    )
                    .build();
            
            cognitoClient.signUp(request);
            
        } catch (Exception e) {
            throw new RuntimeException("Error registrando usuario en Cognito: " + e.getMessage());
        }
    }
    
    // Login en Cognito
    public AdminInitiateAuthResponse signIn(String email, String password) {
        try {
            Map<String, String> authParams = new HashMap<>();
            authParams.put("USERNAME", email);
            authParams.put("PASSWORD", password);
            authParams.put("SECRET_HASH", calculateSecretHash(email));
            
            AdminInitiateAuthRequest request = AdminInitiateAuthRequest.builder()
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .authParameters(authParams)
                    .build();
            
            return cognitoClient.adminInitiateAuth(request);
            
        } catch (Exception e) {
            throw new RuntimeException("Error en login: " + e.getMessage());
        }
    }
    
    // Confirmar registro (código de verificación)
    public void confirmSignUp(String email, String confirmationCode) {
        try {
            ConfirmSignUpRequest request = ConfirmSignUpRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .confirmationCode(confirmationCode)
                    .secretHash(calculateSecretHash(email))
                    .build();
            
            cognitoClient.confirmSignUp(request);
            
        } catch (Exception e) {
            throw new RuntimeException("Error confirmando registro: " + e.getMessage());
        }
    }
    
    // Calcular Secret Hash (si tienes Client Secret configurado)
    private String calculateSecretHash(String username) {
        if (clientSecret == null || clientSecret.isEmpty()) {
            return null;
        }
        
        try {
            String message = username + clientId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec signingKey = new SecretKeySpec(
                clientSecret.getBytes(StandardCharsets.UTF_8), 
                "HmacSHA256"
            );
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error calculando secret hash");
        }
    }
}