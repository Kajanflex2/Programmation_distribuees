# Guide complet d'installation et d'optimisation de Kubernetes sur Ubuntu 24.04 LTS

Ce guide détaille les étapes d'installation d'un cluster Kubernetes sur Ubuntu 24.04 LTS, incluant l'optimisation pour des systèmes à mémoire limitée (8 Go RAM).

## Table des matières
- [Prérequis](#prérequis)
- [Préparation du système](#préparation-du-système)
- [Installation de kubeadm, kubelet et kubectl](#installation-de-kubeadm-kubelet-et-kubectl)
- [Configuration du nœud master](#configuration-du-nœud-master)
- [Configuration des nœuds worker](#configuration-des-nœuds-worker)
- [Vérification du cluster](#vérification-du-cluster)
- [Installation du réseau de pods](#installation-du-réseau-de-pods)
- [Optimisation pour système à mémoire limitée (8 Go de RAM)](#optimisation-pour-système-à-mémoire-limitée-8-go-de-ram)
- [Déploiement d'une application de test](#déploiement-dune-application-de-test)
- [Installation du tableau de bord Kubernetes](#installation-du-tableau-de-bord-kubernetes)
- [Utilisation de Minikube (alternative pour développement)](#utilisation-de-minikube-alternative-pour-développement)
- [Résolution des problèmes courants](#résolution-des-problèmes-courants)

## Prérequis

- Ubuntu 24.04 LTS sur tous les nœuds (master et workers)
- Minimum 2 Go de RAM par machine (recommandé: 4 Go pour master)
- 2 CPUs ou plus sur le nœud master
- Connexion réseau fonctionnelle entre tous les nœuds
- Docker Engine ou containerd installé
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

### 5. Configurer containerd pour Kubernetes

Cette étape est **cruciale** pour éviter l'erreur "ERROR CRI: container runtime is not running" lors de l'initialisation du cluster.

```bash
# Créer le répertoire de configuration si nécessaire
sudo mkdir -p /etc/containerd

# Générer la configuration par défaut
sudo containerd config default | sudo tee /etc/containerd/config.toml

# IMPORTANT: Modifier la configuration pour utiliser systemd cgroup
# Cette ligne est essentielle pour que Kubernetes fonctionne correctement avec containerd
sudo sed -i 's/SystemdCgroup \= false/SystemdCgroup \= true/g' /etc/containerd/config.toml

# Redémarrer containerd pour appliquer les changements
sudo systemctl restart containerd
```

### 6. Installer les dépendances

```bash
sudo apt install -y apt-transport-https ca-certificates curl gnupg lsb-release
```

## Installation de kubeadm, kubelet et kubectl

Exécutez ces commandes sur **tous les nœuds** (master et workers).

### 1. Ajouter la clé GPG de Kubernetes

Pour Ubuntu 24.04, utilisez la dernière clé et source officielles :

```bash
# Créer le répertoire pour les clés
sudo mkdir -p /etc/apt/keyrings

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

## Optimisation pour système à mémoire limitée (8 Go de RAM)

Si vous travaillez sur un système avec une mémoire limitée à 8 Go de RAM, l'exécution d'un cluster Kubernetes complet peut être exigeante. Voici des conseils pour optimiser l'utilisation des ressources:

### 1. Utiliser un seul nœud (Master uniquement)

Pour les environnements de développement avec une mémoire limitée, configurez le nœud master pour exécuter aussi des pods applicatifs :

```bash
# Supprimer le taint du nœud master pour permettre l'exécution de pods
kubectl taint nodes --all node-role.kubernetes.io/control-plane- node-role.kubernetes.io/master-

# Limitez le nombre de pods par nœud
sudo nano /var/lib/kubelet/config.yaml
# Ajouter ou modifier la ligne:
maxPods: 50
# Puis redémarrer kubelet
sudo systemctl restart kubelet
```

### 2. Limiter les ressources des composants système

Réduisez les ressources allouées aux composants du plan de contrôle :

```bash
# Ajuster les ressources du plan de contrôle
sudo nano /etc/kubernetes/manifests/kube-apiserver.yaml
sudo nano /etc/kubernetes/manifests/kube-controller-manager.yaml
sudo nano /etc/kubernetes/manifests/kube-scheduler.yaml
sudo nano /etc/kubernetes/manifests/etcd.yaml

# Ajouter ou modifier les limites de ressources dans chaque fichier:
resources:
  requests:
    cpu: 100m
    memory: 100Mi
  limits:
    cpu: 200m
    memory: 200Mi
```

### 3. Désactiver ou limiter les addons non essentiels

Évitez d'installer des addons lourds comme le tableau de bord Kubernetes ou Prometheus si vous n'en avez pas absolument besoin.

```bash
# Si vous avez installé le dashboard et que vous ne l'utilisez pas activement
kubectl delete -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml
```

### 4. Configurer des limites de ressources pour vos applications

Définissez toujours des limites de ressources pour vos déploiements d'applications :

```yaml
resources:
  requests:
    memory: "64Mi"
    cpu: "50m"
  limits:
    memory: "128Mi"
    cpu: "100m"
```

### 5. Arrêter le cluster quand il n'est pas utilisé

Libérez de la mémoire en arrêtant les services Kubernetes lorsque vous ne les utilisez pas :

```bash
# Arrêter les services Kubernetes
sudo systemctl stop kubelet

# Désactiver le démarrage automatique
sudo systemctl disable kubelet

# Réactiver et démarrer quand nécessaire
sudo systemctl enable kubelet
sudo systemctl start kubelet
```

### 6. Ne pas utiliser Kubernetes et Docker/Minikube simultanément

Si vous avez déjà Docker et/ou Minikube installés, n'exécutez pas tous ces services en même temps :

```bash
# Arrêter Minikube si vous utilisez Kubernetes
minikube stop

# Si Docker n'est pas utilisé par Kubernetes, vous pouvez l'arrêter aussi
sudo systemctl stop docker
```

### 7. Alternative recommandée: utiliser Minikube au lieu de kubeadm

Pour les environnements de développement avec 8 Go de RAM, Minikube est souvent une meilleure option que kubeadm :

```bash
# Désinstaller kubeadm, kubelet et kubectl
sudo kubeadm reset
sudo apt-mark unhold kubelet kubeadm kubectl
sudo apt remove -y kubelet kubeadm kubectl

# Installer et configurer Minikube avec des ressources minimales
minikube config set memory 1500
minikube config set cpus 1
minikube start --driver=docker
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

Si vous souhaitez juste tester Kubernetes localement pour le développement, Minikube est une alternative plus simple et plus légère :

```bash
# Installer Minikube
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# Démarrer un cluster avec ressources limitées (pour 8 Go RAM)
minikube start --driver=docker --memory=1500 --cpus=1

# Vérifier l'état
minikube status
```

## Résolution des problèmes courants

### 1. Erreur "ERROR CRI: container runtime is not running"

Cette erreur est très courante lors de l'initialisation du cluster avec kubeadm.

```
[preflight] Running pre-flight checks
error execution phase preflight: [preflight] Some fatal errors occurred:
	[ERROR CRI]: container runtime is not running: output: time="..." level=fatal msg="validate service connection: validate CRI v1 runtime API..."
```

Solutions:
1. Vérifiez que containerd est correctement configuré pour utiliser systemd cgroup:
   ```bash
   sudo sed -i 's/SystemdCgroup \= false/SystemdCgroup \= true/g' /etc/containerd/config.toml
   sudo systemctl restart containerd
   ```

2. Si le problème persiste, essayez de supprimer complètement la configuration:
   ```bash
   sudo rm -f /etc/containerd/config.toml
   sudo systemctl restart containerd
   ```

3. Redémarrez les services et réessayez:
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl restart containerd
   sudo systemctl restart kubelet
   ```

### 2. Pods en état "Pending" ou "ContainerCreating"

Vérifiez les événements du pod :

```bash
kubectl describe pod <pod-name>
```

### 3. Problèmes de réseau CNI

Vérifiez les pods du système :

```bash
kubectl get pods -n kube-system
```

Si des pods de réseau (calico, flannel, etc.) sont en erreur, vérifiez leurs logs :

```bash
kubectl logs -n kube-system <pod-name>
```

### 4. Nœuds en état "NotReady"

Vérifiez le statut et les logs de kubelet :

```bash
sudo systemctl status kubelet
sudo journalctl -xeu kubelet
```

Cette erreur est souvent liée à l'absence d'un plugin de réseau CNI. Assurez-vous d'avoir installé un réseau CNI comme Calico ou Flannel après l'initialisation du cluster.

### 5. Problèmes avec containerd

Si vous rencontrez des problèmes avec containerd:

```bash
# Vérifier l'état de containerd
sudo systemctl status containerd

# Consulter les logs de containerd
sudo journalctl -xeu containerd
```

Pour vérifier la communication entre kubelet et containerd:

```bash
sudo crictl info
```

### 6. Réinitialiser un nœud

Si vous devez réinitialiser un nœud pour recommencer :

```bash
sudo kubeadm reset
```

### 7. Problèmes de certificats expirés

Renouvelez les certificats :

```bash
sudo kubeadm certs renew all
```

### 8. Problèmes de mémoire insuffisante

Si vous rencontrez des problèmes de mémoire insuffisante, vérifiez l'utilisation de la mémoire :

```bash
free -h
```

Solutions:
1. Arrêtez les services non essentiels
2. Suivez les recommandations d'optimisation pour systèmes à mémoire limitée
3. Considérez l'utilisation de Minikube au lieu d'un cluster complet

### 9. Commande de diagnostic général

Pour exécuter un test de diagnostic complet :

```bash
kubectl cluster-info dump > cluster-info.txt
```