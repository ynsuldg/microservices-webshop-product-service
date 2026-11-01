# Product Service

## Ansvar

Product Service hanterar produkter i Microservices Webshop. Tjänsten har endpoints för att hämta,
skapa och ta bort produkter samt JWT-baserade behörighetskontroller.

Betalningsflödet använder interna lagerreservationer. Produkter reserveras före Checkout,
reservationen förbrukas efter betalning och lagret återställs efter verifierad utgång eller avbrytning.
Upprepade anrop ska inte ändra lagret flera gånger.

## Tekniker

Java 17, Spring Boot 4, Spring Security, OAuth2 Resource Server, JWT, Maven och PostgreSQL.

## Databas

PostgreSQL hos Neon lagrar produkter och lagerreservationer.

## Miljövariabler

| Variabel | Lokalt | Render |
| --- | --- | --- |
| `DB_URL` | Utvecklingsdatabasens JDBC-adress. | Nåbar JDBC-adress till produktdatabasen. |
| `DB_USERNAME`, `DB_PASSWORD` | Databasens inloggning. | Databasens inloggning. |
| `JWT_PUBLIC_URL` | `http://localhost:8080/auth/jwks` | `https://microservices-webshop-auth-service.onrender.com/auth/jwks` |
| `ALLOWED_ORIGINS` | `http://localhost:5173` | `https://microservices-webshop-frontend.onrender.com` |
| `INTERNAL_SERVICE_TOKEN` | Gemensam intern hemlighet, minst 32 tecken. | Samma värde i gateway, payment, order och product. |

Flera origins anges kommaseparerat. Hemliga värden ska inte sparas i Git.

## Starta applikationen lokalt

Sätt miljövariablerna och starta från projektmappen.

**macOS/Linux:**

```bash
./mvnw spring-boot:run
```

**Windows PowerShell:**

```powershell
.\mvnw.cmd spring-boot:run
```

Kräver Java 17 eller senare och PostgreSQL. Starta auth-service för JWT-verifiering.
Lokal adress: `http://localhost:8081`. Frontendens anrop går via gatewayn.

## Statussida

Rotadressen `/` visar tjänstens namn och **Service is running** med samma utseende som övriga backendtjänster.
Sidan är publik och kontrollerar bara att denna tjänst svarar, inte övriga tjänster eller externa beroenden.
API:ernas befintliga säkerhetsregler gäller fortfarande.

## Render

| Tjänst | Render-adress |
| --- | --- |
| Frontend – öppna webshoppen | [microservices-webshop-frontend.onrender.com](https://microservices-webshop-frontend.onrender.com) |
| Auth Service | [microservices-webshop-auth-service.onrender.com](https://microservices-webshop-auth-service.onrender.com) |
| Product Service | [microservices-webshop-product-service.onrender.com](https://microservices-webshop-product-service.onrender.com) |
| Order Service | [microservices-webshop-order-service.onrender.com](https://microservices-webshop-order-service.onrender.com) |
| Email Service | [microservices-webshop-email-service.onrender.com](https://microservices-webshop-email-service.onrender.com) |
| Gateway Service | [microservices-webshop-gateway-service.onrender.com](https://microservices-webshop-gateway-service.onrender.com) |
| Payment Service | [microservices-webshop-payment-service.onrender.com](https://microservices-webshop-payment-service.onrender.com) |
- Docker-image: `ynsuldg/microservices-webshop-product-service:1.0.0`.

Använd **Web Service → Existing Image**, sätt miljövariablerna i Render och behåll Docker-imagens startkommando.
Efter en ny image från senaste `main` väljs **Manual Deploy → Deploy latest reference**.
En äldre Actions-omkörning kan skriva över samma tagg med äldre kod.

Backendadressen tillhör ett API. Rotadressen visar den publika statussidan.
Render Free kan ge väntetid när tjänsterna väcks. Se [Render-dokumentationen](https://render.com/docs/deploying-an-image).

## Relaterade repositories

- [Auth Service](https://github.com/ynsuldg/microservices-webshop-auth-service)
- [Order Service](https://github.com/ynsuldg/microservices-webshop-order-service)
- [Email Service](https://github.com/ynsuldg/microservices-webshop-email-service)
- [Gateway Service](https://github.com/ynsuldg/microservices-webshop-gateway-service)
- [Payment Service](https://github.com/ynsuldg/microservices-webshop-payment-service)
- [Frontend](https://github.com/ynsuldg/microservices-webshop-frontend)
