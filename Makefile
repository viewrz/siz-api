sudo :=$(shell docker ps 1>/dev/null 2>/dev/null || echo 'sudo')

compile:
	sbt compile

build: check_sudo
	$(sudo) docker build -t siz-api .

clean:
	rm -rf target project/target
	$(sudo) docker rm -f siz-api-mongo-test

vagrant-up:
	$(sudo) vagrant up

vagrant-halt:
	$(sudo) vagrant halt

tests:
	$(sudo) docker run --name=siz-api-mongo-test -d -p 27017:27017 mongo:2.6 mongod --smallfiles --nojournal 
	MONGODB_URI=mongodb://localhost/siz sbt test
	$(sudo) docker rm -f siz-api-mongo-test
