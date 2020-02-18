 When running from minikube:

* all images must be built inside the kubernetes vm on virtualbox!

* in order to reach the true kubernetes service, localhost won't work - need to run

  ```bash
  $ minikube service list
  ```

  The resulting list will present all services exposed inside VM. Locate the relevant and use the provided IP and port.