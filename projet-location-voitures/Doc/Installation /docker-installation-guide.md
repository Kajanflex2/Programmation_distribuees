# Guide d'installation de Docker Engine sur Ubuntu 24.04 LTS

Ce guide vous explique comment installer Docker Engine sur Ubuntu 24.04 LTS de manière détaillée.

## Table des matières
- [Prérequis](#prérequis)
- [Installation de Docker](#installation-de-docker)
  - [Méthode 1 : Installation via le dépôt officiel (recommandée)](#méthode-1--installation-via-le-dépôt-officiel-recommandée)
  - [Méthode 2 : Installation via le package DEB](#méthode-2--installation-via-le-package-deb)
  - [Méthode 3 : Installation via le script d'installation](#méthode-3--installation-via-le-script-dinstallation)
- [Configuration post-installation](#configuration-post-installation)
- [Vérification de l'installation](#vérification-de-linstallation)
- [Désinstallation de Docker Engine](#désinstallation-de-docker-engine)
- [Commandes Docker de base](#commandes-docker-de-base)
- [Résolution des problèmes courants](#résolution-des-problèmes-courants)

## Prérequis

Vérifiez que votre système est à jour :

```bash
sudo apt update
sudo apt upgrade -y
```

Assurez-vous que vous disposez des privilèges administrateur (sudo) sur votre système.

## Installation de Docker

### Méthode 1 : Installation via le dépôt officiel (recommandée)

#### 1. Désinstaller les anciennes versions (si présentes)

```bash
sudo apt remove docker docker-engine docker.io containerd runc
```

#### 2. Installer les packages prérequis

```bash
sudo apt install -y apt-transport-https ca-certificates curl software-properties-common gnupg lsb-release
```

#### 3. Ajouter la clé GPG officielle de Docker

```bash
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
```

#### 4. Configurer le dépôt stable

```bash
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
```

#### 5. Mettre à jour la liste des packages

```bash
sudo apt update
```

#### 6. Installer Docker Engine, containerd et Docker Compose

```bash
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
```

### Méthode 2 : Installation via le package DEB

Si vous préférez installer Docker à partir d'un package DEB, suivez ces étapes :

#### 1. Accéder à la page des versions de Docker

Visitez [https://download.docker.com/linux/ubuntu/dists/](https://download.docker.com/linux/ubuntu/dists/)

#### 2. Naviguez vers votre version d'Ubuntu, puis vers `pool/stable/`

#### 3. Téléchargez les packages .deb pour Docker Engine, Docker CLI et containerd

```bash
curl -O https://download.docker.com/linux/ubuntu/dists/noble/pool/stable/amd64/docker-ce_24.0.0-1~ubuntu.24.04~noble_amd64.deb
curl -O https://download.docker.com/linux/ubuntu/dists/noble/pool/stable/amd64/docker-ce-cli_24.0.0-1~ubuntu.24.04~noble_amd64.deb
curl -O https://download.docker.com/linux/ubuntu/dists/noble/pool/stable/amd64/containerd.io_1.6.9-1_amd64.deb
```

#### 4. Installez les packages téléchargés

```bash
sudo apt install -y ./containerd.io_1.6.9-1_amd64.deb ./docker-ce-cli_24.0.0-1~ubuntu.24.04~noble_amd64.deb ./docker-ce_24.0.0-1~ubuntu.24.04~noble_amd64.deb
```

### Méthode 3 : Installation via le script d'installation

Docker fournit un script pratique pour les installations non interactives :

```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
```

## Configuration post-installation

### 1. Ajouter votre utilisateur au groupe Docker

Pour utiliser Docker sans sudo :

```bash
sudo usermod -aG docker $USER
```

Appliquez les changements de groupe en vous déconnectant puis en vous reconnectant, ou en exécutant :

```bash
newgrp docker
```

### 2. Configurer Docker pour démarrer au boot

```bash
sudo systemctl enable docker.service
sudo systemctl enable containerd.service
```

### 3. Configurer le daemon Docker (optionnel)

Créez un fichier de configuration JSON :

```bash
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<EOF
{
  "exec-opts": ["native.cgroupdriver=systemd"],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  },
  "dns": ["8.8.8.8", "8.8.4.4"],
  "storage-driver": "overlay2"
}
EOF
```

Redémarrez Docker pour appliquer les changements :

```bash
sudo systemctl restart docker
```

### 4. Optimisation pour système à mémoire limitée (8 Go de RAM)

Si vous travaillez sur un système avec une RAM limitée et installez plusieurs outils DevOps (Docker, Kubernetes, Minikube), il est important d'optimiser l'utilisation des ressources :

```bash
# Limiter l'utilisation mémoire de Docker
# Ajouter ces lignes à /etc/docker/daemon.json
{
  "exec-opts": ["native.cgroupdriver=systemd"],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  },
  "dns": ["8.8.8.8", "8.8.4.4"],
  "storage-driver": "overlay2",
  "default-shm-size": "64M",
  "memory": "1G"
}

# Redémarrer Docker pour appliquer les changements
sudo systemctl restart docker

# Nettoyer les ressources inutilisées régulièrement
docker system prune -a --volumes

# Arrêter Docker quand vous ne l'utilisez pas
sudo systemctl stop docker
```

## Vérification de l'installation

### 1. Vérifier la version de Docker

```bash
docker --version
```

### 2. Afficher les informations sur le daemon Docker

```bash
docker info
```

### 3. Vérifier que Docker fonctionne correctement

```bash
docker run hello-world
```

Si l'installation est réussie, vous devriez voir un message de confirmation indiquant que Docker fonctionne correctement.

## Désinstallation de Docker Engine

Si vous souhaitez désinstaller Docker Engine, containerd et Docker Compose :

```bash
sudo apt purge -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo apt autoremove -y
```

Pour supprimer tous les conteneurs, images, volumes et configurations :

```bash
sudo rm -rf /var/lib/docker
sudo rm -rf /var/lib/containerd
sudo rm -rf /etc/docker
```

## Commandes Docker de base

Voici quelques commandes Docker essentielles pour bien démarrer :

### Gérer les images

```bash
# Lister les images disponibles localement
docker images

# Télécharger une image depuis Docker Hub
docker pull nginx

# Supprimer une image
docker rmi nginx
```

### Gérer les conteneurs

```bash
# Lister tous les conteneurs en cours d'exécution
docker ps

# Lister tous les conteneurs (y compris ceux arrêtés)
docker ps -a

# Créer et démarrer un conteneur
docker run -d -p 80:80 --name mon-nginx nginx

# Arrêter un conteneur
docker stop mon-nginx

# Démarrer un conteneur arrêté
docker start mon-nginx

# Supprimer un conteneur
docker rm mon-nginx
```

### Inspecter et manipuler les conteneurs

```bash
# Voir les logs d'un conteneur
docker logs mon-nginx

# Exécuter une commande dans un conteneur en cours d'exécution
docker exec -it mon-nginx bash

# Voir les détails d'un conteneur
docker inspect mon-nginx
```

## Résolution des problèmes courants

### Problème d'accès au daemon Docker

Si vous obtenez l'erreur `permission denied` :

```
Got permission denied while trying to connect to the Docker daemon socket
```

Solutions :
1. Assurez-vous d'avoir ajouté votre utilisateur au groupe docker et de vous être déconnecté/reconnecté
2. Ou exécutez Docker avec sudo : `sudo docker <commande>`

### Erreur "Connection Refused"

Si vous obtenez une erreur de connexion refusée :

```
Cannot connect to the Docker daemon at unix:///var/run/docker.sock. Is the docker daemon running?
```

Solutions :
1. Vérifiez que le service Docker est en cours d'exécution :
   ```bash
   sudo systemctl status docker
   ```
2. Si arrêté, démarrez-le :
   ```bash
   sudo systemctl start docker
   ```

### Problèmes de réseau Docker

Si les conteneurs ne peuvent pas accéder à Internet :

```bash
# Vérifiez la configuration réseau Docker
docker network ls

# Recréez le réseau bridge par défaut
docker network rm bridge
systemctl restart docker
```

### Stockage insuffisant

Si vous obtenez des erreurs d'espace disque :

```bash
# Voir l'espace utilisé par Docker
docker system df

# Supprimer toutes les ressources inutilisées
docker system prune -a --volumes
```

### Gestion des ressources système

Pour les systèmes avec une mémoire limitée (8 Go de RAM ou moins) qui exécutent d'autres services comme Kubernetes ou Minikube :

```bash
# Arrêter et désactiver Docker lorsqu'il n'est pas utilisé
sudo systemctl stop docker
sudo systemctl disable docker

# Réactiver et démarrer Docker quand nécessaire
sudo systemctl enable docker
sudo systemctl start docker

# Surveiller l'utilisation des ressources par Docker
docker stats

# Limiter le nombre de conteneurs actifs
# Arrêtez les conteneurs inutilisés
docker ps -a
docker stop $(docker ps -q)
```

Si vous travaillez sur un projet DevSecOps avec plusieurs outils (Docker, Kubernetes, Minikube), n'utilisez qu'un seul environnement à la fois pour économiser les ressources. Par exemple, arrêtez Kubernetes/Minikube lorsque vous travaillez uniquement avec Docker.