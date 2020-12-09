example k8s operator with scala and zio
===

### development

### Microk8s

install:  
`snap install microk8s --classic --channel=1.18/stable`

publish to:
- `MICROK8S=1 sbt docker:publish`
- update `spec.template.spec.containers.image` for the operator to use `localhost:32000` registry

get token:  
`TOKEN=$(k get secret -o json | jq -r .items[0].data.token | base64 -d)`

curl API:  
`curl -k https://localhost:16443 -H "Authorization: Bearer $TOKEN"`
