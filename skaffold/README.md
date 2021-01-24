# Steps
## Set up local k8s cluster
We use k3d:
```
$ k3d cluster create skaffold-tests --agents 1 --image docker.io/rancher/k3s:v1.19.7-k3s1
```

