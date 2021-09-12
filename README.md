podman-compose up --build -d
podman-compose push
podman rmi $(podman images -q -f dangling=true)
podman logs -f --details  containerID
podman system prune -f --volumes
