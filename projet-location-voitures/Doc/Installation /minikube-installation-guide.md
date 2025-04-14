# Guide d'installation de Minikube sur Ubuntu 24.04 LTS

Ce guide détaille l'installation et l'utilisation de Minikube sur Ubuntu 24.04 LTS, permettant d'exécuter un cluster Kubernetes à nœud unique localement à des fins de développement.

## Table des matières
- [Qu'est-ce que Minikube ?](#quest-ce-que-minikube-)
- [Prérequis](#prérequis)
- [Méthodes d'installation](#méthodes-dinstallation)
  - [Méthode 1 : Installation directe du binaire](#méthode-1--installation-directe-du-binaire)
  - [Méthode 2 : Installation via le gestionnaire de paquets](#méthode-2--installation-via-le-gestionnaire-de-paquets)
  - [Méthode 3 : Installation via Snap](#méthode-3--installation-via-snap)
- [Choix du pilote (driver)](#choix-du-pilote-driver)
- [Démarrage de Minikube](#démarrage-de-minikube)
- [Vérification de l'installation](#vérification-de-linstallation)
- [Opérations de base avec Minikube](#opérations-de-base-avec-minikube)
- [Configuration des ressources](#configuration-des-ressources)
- [Addons Minikube](#addons-minikube)
- [Exploitation du registre Docker intégré](#exploitation-du-registre-docker-intégré)
- [Utilisation des volumes persistants](#utilisation-des-volumes-persistants)
- [Networking](#networking)
- [Nettoyage et réinitialisation](#nettoyage-et-réinitialisation)
- [Résolution des problèmes courants](#résolution-des-problèmes-courants)

## Qu'est-ce que Minikube ?

Minikube est un outil qui permet d'exécuter facilement Kubernetes localement. Il crée une machine virtuelle ou utilise le moteur de conteneurisation de votre système pour faire fonctionner un cluster Kubernetes à nœud unique. C'est une solution idéale pour :

- Apprendre Kubernetes
- Développer des applications pour Kubernetes localement
- Tester des déploiements dans un environnement isolé
- Expérimenter avec les fonctionnalités de Kubernetes

## Prérequis

- Ubuntu 24.04 LTS
- Au moins 2 Go de RAM disponible (4 Go recommandés)
- 2 CPUs ou plus
- Environ 20 Go d'espace disque libre
- Une connexion Internet pour télécharger les composants nécessaires
- Un des hyperviseurs suivants ou Docker:
  - Docker (recommandé pour Ubuntu)
  - VirtualBox
  - KVM
  - QEMU
  - Podman

## Méthodes d'installation

### Méthode 1 : Installation directe du binaire

Cette méthode est la plus simple et fonctionne pour toutes les distributions Linux.

```bash
# Télécharger la dernière version stable de Minikube
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64

# Rendre le binaire exécutable et le déplacer dans un répertoire du PATH
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# Vérifier l'installation
minikube version
```

### Méthode 2 : Installation via le gestionnaire de paquets

Vous pouvez utiliser le gestionnaire de paquets Debian pour installer Minikube :

```bash
# Télécharger le package Debian
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube_latest_amd64.deb

# Installer le package
sudo dpkg -i minikube_latest_amd64.deb

# Vérifier l'installation
minikube version
```

### Méthode 3 : Installation via Snap

Ubuntu prend en charge les paquets Snap, ce qui facilite l'installation et les mises à jour :

```bash
# Installer Minikube via Snap
sudo snap install minikube

# Vérifier l'installation
minikube version
```

## Choix du pilote (driver)

Minikube peut utiliser différents pilotes pour créer et gérer le cluster Kubernetes. Voici les options recommandées pour Ubuntu 24.04 :

### Docker (recommandé)

Si vous avez déjà Docker installé, c'est l'option la plus simple et la plus légère :

```bash
# Vérifier que Docker est installé et en cours d'exécution
docker --version
sudo systemctl status docker

# Si Docker n'est pas installé, suivez le guide d'installation de Docker
```

### VirtualBox

Si vous préférez utiliser une machine virtuelle complète :

```bash
# Installer VirtualBox
sudo apt update
sudo apt install -y virtualbox virtualbox-ext-pack
```

### KVM

Pour utiliser KVM, qui est l'hyperviseur natif de Linux :

```bash
# Installer KVM et ses outils
sudo apt update
sudo apt install -y qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils virtinst virt-manager

# Ajouter votre utilisateur aux groupes requis
sudo usermod -aG libvirt $(whoami)
sudo usermod -aG kvm $(whoami)

# Redémarrer pour que les changements de groupe prennent effet
# ou utiliser la commande suivante pour appliquer les changements dans la session actuelle
newgrp libvirt
newgrp kvm
```

## Démarrage de Minikube

### Démarrage avec le pilote Docker (recommandé)

```bash
minikube start --driver=docker
```

### Démarrage avec VirtualBox

```bash
minikube start --driver=virtualbox
```

### Démarrage avec KVM

```bash
minikube start --driver=kvm2
```

### Définir un pilote par défaut

Pour éviter de spécifier le pilote à chaque démarrage :

```bash
minikube config set driver docker
```

### Démarrage avec plus de ressources

```bash
minikube start --cpus=4 --memory=8g --disk-size=30g
```

## Vérification de l'installation

### Vérifier l'état de Minikube

```bash
minikube status
```

### Accéder au tableau de bord Kubernetes

```bash
minikube dashboard
```

### Vérifier la connexion avec kubectl

```bash
kubectl get nodes
```

Si kubectl n'est pas installé, vous pouvez l'utiliser via Minikube :

```bash
minikube kubectl -- get nodes
```

Ou créer un alias :

```bash
alias kubectl="minikube kubectl --"
```

## Opérations de base avec Minikube

### Arrêter le cluster

```bash
minikube stop
```

### Redémarrer le cluster

```bash
minikube start
```

### Mettre en pause le cluster

```bash
minikube pause
```

### Reprendre un cluster en pause

```bash
minikube unpause
```

### Créer un nouveau cluster

```bash
minikube delete
minikube start
```

### Voir les logs de Minikube

```bash
minikube logs
```

## Configuration des ressources

### Modifier les ressources après installation

```bash
# Arrêter le cluster
minikube stop

# Modifier la configuration
minikube config set memory 4096
minikube config set cpus 2
minikube config set disk-size 40g

# Redémarrer
minikube start
```

### Vérifier la configuration actuelle

```bash
minikube config view
```

## Addons Minikube

Minikube propose plusieurs addons qui ajoutent des fonctionnalités au cluster.

### Lister les addons disponibles

```bash
minikube addons list
```

### Activer un addon

```bash
# Activer l'addon ingress
minikube addons enable ingress

# Activer l'addon de métriques
minikube addons enable metrics-server

# Activer le registre Docker
minikube addons enable registry
```

### Désactiver un addon

```bash
minikube addons disable <nom-addon>
```

### Addons populaires

- `dashboard` : Interface utilisateur web pour Kubernetes
- `ingress` : Contrôleur Ingress Nginx
- `metrics-server` : Collecte de métriques pour HPA
- `registry` : Registre Docker local
- `storage-provisioner` : Provisionnement de stockage
- `metallb` : Load balancer pour exposer des services

## Exploitation du registre Docker intégré

### Activer le registre

```bash
minikube addons enable registry
```

### Accéder au registre depuis l'hôte

```bash
# Ouvrir un tunnel pour accéder au registre
minikube addons enable registry-creds

# Configurer Docker pour utiliser le registre insécurisé
echo '{ "insecure-registries": ["localhost:5000"] }' | sudo tee /etc/docker/daemon.json
sudo systemctl restart docker
```

### Utiliser le registre

```bash
# Tagger une image pour le registre local
docker tag my-image:latest localhost:5000/my-image:latest

# Pousser l'image vers le registre
docker push localhost:5000/my-image:latest

# Utiliser l'image dans Kubernetes
kubectl create deployment my-app --image=localhost:5000/my-image:latest
```

## Utilisation des volumes persistants

### Créer un volume persistant

```bash
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: my-pvc
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
EOF
```

### Monter un répertoire hôte

```bash
# Créer un répertoire sur l'hôte
mkdir -p ~/minikube-data

# Monter le répertoire lors du démarrage
minikube stop
minikube start --mount --mount-string="~/minikube-data:/data"
```

## Networking

### Exposer un service

```bash
# Déployer une application
kubectl create deployment nginx --image=nginx
kubectl expose deployment nginx --port=80 --type=NodePort

# Obtenir l'URL du service
minikube service nginx --url
```

### Accéder à un service via l'adresse IP de Minikube

```bash
# Obtenir l'adresse IP de Minikube
minikube ip

# Utiliser l'adresse et le port NodePort pour accéder au service
```

### Utiliser un Ingress

```bash
# Activer l'addon ingress
minikube addons enable ingress

# Créer une règle Ingress
cat <<EOF | kubectl apply -f -
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: example-ingress
spec:
  rules:
  - host: example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: nginx
            port:
              number: 80
EOF

# Ajouter l'entrée dans /etc/hosts
echo "$(minikube ip) example.com" | sudo tee -a /etc/hosts
```

## Nettoyage et réinitialisation

### Supprimer un cluster

```bash
minikube delete
```

### Supprimer tous les clusters

```bash
minikube delete --all
```

### Nettoyer le cache Minikube

```bash
minikube cache delete
```

### Suppression complète

```bash
# Supprimer les clusters et la configuration
minikube delete --all --purge
```

## Résolution des problèmes courants

### Minikube ne démarre pas

Si Minikube ne démarre pas, vérifiez les logs :

```bash
minikube start --alsologtostderr -v=4
```

Solutions possibles :
- Assurez-vous que votre pilote (Docker, VirtualBox, KVM) est correctement installé
- Vérifiez que vous avez suffisamment d'espace disque et de mémoire
- Vérifiez les permissions de l'utilisateur (groupes docker, libvirt, etc.)

### Problèmes de réseau

Si vous rencontrez des problèmes de réseau :

```bash
# Vérifier le réseau Minikube
minikube ssh "ping -c 4 google.com"

# Redémarrer le cluster avec des paramètres de réseau différents
minikube delete
minikube start --network-plugin=cni --cni=calico
```

### Impossible d'accéder aux services

```bash
# Vérifier l'état du service
kubectl get svc

# Utiliser minikube service pour accéder directement
minikube service <service-name>
```

### Images Docker non trouvées

```bash
# Charger une image Docker dans Minikube
minikube image load my-image:latest

# Ou utiliser le registre Docker interne de Minikube
eval $(minikube docker-env)
docker build -t my-image:latest .
```

### Réinitialisation des addons

Si un addon ne fonctionne pas correctement :

```bash
minikube addons disable <addon-name>
minikube addons enable <addon-name>
```

### Problèmes de stockage persistant

```bash
# Vérifier les volumes persistants
kubectl get pv,pvc

# Réinitialiser le provisioner de stockage
minikube stop
minikube start --extra-config=apiserver.enable-admission-plugins="LimitRanger,NamespaceExists,NamespaceLifecycle,ResourceQuota,ServiceAccount,DefaultStorageClass,MutatingAdmissionWebhook"
```

### Mettre à jour Minikube

Si vous rencontrez des problèmes, essayez de mettre à jour vers la dernière version :

```bash
# Pour l'installation binaire
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# Pour l'installation via snap
sudo snap refresh minikube
```