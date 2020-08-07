example k8s operator with scala and zio
===

### development

Publish to MicroK8s
- `MICROK8S=1 sbt docker:publish`
- update `spec.template.spec.containers.image` for the operator to use `localhost:32000` registry
