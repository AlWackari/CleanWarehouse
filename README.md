[![GCP Deploy](https://github.com/AlWackari/CleanWarehouse/actions/workflows/deploy.yml/badge.svg)](https://github.com/AlWackari/CleanWarehouse/actions/workflows/deploy.yml)
![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Container-2496ED?logo=docker&logoColor=white)

## DESCRIPTION
Clean Warehouse is a WMS sample written in JAVA.
It collects any type of Product logically and physically tracking his position inside the storage system, managing concurrency and allowing updates only through a transaction type mechanism.
The storage system is provided first during the setup in order for the WM to know where to store Products, without that the WM can't start.

## SCOPE
Clean Warehouse has been created to successfully implement and train the following concepts:

**Architecture**
* Domain Driven Design
* Hexagonal Architecture / Port and Adapters
* API REST
* Secure Authentication 

**DevOps**
* CI/CD Pipeline through GitHub Actions
* Containerization with Docker
* Cloud Platform on Google
