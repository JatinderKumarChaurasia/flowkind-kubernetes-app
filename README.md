
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

###### switching to docker as because of testcontainers only support docker . it does not support podman


    docker-compose up -d
    docker-compose up -d mongodb
    docker-compose up -d mysql
    docker-compose up -d review
    docker-compose up -d recommendation
    docker-compose up -d product
    docker-compose up -d product-service
    docker-compose down
    docker-compose container list
    docker-compose logs product

###### Docker Database commands
    to see the list of products inserted into mongodb
      docker-compose exec mongodb mongo product-db --quiet --eval "db.products.find()"
    to see the list of recommendations inserted in mongodb
      docker-compose exec mongodb mongo recommendation-db --quiet --eval "db.recommendations.find()"
    to see the list of reviews created for product on mysql (username=jatinder, password=root)
      docker-compose exec mysql mysql -ujatinder -p review-db -e "select * from reviews"  (password=root,username=jatinder)

    
