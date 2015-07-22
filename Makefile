compile:
	sbt compile

build:
	sudo docker build -t siz-api .

clean:
	rm -rf target project/target

vagrant-up:
	sudo vagrant up

vagrant-halt:
	sudo vagrant halt

tests:
	sudo docker run --name=siz-api-mongo-test -d -p 27017:27017 mongo:2.6
	MONGODB_URI=mongodb://localhost/siz sbt test
	sudo docker rm -f siz-api-mongo-test
