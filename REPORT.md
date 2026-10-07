# ПРАКТИКАЛЫҚ ЖҰМЫС №6
**Тақырып:** Бағдарламалау және қосымшаны іске асыру — фреймворк, REST API және дерекқор қолданылған көпдеңгейлі қосымша

**Орындаған:** ______________________  **Топ:** ________  **Күні:** ________

**Пәндік аймақ:** Онлайн-дүкен (тауарлар каталогы)
**Құрал:** IntelliJ IDEA, Java 17, Spring Boot 3.3.4, Maven, H2 / PostgreSQL, Postman / IntelliJ HTTP Client

---

## 1. Жұмыстың мақсаты
Алдын ала жобаланған архитектура негізінде қосымшаны Java, Spring Boot, REST API және дерекқор арқылы іске асыру; Controller → Service → Repository → Database қабаттарын бөлу; валидация, қателерді өңдеу және API тестілеуді меңгеру.

## 2. Технологиялық стек және таңдау себебі
| Технология | Не үшін |
|---|---|
| Java 17 | Тұрақты LTS нұсқа, records қолдауы (DTO үшін) |
| Spring Boot 3.3.4 | Автоконфигурация, DI, енгізілген Tomcat |
| Spring Web | REST контроллерлер |
| Spring Data JPA + Hibernate | ORM, SQL жазбай CRUD |
| Bean Validation (Jakarta) | Кіріс деректерін тексеру |
| H2 / PostgreSQL | H2 — орнатусыз іске қосу; PostgreSQL — профиль арқылы |
| springdoc-openapi 2.6.0 | Swagger UI құжаттамасы |
| Lombok | Getter/Setter кодын азайту |
| JUnit 5 + Mockito | Unit-тесттер |
| Maven | Тәуелділік басқару |

## 3. Талдау (Этап 1)
**Функционалдық талаптар**
- FR-01 Пайдаланушы тауар қоса алады.
- FR-02 Пайдаланушы тауарлар тізімін алады (пагинация және сұрыптаумен).
- FR-03 Пайдаланушы тауарды атауы бойынша іздейді.
- FR-04 Пайдаланушы тауар ақпаратын өзгертеді.
- FR-05 Пайдаланушы тауарды жояды.
- FR-06 Пайдаланушы тауарды санат және баға аралығы бойынша сүзеді.
- FR-07 Пайдаланушы санаттар мен жеткізушілер тізімін көреді / қосады.

**Функционалдық емес талаптар**
- NFR-01 API дұрыс HTTP-кодтар қайтаруы керек (200, 201, 204, 400, 404, 409, 500).
- NFR-02 Қате деректер жүйеге өтпей, түсінікті қате хабарламасымен қабылданбауы керек.
- NFR-03 Клиентке Entity емес, DTO беріледі.
- NFR-04 Бизнес-логика тек Service қабатында болады.

## 4. Дерекқорды жобалау (Этап 2)
```
CATEGORIES               PRODUCTS                          SUPPLIERS
-----------              ----------------------            -----------
id  PK BIGINT            id          PK BIGINT             id    PK BIGINT
name VARCHAR(100)        name        VARCHAR(200) NN       name  VARCHAR(150) NN
 NN, UNIQUE              description VARCHAR(1000)         phone VARCHAR(30)
        ^                price       DECIMAL(12,2) NN            ^
        | 1             created_at  TIMESTAMP NN                | N
        |               category_id  FK -> categories.id NN     |
        +-------- N ----+                                       |
                                 PRODUCT_SUPPLIER               |
                                 ------------------             |
                                 product_id  FK -> products.id -+ (N:M)
                                 supplier_id FK -> suppliers.id
                                 PK (product_id, supplier_id)
```
- **Кестелер саны:** 4 (categories, products, suppliers, product_supplier) — талап ≥ 3 орындалды.
- **Байланыстар:** Category 1—N Product (`@ManyToOne`), Product N—M Supplier (`@ManyToMany`).
- **Міндетті өрістер:** `name`, `price`, `created_at`, `category_id`.

