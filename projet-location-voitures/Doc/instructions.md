# Guide d'implémentation du projet DevSecOps

Ce guide vous aidera à mettre en place votre projet DevSecOps avec deux microservices, Docker, Kubernetes, et une API Gateway sécurisée.

## 1. Préparation de l'environnement

### Installation des outils nécessaires
```bash
# Installer Docker
# Suivre les instructions pour votre système d'exploitation: https://docs.docker.com/get-docker/

# Installer Minikube
# Suivre les instructions: https://minikube.sigs.k8s.io/docs/start/

# Installer kubectl
# Suivre les instructions: https://kubernetes.io/docs/tasks/tools/

# Installer Istio
curl -L https://istio.io/downloadIstio | sh -
# Suivre les instructions à l'écran pour ajouter istioctl à votre PATH
```

## 2. Service de location de voitures (Premier microservice)

### 2.1 Configuration du projet

Créez un nouveau projet Spring Boot avec les dépendances nécessaires (Web, JPA, H2).

```bash
# Créer le projet via Spring Initializr ou votre IDE
# Assurez-vous d'ajouter les dépendances Web, Data JPA et H2
```

### 2.2 Implémentation des entités

Créez les entités JPA suivantes :
- `Vehicle` (classe abstraite)
- `Car` (sous-classe de Vehicle)
- `Van` (sous-classe de Vehicle)
- `Person`
- `Rental`

### 2.3 Implémentation des repositories

Créez les repositories Spring Data JPA :
- `VehicleRepository`
- `CarRepository`
- `VanRepository`
- `PersonRepository`
- `RentalRepository`

### 2.4 Implémentation du service

Créez le service métier `RentService` avec les méthodes suivantes :
- `getAllVehicles()`
- `getAllAvailableVehicles()`
- `getVehicleByPlateNumber(String plateNumber)`
- `rentVehicle(String plateNumber, RentalRequestDTO rentalRequest)`
- `returnVehicle(String plateNumber)`

### 2.5 Implémentation du contrôleur REST

Créez le contrôleur REST `VehicleRentalController` avec les endpoints suivants :
- `GET /cars` - Liste de tous les véhicules
- `GET /cars/available` - Liste des véhicules disponibles
- `GET /cars/{plateNumber}` - Détails d'un véhicule
- `PUT /cars/{plateNumber}?rent=true` - Louer un véhicule
- `PUT /cars/{plateNumber}?rent=false` - Retourner un véhicule

### 2.6 Dockerisation

Créez un Dockerfile pour l'application :

```dockerfile
FROM openjdk:17-oracle
EXPOSE 8080
ADD ./build/libs/rent-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```

Construisez et publiez l'image Docker :

```bash
./gradlew build
docker build -t votreusername/rentalservice:1 .
docker push votreusername/rentalservice:1
```

## 3. Service utilisateur (Deuxième microservice)

### 3.1 Configuration du projet

Créez un nouveau projet Spring Boot avec les dépendances nécessaires (Web, JPA, H2).

```bash
# Créer le projet via Spring Initializr ou votre IDE
# Assurez-vous d'ajouter les dépendances Web, Data JPA et H2
```

### 3.2 Implémentation des entités

Créez l'entité JPA `User` avec les champs suivants :
- `id`
- `username`
- `firstName`
- `lastName`
- `email`

### 3.3 Implémentation du repository

Créez le repository Spring Data JPA `UserRepository`.

### 3.4 Implémentation du service

Créez le service métier `UserService` avec les méthodes suivantes :
- `getAllUsers()`
- `getUserById(Long id)`
- `getUserByUsername(String username)`
- `createUser(User user)`
- `updateUser(Long id, User updatedUser)`
- `deleteUser(Long id)`

### 3.5 Implémentation du contrôleur REST

Créez le contrôleur REST `UserController` avec les endpoints suivants :
- `GET /users` - Liste de tous les utilisateurs
- `GET /users/{id}` - Détails d'un utilisateur par ID
- `GET /users/username/{username}` - Détails d'un utilisateur par username
- `POST /users` - Créer un utilisateur
- `PUT /users/{id}` - Mettre à jour un utilisateur
- `DELETE /users/{id}` - Supprimer un utilisateur

