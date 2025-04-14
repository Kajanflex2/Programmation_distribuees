# Guide d'installation de kubectl sur Ubuntu 24.04 LTS

Ce guide détaille l'installation et la configuration de kubectl, l'outil en ligne de commande pour contrôler les clusters Kubernetes.

## Table des matières
- [Qu'est-ce que kubectl?](#quest-ce-que-kubectl)
- [Méthodes d'installation](#méthodes-dinstallation)
  - [Méthode 1 : Installation depuis les dépôts Kubernetes](#méthode-1--installation-depuis-les-dépôts-kubernetes)
  - [Méthode 2 : Installation directe du binaire](#méthode-2--installation-directe-du-binaire)
  - [Méthode 3 : Installation via snap](#méthode-3--installation-via-snap)
- [Configuration de kubectl](#configuration-de-kubectl)
- [Vérification de l'installation](#vérification-de-linstallation)
- [Commandes kubectl essentielles](#commandes-kubectl-essentielles)
- [Autocomplétion et alias](#autocomplétion-et-alias)
- [Configuration multi-cluster](#configuration-multi-cluster)
- [Plugins kubectl](#plugins-kubectl)
- [Résolution de problèmes](#résolution-de-problèmes)

## Qu'est-ce que kubectl?

kubectl est l'outil en ligne de commande officiel de Kubernetes. Il vous permet de :

- Déployer des applications
- Gérer les ressources du cluster
- Afficher les logs
- Exécuter des commandes dans des conteneurs
- Gérer toutes les ressources Kubernetes (pods, services, deployments, etc.)

## Méthodes d'installation

### Méthode 1 : Installation depuis les dépôts Kubernetes

Cette méthode est recommandée car elle facilite les mises à jour futures.

#### 1. Installer les prérequis

```bash
sudo apt update
sudo apt install -y apt-transport-https ca-certificates curl
```

#### 2. Télécharger la clé GPG et ajouter le dépôt Kubernetes

```bash
# Créer le répertoire pour les clés
sudo mkdir -p /etc/apt/keyrings

# Télécharger la clé GPG
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.29/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

# Ajouter le dépôt Kubernetes
echo "deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.29/deb/ /" | sudo tee /etc/apt/sources.list.d/kubernetes.list
```

#### 3. Mettre à jour et installer kubectl

```bash
sudo apt update
sudo apt install -y kubectl
```

### Méthode 2 : Installation directe du binaire

Si vous préférez ne pas ajouter un nouveau dépôt ou si vous souhaitez une version spécifique de kubectl.

#### 1. Télécharger la dernière version stable

```bash
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
```

Pour une version spécifique, remplacez `$(curl -L -s https://dl.k8s.io/release/stable.txt)` par la version désirée, par exemple `v1.29.0`.

#### 2. Vérifier le binaire (optionnel mais recommandé)

```bash
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl.sha256"
echo "$(cat kubectl.sha256)  kubectl" | sha256sum --check
```

Vous devriez voir : `kubectl: OK`

#### 3. Installer kubectl

```bash
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
```

### Méthode 3 : Installation via snap

Ubuntu supporte Snap, qui est une autre façon simple d'installer kubectl :

```bash
sudo snap install kubectl --classic
```

## Configuration de kubectl

Pour utiliser kubectl, vous avez besoin d'un fichier de configuration (kubeconfig) qui contient les informations du cluster.

### 1. Créer le répertoire de configuration

```bash
mkdir -p ~/.kube
```

### 2. Copier le fichier de configuration

Si vous avez installé Kubernetes avec kubeadm sur la même machine :

```bash
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

Si vous vous connectez à un cluster distant, copiez le fichier de configuration fourni par l'administrateur du cluster :

```bash
# Exemple:
# scp admin@master-node:/etc/kubernetes/admin.conf ~/.kube/config
```

### 3. Vérifier la configuration

```bash
kubectl config view
```

## Vérification de l'installation

Pour vérifier que kubectl fonctionne correctement :

```bash
kubectl version --client
```

Cela affiche la version du client kubectl. Si vous avez un cluster configuré :

```bash
kubectl cluster-info
```

## Commandes kubectl essentielles

Voici quelques commandes de base pour vous aider à démarrer avec kubectl :

### Obtenir des informations sur les ressources

```bash
# Lister tous les pods
kubectl get pods

# Lister tous les pods dans tous les namespaces
kubectl get pods --all-namespaces

# Lister tous les services
kubectl get services

# Lister tous les deployments
kubectl get deployments

# Obtenir des informations détaillées sur un pod
kubectl describe pod <pod-name>
```

### Créer et gérer des ressources

```bash
# Créer des ressources à partir d'un fichier YAML
kubectl apply -f <filename.yaml>

# Créer un déploiement
kubectl create deployment nginx --image=nginx

# Exposer un déploiement comme service
kubectl expose deployment nginx --port=80 --type=NodePort

# Mettre à jour un déploiement
kubectl set image deployment/nginx nginx=nginx:1.19
```

### Interagir avec les pods

```bash
# Obtenir les logs d'un pod
kubectl logs <pod-name>

# Exécuter une commande dans un pod
kubectl exec -it <pod-name> -- /bin/bash

# Copier des fichiers vers/depuis un pod
kubectl cp <pod-name>:/path/to/file ./local-file
```

### Supprimer des ressources

```bash
# Supprimer un pod
kubectl delete pod <pod-name>

# Supprimer un service
kubectl delete service <service-name>

# Supprimer un déploiement
kubectl delete deployment <deployment-name>

# Supprimer toutes les ressources d'un fichier YAML
kubectl delete -f <filename.yaml>
```

## Autocomplétion et alias

### 1. Configurer l'autocomplétion

Pour Bash :

```bash
echo 'source <(kubectl completion bash)' >>~/.bashrc
source ~/.bashrc
```

Pour ZSH :

```bash
echo 'source <(kubectl completion zsh)' >>~/.zshrc
source ~/.zshrc
```

### 2. Créer un alias pour kubectl

De nombreux utilisateurs créent l'alias `k` pour kubectl :

```bash
echo 'alias k=kubectl' >>~/.bashrc
echo 'complete -F __start_kubectl k' >>~/.bashrc
source ~/.bashrc
```

### 3. Optimisation pour systèmes à mémoire limitée

Sur un système avec 8 Go de RAM, en particulier lorsque vous exécutez plusieurs outils DevOps (Docker, Kubernetes/Minikube), certaines commandes kubectl peuvent être gourmandes en ressources. Voici quelques alias et fonctions optimisés :

```bash
# Ajouter ces lignes à votre ~/.bashrc ou ~/.zshrc

# Requêtes optimisées pour limiter la charge
alias kgp='kubectl get pods --no-headers'
alias kgpw='kubectl get pods -o wide --no-headers'
alias kgs='kubectl get services --no-headers'
alias kgn='kubectl get nodes --no-headers'

# Fonction "watch" optimisée avec rafraîchissement réduit
kwatch() {
  watch -n 5 -d "kubectl get $@ --no-headers"
}

# Obtenir des logs sans surcharger le système
klogs() {
  kubectl logs $1 --tail=50
}

# Nettoyer les ressources terminées pour libérer de la mémoire
kclean() {
  kubectl delete pods --field-selector=status.phase==Succeeded --all-namespaces
  kubectl delete pods --field-selector=status.phase==Failed --all-namespaces
}

# Configurer temporairement le niveau de verbosité (pour déboguer puis revenir à normal)
kdebug() {
  export KUBE_VERBOSE_LEVEL=5
  kubectl $@
  export KUBE_VERBOSE_LEVEL=0
}
```

Ajoutez ces alias à votre fichier `.bashrc` ou `.zshrc` pour faciliter l'utilisation de kubectl tout en minimisant l'impact sur les ressources système.

## Configuration multi-cluster

Si vous travaillez avec plusieurs clusters, vous pouvez configurer kubectl pour passer facilement de l'un à l'autre.

### 1. Afficher les contextes disponibles

```bash
kubectl config get-contexts
```

### 2. Basculer entre les contextes

```bash
kubectl config use-context <context-name>
```

### 3. Définir un namespace par défaut pour un contexte

```bash
kubectl config set-context --current --namespace=<namespace-name>
```

## Plugins kubectl

kubectl supporte les plugins qui étendent ses fonctionnalités.

### 1. Installer krew (gestionnaire de plugins kubectl)

```bash
(
  set -x; cd "$(mktemp -d)" &&
  OS="$(uname | tr '[:upper:]' '[:lower:]')" &&
  ARCH="$(uname -m | sed -e 's/x86_64/amd64/' -e 's/\(arm\)\(64\)\?.*/\1\2/' -e 's/aarch64$/arm64/')" &&
  KREW="krew-${OS}_${ARCH}" &&
  curl -fsSLO "https://github.com/kubernetes-sigs/krew/releases/latest/download/${KREW}.tar.gz" &&
  tar zxvf "${KREW}.tar.gz" &&
  ./"${KREW}" install krew
)
```

Ajoutez-le à votre PATH :

```bash
echo 'export PATH="${KREW_ROOT:-$HOME/.krew}/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

### 2. Rechercher et installer des plugins

```bash
# Lister les plugins disponibles
kubectl krew search

# Installer un plugin (exemple: ctx pour la gestion des contextes)
kubectl krew install ctx

# Utiliser un plugin installé
kubectl ctx
```

### Plugins populaires

- `ctx` : Gérer facilement les contextes
- `ns` : Basculer entre les namespaces
- `neat` : Nettoyer les manifestes YAML
- `view-secret` : Décoder facilement les secrets Kubernetes
- `access-matrix` : Afficher la matrice des autorisations RBAC

## Résolution de problèmes

### 1. Optimisation de kubectl pour systèmes à mémoire limitée (8 Go RAM)

Si vous travaillez sur un système avec des ressources limitées, voici quelques conseils pour optimiser l'utilisation de kubectl:

```bash
# Limiter la quantité de données retournées
kubectl get pods --field-selector=metadata.namespace=default

# Utiliser le format "custom-columns" plutôt que "-o wide" pour limiter la sortie
kubectl get pods -o custom-columns=NAME:.metadata.name,STATUS:.status.phase

# Éviter les commandes qui chargent beaucoup de données
# Au lieu de "kubectl get all --all-namespaces", utilisez:
kubectl get pods -n kube-system
kubectl get services -n kube-system
```

Pour réduire la charge système lors de l'utilisation de kubectl avec watch:

```bash
# Au lieu de cette commande, qui peut être lourde:
kubectl get pods -w

# Utilisez un intervalle plus long:
watch -n 10 kubectl get pods
```

Utilisez le cache local pour réduire les appels à l'API server:

```bash
# Créer un répertoire pour le cache
mkdir -p ~/.kube/cache

# Configurer kubectl pour utiliser le cache
kubectl config set-context --current --cache-dir=~/.kube/cache
```

### 2. Gestion des ressources entre kubectl, Kubernetes et autres outils

Si vous utilisez kubectl avec un cluster Kubernetes complet et Docker sur un système à 8 Go de RAM:

```bash
# Après l'utilisation de kubectl pour les tâches administratives, 
# vous pouvez limiter la charge du serveur API en arrêtant certains services non critiques:

# Mettre en pause les déploiements non critiques
kubectl scale deployment non-critical-app --replicas=0 -n application-namespace

# Reprendre les déploiements quand nécessaire
kubectl scale deployment non-critical-app --replicas=1 -n application-namespace
```

Si vous alternez entre Minikube et un cluster Kubernetes standard:

```bash
# Lorsque vous passez à Minikube:
export KUBECONFIG=~/.kube/minikube-config

# Lorsque vous revenez à votre cluster principal:
export KUBECONFIG=~/.kube/config
```

### 3. Erreur "The connection to the server localhost:8080 was refused"

Cette erreur se produit lorsque kubectl ne peut pas se connecter au serveur API Kubernetes.

Solutions :
- Vérifiez que votre fichier `~/.kube/config` est correctement configuré
- Si vous utilisez minikube, exécutez `minikube start`
- Assurez-vous que le cluster Kubernetes fonctionne

### 2. Erreur "Unable to connect to the server: x509: certificate"

Cette erreur se produit lorsque les certificats ne sont pas correctement configurés.

Solutions :
- Vérifiez que votre fichier de configuration contient les bons certificats
- Renouvelez les certificats si nécessaire
- Assurez-vous que la date système est correcte (les certificats peuvent échouer si la date est incorrecte)

### 3. Debugger la connexion à l'API

Pour voir plus d'informations sur la connexion à l'API :

```bash
kubectl --v=9 get pods
```

Le niveau de verbosité `9` affiche des informations détaillées sur les connexions HTTP.

### 4. Vérifier les permissions

Si vous recevez des erreurs d'autorisation :

```bash
kubectl auth can-i <verb> <resource>
```

Par exemple :

```bash
kubectl auth can-i create deployments
```