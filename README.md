# 📘 CRUD de Blog con gRPC, Java y MongoDB

Este proyecto es un **CRUD** de un blog utilizando **gRPC**, **Java**, **MongoDB**, y **Maven**. Se ha implementado con **Lombok** para reducir la verbosidad del código. Además, se proporciona un `docker-compose.yml` para levantar una instancia de MongoDB y una colección de **Postman** para realizar pruebas del servicio.

---

## 🚀 Tecnologías Utilizadas

- **Java** (Maven)
- **gRPC** (Protocol Buffers)
- **MongoDB** (conexión vía Docker)
- **Lombok**
- **Postman** (para pruebas de consumo del servicio)

---

## 📂 Estructura del Proyecto

📁 `src/main/proto/blog.proto` → Archivo de definición de los servicios gRPC.

📁 `src/main/java/com/devluis/blog/service/` → Implementación del servicio gRPC.

📁 `src/main/resources/application.properties` → Configuración del proyecto (MongoDB, puerto, etc.).

📁 `docker-compose.yml` → Configuración de MongoDB para levantar la base de datos con Docker.

📁 `postman/` → Instrucciones para probar el servicio con Postman.

---

## 📜 Definición del Servicio gRPC (`blog.proto`)

El servicio **BlogService** expone cinco métodos para gestionar el CRUD del blog:

```proto
syntax = 'proto3';

package blog;

option java_package = 'com.proto.blog';
option java_multiple_files = true;

import 'google/protobuf/empty.proto';

message Blog{
  string id = 1;
  string author = 2;
  string title = 3;
  string content = 4;
}

message BlogId {
  string id = 1;
}

service BlogService{
  rpc createBlog(Blog) returns (BlogId);
  rpc readBlog(BlogId) returns (Blog);
  rpc updateBlog(Blog) returns (google.protobuf.Empty);
  rpc deleteBlog(BlogId) returns (google.protobuf.Empty);
  rpc listBlogs(google.protobuf.Empty) returns (stream Blog);
}
```

---

## ⚙️ Configuración de `application.properties`

Este archivo se usa para leer las configuraciones del aplicativo, como la conexión a **MongoDB** y otros parámetros.

Ejemplo de configuración:

```properties
grpc.server.port=9092
db.mongo.url=mongodb://root:root@localhost:27017/
db.mongo.database=blogdb
db.mongo.blog.collection=blog
```

---

## 🐳 Configuración de MongoDB con Docker

Para facilitar la ejecución del servicio sin necesidad de instalar MongoDB manualmente, se proporciona un `docker-compose.yml`:

```yaml
version: '3.1'

services:
   mongo:
      image: mongo
      restart: always
      ports:
         - "27017:27017"
      environment:
         MONGO_INITDB_ROOT_USERNAME: root
         MONGO_INITDB_ROOT_PASSWORD: root

   mongo-express:
      image: mongo-express
      restart: always
      ports:
         - "8081:8081"
      environment:
         ME_CONFIG_MONGODB_ENABLE_ADMIN: true
         ME_CONFIG_MONGODB_ADMINUSERNAME: root
         ME_CONFIG_MONGODB_ADMINPASSWORD: root
         ME_CONFIG_MONGODB_URL: mongodb://root:root@mongo:27017/
```

Para ejecutarlo:

```sh
docker-compose up -d
```

---

## 🔥 Pruebas con Postman

Para probar los servicios gRPC con **Postman**, sigue estos pasos:

1️⃣ **Abrir Postman** y crear una **nueva colección**.

2️⃣ **Importar el archivo `.proto`** dentro de la colección.

3️⃣ **Configurar la URL** con `grpc://localhost:9092`.

4️⃣ **Seleccionar el servicio** (`BlogService`) y la operación a probar (`createBlog`, `readBlog`, etc.).

5️⃣ **Enviar los parámetros** en formato JSON, por ejemplo, para crear un blog:

```json
{
  "author": "Luis Vargas",
  "title": "Mi Primer Blog",
  "content": "Este es un blog de prueba usando gRPC."
}
```

6️⃣ **Ejecutar la petición** y verificar la respuesta.

---

## ✅ Requisitos Previos

✔️ Java 11 o superior ☕

✔️ Maven 3.6 o superior 📦

✔️ Docker (para MongoDB) 🐳

✔️ Postman (para pruebas gRPC) 🔬

✔️ Protoc (Protocol Buffers Compiler) ⚡

---

Este proyecto demuestra cómo implementar un **CRUD con gRPC y MongoDB** en Java, utilizando buenas prácticas y facilitando la integración con herramientas modernas. 🚀

