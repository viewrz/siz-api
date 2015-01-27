siz-api
=======

Siz.io API

# Dependencies and technology
- ReactiveMongo
- Vagrant
- Docker
- BCrypt
- Spracebook

# Commands :
## Run the app in test
    sbt run
    open http://localhost:9000
## Run in docker
    docker build . -tag "siz-api"
    docker run -t -p 9000:9000 siz-api
    open http://localhost:9000
## Run in a docker in vagrant
    vagrant up
    open http://localhost:9000
