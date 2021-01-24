# Steps
## Set up local k8s cluster
We use k3d:
```
$ k3d cluster create skaffold-test
```
## Development using skaffold
Run the app in the `dev` mode, meaning it will be re-deployed each time a change in the code will be detected.
```
$ skaffold dev
```

Run the app in the `run` mode, meaning run just once, do not monitor for changes and redeploy.
`tail` is optional and allows to tail logs.
```
$ skaffold run --tail
```
To deploy the app that was built previously (using dev, run on build)
```
$ skaffold deploy --images skaffold-example
```
To delete anything (results of deploy, run, dev etc.) just run this:
```
$ skaffold delete
```