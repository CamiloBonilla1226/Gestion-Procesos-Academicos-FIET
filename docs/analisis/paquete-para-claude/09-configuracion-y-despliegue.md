

===== ARCHIVO: backend/solicitudes/pom.xml =====

<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.5.4</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.unicauca.cfiet</groupId>
	<artifactId>solicitudes</artifactId>
	<version>0.0.1</version>
	<name>solicitudes</name>
	<description>Aplicación para la gestión de solicitudes del consejo de facultad FIET</description>
	<url/>
	<licenses>
		<license/>
	</licenses>
	<developers>
		<developer/>
	</developers>
	<scm>
		<connection/>
		<developerConnection/>
		<tag/>
		<url/>
	</scm>
	<properties>
		<java.version>17</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.apache.poi</groupId>
			<artifactId>poi-ooxml</artifactId>
			<version>5.2.5</version>
		</dependency>
		<dependency>
			<groupId>org.springdoc</groupId>
			<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
			<version>2.8.10</version>
		</dependency>
		<dependency>
			<groupId>org.modelmapper</groupId>
			<artifactId>modelmapper</artifactId>
			<version>2.4.5</version>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>
		<dependency>
			<groupId>io.jsonwebtoken</groupId>
			<artifactId>jjwt-api</artifactId>
			<version>0.11.2</version>
		</dependency>
		<dependency>
			<groupId>io.jsonwebtoken</groupId>
			<artifactId>jjwt-impl</artifactId>
			<version>0.11.2</version>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>io.jsonwebtoken</groupId>
			<artifactId>jjwt-jackson</artifactId>
			<version>0.11.2</version>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>com.mysql</groupId>
			<artifactId>mysql-connector-j</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<configuration>
					<annotationProcessorPaths>
						<path>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</path>
					</annotationProcessorPaths>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</exclude>
					</excludes>
				</configuration>
			</plugin>
		</plugins>
	</build>

</project>



===== ARCHIVO: frontend/package.json =====

{
  "name": "front-fiet-sc",
  "version": "0.0.0",
  "scripts": {
    "ng": "ng",
    "start": "ng serve",
    "build": "ng build",
    "watch": "ng build --watch --configuration development",
    "test": "ng test",
    "serve:ssr:front-fiet-sc": "node dist/front-fiet-sc/server/server.mjs"
  },
  "prettier": {
    "printWidth": 100,
    "singleQuote": true,
    "overrides": [
      {
        "files": "*.html",
        "options": {
          "parser": "angular"
        }
      }
    ]
  },
  "private": true,
  "dependencies": {
    "@angular/common": "^20.2.0",
    "@angular/compiler": "^20.2.0",
    "@angular/core": "^20.2.0",
    "@angular/forms": "^20.2.0",
    "@angular/platform-browser": "^20.2.0",
    "@angular/platform-server": "^20.2.0",
    "@angular/router": "^20.2.0",
    "@angular/ssr": "^20.2.1",
    "@primeuix/themes": "^1.2.3",
    "bootstrap": "^5.3.8",
    "express": "^5.1.0",
    "primeflex": "^4.0.0",
    "primeicons": "^7.0.0",
    "primeng": "^20.1.1",
    "rxjs": "~7.8.0",
    "sweetalert2": "^11.23.0",
    "tslib": "^2.3.0",
    "zone.js": "~0.15.0"
  },
  "devDependencies": {
    "@angular/build": "^20.2.1",
    "@angular/cli": "^20.2.1",
    "@angular/compiler-cli": "^20.2.0",
    "@types/express": "^5.0.1",
    "@types/jasmine": "~5.1.0",
    "@types/node": "^20.17.19",
    "jasmine-core": "~5.9.0",
    "karma": "~6.4.0",
    "karma-chrome-launcher": "~3.2.0",
    "karma-coverage": "~2.2.0",
    "karma-jasmine": "~5.1.0",
    "karma-jasmine-html-reporter": "~2.1.0",
    "typescript": "~5.9.2"
  }
}



===== ARCHIVO: frontend/angular.json =====

