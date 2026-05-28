# Hike Load Planner

Веб-приложение для формирования раскладки снаряжения в туристическом походе (курсовая работа).

## Стек

- Java 17, Spring Boot 3.3, **Spring Security + JWT**
- **React** (Vite + TypeScript) — `frontend/`
- **MySQL 8**
- Maven

## Быстрый старт

### 1. MySQL (обязательно до запуска приложения)

Ошибка `Connection refused` на порту **3306** значит, что **MySQL не запущен**.

#### Установка на Windows (без Docker)

1. Скачайте **MySQL 8.0** или **8.4**: https://dev.mysql.com/downloads/installer/ (MySQL Installer).
2. Тип установки: **Server only** или **Developer Default**.
3. Запомните пароль пользователя **root**.
4. Порт: **3306**.
5. По желанию установите **MySQL Workbench** (удобно выполнять SQL).

**Создать БД для проекта**

MySQL Workbench → подключиться как root → **SQL** → выполнить (молния / F5) файл:

`scripts/init-database-mysql.sql`

**Проверить службу**

`Win + R` → `services.msc` → служба **MySQL80** (или **MySQL84**) → **Выполняется**.

**Свои логин/пароль**

Скопируйте `application-local.yml.example` → `application-local.yml` и измените URL/логин/пароль.

Запуск: `mvn spring-boot:run -Dspring-boot.run.profiles=local`

#### Docker (опционально)

`.\start-db.ps1` или `docker compose up -d` — поднимет MySQL 8.4 на порту 3306.

### 2. React + JWT (основной UI)

**Терминал 1 — backend:**

```powershell
mvn spring-boot:run
```

**Терминал 2 — frontend:**

```powershell
cd frontend
npm install
npm run dev
```

Откройте **http://localhost:5173** — логин через JWT (`Authorization: Bearer …`).

- Демо: `organizer@hike.local` / `demo`
- Токен хранится в `localStorage` (`hike_access_token`)
- API: `http://localhost:8080/api/**` (в dev проксируется Vite)
- UI: список походов → карточка с вкладками (участники, снаряжение, питание, раскладка, моя раскладка)

**Переменные (prod):**

- `JWT_SECRET` — секрет ≥ 32 символов
- `JWT_EXPIRATION_HOURS` — срок жизни токена (по умолчанию 24)

### 2b. Thymeleaf (legacy, сессия cookie)

**http://localhost:8080/login** — старый UI, отдельная цепочка Spring Security (form login).

### 3. Запуск приложения

**Сначала один раз** (скачать драйвер MySQL и пересобрать):

```powershell
cd C:\Users\dorzh\Projects\hike-load-planner
mvn clean compile
```

В Cursor / IntelliJ: правый клик по `pom.xml` → **Maven** → **Reload project** (обновить зависимости).

Запуск:

```powershell
mvn spring-boot:run
```

Или из IDE — только **после** Reload Maven. Ошибка `Cannot load driver class: com.mysql.cj.jdbc.Driver` = зависимости не обновились → `mvn clean compile` + Reload project.

Тестовый пользователь: **`organizer@hike.local`** / **`demo`** (пароль обновляется при каждом старте в BCrypt).

Сначала **login** (получить `accessToken`), затем заголовок `Authorization: Bearer <token>` — см. `test-api.ps1`.

### 4. Проверка API (без платной лицензии IntelliJ)

**PowerShell** (рекомендуется): при запущенном приложении откройте второй терминал:

```powershell
cd C:\Users\dorzh\Projects\hike-load-planner
.\test-api.ps1
```

**Postman** (бесплатно): https://www.postman.com/downloads/ — импортируйте запросы из раздела ниже.

**Cursor / VS Code**: установите расширение **REST Client**, откройте `api-tests.http`, над запросом нажмите **Send Request**.

**curl** (один запрос):

```powershell
curl.exe -X POST http://localhost:8080/api/hikes -H "Content-Type: application/json" -H "X-User-Id: 1" -d "{\"name\":\"Тест\",\"startDate\":\"2026-07-01\",\"durationDays\":3}"
```

### 5. Примеры API

**Создать поход** (без «макс. вес рюкзака» — лимит у участника):

```http
POST http://localhost:8080/api/hikes
Content-Type: application/json
X-User-Id: 1

{
  "name": "Выходные в Кавказ",
  "startDate": "2026-06-01",
  "durationDays": 3
}
```

**Добавить участника** (пол + индивидуальный лимит):

```http
POST http://localhost:8080/api/hikes/1/participants
Content-Type: application/json
X-User-Id: 1

{
  "name": "Анна",
  "email": "anna@example.com",
  "gender": "FEMALE",
  "maxWeightKg": 15
}
```

**Общее снаряжение:**

```http
POST http://localhost:8080/api/hikes/1/gear/shared
Content-Type: application/json
X-User-Id: 1

{
  "name": "Палатка 3-местная",
  "weightKg": 2.4,
  "type": "SHARED"
}
```

**Сформировать раскладку:**

```http
POST http://localhost:8080/api/hikes/1/load-plan/generate
X-User-Id: 1
```

## План работ

См. [PLAN.md](PLAN.md) — этап 1 в процессе, этапы 2–4 далее.

## Структура

- `domain` — JPA-сущности
- `repository` — Spring Data
- `service` — бизнес-логика
- `web.controller` — REST JSON
