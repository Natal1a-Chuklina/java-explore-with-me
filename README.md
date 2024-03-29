# Explore with me
Приложение является афишей событий. Пользователи могут по фильтрам искать интересующие события и регистрироваться на них, а также могут сами создавать события. В качестве дополнительного функционала была реализована система дружбы и подписок, для возможности получения специализированной выборки событий, в которых участвуют друзья и/или люди, на которых подписан пользователь. Кроме того, была добавлена возможность менять видимость событий, в которых участвует пользователь.

## Содержание
- [Архитектура приложения](#архитектура-приложения)
- [Системные требования](#системные-требования)
- [Установка](#установка)
- [Использование](#использование)
- [Стэк технологий](#стэк-технологий)

## Архитектура приложения
Приложение состоит из двух сервисов: основной сервис и сервис для сбора статистики. Каждый сервис имеет свою собственную БД.

Основной сервис разделен на три части:
* Публичная - доступна любому, даже не зарегистрированному пользователю
* Приватная - доступна только авторизованным пользователям
* Административная - доступна только администраторам сервиса

## Системные требования
- Java 11+
- Maven 3.1+
- Docker Desktop

## Установка
1. Склонируйте репозиторий:
```sh
git clone <адрес репозитория>
```
2. Выполните сборку проекта:
```sh
mvn package
```
3. Создайте и запустите docker-контейнер:
```sh
docker compose up
```
Основной сервис будет доступен по адресу: http://localhost:8080, cервис статистики: http://localhost:9090.

В application.properties сервисов можно посмотреть доступные профили для более удобного запуска при разработке и тестировании.

## Использование
API приложения:
1) [Спецификация основного сервиса](https://raw.githubusercontent.com/Natal1a-Chuklina/java-explore-with-me/main/ewm-main-service-spec.json)
2) [Спецификация сервиса статистики](https://raw.githubusercontent.com/Natal1a-Chuklina/java-explore-with-me/main/ewm-stats-service-spec.json)
## Стэк технологий
* Spring Boot
* REST
* Maven
* PostgreSQL
* Docker
* Lombok
* Hibernate
* JUnit 5
* SLF4J