{
  "$schema": "./node_modules/@angular/cli/lib/config/schema.json",
  "version": 1,
  "newProjectRoot": "projects",
  "projects": {
    "front-fiet-sc": {
      "projectType": "application",
      "schematics": {},
      "root": "",
      "sourceRoot": "src",
      "prefix": "app",
      "architect": {
        "build": {
          "builder": "@angular/build:application",
          "options": {
            "browser": "src/main.ts",
            "polyfills": [
              "zone.js"
            ],
            "tsConfig": "tsconfig.app.json",
            "assets": [
              {
                "glob": "**/*",
                "input": "public"
              }
            ],
            "styles": [
              "src/styles.css",
              "node_modules/primeicons/primeicons.css",
              "node_modules/bootstrap/dist/css/bootstrap.min.css"
            ],
            "scripts": [
              "node_modules/@popperjs/core/dist/umd/popper.min.js",
              "node_modules/bootstrap/dist/js/bootstrap.min.js"
            ],
            "server": "src/main.server.ts",
            "outputMode": "server",
            "ssr": {
              "entry": "src/server.ts"
            }
          },
          "configurations": {
            "production": {
              "budgets": [
                {
                  "type": "initial",
                  "maximumWarning": "500kB",
                  "maximumError": "1MB"
                },
                {
                  "type": "anyComponentStyle",
                  "maximumWarning": "4kB",
                  "maximumError": "8kB"
                }
              ],
              "outputHashing": "all"
            },
            "development": {
              "optimization": false,
              "extractLicenses": false,
              "sourceMap": true
            }
          },
          "defaultConfiguration": "production"
        },
        "serve": {
          "builder": "@angular/build:dev-server",
          "configurations": {
            "production": {
              "buildTarget": "front-fiet-sc:build:production"
            },
            "development": {
              "buildTarget": "front-fiet-sc:build:development"
            }
          },
          "defaultConfiguration": "development"
        },
        "extract-i18n": {
          "builder": "@angular/build:extract-i18n"
        },
        "test": {
          "builder": "@angular/build:karma",
          "options": {
            "polyfills": [
              "zone.js",
              "zone.js/testing"
            ],
            "tsConfig": "tsconfig.spec.json",
            "assets": [
              {
                "glob": "**/*",
                "input": "public"
              }
            ],
            "styles": [
              "src/styles.css"
            ]
          }
        }
      }
    }
  },
  "cli": {
    "analytics": false
  }
}



===== ARCHIVO: backend/docker-compose.yml =====

# Orquestación local del backend + base de datos de
# "Gestión de Procesos Académicos FIET" (trabajo de grado).
# Base heredada de https://github.com/jdacamacho/back-fiet-sc.
#
# Uso:
#   1) Generar el jar:  cd solicitudes && ./mvnw clean install -DskipTests
#   2) Levantar todo:   docker compose up --build
# El backend queda en http://localhost:8080 y MySQL expuesto en el
# puerto 3307 del host (3306 dentro del contenedor).
version: '3'

services:
  solicitudes-consejo-de-facultad:
    build: .
    mem_limit: 512m
    ports:
      - "8080:8080"
    environment:
      SERVER_PORT: 8080
      DB_URL: jdbc:mysql://cfiet_database:3306/cfiet?createDatabaseIfNotExist=true&serverTimezone=UTC
      DB_USER_NAME: root
      DB_PASSWORD: <REDACTADO>
      UPLOADS_PATH: /app/uploads
    volumes:
      - ./uploads:/app/uploads
    restart: always
    depends_on:
      cfiet_database:
        condition: service_healthy
  cfiet_database:
    image: mysql:8.0.33
    ports:
      - "3307:3306"
    environment:
      MYSQL_ROOT_PASSWORD: <REDACTADO>
      MYSQL_PASSWORD: <REDACTADO>
      MYSQL_DATABASE: cfiet_database
    restart: always
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 10s
      retries: 10


===== ARCHIVO: backend/Dockerfile =====

## Execute application
FROM eclipse-temurin:17-jdk-jammy

# Directorio de trabajo
WORKDIR /app

# Ruta donde se guardarán los archivos
ENV UPLOADS_PATH=/app/uploads

# Crear carpeta de uploads
RUN mkdir -p /app/uploads

# Copiar el jar
ARG JAR_FILE=solicitudes/target/solicitudes-0.0.1.jar
COPY ${JAR_FILE} app_cfiet.jar

# Puerto de la app
EXPOSE 8080

# Ejecutar la app
ENTRYPOINT ["java", "-jar", "app_cfiet.jar"]



===== ARCHIVO: frontend/dockerfile =====

FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

FROM node:20-alpine
WORKDIR /app

COPY --from=build /app/dist ./dist
COPY package*.json ./

RUN npm install --omit=dev

EXPOSE 4200

CMD ["node", "dist/front-fiet-sc/server/server.mjs"]



===== ARCHIVO: backend/solicitudes/src/main/resources/application.properties =====

spring.application.name=solicitudes-consejo-de-facultad

#URL
url.application=/api/unicauca/fiet/consejo/
url.backend=http://localhost:8080
url.frontend=http://localhost:4200

# Server port
server.port=${SERVER_PORT}

# DATASOURCE (MYSQL)
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER_NAME}
spring.datasource.password=<REDACTADO>

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# IMPORT SQL
spring.sql.init.mode=never
spring.jpa.defer-datasource-initialization=true
# Ruta base para archivos cargados
app.uploads.base-path=${UPLOADS_PATH:/app/uploads}

# Table names physically
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

# Tamanio de archivos
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB

