# Guide d'installation de Kubernetes sur Ubuntu 24.04 LTS

Ce guide détaille les étapes d'installation d'un cluster Kubernetes sur Ubuntu 24.04 LTS.

## Table des matières
- [Prérequis](#prérequis)
- [Préparation du système](#préparation-du-système)
- [Installation de kubeadm, kubelet et kubectl](#installation-de-kubeadm-kubelet-et-kubectl)
- [Configuration du nœud master](#configuration-du-nœud-master)
- [Configuration des nœuds worker](#configuration-des-nœuds-worker)
- [Vérification du cluster](#vérification-du-cluster)
- [Installation du réseau de pods](#installation-du-réseau-de-pods)
- [Déploiement d'une application de test](#déploiement-dune-application-de-test)
- [Installation du tableau de bord Kubernetes](#installation-du-tableau-de-bord-kubernetes)
- [Utilisation de Minikube (alternative pour développement)](#utilisation-de-minikube-alternative-pour-développement)
- [Résolution des problèmes courants](#résolution-des-problèmes-courants)

## Prérequis

- Ubuntu 24.04 LTS sur tous les nœuds (master et workers)
- Minimum 2 Go de RAM par machine
- 2 CPUs ou plus sur le nœud master
- Connexion réseau fonctionnelle entre tous les nœuds
- Docker Engine installé (voir guide Docker)
- Accès root/sudo sur toutes les machines
- Swap désactivé pour garantir le bon fonctionnement de Kubernetes

## Préparation du système

Exécutez ces commandes sur **tous les nœuds** (master et workers).

### 1. Mettre à jour le système

```bash
sudo apt update
sudo apt upgrade -y
```

### 2. Désactiver le swap

```bash
# Désactiver le swap immédiatement
sudo swapoff -a

# Désactiver le swap de façon permanente en commentant les lignes de swap dans /etc/fstab
sudo sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab
```

### 3. Charger les modules kernel nécessaires

```bash
cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF

sudo modprobe overlay
sudo modprobe br_netfilter
```

### 4. Configurer les paramètres réseau pour Kubernetes

```bash
cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF

# Appliquer les paramètres sans redémarrer
sudo sysctl --system
```

### 5. Installer les dépendances

```bash
sudo apt install -y apt-transport-https ca-certificates curl gnupg lsb-release
```

## Installation de kubeadm, kubelet et kubectl

Exécutez ces commandes sur **tous les nœuds** (master et workers).

### 1. Ajouter la clé GPG de Kubernetes

Pour Ubuntu 24.04, utilisez la dernière clé et source officielles :

```bash
# Télécharger la clé GPG publique de Kubernetes
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.29/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

# Ajouter le dépôt Kubernetes
echo "deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.29/deb/ /" | sudo tee /etc/apt/sources.list.d/kubernetes.list
```

### 2. Mettre à jour apt et installer les composants

```bash
sudo apt update
sudo apt install -y kubelet kubeadm kubectl

# Empêcher les mises à jour automatiques
sudo apt-mark hold kubelet kubeadm kubectl
```

### 3. Activer et démarrer kubelet

```bash
sudo systemctl enable --now kubelet
```

## Configuration du nœud master

Exécutez ces commandes **uniquement sur le nœud master**.

### 1. Initialiser le cluster avec kubeadm

```bash
sudo kubeadm init --pod-network-cidr=10.244.0.0/16 --kubernetes-version=v1.29.0
```

Si vous avez plusieurs interfaces réseau, vous devrez spécifier l'interface que Kubernetes doit utiliser :

```bash
sudo kubeadm init --pod-network-cidr=10.244.0.0/16 --apiserver-advertise-address=<IP_ADDRESS> --kubernetes-version=v1.29.0
```

### 2. Configurer kubectl pour l'utilisateur actuel

```bash
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

Si vous utilisez un utilisateur différent du administrateur :

```bash
export KUBECONFIG=/etc/kubernetes/admin.conf
```

### 3. Afficher la commande pour rejoindre des nœuds workers

La commande `kubeadm init` affiche à la fin une commande `kubeadm join` que vous utiliserez pour ajouter des nœuds worker au cluster. Elle ressemble à ceci :

```bash
sudo kubeadm join <master-ip>:<master-port> --token <token> --discovery-token-ca-cert-hash sha256:<hash>
```

Notez cette commande, car vous en aurez besoin pour les nœuds worker.

Si vous perdez ce token, vous pouvez en générer un nouveau avec :

```bash
sudo kubeadm token create --print-join-command
```

## Configuration des nœuds worker

Sur chaque nœud worker, exécutez la commande d'adhésion que vous avez obtenue du nœud master :

```bash
sudo kubeadm join <master-ip>:<master-port> --token <token> --discovery-token-ca-cert-hash sha256:<hash>
```

## Vérification du cluster

Sur le nœud master, vérifiez que tous les nœuds ont rejoint le cluster :

```bash
kubectl get nodes
```

Les nœuds apparaîtront avec le statut `NotReady` jusqu'à ce que vous installiez un plugin réseau de pod.

## Installation du réseau de pods

Sur le nœud master, installez un plugin de réseau CNI (Container Network Interface). Voici l'exemple avec Calico :

```bash
kubectl apply -f https://raw.githubusercontent.com/projectcalico/calico/v3.25.1/manifests/calico.yaml
```

Alternatives à Calico :

### Flannel
```bash
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml
```

### Weave Net
```bash
kubectl apply -f "https://github.com/weaveworks/weave/releases/download/latest_release/weave-daemonset-k8s.yaml"
```

Après quelques minutes, vérifiez que tous les nœuds sont maintenant prêts :

```bash
kubectl get nodes
```

## Déploiement d'une application de test

Pour vérifier que votre cluster fonctionne correctement, déployez une application de test :

```bash
kubectl create deployment nginx --image=nginx
kubectl expose deployment nginx --port=80 --type=NodePort
```

Vérifiez que le pod est en cours d'exécution :

```bash
kubectl get pods
```

Obtenez le port sur lequel le service est exposé :

```bash
kubectl get svc nginx
```

Vous pouvez maintenant accéder à l'application via `http://<worker-node-ip>:<node-port>`.

## Installation du tableau de bord Kubernetes

Pour surveiller votre cluster via une interface web :

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml
```

Créez un utilisateur admin pour le tableau de bord :

```bash
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ServiceAccount
metadata:
  name: admin-user
  namespace: kubernetes-dashboard
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: admin-user
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: admin-user
  namespace: kubernetes-dashboard
EOF
```

Obtenez le token pour vous connecter :

```bash
kubectl -n kubernetes-dashboard create token admin-user
```

Démarrez le proxy pour accéder au tableau de bord :

```bash
kubectl proxy
```

Accédez au tableau de bord via : http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/

## Utilisation de Minikube (alternative pour développement)

Si vous souhaitez juste tester Kubernetes localement pour le développement, Minikube est une alternative plus simple :

```bash
# Installer Minikube
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# Démarrer un cluster
minikube start --driver=docker

# Vérifier l'état
minikube status
```

## Résolution des problèmes courants

### 1. Pods en état "Pending" ou "ContainerCreating"

Vérifiez les événements du pod :

```bash
kubectl describe pod <pod-name>
```

### 2. Problèmes de réseau CNI

Vérifiez les pods du système :

```bash
kubectl get pods -n kube-system
```

Si des pods de réseau (calico, flannel, etc.) sont en erreur, vérifiez leurs logs :

```bash
kubectl logs -n kube-system <pod-name>
```

### 3. Nœuds en état "NotReady"

Vérifiez le statut et les logs de kubelet :

```bash
sudo systemctl status kubelet
sudo journalctl -xeu kubelet
```

### 4. Réinitialiser un nœud

Si vous devez réinitialiser un nœud pour recommencer :

```bash
sudo kubeadm reset
```

### 5. Problèmes de certificats expirés

Renouvelez les certificats :

```bash
sudo kubeadm certs renew all
```

### 6. Commande de diagnostic général

Pour exécuter un test de diagnostic complet :

```bash
kubectl cluster-info dump > cluster-info.txt
```