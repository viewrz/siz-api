# -*- mode: ruby -*-
# vi: set ft=ruby :

ENV['VAGRANT_DEFAULT_PROVIDER'] = 'docker'

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  config.vm.provider "docker" do |d|
    #d.force_host_vm = true # Support problem with Linux
    d.remains_running = true
  end

  config.vm.define "mongo" do |mongo|
    mongo.vm.provider "docker" do |d|
      d.image = "mongo"
      d.name = "siz-api-mongo"
    end
  end

  config.vm.define "siz-api" do |siz_api|
    siz_api.vm.synced_folder ".", "/var/www/siz-api"

    siz_api.vm.provider "docker" do |d|
      d.image = "jdauphant/siz-api"
      d.name = "siz-api"
      d.link("siz-api-mongo:mongo")
      d.link("siz-api-rethinkdb:rethinkdb")
      d.ports = ["9000:9000"]
      d.create_args = ["-t"]
      d.cmd = ["sbt", "run", "-Dmongodb.uri=mongodb://mongo:27017/siz"]
    end
  end
end
