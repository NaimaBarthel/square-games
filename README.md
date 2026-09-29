
# API Games (Square Games)

API REST développée avec Spring Boot permettant la gestion, la persistance et le déroulement au tour par tour de jeux de plateau (TicTacToe, Taquin, ConnectFour) via le moteur `square-games-engine`.  
Cette API fonctionne en synergie avec le microservice [square-users](https://github.com/NaimaBarthel/square-users) : elle utilise `RestClient` pour valider l'existence des joueurs avant de leur permettre de créer des parties et applique un contrôle strict de l'identité du joueur lors de la réalisation des coups.

---

## 🛠️ Technologies

* **Java 21**
* **Spring Boot 3.x**
* **Spring Web MVC**
* **Spring Data JPA**
* **RestClient** (client HTTP synchrone de Spring 3)
* **H2 Database** (base de données relationnelle locale / mémoire)
* **Moteur de jeu** : `fr.le_campus_numerique.square_games.engine`
* **springdoc-openapi / Swagger UI** (v2.5.0)
* **Maven** (avec Maven Wrapper)

---

## 📁 Architecture du projet

```text
square-games
│
├── config
│   └── OpenApiConfig
│
├── controllers
│   ├── GameController
│   └── dto
│       ├── GameCreationParams
│       ├── GameDto
│       └── MoveParams
│
├── dao
│   ├── entities
│   │   └── GameEntity
│   └── repositories
│       └── GameRepository (JpaRepository)
│
├── services
│   ├── GameService
│   ├── GameServiceImpl
│   └── UserRestClient (Client HTTP vers square-users)
│
└── resources
    ├── application.properties
    └── application-local.properties

```

Le microservice applique le patron en couches avec injection par constructeur :

* **Controller** : expose les endpoints REST, gère la négociation de contenu et les codes HTTP d'erreur (`400`, `403`, `404`).
* **Service** : orchestre le cycle de vie des jeux, contacte le service d'utilisateurs et contrôle les règles de tour par tour.
* **DAO / Persistence** : sauvegarde l'état des parties en base relationnelle H2.

---

## ⚙️ Prérequis

Avant de lancer le projet, assurez-vous d'avoir :

* **JDK 21** ou supérieur
* **Git**
* Le microservice **`square-users`** préalablement démarré sur le port `8081` (requis pour valider les identifiants utilisateurs).

---

## 🗄️ Base de données

L'application s'appuie sur une base de données H2.

La configuration standard se trouve dans `src/main/resources/application.properties` :

```properties
spring.datasource.url=jdbc:h2:mem:gamesdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true

```

*(Si vous optez pour la persistance locale sur fichier disque : `jdbc:h2:file:./data/gamesdb`).*

### Console web H2

Lorsque l'application tourne :

* **URL :** `http://localhost:8080/h2-console`
* **JDBC URL :** `jdbc:h2:mem:gamesdb` (ou `jdbc:h2:file:./data/gamesdb`)
* **User :** `sa`
* **Password :** *(vide)*

---

## 🔧 Configuration

L'API Games écoute sur le port **8080** et cible l'API Users sur le port **8081**.

Dans `src/main/resources/application.properties` :

```properties
server.port=8080
spring.application.name=square-games

# URL de base pour les requêtes RestClient vers square-users
users.service.url=http://localhost:8081

```

---

## ▶️ Lancer l'application

### 1. Cloner le repository

```bash
git clone [https://github.com/NaimaBarthel/square-games.git](https://github.com/NaimaBarthel/square-games.git)
cd square-games

```

### 2. Démarrer avec le Maven Wrapper

**Sous Linux / macOS :**

```bash
./mvnw spring-boot:run

```

**Sous Windows :**

```cmd
.\mvnw.cmd spring-boot:run

```

L'application démarre et écoute sur :

```text
http://localhost:8080

```

---

## 🎮 Endpoints REST & Règles Métier

> ⚠️ L'en-tête HTTP **`X-UserId`** est obligatoire pour les opérations d'écriture et de filtrage personnalisé.

| Méthode | Endpoint | En-tête requis | Description | Réponses HTTP |
| --- | --- | --- | --- | --- |
| `POST` | `/games` | `X-UserId: <UUID>` | Crée une partie (valide le joueur via `square-users`) | `201 Created`, `400 Bad Request`, `403 Forbidden` |
| `GET` | `/games` | `X-UserId: <UUID>` | Liste les parties actives du joueur connecté | `200 OK` |
| `GET` | `/games/{gameId}` | *(aucun)* | Récupère l'état complet du plateau et des joueurs | `200 OK`, `404 Not Found` |
| `GET` | `/games/{gameId}/tokens/{tokenId}/moves` | *(aucun)* | Liste les coordonnées accessibles pour un jeton | `200 OK`, `404 Not Found` |
| `POST` | `/games/{gameId}/moves` | `X-UserId: <UUID>` | Joue un coup (vérifie que c'est le tour du joueur) | `200 OK`, `400 Bad Request`, `403 Forbidden`, `404 Not Found` |

---

### Exemples d'appels

#### 1. Créer une nouvelle partie

`POST http://localhost:8080/games`

**Headers :**

* `Content-Type: application/json`
* `X-UserId: fb20f8b4-c32c-43cf-8b4e-0418880ae115`

**Corps de requête (JSON) :**

```json
{
  "gameType": "tictactoe",
  "boardSize": 3
}

```

*Si l'utilisateur existe dans `square-users` :* `201 Created` avec le JSON du jeu.

*Si l'utilisateur est inexistant :* `403 Forbidden` (`{"error": "Utilisateur non reconnu ou inexistant"}`).

---

#### 2. Récupérer les parties d'un joueur

`GET http://localhost:8080/games`

**Headers :**

* `X-UserId: fb20f8b4-c32c-43cf-8b4e-0418880ae115`

**Réponse (`200 OK`) :** Liste des parties associées au joueur.

---

#### 3. Consulter une partie par son ID

`GET http://localhost:8080/games/007af496-24e4-4a16-a1b9-2a9df185e01b`

**Réponse (`200 OK`) :** État sérialisé du moteur de jeu.

*Si non trouvé :* `404 Not Found`.

---

#### 4. Jouer un coup (Tour par tour)

`POST http://localhost:8080/games/007af496-24e4-4a16-a1b9-2a9df185e01b/moves`

**Headers :**

* `Content-Type: application/json`
* `X-UserId: fb20f8b4-c32c-43cf-8b4e-0418880ae115`

**Corps :**

```json
{
  "position": {
    "x": 1,
    "y": 1
  }
}

```

* **Succès (`200 OK`) :** Le coup est appliqué, la main passe au joueur suivant.
* **Refus (`403 Forbidden`) :** Si ce n'est pas le tour de ce joueur ou s'il s'agit d'une tentative d'usurpation.
* **Erreur (`400 Bad Request`) :** Si la case est déjà occupée ou hors limites.

---

## 🔄 Interaction inter-services avec `square-users`

Lors de la création d'une partie (`POST /games`), `square-games` effectue un appel synchrone via `RestClient` :

```text
Client (Bruno / Swagger)
       │
       │ POST /games (Header: X-UserId: <UUID>)
       ▼
 square-games :8080
       │
       │ GET http://localhost:8081/users/<UUID>/valid (RestClient)
       ▼
 square-users :8081
       │
       ├─ Si 200 OK ────────▶ square-games instancie le jeu (201 Created)
       └─ Si 404 Not Found ─▶ square-games rejette l'action (403 Forbidden)

```

---

## 📖 Swagger / OpenAPI

La documentation interactive est générée dynamiquement avec **SpringDoc OpenAPI** :

* **Swagger UI :** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
* **Spécification JSON brute :** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

Depuis l'interface web, vous pouvez renseigner directement le paramètre d'en-tête `X-UserId` et tester la logique de création et de jeu.

---

## 🔐 Sécurité

L'autorisation repose actuellement sur l'en-tête déclaratif `X-UserId`.

Ce système pédagogique permet de mettre en pratique la délégation de contrôle entre microservices. La signature cryptographique des requêtes (tokens signés JWT) sera introduite lors de la prochaine itération.
EOF

```

---

### Commandes pour pousser sur GitHub :

```bash
git add README.md
git commit -m "docs: add comprehensive README for square-games"
git push origin HEAD

```