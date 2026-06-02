# PharmaConnect — Authentication (Complete End-to-End Reference + Full Source)

> **Purpose:** a self-contained dump of how authentication works across the **backend (`pc`, Spring Boot)** and **frontend (`pc-frontend`, Angular 21)** — concepts, flows, *and the full source of every file involved*. Paste this into an LLM as context to answer any auth question about this project.
>
> **State documented:** the **post-removal target state**. Forgot-password / reset-password and the dead "Remember Me" checkbox are being removed, so they are omitted from the source below. Where a file loses members in that removal, a note lists exactly what was dropped. (If you diff against current code you may still see those artifacts pending deletion.)
>
> **Layout:** Part 1 = concepts & flows. Part 2 = full source of every auth file (backend then frontend).

---

# PART 1 — Concepts & Flows

## 1.1 TL;DR mental model

- **Stateful session-cookie authentication via Spring Security** — *not* JWT, not OAuth.
- On login the backend verifies email/password, creates an **HTTP session**, and the servlet container returns a **`JSESSIONID` cookie**. Every later request carries that cookie (the Angular interceptor forces `withCredentials: true`), and Spring Security restores the user from the session.
- Passwords are stored as **BCrypt** hashes (`password_hash`). Plaintext is never stored.
- **Three roles:** `BUYER` (patient, default at registration), `SELLER` (pharmacy), `ADMIN`. Authorization is enforced **server-side** in `SecurityConfig`.
- The frontend mirrors the user (`id/email/name/role/...`) in **`localStorage`** — that is **UI-only** state (navbar, redirects, route guards). It is **not** the security boundary; the server session + `SecurityConfig` are.

```
  Browser (Angular)                          Server (Spring Boot)
  ─────────────────                          ────────────────────
  login form ──POST /api/auth/login────────▶ AuthController.login
   (interceptor: withCredentials)             └─ AuthenticationManager
                                                 └─ AppUserDetailsService (load User by email)
                                                 └─ BCrypt password check
                                              store SecurityContext in HTTP session
  ◀──── 200 + User JSON + Set-Cookie: JSESSIONID
  localStorage ← user fields (UI state)
  ...
  any request ──Cookie: JSESSIONID─────────▶ SecurityFilterChain
                                              └─ restore SecurityContext from session
                                              └─ authorizeHttpRequests (role checks)
  logout ──POST /api/auth/logout───────────▶ Spring logout DSL: invalidate session,
  localStorage.clear()                          delete JSESSIONID, 200
```

## 1.2 Roles

| Role | Meaning | Assigned when |
|------|---------|---------------|
| `BUYER` | Patient / end-user | `AuthService.registerNewUser` sets `role="BUYER"` for every new account |
| `SELLER` | Pharmacy operator | Upgraded during onboarding via `POST /api/seller-onboarding/register-pharmacy` |
| `ADMIN` | Platform admin | Provisioned out-of-band (no public path) |

**Authority mapping:** `AppUserDetailsService` does `.roles(u.getRole())`; Spring prepends `ROLE_`, so `SELLER` → `ROLE_SELLER`. `SecurityConfig` uses `.hasRole("SELLER")` (checks `ROLE_SELLER`). Consistent.

## 1.3 Key concepts (as used here)

- **SecurityFilterChain** — applies CORS, session, and authorization rules to every request.
- **AuthenticationManager → ProviderManager → DaoAuthenticationProvider** — verifies credentials using a `UserDetailsService` + `PasswordEncoder`.
- **UserDetailsService (`AppUserDetailsService`)** — loads the app `User` into Spring's `UserDetails` (username=email, password=hash, role).
- **PasswordEncoder (`BCryptPasswordEncoder`)** — hash/verify passwords.
- **SecurityContext / SecurityContextHolder** — holds the authenticated principal; on login it's explicitly saved into the HTTP session.
- **JSESSIONID** — the session cookie; the de-facto credential.
- **CORS with credentials** — backend `allowCredentials(true)` + frontend `withCredentials`; both required for the cookie to flow cross-origin.
- **Functional route guards (`CanActivateFn`)** — client-side redirects from `localStorage` role; **UX only**.
- **HTTP interceptor (`HttpInterceptorFn`)** — adds `withCredentials` to every request.

## 1.4 End-to-end flows

**Registration (buyer):** `register.ts` validates → `POST /api/auth/register` → `AuthService.registerNewUser` (dup-check, `role=BUYER`, BCrypt) → `201` → FE auto-`login()` → `/search`.

**Seller onboarding:** `register-pharmacy.ts` → `register()` (creates BUYER) → `registerPharmacy()` → `POST /api/seller-onboarding/register-pharmacy` (creates `Pharmacy`, **upgrades role to SELLER**) → `login()` → `/seller/dashboard`.

**Login:** `login.ts` → `POST /api/auth/login` (`withCredentials`) → `AuthenticationManager.authenticate` → `AppUserDetailsService` loads user → BCrypt check → build `SecurityContext`, store in session → `Set-Cookie: JSESSIONID` → return `User` JSON → FE stores user in `localStorage`, redirects by role (`ADMIN`→`/admin/sellers`, `SELLER`→`/seller/dashboard`, else `/search`). Bad creds → `401`.