## 5. REST API жобасы (Этап 3)
| Әдіс | Endpoint | Мақсаты | Сәтті код |
|---|---|---|---|
| GET | /api/products | Тізім (page, size, sort, name, categoryId, minPrice, maxPrice) | 200 |
| GET | /api/products/{id} | Тауарды алу | 200 |
| GET | /api/products/search?name= | Атау бойынша іздеу | 200 |
| POST | /api/products | Тауар жасау | 201 |
| PUT | /api/products/{id} | Тауарды жаңарту | 200 |
| DELETE | /api/products/{id} | Тауарды жою | 204 |
| GET | /api/categories | Санаттар тізімі | 200 |
| POST | /api/categories | Санат жасау | 201 |
| GET | /api/suppliers | Жеткізушілер тізімі | 200 |
| POST | /api/suppliers | Жеткізуші жасау | 201 |

Барлығы: **10 endpoint** (талап ≥ 5).

## 6. Жоба құрылымы (Этап 4)
```
shop-api
├── pom.xml
├── requests.http
└── src
    ├── main
    │   ├── java/kz/practice/shop
    │   │   ├── ShopApplication.java
    │   │   ├── controller   (ProductController, CatalogController)
    │   │   ├── service      (ProductService, CatalogService)
    │   │   ├── repository   (ProductRepository, CategoryRepository, SupplierRepository)
    │   │   ├── entity       (Product, Category, Supplier)
    │   │   ├── dto          (ProductRequest, ProductResponse, NameRequest, ErrorResponse)
    │   │   ├── exception    (ResourceNotFoundException, GlobalExceptionHandler)
    │   │   └── config       (DataLoader)
    │   └── resources        (application.properties, application-postgres.properties)
    └── test/java/kz/practice/shop/service/ProductServiceTest.java
```

## 7. Іске асыру ерекшеліктері (Этап 5–9)
- **Entity (Этап 5):** `Product`, `Category`, `Supplier`; `@ManyToOne`, `@ManyToMany`, `@PrePersist` (createdAt).
- **Service (Этап 6):** CRUD, санат/жеткізушіні тексеру, `Specification` арқылы динамикалық сүзгі. Controller Repository-ге тікелей шықпайды.
- **Валидация (Этап 7):** `@NotBlank`, `@Size`, `@NotNull`, `@Positive` — `ProductRequest` ішінде, контроллерде `@Valid`.
  Тексерілетін жағдайлар: бос атау, теріс баға, нөлдік баға, жоқ id, қате JSON.
- **Қателерді өңдеу (Этап 8):** `ResourceNotFoundException` + `@RestControllerAdvice`:

| Қате | HTTP код |
|---|---|
| ResourceNotFoundException | 404 |
| MethodArgumentNotValidException | 400 |
| HttpMessageNotReadable / TypeMismatch | 400 |
| DataIntegrityViolation | 409 |
| Басқа Exception | 500 |

- **Controller (Этап 9):** `@RestController`, `ResponseEntity.created(...)` (201 + Location), `noContent()` (204).

## 8. Қосымша тапсырмалар (талап ≥ 2, орындалғаны: 6)
| Нұсқа | Күйі | Қалай |
|---|---|---|
| А. Пагинация | ✅ | `?page=0&size=10` (Pageable) |
| Б. Сұрыптау | ✅ | `?sort=price,desc` |
| В. Іздеу | ✅ | `/api/products/search?name=phone` |
| Г. Сүзгі | ✅ | `?minPrice=100&maxPrice=1000&categoryId=1` |
| Д. DTO | ✅ | `ProductRequest`, `ProductResponse` |
| Е. Swagger/OpenAPI | ✅ | http://localhost:8080/swagger-ui.html |
| Ж. Unit-тест | ✅ | `ProductServiceTest` — 4 тест |

