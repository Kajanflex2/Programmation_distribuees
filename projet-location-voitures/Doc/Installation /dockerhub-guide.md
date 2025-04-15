# Guide d'utilisation de Docker Hub

Ce guide explique comment utiliser Docker Hub pour stocker, partager et gérer vos images Docker.

## Table des matières
- [Qu'est-ce que Docker Hub ?](#quest-ce-que-docker-hub-)
- [Création d'un compte Docker Hub](#création-dun-compte-docker-hub)
- [Authentification et connexion](#authentification-et-connexion)
- [Création et gestion de dépôts](#création-et-gestion-de-dépôts)
- [Publication d'images sur Docker Hub](#publication-dimages-sur-docker-hub)
- [Téléchargement d'images depuis Docker Hub](#téléchargement-dimages-depuis-docker-hub)
- [Organisation et équipes](#organisation-et-équipes)
- [Bonnes pratiques de sécurité](#bonnes-pratiques-de-sécurité)
- [Automatisation avec CI/CD](#automatisation-avec-cicd)
- [Limites et fonctionnalités payantes](#limites-et-fonctionnalités-payantes)
- [Alternatives à Docker Hub](#alternatives-à-docker-hub)
- [Résolution des problèmes courants](#résolution-des-problèmes-courants)

## Qu'est-ce que Docker Hub ?

Docker Hub est le registre d'images Docker officiel, qui fonctionne comme un dépôt centralisé pour les images Docker. Il permet de :
- Stocker des images Docker publiques ou privées
- Partager des images avec d'autres utilisateurs ou organisations
- Automatiser les builds d'images via l'intégration avec GitHub ou BitBucket
- Découvrir des milliers d'images officielles et communautaires

## Création d'un compte Docker Hub

### 1. Inscription

1. Accédez à [Docker Hub](https://hub.docker.com/)
2. Cliquez sur "Sign Up" (S'inscrire)
3. Remplissez le formulaire avec :
   - Nom d'utilisateur (ce sera votre Docker ID)
   - Adresse e-mail
   - Mot de passe
4. Acceptez les conditions d'utilisation
5. Cliquez sur "Sign Up"
6. Confirmez votre adresse e-mail en cliquant sur le lien dans l'e-mail que vous recevrez

### 2. Choix du plan

Après avoir créé votre compte, vous pouvez choisir entre:
- Plan gratuit: 1 dépôt privé, téléchargements limités
- Plans payants: pour plus de dépôts privés et de fonctionnalités

## Authentification et connexion

### Connexion via l'interface web

1. Accédez à [Docker Hub](https://hub.docker.com/)
2. Entrez votre nom d'utilisateur et mot de passe
3. Cliquez sur "Sign In"

### Connexion via la ligne de commande

Pour publier des images, vous devez d'abord vous connecter à Docker Hub depuis votre terminal:

```bash
docker login
```

Entrez votre nom d'utilisateur et votre mot de passe lorsque vous y êtes invité.

Pour une sécurité accrue, utilisez un token d'accès personnel au lieu de votre mot de passe:

1. Connectez-vous à Docker Hub dans votre navigateur
2. Cliquez sur votre profil dans le coin supérieur droit
3. Sélectionnez "Account Settings" > "Security" > "New Access Token"
4. Donnez un nom à votre token et sélectionnez les autorisations
5. Utilisez ce token à la place de votre mot de passe lors de la connexion

```bash
docker login -u votre-nom-utilisateur
# Entrez votre token à la place du mot de passe quand demandé
```

## Création et gestion de dépôts

### Créer un nouveau dépôt

1. Connectez-vous à Docker Hub
2. Cliquez sur "Create Repository" (Créer un dépôt)
3. Configurez votre dépôt:
   - Nom: `<votre-nom-utilisateur>/<nom-du-dépôt>`
   - Visibilité: Public (accessible à tous) ou Private (accessible uniquement aux utilisateurs autorisés)
   - Description (optionnelle)
   - Constructions automatisées (optionnelles, pour lier à GitHub/BitBucket)
4. Cliquez sur "Create"

### Gérer un dépôt existant

Dans la page de votre dépôt, vous pouvez:
- Modifier la description et les paramètres
- Gérer les collaborateurs (pour les dépôts privés)
- Voir les statistiques de téléchargement
- Configurer les webhooks pour l'intégration avec d'autres services
- Supprimer ou transférer le dépôt

## Publication d'images sur Docker Hub

### Identifier votre image avec un tag

Avant de publier une image, vous devez la tagger avec votre nom d'utilisateur Docker Hub:

```bash
docker tag image-source votre-nom-utilisateur/nom-du-dépôt:tag
```

Par exemple:
```bash
docker tag mon-application:1.0 johndoe/mon-application:1.0
```

### Publier l'image

Une fois l'image taguée, publiez-la sur Docker Hub:

```bash
docker push votre-nom-utilisateur/nom-du-dépôt:tag
```

Par exemple:
```bash
docker push johndoe/mon-application:1.0
```

### Publier plusieurs tags

Vous pouvez publier la même image avec plusieurs tags:

```bash
docker tag mon-application:1.0 johndoe/mon-application:latest
docker push johndoe/mon-application:latest
```

### Mettre à jour une image existante

Pour mettre à jour une image existante, créez simplement une nouvelle version et utilisez le même tag:

```bash
# Construire une nouvelle version
docker build -t mon-application:1.1 .

# Tagger avec le même nom que précédemment
docker tag mon-application:1.1 johndoe/mon-application:latest

# Pousser vers Docker Hub
docker push johndoe/mon-application:latest
```

## Téléchargement d'images depuis Docker Hub

### Télécharger une image publique

```bash
docker pull nom-image:tag
```

Par exemple:
```bash
docker pull nginx:latest
```

Si aucun tag n'est spécifié, Docker télécharge l'image avec le tag `latest`:

```bash
docker pull nginx
```

### Télécharger une image privée

Pour les images privées, vous devez d'abord vous connecter:

```bash
docker login
docker pull votre-nom-utilisateur/votre-dépôt-privé:tag
```

### Utiliser une image sans la télécharger explicitement

Lorsque vous exécutez une commande `docker run` avec une image qui n'existe pas localement, Docker la télécharge automatiquement depuis Docker Hub:

```bash
docker run -d -p 80:80 nginx
```

## Organisation et équipes

### Créer une organisation

1. Connectez-vous à Docker Hub
2. Cliquez sur "Organizations" dans le menu principal
3. Cliquez sur "Create Organization"
4. Entrez un nom d'organisation et une adresse e-mail
5. Choisissez un plan tarifaire
6. Cliquez sur "Create Organization"

### Gérer les membres de l'organisation

1. Accédez à votre organisation
2. Cliquez sur l'onglet "Members"
3. Cliquez sur "Add Member"
4. Entrez le nom d'utilisateur ou l'email du membre
5. Attribuez un rôle (Owner, Member, etc.)

### Créer et gérer des équipes

Les équipes permettent de gérer les accès aux dépôts:

1. Dans votre organisation, allez à l'onglet "Teams"
2. Cliquez sur "Create Team"
3. Donnez un nom et une description à l'équipe
4. Ajoutez des membres
5. Configurez les autorisations pour les dépôts

## Bonnes pratiques de sécurité

### Utiliser des tokens d'accès personnels

Au lieu d'utiliser votre mot de passe principal:

1. Créez des tokens d'accès avec des privilèges limités
2. Utilisez des tokens différents pour différents projets/environnements
3. Renouvelez régulièrement vos tokens

### Scanner vos images

Docker Hub propose des scans de sécurité pour détecter les vulnérabilités:

1. Accédez à votre dépôt
2. Cliquez sur l'onglet "Tags"
3. Vérifiez les résultats des scans pour chaque image

### Signer vos images

Utilisez Docker Content Trust pour signer vos images:

```bash
# Activer Docker Content Trust
export DOCKER_CONTENT_TRUST=1

# Pousser une image signée
docker push votre-nom-utilisateur/nom-du-dépôt:tag
```

## Automatisation avec CI/CD

### Intégration avec GitHub Actions

Exemple de fichier `.github/workflows/docker-publish.yml`:

```yaml
name: Docker

on:
  push:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Login to DockerHub
        uses: docker/login-action@v1
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}
      
      - name: Build and push
        uses: docker/build-push-action@v2
        with:
          push: true
          tags: votre-nom-utilisateur/nom-du-dépôt:latest
```

### Builds automatisés sur Docker Hub

Vous pouvez configurer des builds automatisés directement sur Docker Hub:

1. Accédez à votre dépôt
2. Cliquez sur "Builds" > "Configure Automated Builds"
3. Connectez votre compte GitHub ou BitBucket
4. Configurez les règles de build (branche, tag, etc.)

## Limites et fonctionnalités payantes

### Limites du plan gratuit

- 1 dépôt privé
- 200 téléchargements publics par 6 heures
- Pas de support prioritaire

### Fonctionnalités des plans payants

- Plus de dépôts privés
- Plus de téléchargements
- Support prioritaire
- Scans de sécurité
- Fonctionnalités avancées d'équipe et d'organisation

## Alternatives à Docker Hub

Si Docker Hub ne répond pas à vos besoins, considérez ces alternatives:

- **GitHub Container Registry** : intégré à GitHub
- **Google Container Registry** : pour les utilisateurs de Google Cloud
- **Amazon ECR** : pour les utilisateurs d'AWS
- **Azure Container Registry** : pour les utilisateurs d'Azure
- **Harbor** : registre open-source auto-hébergé
- **Nexus Repository** : solution complète de gestion d'artefacts

## Résolution des problèmes courants

### "Permission denied" lors du push

```
denied: requested access to the resource is denied
```

Solutions:
1. Vérifiez que vous êtes connecté: `docker login`
2. Vérifiez que l'image est correctement taguée avec votre nom d'utilisateur
3. Vérifiez que vous avez les droits sur le dépôt

### Limite de téléchargement atteinte

```
You have reached your pull rate limit
```

Solutions:
1. Attendez que la limite soit réinitialisée
2. Connectez-vous avec un compte Docker Hub: `docker login`
3. Passez à un plan payant pour augmenter la limite

### Problèmes de réseau lors du push/pull

```
error during connect: Post "https://registry-1.docker.io/v2/": dial tcp: lookup registry-1.docker.io: no such host
```

Solutions:
1. Vérifiez votre connexion internet
2. Vérifiez vos paramètres DNS
3. Si vous êtes derrière un proxy d'entreprise, configurez Docker pour l'utiliser

### Images corrompues ou incomplètes

```
unexpected EOF
```

Solutions:
1. Supprimez l'image locale: `docker rmi image-name`
2. Réessayez de télécharger: `docker pull image-name`
3. Vérifiez l'espace disque disponible