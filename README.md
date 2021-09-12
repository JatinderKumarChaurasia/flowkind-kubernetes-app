
###### Product Composite Link
    http://localhost:8080/product-composite/1

###### swagger ui link
    http://localhost:8080/openapi/swagger-ui.html   

###### podman commands to build
    podman-compose up --build -d
    podman-compose push
    podman rmi $(podman images -q -f dangling=true)
    podman logs -f --details  containerID
    podman system prune -f --volumes
    podman container logs container_name

###### github container registry login
    podman login ghcr.io
###### gitlab container registry login
    podman login registry.gitlab.com

    
