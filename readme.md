# Distributed Prime Generation with Akka

We use a distributed variation on the sieve of Eratosthenes to generate primes. The clients (SilverSlaves) pull work from the master (DarkLord), and also pull needed data. The master talks with a storage actor (named ColdCellar) to save and load data. Master periodically generates tasks to give out to clients. Master does not care about giving duplicate or even already-done jobs, and this makes it able to store no state on clients. However, the logic of task delivery and generation has been set up such that duplicate tasks should be rare.
An Allseer actor is used to ask the master for outputting primes. It supports doing this periodically.
All components are very resilient against crashes, though a supervisor might be needed to actually restart them. Akka does restart actors it recognizes as crashed. The clients don't have any meaningful state (beyond their current task, which they can safely lose), and master just uses ColdCellar for its important state. We persist the data in ColdCellar almost completely on disk, so the system will recover all done jobs. Reconnection is also done automatically through Akka.

## Docker

There are dockerfiles for all three main modules: docker-id (the master), docker-ss (the clients), docker-as (Allseer). You need to first build their base image (run the commands from the repository's root):

`docker build --tag bd971-base -f docker-base .`

Then you can run these:
```
docker build --tag bd971-id -f docker-id .
docker build --tag bd971-ss -f docker-ss .
docker build --tag bd971-as -f docker-as .
```

Now you can start them:
```
docker run -p2557:2551 bd971-as
docker run -p2556:2551 bd971-ss
docker run -p2555:2551 bd971-ss
docker run -p2552:2552 bd971-id
...
```

Note that since clients and Allseer have been configured for Docker, you can't run multiple instances of them on the same OS (same container); Because they all connect to a single port. You need to run them inside unique containers with uniquely configured ports (as you can see in the example).

The server though should always be run on `-p2552:2552`. You also need to specify the server's IP in the source code, both in the SharedSpace.scala and application.conf; This address needs to be accessible from inside the containers, so you can't use the localhost.