## 9. IntelliJ IDEA-да іске қосу қадамдары
1. Zip-ті ашыңыз → **File → Open** → `shop-api` папкасын (немесе `pom.xml`) таңдаңыз → **Open as Project**.
2. JDK 17 орнатыңыз: **File → Project Structure → Project SDK = 17**.
3. Lombok үшін: **Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing** (қосулы болуы керек).
4. Maven тәуелділіктерін жүктеңіз (оң жақ Maven панелі → Reload).
5. `ShopApplication.java` ашып, жасыл ▶ батырмасын басыңыз (Run).
6. Консольде `Started ShopApplication` көрінсе — қосымша http://localhost:8080 мекенжайында жұмыс істейді.
7. Тест: `requests.http` файлын ашып, әр сұраныстың жанындағы ▶ басыңыз (немесе Postman қолданыңыз).
8. Unit-тесттер: `ProductServiceTest` → оң жақ батырма → **Run 'ProductServiceTest'**, не терминалда `mvn test`.
9. H2 консолі: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:shopdb`, user: `sa`, құпиясөз бос).

**PostgreSQL-ге ауысу:** `CREATE DATABASE shopdb;` жасап, **Run → Edit Configurations → Active profiles = `postgres`** деп қойыңыз (логин/құпиясөз `application-postgres.properties` ішінде).

## 10. API тестілеу нәтижелері (Этап 10)
> «Нақты нәтиже» бағанын өз іске қосуыңыздан толтырыңыз (Postman/HTTP Client).

| № | Сұраныс | Күтілетін нәтиже | Нақты нәтиже |
|---|---|---|---|
| 1 | POST /api/products (дұрыс JSON) | 201 Created, `Location` тақырыбы | |
| 2 | GET /api/products | 200 OK, `content` массиві бар Page | |
| 3 | GET /api/products/1 | 200 OK, Phone X | |
| 4 | GET /api/products/999 | 404 Not Found, «Тауар табылмады: id=999» | |
| 5 | POST `{"name":"","price":-100}` | 400 Bad Request, `details` ішінде name, price, categoryId | |
| 6 | POST price = 0 | 400 Bad Request | |
| 7 | POST categoryId = 999 | 404 Not Found | |
| 8 | PUT /api/products/1 | 200 OK | |
| 9 | DELETE /api/products/2 | 204 No Content | |
| 10 | GET /api/products/search?name=phone | 200 OK | |
| 11 | GET /api/products?page=0&size=2&sort=price,desc | 200 OK, баға кему ретімен | |

**Талап етілетін скриншоттар (≥ 3):**
1. IntelliJ-де жоба құрылымы және сәтті іске қосылған консоль.
2. Postman/HTTP Client: POST → 201 және GET /999 → 404.
3. Қате деректер → 400 жауабы (немесе Swagger UI беті).

## 11. Сыртқы кітапхананы талдау
| Параметр | Жауап |
|---|---|
| Атауы | Spring Data JPA (Hibernate ORM-мен бірге) |
| Нұсқасы | Spring Boot 3.3.4 құрамында (Spring Data JPA 3.3.x, Hibernate 6.5.x) — `mvn dependency:tree` арқылы тексеріңіз |
| Мақсаты | Java объектілерін реляциялық кестелермен байланыстыру және репозиторийлер арқылы деректермен жұмыс |
| Неге қолданылды | CRUD, пагинация, сұрыптау, динамикалық сүзгіні дайын құралмен тез әрі қауіпсіз жасау үшін |
| Қандай функциялар | `JpaRepository` (save, findById, deleteById, findAll(Pageable)), `Specification`, транзакциялар, DDL генерациясы, байланыстарды басқару |
| Баламалар | MyBatis, JDBC Template, jOOQ, Spring Data JDBC, EclipseLink |
| Артықшылықтары | Аз код, СУБД-дан тәуелсіздік, кірістірілген пагинация, SQL injection-нен қорғау (параметрленген сұраныс) |
| Кемшіліктері | Үйрену қиындығы, N+1 мәселесі, күрделі сұраныстарда өнімділік, «магия» көп |

**Сұрақ: неге дайын кітапхана қолданған орынды?**
Өз ORM-ыңызды жазу көп уақыт алады және қателерге толы болады (транзакциялар, кэш, байланыстар, SQL диалекттері, қауіпсіздік). Дайын кітапхана мыңдаған жобада тексерілген, үнемі жаңартылады, қауымдастық қолдауы бар. Студент уақытын инфрақұрылымға емес, бизнес-логикаға жұмсайды.

## 12. Бақылау сұрақтарына жауаптар
1. **Бизнес-логика неге Controller-де болмауы керек?** Controller тек HTTP-ды өңдейді. Логика онда болса, қайта қолдану, тестілеу қиындайды, код шатасады (SRP бұзылады).
2. **Көпдеңгейлі архитектура артықшылығы?** Жауапкершілікті бөлу, оңай тестілеу, қабатты басқасына ауыстыру (мысалы, СУБД), командалық жұмыс, масштабтау.
3. **Entity мен DTO айырмашылығы?** Entity — дерекқор кестесінің көрінісі (ішкі модель); DTO — клиентпен алмасуға арналған деректер контейнері.
4. **Entity-ді клиентке неге бірден беруге болмайды?** Құпия өрістер ашылады, lazy-байланыстар қате/цикл шығарады, API дерекқор құрылымына тәуелді болады, mass assignment қаупі.
5. **Басқа кестелер сілтеме жасайтын жазбаны жойсақ?** FK шектеуі бұзылады — СУБД қате қайтарады (бізде 409). Шешім: алдымен байланыстарды жою, `ON DELETE CASCADE`, немесе soft delete.
6. **HTTP кодтары:** 200 — сәтті; 201 — ресурс жасалды; 400 — клиент сұранысы қате; 404 — ресурс табылмады; 500 — сервердің ішкі қатесі.
7. **PUT мен POST:** POST — жаңа ресурс жасау (идемпотентті емес); PUT — ресурсты id бойынша толық ауыстыру/жаңарту (идемпотентті).
8. **PUT мен PATCH:** PUT — барлық өрісті ауыстырады; PATCH — тек өзгерген өрістерді жаңартады.
9. **Валидация қайда?** Бірнеше қабатта: шекарада (Controller/DTO — формат), Service-те (бизнес-ережелер), дерекқорда (NOT NULL, UNIQUE, FK). Негізгі синтаксистік тексеру — DTO-да.
10. **Қателерді өңдемесек?** Клиент 500 және stack trace алады, ішкі ақпарат ашылады, қолданушыға түсініксіз, кейде транзакция/ресурс дұрыс жабылмайды.
11. **Dependency Injection не үшін?** Объектілер арасындағы тәуелділікті сырттан беру; байланысты азайтады, тестілеуді (mock) жеңілдетеді.
12. **Фреймворк объектілерді қалай басқарады?** Spring IoC контейнері `@Component/@Service/@Repository` бинтерін жасайды, тәуелділіктерді енгізеді және өмірлік циклін басқарады (әдепкіде singleton).
13. **ORM артықшылықтары?** Аз SQL, объектік модель, СУБД-ны ауыстыру оңай, кірістірілген кэш пен транзакциялар.
14. **ORM мәселелері?** N+1 сұраныс, lazy-loading қателері, өнімділік, күрделі сұраныстарды бақылау қиын, үйрену қисығы.
15. **Кітапханаларды неге жаңартып отыру керек?** Осалдықтарды жабу, қателерді түзету, жаңа мүмкіндіктер және үйлесімділік.
16. **Сыртқы тәуелділіктің қауіпсіздік тәуекелдері?** Белгілі осалдықтар (CVE), supply-chain шабуылдары, жалған/зиянды пакеттер, қолдауы тоқтатылған кітапханалар.
17. **Unit-тест пен интеграциялық тест?** Unit — бір класты/әдісті оқшауда (mock) тексереді; интеграциялық — бірнеше компонентті (мысалы, Controller + Service + БД) бірге тексереді.
18. **API-ды UI-дан бөлек неге тестілеу керек?** API бірнеше клиентке қызмет етеді; UI қателерін API қателерінен ажырату, тестілеу жылдамырақ және автоматтандыруға оңай.
19. **Repository-де бизнес-логика болса?** Қабаттар шатасады, логика қайталанады, тестілеу және СУБД ауыстыру қиындайды.
20. **Қосымшаны қалай масштабтауға болады?** Бірнеше инстанс + жүктеме балансшысы (stateless API), кэш (Redis), дерекқор индекстері/репликация, асинхронды өңдеу (RabbitMQ/Kafka), Docker/Kubernetes, қажет болса микросервистерге бөлу.

## 13. Қорытынды
Жұмыс барысында Spring Boot негізінде көпдеңгейлі (Controller–Service–Repository–Database) REST қосымшасы жасалды: 4 кесте және байланыстар, 10 endpoint, DTO, валидация, ортақ қателерді өңдеу, пагинация/сұрыптау/сүзгі/іздеу, Swagger және unit-тесттер іске асырылды. Алынған дағдылар — модельді кодқа айналдыру, архитектуралық шешім қабылдау және API-ды тестілеу.