**Authenticated request:** interceptor adds `withCredentials` → cookie sent → filter chain restores context → role rule checked (e.g. `/api/inventory/** → hasRole('SELLER')`). Missing session → 401/403; wrong role → 403.

**Logout:** `AuthService.logout()` → `POST /api/auth/logout` (Spring logout DSL: invalidate session, delete `JSESSIONID`, 200) → FE `localStorage.clear()`.

**Route guarding:** guards read `localStorage.userRole` for instant redirects (UX). Real protection is the role-gated APIs; a spoofed `localStorage` yields empty/forbidden pages, not data.

## 1.5 Authorization matrix (server-enforced, from `SecurityConfig`)

| Matcher | Access |
|---|---|
| `/api/auth/login`, `/api/auth/register`, `/api/auth/logout` | `permitAll` |
| `/api/search/**` | `permitAll` |
| `/api/auth/medicines/**` | `permitAll` |
| `/api/seller-onboarding/**` | `permitAll` |
| `GET /api/admin/medicines` | `authenticated` |
| `GET /api/seller-portal/*/analytics` | `authenticated` |
| `/api/admin/**`, `/api/auth/admin/**` | `hasRole('ADMIN')` |
| `/api/auth/seller/**`, `/api/inventory/**`, `/api/seller-portal/**`, `/api/pharmacies/**` | `hasRole('SELLER')` |
| everything else | `authenticated` |

## 1.6 Gotchas (needed for correct answers)

1. **Session, not JWT.** No bearer token for app auth; the credential is `JSESSIONID` + server session.
2. **`localStorage` is not security.** It's UI state; it can't grant access to protected APIs.
3. **Guards are cosmetic.** UX redirects only; enforcement is server-side.
4. **CSRF is disabled** (`csrf().disable()`). Cookie auth + CORS is the current containment.
5. **Cross-site cookie caveat.** Local `:4200`/`:10000` are same-site (works). Across Codespaces subdomains it's cross-site → `JSESSIONID` needs `SameSite=None; Secure` to flow. First thing to check if sessions don't persist there.
6. **`AuthService.authenticateUser` is unused** — login uses `AuthenticationManager`, not this method.
7. **Default role `BUYER`**; SELLER only via onboarding; ADMIN out-of-band.
8. **Dead form fields:** `register` collects city/address/pincode but only sends name/email/password/phone. `register-pharmacy` collects licenseNumber/gstNumber/operatingHours but never sends them. (Documented so the LLM doesn't assume they're persisted.)

---

# PART 2 — Full source of every auth file

> Backend root: `pc/src/main/java/com/cts/mfrp/pc/`. Frontend root: `pc-frontend/src/`.
> Files shown in their **post-removal** form.

## 2.1 Backend

### `model/User.java` — user entity (table `users`)
> Removed in post-removal: `resetToken`, `resetTokenExpiry` fields.
```java
package com.cts.mfrp.pc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private String id;

    private String name;

    @Column(unique = true)
    private String email;

    private String phone;

    @Column(name = "password_hash")
    private String passwordHash;

    private String role;
    private Float lat;
    private Float lng;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

### `repository/UserRepository.java`
> Removed in post-removal: `findByResetToken`.
```java
package com.cts.mfrp.pc.repository;

import com.cts.mfrp.pc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
}
```

### `dto/LoginRequest.java`
```java
package com.cts.mfrp.pc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
           message = "Please provide a valid email address (e.g. john@example.com)")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
```

### `dto/RegistrationRequest.java`
```java
package com.cts.mfrp.pc.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegistrationRequest {

    @NotBlank(message = "Full name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
           message = "Please provide a valid email address (e.g. john@example.com)")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;
}
```

### `config/AppUserDetailsService.java` — Spring Security ↔ User bridge
```java
package com.cts.mfrp.pc.config;

