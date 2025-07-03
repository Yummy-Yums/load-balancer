# 🚀 Load Balancer

> A high-performance, scalable load balancer built with modern distributed systems principles

[![Scala](https://img.shields.io/badge/Scala-3.3.6-red.svg)](https://scala-lang.org/)
[![ZIO](https://img.shields.io/badge/ZIO-2.x-blue.svg)](https://zio.dev/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Yummy-Yums/load-balancer)

## ✨ Features

- **🔄 Multiple Load Balancing Algorithms**
  - Round Robin
  - Weighted Round Robin
  - Least Connections
  - IP Hash
  - Health Check-based routing

- **⚡ High Performance**
  - Async/non-blocking I/O with ZIO
  - Efficient connection pooling
  - Minimal latency overhead

- **🔧 Configuration-Driven**
  - YAML/JSON configuration
  - Environment-based configs


## 🚀 Quick Start

### Prerequisites

- Java 11+
- SBT 1.x
- Docker (optional)

### Installation

```bash
git clone https://github.com/Yummy-Yums/load-balancer.git
cd load-balancer
sbt compile
```


### Configuration

Create a `application.conf` file:

```hocon
Application {
    port = 8080
    host = "localhost"
    backends=[
    "http://127.0.0.1:8081",
    "http://127.0.0.1:8082",
    "http://127.0.0.1:8083"
    ]
    health-check-interval = 3
}
```

## 🔧 Load Balancing Strategies

### Round Robin
Distributes requests evenly across all healthy backends.

```scala
LoadBalancer.roundRobin(backends)
```

## 📊 Monitoring

### Health Checks
Access health status at `/health`:

```bash
curl http://localhost:8080/health
```

Response:
```json
insert response 
```

## 🧪 Testing

### Unit Tests
```bash
sbt test
```

## 🛠️ Development

### Project Structure

```
├── build.sbt
├── lb
├── lb.jar
├── project
│   ├── build.properties
│   ├── metals.sbt
│   ├── plugins.sbt
│   ├── project
│   └── target
├── readme.md
├── src
│   ├── main
│   └── test
```

### Key Dependencies

```scala
libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "2.0.21",
  "dev.zio" %% "zio-http" % "3.0.0-RC4",
  "dev.zio" %% "zio-config" % "4.0.0-RC16",
  "dev.zio" %% "zio-json" % "0.6.2",
  "dev.zio" %% "zio-metrics-prometheus" % "2.0.8",
  "dev.zio" %% "zio-logging" % "2.1.16"
)
```

## 📚 Documentation

- [API Documentation](https://deepwiki.com/Yummy-Yums/load-balancer)



### Code Style

We use Scalafmt for code formatting:

```bash
sbt scalafmt
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