### 3.6 Dockerisation

Créez un Dockerfile pour l'application :

```dockerfile
FROM openjdk:17-oracle
EXPOSE 8081
ADD ./build/libs/user-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```

Construisez et publiez l'image Docker :

```bash
./gradlew build
docker build -t votreusername/userservice:1 .
docker push votreusername/userservice:1
```

## 4. Déploiement sur Kubernetes

### 4.1 Démarrer Minikube

```bash
minikube start --driver=docker
```

### 4.2 Activer les addons nécessaires

```bash
minikube addons enable ingress
```

### 4.3 Installation d'Istio

```bash
istioctl install --set profile=demo -y
kubectl label namespace default istio-injection=enabled
kubectl apply -f samples/addons
```

### 4.4 Déployer les applications

Préparez les fichiers YAML de déploiement (remplacez ${DOCKER_USERNAME} par votre nom d'utilisateur Docker Hub) :

- `rent-deployment.yaml`
- `user-deployment.yaml`
- `gateway.yaml`
- `rbac.yaml`
- `istio-security.yaml`

Appliquez les configurations :

```bash
kubectl apply -f rent-deployment.yaml
kubectl apply -f user-deployment.yaml
kubectl apply -f gateway.yaml
kubectl apply -f rbac.yaml
kubectl apply -f istio-security.yaml
```

### 4.5 Vérifier les déploiements

```bash
kubectl get pods
kubectl get services
kubectl get virtualservices
kubectl get gateways
```

### 4.6 Accéder aux applications

Récupérez l'adresse IP et le port de l'Istio Ingress Gateway :

```bash
kubectl -n istio-system port-forward deployment/istio-ingressgateway 31380:8080
```

Accédez aux applications dans votre navigateur :
- Service de location : http://localhost:31380/rental/cars
- Service utilisateur : http://localhost:31380/users

## 5. Test des fonctionnalités

### 5.1 Test du service de location

```bash
# Récupérer la liste des voitures
curl http://localhost:31380/rental/cars

# Récupérer les détails d'une voiture
curl http://localhost:31380/rental/cars/11AA22

# Louer une voiture
curl -X PUT -H "Content-Type: application/json" -d '{"begin": "12/10/2023", "end": "15/10/2023", "personName": "John Doe"}' "http://localhost:31380/rental/cars/11AA22?rent=true"

# Retourner une voiture
curl -X PUT "http://localhost:31380/rental/cars/11AA22?rent=false"
```

### 5.2 Test du service utilisateur

```bash
# Récupérer la liste des utilisateurs
curl http://localhost:31380/users

# Créer un utilisateur
curl -X POST -H "Content-Type: application/json" -d '{"username": "jsmith", "firstName": "John", "lastName": "Smith", "email": "jsmith@example.com"}' "http://localhost:31380/users"

# Récupérer les détails d'un utilisateur
curl http://localhost:31380/users/1

# Mettre à jour un utilisateur
curl -X PUT -H "Content-Type: application/json" -d '{"username": "jsmith", "firstName": "John", "lastName": "Smith", "email": "john.smith@example.com"}' "http://localhost:31380/users/1"

# Supprimer un utilisateur
curl -X DELETE "http://localhost:31380/users/1"
```

## 6. Surveillance et journalisation

### 6.1 Kiali (visualisation du maillage de services)

```bash
istioctl dashboard kiali
```

### 6.2 Prometheus (métriques)

```bash
istioctl dashboard prometheus
```

### 6.3 Grafana (tableaux de bord)

```bash
istioctl dashboard grafana
```

### 6.4 Jaeger (traçage)

```bash
istioctl dashboard jaeger
```

## 7. Sécurité supplémentaire

### 7.1 Vérification de la sécurité mTLS

```bash
# Vérifier que mTLS est activé
kubectl get peerauthentication default -n default -o yaml
```

### 7.2 Tester les règles RBAC

```bash
# Créer un pod avec un service account sans permissions
kubectl run test-pod --image=busybox --restart=Never --serviceaccount=default -- sleep 3600

# Essayer d'accéder aux services depuis ce pod
kubectl exec -it test-pod -- wget -O- http://rentalservice/cars
```