import com.cts.mfrp.pc.model.User;
import com.cts.mfrp.pc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user with email: " + email));
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .roles(u.getRole())   // "BUYER"/"SELLER"/"ADMIN" -> ROLE_*
                .build();
    }
}
```

### `config/SecurityConfig.java` — filter chain, authz, CORS, logout, beans
> Removed in post-removal: the `/api/auth/forgot-password` and `/api/auth/reset-password` entries from `permitAll`.
```java
package com.cts.mfrp.pc.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.cors.origins:http://localhost:4200}")
    private String allowedOrigins;

    private final AppUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new org.springframework.security.authentication.ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // public auth endpoints
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/logout"
                        ).permitAll()
                        // public search + price compare + bootstrap signup flow
                        .requestMatchers("/api/search/**").permitAll()
                        .requestMatchers("/api/auth/medicines/**").permitAll()
                        .requestMatchers("/api/seller-onboarding/**").permitAll()
                        // shared endpoints (declared BEFORE the broad role rules)
                        .requestMatchers(HttpMethod.GET, "/api/admin/medicines").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/seller-portal/*/analytics").authenticated()
                        // admin
                        .requestMatchers("/api/admin/**", "/api/auth/admin/**").hasRole("ADMIN")
                        // seller
                        .requestMatchers(
                                "/api/auth/seller/**",
                                "/api/inventory/**",
                                "/api/seller-portal/**",
                                "/api/pharmacies/**"
                        ).hasRole("SELLER")
                        // anything else needs to be logged in
                        .anyRequest().authenticated()
                )
                .logout(l -> l
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((req, res, auth) -> res.setStatus(200))
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        configuration.setAllowedOriginPatterns(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### `controller/AuthController.java` — `/api/auth/login`, `/register`
> Removed in post-removal: `forgotPassword` and `resetPassword` endpoints. (Logout is handled by the Spring logout DSL in `SecurityConfig`, not here.)
```java
package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.LoginRequest;
import com.cts.mfrp.pc.dto.RegistrationRequest;
import com.cts.mfrp.pc.model.User;
import com.cts.mfrp.pc.repository.UserRepository;
import com.cts.mfrp.pc.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            request.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
            return ResponseEntity.ok(user);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid credentials. Please try again.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest regRequest) {
        try {
            User newUser = authService.registerNewUser(regRequest);
            return ResponseEntity.status(201).body(Map.of("message", "Registration successful", "email", newUser.getEmail()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
```

### `service/AuthService.java`
> Removed in post-removal (full-clean): `processForgotPassword`, `resetPassword`, the `EmailService` field + import, and the now-unused `UUID`/`LocalDateTime` imports. (If "keep email infra" is chosen instead, the `EmailService` field remains but is unused.) `authenticateUser` is retained but is dead code — login does not use it.
```java
package com.cts.mfrp.pc.service;

import com.cts.mfrp.pc.dto.RegistrationRequest;
import com.cts.mfrp.pc.model.User;
import com.cts.mfrp.pc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // NOTE: not used by the login flow (login goes through AuthenticationManager +
    // AppUserDetailsService). Effectively dead code; kept as-is.
    public User authenticateUser(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authentication failed: User not found."));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials. Please try again.");
        }
        return user;
    }

    public User registerNewUser(RegistrationRequest regRequest) {
        if (userRepository.findByEmail(regRequest.getEmail()).isPresent()) {
            throw new RuntimeException("An account with this email already exists.");
        }

        User user = new User();
        user.setName(regRequest.getName());
        user.setEmail(regRequest.getEmail());
        user.setPhone(regRequest.getPhone());
        user.setRole("BUYER");
        user.setPasswordHash(passwordEncoder.encode(regRequest.getPassword()));

        return userRepository.save(user);
    }
}
```

### `controller/PharmacyRegistrationController.java` — seller onboarding (role upgrade)
```java
package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.SellerPharmacyRegistrationRequestDto;
import com.cts.mfrp.pc.model.Pharmacy;
import com.cts.mfrp.pc.service.PharmacyRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller-onboarding")
public class PharmacyRegistrationController {

    private final PharmacyRegistrationService pharmacyRegistrationService;

    public PharmacyRegistrationController(PharmacyRegistrationService pharmacyRegistrationService) {
        this.pharmacyRegistrationService = pharmacyRegistrationService;
    }

    @PostMapping("/register-pharmacy")
    public ResponseEntity<?> registerPharmacyAndUpgradeUserRole(@Valid @RequestBody SellerPharmacyRegistrationRequestDto registrationRequestDto) {
        try {
            Pharmacy successfullyRegisteredPharmacy = pharmacyRegistrationService.registerNewPharmacyForSeller(registrationRequestDto);
            return ResponseEntity.ok(successfullyRegisteredPharmacy);
        } catch (Exception exception) {
            return ResponseEntity.badRequest().body("Failed to complete seller onboarding: " + exception.getMessage());
        }
    }
}
```
> `registerNewPharmacyForSeller(dto)` creates the `Pharmacy` and upgrades the linked user's role from `BUYER` to `SELLER`. The endpoint is `permitAll` because it runs immediately after account creation, before the user can authenticate as a seller.

### `resources/application.properties` — auth-relevant config
> Removed in post-removal (full-clean): `gemini.*` (chatbot) and `brevo.api.key` (reset email). Shown without them.
```properties
spring.application.name=pharmaconnect
server.port=${PORT:10000}
spring.main.lazy-initialization=true

# DATABASE (MySQL)
spring.datasource.url=${MYSQL_URL:jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:pharmaconnect}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}
spring.datasource.username=${MYSQL_USERNAME:${DB_USER:root}}
spring.datasource.password=${MYSQL_PASSWORD:${DB_PASSWORD:****}}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / HIBERNATE
spring.jpa.hibernate.ddl-auto=update      # NOTE: 'update' never DROPS columns — removed entity fields leave their DB columns behind
spring.jpa.show-sql=true

# CORS (consumed by SecurityConfig)
app.cors.origins=${CORS_ORIGINS:http://localhost:4200,https://*.app.github.dev,https://YOUR-FRONTEND-NAME.onrender.com}

# FILE UPLOADS
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
```

## 2.2 Frontend

### `src/environments/environment.ts` — runtime API base URL
```ts
function deriveApiBaseUrl(): string {
  if (typeof window === 'undefined') return 'http://localhost:10000';
  const host = window.location.host;
  if (host.endsWith('.app.github.dev') && host.includes('-4200.')) {
    return `${window.location.protocol}//${host.replace('-4200.', '-10000.')}`;
  }
  return 'http://localhost:10000';
}

export const environment = {
  apiBaseUrl: deriveApiBaseUrl(),
};
```

### `src/app/app.config.ts` — DI providers (registers the interceptor)
```ts
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { credentialsInterceptor } from './interceptors/credentials.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([credentialsInterceptor]))
  ]
};
```

### `src/app/interceptors/credentials.interceptor.ts` — sends the session cookie
```ts
import { HttpInterceptorFn } from '@angular/common/http';

export const credentialsInterceptor: HttpInterceptorFn = (req, next) =>
  next(req.clone({ withCredentials: true }));
```

### `src/app/services/auth.service.ts` — auth API client + localStorage session state
> Removed in post-removal: `forgotPassword` and `resetPassword` methods.
```ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private base = `${environment.apiBaseUrl}/api/auth`;
  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.base}/login`, { email, password }).pipe(
      tap(user => {
        localStorage.setItem('userId', user.id);
        localStorage.setItem('userEmail', user.email);
        localStorage.setItem('userName', user.name);
        localStorage.setItem('userRole', user.role);
        if (user.phone) localStorage.setItem('userPhone', user.phone);
        if (user.createdAt) localStorage.setItem('userCreatedAt', user.createdAt);
      })
    );
  }

  register(name: string, email: string, password: string, phone: string): Observable<any> {
    return this.http.post(`${this.base}/register`, { name, email, password, phone }, { responseType: 'text' }) as Observable<any>;
  }

  registerPharmacy(dto: {
    pharmacyName: string;
    pharmacyAddress: string;
    contactPhoneNumber: string;
    locationLatitude: number | null;
    locationLongitude: number | null;
    isOperated247: boolean;
    sellerEmailAddress: string;
  }): Observable<any> {
    return this.http.post<any>(`${environment.apiBaseUrl}/api/seller-onboarding/register-pharmacy`, dto);
  }

  logout(): void {
    this.http.post(`${this.base}/logout`, {}, { responseType: 'text' }).subscribe({
      next: () => localStorage.clear(),
      error: () => localStorage.clear()
    });
  }

  getCurrentUser() {
    return {
      id: localStorage.getItem('userId'),
      email: localStorage.getItem('userEmail'),
      name: localStorage.getItem('userName'),
      role: localStorage.getItem('userRole'),
      phone: localStorage.getItem('userPhone'),
      createdAt: localStorage.getItem('userCreatedAt')
    };
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('userId');
  }

  getRole(): string | null {
    return localStorage.getItem('userRole');
  }
}
```

### `src/app/components/auth/login/login.ts`
> Removed in post-removal: the `rememberMe` field (dead/cosmetic).
```ts
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  email = '';
  password = '';
  errorMessage = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit() {
    if (!this.email || !this.password) {
      this.errorMessage = 'Please fill in all fields.';
      return;
    }
    this.loading = true;
    this.errorMessage = '';
    this.authService.login(this.email, this.password).subscribe({
      next: (user) => this.redirectByRole(user.role),
      error: (err) => {
        this.loading = false;
        this.errorMessage = typeof err.error === 'string' ? err.error : 'Invalid email or password.';
      }
    });
  }

  private redirectByRole(role: string) {
    if (role === 'ADMIN') this.router.navigate(['/admin/sellers']);
    else if (role === 'SELLER') this.router.navigate(['/seller/dashboard']);
    else this.router.navigate(['/search']);
  }
}
```

### `src/app/components/auth/login/login.html`
> Removed in post-removal: the `.pc-remember` block (Remember-Me checkbox + "Forgot password?" link).
```html
<div class="pc-auth-page">

  <a class="pc-auth-back" href="/">
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><polyline points="15 18 9 12 15 6"/></svg>
    Back to Home
  </a>

  <div class="pc-auth-card">

    <div class="pc-auth-icon">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <path d="M4 14h16"/><path d="M6 14c0 3 2.5 5 6 5s6-2 6-5"/><path d="M12 14 C 14 12 14 10 12 9 C 10 8 10 6 12 5"/><circle cx="12" cy="4.5" r="1.2" fill="currentColor"/>
      </svg>
    </div>

    <h1 class="pc-auth-title">Welcome back</h1>
    <p class="pc-auth-sub">Sign in to your PharmaConnect account</p>

    @if (errorMessage) {
      <div class="pc-alert pc-alert-error">{{ errorMessage }}</div>
    }

    <form (ngSubmit)="onSubmit()">
      <div class="pc-form-group" style="margin-bottom:1rem">
        <label class="pc-label" for="email">Email Address</label>
        <input id="email" class="pc-input" type="email" [(ngModel)]="email" name="email" placeholder="user@example.com" required />
        <span class="pc-hint">Example: john.doe&#64;email.com</span>
      </div>

      <div class="pc-form-group" style="margin-bottom:0.5rem">
        <label class="pc-label" for="password">Password</label>
        <input id="password" class="pc-input" type="password" [(ngModel)]="password" name="password" placeholder="Enter your password" required />
      </div>

      <button type="submit" class="pc-auth-submit" [disabled]="loading">
        @if (loading) { Signing in... } @else { Login }
      </button>
    </form>

    <div class="pc-auth-switch" style="margin-top:1.25rem">
      Don't have an account? <a routerLink="/register">Sign Up</a>
    </div>

  </div>
</div>
```

### `src/app/components/auth/register/register.ts`
```ts
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-register',
  imports: [FormsModule, RouterLink, CommonModule],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {
  name = '';
  email = '';
  phone = '';
  city = '';        // collected but NOT sent to backend
  address = '';     // collected but NOT sent to backend
  pincode = '';     // collected but NOT sent to backend
  password = '';
  confirmPassword = '';
  agreedToTerms = false;

  errorMessage = '';
  loading = false;
  registered = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit() {
    if (!this.name || !this.email || !this.phone || !this.password || !this.confirmPassword) {
      this.errorMessage = 'Please fill in all required fields.';
      return;
    }
    if (!/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(this.email.trim())) {
      this.errorMessage = 'Please enter a valid email address (e.g. john@example.com).';
      return;
    }
    if (!/^[0-9]{10}$/.test(this.phone)) {
      this.errorMessage = 'Phone must be exactly 10 digits (e.g. 9876543210).';
      return;
    }
    if (this.password.length < 8) {
      this.errorMessage = 'Password must be at least 8 characters.';
      return;
    }
    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }
    if (!this.agreedToTerms) {
      this.errorMessage = 'You must agree to the Terms of Service to continue.';
      return;
    }
    this.loading = true;
    this.errorMessage = '';

    this.authService.register(this.name, this.email, this.password, this.phone).subscribe({
      next: () => {
        this.authService.login(this.email, this.password).subscribe({
          next: () => {
            this.loading = false;
            this.registered = true;
            setTimeout(() => this.router.navigate(['/search']), 3000);
          },
          error: () => {
            this.loading = false;
            this.registered = true;
            setTimeout(() => this.router.navigate(['/login']), 3000);
          }
        });
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = typeof err.error === 'string' ? err.error : 'Registration failed. Check phone (10 digits) and password (min 8 chars).';
      }
    });
  }
}
```

### `src/app/components/auth/register/register.html`
```html
@if (registered) {
  <div class="pc-auth-page">
    <div class="pc-auth-card" style="text-align:center;padding:3rem 2rem;max-width:420px">
      <div class="reg-success-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" width="36" height="36"><polyline points="20 6 9 17 4 12"/></svg>
      </div>
      <h1 class="pc-auth-title" style="margin-bottom:0.75rem">Account Created!</h1>
      <p style="color:var(--text-muted);margin-bottom:0.5rem;line-height:1.6">Welcome to PharmaConnect! Your patient account is ready.</p>
      <p style="color:var(--text-muted);font-size:0.875rem">Redirecting you to search in a moment...</p>
    </div>
  </div>
} @else {

<div class="pc-auth-page">
  <a class="pc-auth-back" href="/">
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><polyline points="15 18 9 12 15 6"/></svg>
    Back to Home
  </a>

  <div class="pc-auth-card pc-auth-card-lg">
    <div class="pc-auth-icon">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <path d="M4 14h16"/><path d="M6 14c0 3 2.5 5 6 5s6-2 6-5"/><path d="M12 14 C 14 12 14 10 12 9 C 10 8 10 6 12 5"/><circle cx="12" cy="4.5" r="1.2" fill="currentColor"/>
      </svg>
    </div>

    <h1 class="pc-auth-title">Create User Account</h1>
    <p class="pc-auth-sub">Join PharmaConnect to find and reserve medicines</p>

    @if (errorMessage) { <div class="pc-alert pc-alert-error">{{ errorMessage }}</div> }

    <form (ngSubmit)="onSubmit()">
      <div class="pc-auth-form-grid">
        <div class="pc-form-group">
          <label class="pc-label">Full Name *</label>
          <input class="pc-input" type="text" [(ngModel)]="name" name="name" placeholder="John Doe" required />
        </div>
        <div class="pc-form-group">
          <label class="pc-label">Email Address *</label>
          <input class="pc-input" type="email" [(ngModel)]="email" name="email" placeholder="john@example.com" required />
        </div>
        <div class="pc-form-group">
          <label class="pc-label">Phone Number *</label>
          <input class="pc-input" type="tel" [(ngModel)]="phone" name="phone" placeholder="+91 98765 43210" required />
        </div>
        <div class="pc-form-group">
          <label class="pc-label">City</label>
          <input class="pc-input" type="text" [(ngModel)]="city" name="city" placeholder="Bangalore" />
        </div>
        <div class="pc-form-group pc-auth-form-full">
          <label class="pc-label">Address</label>
          <input class="pc-input" type="text" [(ngModel)]="address" name="address" placeholder="123 Main Street" />
        </div>
        <div class="pc-form-group">
          <label class="pc-label">Pincode</label>
          <input class="pc-input" type="text" [(ngModel)]="pincode" name="pincode" placeholder="560001" />
        </div>
        <div class="pc-form-group"></div>
        <div class="pc-form-group">
          <label class="pc-label">Password *</label>
          <input class="pc-input" type="password" [(ngModel)]="password" name="password" placeholder="Create password" required />
        </div>
        <div class="pc-form-group">
          <label class="pc-label">Confirm Password *</label>
          <input class="pc-input" type="password" [(ngModel)]="confirmPassword" name="confirmPassword" placeholder="Re-enter password" required />
        </div>
      </div>

      <div class="pc-terms" style="margin-bottom:1.25rem">
        <input type="checkbox" [(ngModel)]="agreedToTerms" name="agreedToTerms" id="terms" />
        <label for="terms">I agree to the Terms of Service and Privacy Policy...</label>
      </div>

      <button type="submit" class="pc-auth-submit" [disabled]="loading">
        @if (loading) { Creating account... } @else { Create Account }
      </button>
    </form>

    <div class="pc-auth-switch" style="margin-top:1.25rem">
      Already have an account? <a routerLink="/login">Sign In</a>
    </div>
  </div>
</div>

}
```

### `src/app/components/auth/register-pharmacy/register-pharmacy.ts`
```ts
import { Component, NgZone } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-register-pharmacy',
  imports: [FormsModule, RouterLink, CommonModule],
  templateUrl: './register-pharmacy.html',
  styleUrl: './register-pharmacy.css'
})
export class RegisterPharmacy {
  pharmacyName = '';
  ownerName = '';
  licenseNumber = '';   // collected but NOT sent to backend
  gstNumber = '';       // collected but NOT sent to backend
  email = '';
  phone = '';
  address = '';
  city = '';
  pincode = '';
  operatingHours = '';  // collected but NOT sent to backend
  isOperated247 = false;
  locationLatitude: number | null = null;
  locationLongitude: number | null = null;
  password = '';
  confirmPassword = '';
  agreedToTerms = false;

  loading = false;
  locating = false;
  errorMessage = '';
  registered = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private ngZone: NgZone
  ) {}

  useMyLocation() {
    if (!navigator.geolocation) {
      this.errorMessage = 'Geolocation is not supported by your browser.';
      return;
    }
    this.locating = true;
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        this.ngZone.run(() => {
          this.locationLatitude = parseFloat(pos.coords.latitude.toFixed(6));
          this.locationLongitude = parseFloat(pos.coords.longitude.toFixed(6));
          this.locating = false;
        });
      },
      () => {
        this.ngZone.run(() => {
          this.locating = false;
          this.errorMessage = 'Could not get location. Coordinates are optional.';
        });
      }
    );
  }

  onSubmit() {
    if (!this.pharmacyName || !this.ownerName || !this.email || !this.phone ||
        !this.address || !this.password || !this.confirmPassword) {
      this.errorMessage = 'Please fill in all required fields.';
      return;
    }
    if (!/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(this.email.trim())) {
      this.errorMessage = 'Please enter a valid email address (e.g. contact@pharmacy.com).';
      return;
    }
    if (!/^[0-9]{10}$/.test(this.phone)) {
      this.errorMessage = 'Phone must be exactly 10 digits (e.g. 9876543210).';
      return;
    }
    if (this.password.length < 8) {
      this.errorMessage = 'Password must be at least 8 characters.';
      return;
    }
    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }
    if (!this.agreedToTerms) {
      this.errorMessage = 'You must confirm the terms to register.';
      return;
    }

    const fullAddress = [this.address, this.city, this.pincode].filter(Boolean).join(', ');

    this.loading = true;
    this.errorMessage = '';

    // Chain: create BUYER account -> create pharmacy (upgrades to SELLER) -> login
    this.authService.register(this.ownerName, this.email, this.password, this.phone).subscribe({
      next: () => {
        this.authService.registerPharmacy({
          pharmacyName: this.pharmacyName,
          pharmacyAddress: fullAddress,
          contactPhoneNumber: this.phone,
          locationLatitude: this.locationLatitude,
          locationLongitude: this.locationLongitude,
          isOperated247: this.isOperated247,
          sellerEmailAddress: this.email
        }).subscribe({
          next: () => {
            this.authService.login(this.email, this.password).subscribe({
              next: () => {
                this.loading = false;
                this.registered = true;
                setTimeout(() => this.router.navigate(['/seller/dashboard']), 3500);
              },
              error: () => {
                this.loading = false;
                this.registered = true;
                setTimeout(() => this.router.navigate(['/login']), 3500);
              }
            });
          },
          error: (err) => {
            this.loading = false;
            this.errorMessage = typeof err.error === 'string' ? err.error : 'Pharmacy registration failed. Please try again.';
          }
        });
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = typeof err.error === 'string' ? err.error : 'Account creation failed. Please try again.';
      }
    });
  }
}
```

### `src/app/components/auth/register-pharmacy/register-pharmacy.html`
```html
@if (registered) {
  <div class="pc-auth-page">
    <div class="pc-auth-card" style="text-align:center;padding:3rem 2rem;max-width:480px">
      <div class="reg-success-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" width="36" height="36"><polyline points="20 6 9 17 4 12"/></svg>
      </div>
      <h1 class="pc-auth-title">Pharmacy Registered!</h1>
      <p style="color:var(--text-muted);line-height:1.6">Your pharmacy has been submitted for admin verification...</p>
      <div class="pc-alert pc-alert-warn" style="text-align:left">Pending admin approval — your pharmacy will appear in search results once verified.</div>
      <p style="color:var(--text-muted);font-size:0.875rem">Taking you to your dashboard...</p>
    </div>
  </div>
} @else {

<div class="pc-auth-page">
  <a class="pc-auth-back" href="/">
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><polyline points="15 18 9 12 15 6"/></svg>
    Back to Home
  </a>

  <div class="pc-auth-card pc-auth-card-lg">
    <div class="pc-auth-icon">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
    </div>

    <h1 class="pc-auth-title">Register Your Pharmacy</h1>
    <p class="pc-auth-sub">Join PharmaConnect and reach more customers</p>

    @if (errorMessage) { <div class="pc-alert pc-alert-error">{{ errorMessage }}</div> }

    <form (ngSubmit)="onSubmit()">
      <div class="pc-section-hdr"><h3>Pharmacy Information</h3><p>Details about your pharmacy business</p></div>
      <div class="pc-auth-form-grid">
        <div class="pc-form-group"><label class="pc-label">Pharmacy Name *</label><input class="pc-input" type="text" [(ngModel)]="pharmacyName" name="pharmacyName" required /></div>
        <div class="pc-form-group"><label class="pc-label">Owner Name *</label><input class="pc-input" type="text" [(ngModel)]="ownerName" name="ownerName" required /></div>
        <div class="pc-form-group"><label class="pc-label">Pharmacy License Number *</label><input class="pc-input" type="text" [(ngModel)]="licenseNumber" name="licenseNumber" /></div>
        <div class="pc-form-group"><label class="pc-label">GST Number *</label><input class="pc-input" type="text" [(ngModel)]="gstNumber" name="gstNumber" /></div>
      </div>

      <div class="pc-section-hdr"><h3>Contact Information</h3><p>How customers can reach you</p></div>
      <div class="pc-auth-form-grid">
        <div class="pc-form-group"><label class="pc-label">Email Address *</label><input class="pc-input" type="email" [(ngModel)]="email" name="email" required /></div>
        <div class="pc-form-group"><label class="pc-label">Phone Number *</label><input class="pc-input" type="tel" [(ngModel)]="phone" name="phone" required /></div>
        <div class="pc-form-group"><label class="pc-label">Address *</label><input class="pc-input" type="text" [(ngModel)]="address" name="address" required /></div>
        <div class="pc-form-group"><label class="pc-label">City *</label><input class="pc-input" type="text" [(ngModel)]="city" name="city" /></div>
        <div class="pc-form-group"><label class="pc-label">Pincode *</label><input class="pc-input" type="text" [(ngModel)]="pincode" name="pincode" /></div>
        <div class="pc-form-group"><label class="pc-label">Operating Hours *</label><input class="pc-input" type="text" [(ngModel)]="operatingHours" name="operatingHours" /></div>
      </div>

      <div class="pc-check-row"><input type="checkbox" [(ngModel)]="isOperated247" name="isOperated247" id="open247" /><label for="open247">My pharmacy is open 24x7</label></div>

      <div class="pc-auth-form-grid">
        <div class="pc-form-group"><label class="pc-label">Latitude (optional)</label><input class="pc-input" type="number" [(ngModel)]="locationLatitude" name="locationLatitude" step="any" /></div>
        <div class="pc-form-group"><label class="pc-label">Longitude (optional)</label><input class="pc-input" type="number" [(ngModel)]="locationLongitude" name="locationLongitude" step="any" /></div>
        <div class="pc-form-group pc-auth-form-full">
          <button type="button" class="pc-btn pc-btn-outline" (click)="useMyLocation()" [disabled]="locating">
            {{ locating ? 'Detecting location...' : 'Use My Current Location' }}
          </button>
        </div>
      </div>

      <div class="pc-section-hdr"><h3>Account Security</h3><p>Set up your login credentials</p></div>
      <div class="pc-auth-form-grid">
        <div class="pc-form-group"><label class="pc-label">Password *</label><input class="pc-input" type="password" [(ngModel)]="password" name="password" required /></div>
        <div class="pc-form-group"><label class="pc-label">Confirm Password *</label><input class="pc-input" type="password" [(ngModel)]="confirmPassword" name="confirmPassword" required /></div>
      </div>

      <div class="pc-terms"><input type="checkbox" [(ngModel)]="agreedToTerms" name="agreedToTerms" id="terms" /><label for="terms">I confirm that all information provided is accurate...</label></div>

      <button type="submit" class="pc-auth-submit" [disabled]="loading">
        @if (loading) { Registering... } @else { Register Pharmacy }
      </button>
    </form>

    <div class="pc-auth-switch"><span>Already registered?</span> <a routerLink="/login">Login</a></div>
  </div>
</div>

}
```

### `src/app/guards/auth.guard.ts`
```ts
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isLoggedIn()) return true;
  return router.createUrlTree(['/login']);
};
```

### `src/app/guards/guest.guard.ts`
```ts
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// Redirects already-logged-in users away from auth pages to their role's home
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isLoggedIn()) return true;
  const role = auth.getRole();
  if (role === 'ADMIN') return router.createUrlTree(['/admin/sellers']);
  if (role === 'SELLER') return router.createUrlTree(['/seller/dashboard']);
  return router.createUrlTree(['/search']);
};
```

### `src/app/guards/seller.guard.ts`
```ts
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const sellerGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.getRole() === 'SELLER') return true;
  return router.createUrlTree(auth.isLoggedIn() ? ['/'] : ['/login']);
};
```

### `src/app/guards/admin.guard.ts`
```ts
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.getRole() === 'ADMIN') return true;
  return router.createUrlTree(auth.isLoggedIn() ? ['/'] : ['/login']);
};
```

### `src/app/app.routes.ts` — guard wiring
> Removed in post-removal: the `forgot-password`, `reset-password`, and `chatbot` imports + routes.
```ts
import { Routes } from '@angular/router';
import { LandingPage } from './components/landing-page/landing-page';
import { Login } from './components/auth/login/login';
import { Register } from './components/auth/register/register';
import { RegisterPharmacy } from './components/auth/register-pharmacy/register-pharmacy';
import { SearchPage } from './components/patient/search/search';
import { MyReservations } from './components/patient/my-reservations/my-reservations';
import { SellerDashboard } from './components/seller/dashboard/seller-dashboard';
import { SellerInventory } from './components/seller/inventory/seller-inventory';
import { SellerReservations } from './components/seller/reservations/seller-reservations';
import { SellerDocuments } from './components/seller/documents/seller-documents';
import { AdminSellers } from './components/admin/sellers/admin-sellers';
import { AdminMedicines } from './components/admin/medicines/admin-medicines';
import { AdminAnalytics } from './components/admin/analytics/admin-analytics';
import { AdminDocuments } from './components/admin/documents/admin-documents';
import { AdminProfile } from './components/admin/profile/admin-profile';
import { SellerProfile } from './components/seller/profile/seller-profile';
import { Profile } from './components/patient/profile/profile';
import { authGuard } from './guards/auth.guard';
import { sellerGuard } from './guards/seller.guard';
import { adminGuard } from './guards/admin.guard';
import { guestGuard } from './guards/guest.guard';
import { NotFound } from './components/not-found/not-found';

export const routes: Routes = [
  { path: '', component: LandingPage },
  { path: 'login', component: Login, canActivate: [guestGuard] },
  { path: 'register', component: Register, canActivate: [guestGuard] },
  { path: 'register-pharmacy', component: RegisterPharmacy, canActivate: [guestGuard] },
  { path: 'search', component: SearchPage },
  { path: 'my-reservations', component: MyReservations, canActivate: [authGuard] },
  { path: 'seller/dashboard', component: SellerDashboard, canActivate: [sellerGuard] },
  { path: 'seller/inventory', component: SellerInventory, canActivate: [sellerGuard] },
  { path: 'seller/reservations', component: SellerReservations, canActivate: [sellerGuard] },
  { path: 'seller/documents', component: SellerDocuments, canActivate: [sellerGuard] },
  { path: 'admin/sellers', component: AdminSellers, canActivate: [adminGuard] },
  { path: 'admin/medicines', component: AdminMedicines, canActivate: [adminGuard] },
  { path: 'admin/analytics', component: AdminAnalytics, canActivate: [adminGuard] },
  { path: 'admin/documents', component: AdminDocuments, canActivate: [adminGuard] },
  { path: 'admin/profile', component: AdminProfile, canActivate: [adminGuard] },
  { path: 'seller/profile', component: SellerProfile, canActivate: [sellerGuard] },
  { path: 'profile', component: Profile, canActivate: [authGuard] },
  { path: '**', component: NotFound }
];
```

---

## File index

**Backend (`pc/src/main/java/com/cts/mfrp/pc/`):** `controller/AuthController.java`, `controller/PharmacyRegistrationController.java`, `service/AuthService.java`, `config/SecurityConfig.java`, `config/AppUserDetailsService.java`, `model/User.java`, `repository/UserRepository.java`, `dto/LoginRequest.java`, `dto/RegistrationRequest.java`, `resources/application.properties`.

**Frontend (`pc-frontend/src/`):** `environments/environment.ts`, `app/app.config.ts`, `app/app.routes.ts`, `app/interceptors/credentials.interceptor.ts`, `app/services/auth.service.ts`, `app/components/auth/login/{login.ts,login.html}`, `app/components/auth/register/{register.ts,register.html}`, `app/components/auth/register-pharmacy/{register-pharmacy.ts,register-pharmacy.html}`, `app/guards/{auth,guest,seller,admin}.guard.ts`.
