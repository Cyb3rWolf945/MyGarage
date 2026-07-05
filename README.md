```markdown
# 🚙 MyGarage

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF.svg?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4.svg?style=flat&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Backend](https://img.shields.io/badge/Backend-Node.js_||_Express-339933.svg?style=flat&logo=nodedotjs&logoColor=white)](https://nodejs.org)
[![Database](https://img.shields.io/badge/Database-PostgreSQL_16-4169E1.svg?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org)

**MyGarage** é uma solução completa e nativa para a gestão inteligente de frotas e veículos. Desenvolvida com foco na eficiência do condutor, a aplicação permite digitalizar matrículas em tempo real através da câmara, registar manutenções detalhadas, gerir inventário de peças e sincronizar dados com a cloud através de uma arquitetura robusta **offline-first**.

---

## 🏗️ Arquitetura do Sistema

O ecossistema divide-se numa aplicação Android robusta que garante total usabilidade mesmo sem conectividade, comunicando nativamente com uma API REST escalável e contentorizada.


```

┌────────────────────────────────────────┐       ┌────────────────────────────────────────┐
│          MyGarage (Android App)        │       │         MyGarage-Backend (API)         │
├────────────────────────────────────────┤       ├────────────────────────────────────────┤
│  • Jetpack Compose & Material 3        │──────▶│  • Node.js, TypeScript 6 & Express 5   │
│  • Room DB (Local Cache Engine)        │       │  • Prisma ORM 7 & PostgreSQL 16        │
│  • Google ML Kit & CameraX (OCR)       │◀──────│  • Dockerized Infrastructure           │
│  • Fused Location & DataStore          │       │  • AWS S3 Object Cloud Storage         │
└────────────────────────────────────────┘       └────────────────────────────────────────┘

```

### ✨ Funcionalidades Chave

*   📷 **Leitor de Matrículas (OCR):** Captura instantânea de matrículas através da câmara, processada localmente recorrendo ao *Google ML Kit Text Recognition*.
*   🔄 **Sincronização Offline-First:** Operações CRUD completas gravadas localmente via *Room* e sincronizadas bidirecionalmente com o servidor assim que houver rede.
*   🔧 **Histórico de Serviços:** Registo minucioso de ordens de reparação, associando peças, quantidades, preços e quilometragem do veículo.
*   📍 **Geolocalização Automática:** Identificação do local de estacionamento ou avaria guardando coordenadas GPS reais (*Fused Location Provider*).
*   🔐 **Autenticação Multi-Tenant:** Isolamento total de dados por utilizador via tokens estruturados JWT e encriptação *bcryptjs*.

---

## 🚨 CONFIGURAÇÃO DE SEGURANÇA CRÍTICA (Chaves de API)

Para proteger a integridade do projeto e evitar a exposição pública de credenciais de produção (como no GitHub), todas as chaves privadas são injetadas dinamicamente em tempo de compilação.

O ficheiro `local.properties` **NUNCA** deve ser enviado para o repositório remoto (já incluído no `.gitignore`).

### Como configurar as tuas chaves locais:

1. Na raiz do projeto Android (`MyGarage/`), cria ou abre o ficheiro `local.properties`.
2. Adiciona as tuas credenciais seguindo o modelo abaixo:

```properties
# Configurações de Conectividade da API
MYGARAGE_API_URL=[https://mygaragebackend-production.up.railway.app](https://mygaragebackend-production.up.railway.app)
MATRICULA_USERNAME=teu_utilizador_da_api

# 📍 GOOGLE MAPS API KEY (Obrigatório para o mapa funcionar)
MAPS_API_KEY=AIzaSyCDWc4Benql0vKUjpJUbttnqgi1NppWxAs

```

> ⚠️ **Nota para Avaliação Académica:** Para submissão do trabalho, remova a linha `sdk.dir=...` do ficheiro `local.properties` para não quebrar o ambiente do avaliador, mantendo apenas as chaves (`MAPS_API_KEY`) necessárias para a execução imediata da correção.

---

## 📱 Aplicação Android (`MyGarage/`)

### Estrutura do Projeto

```
app/src/main/java/pt/ipt/dama2026/mygarage/
├── MainActivity.kt           # Ponto de entrada, Navigation Graph & Corrotinas
├── MyGarageApplication.kt    # Inicialização global do contexto da App
├── data/                     # Camada de Dados (Data Layer)
│   ├── local/                # Base de dados Room, Entidades e DAOs
│   ├── model/                # Modelos de transferência de dados (DTOs) e Sync
│   ├── remote/               # Configuração e clientes de rede Retrofit
│   ├── storage/              # Gestão de ficheiros de imagem em disco local
│   └── location/             # Integração com os sensores de localização nativos
├── domain/                   # Camada de Domínio (Business Logic)
│   ├── model/                # Modelos de negócio puros (Vehicle, ServiceLog)
│   ├── repository/           # Definição de contratos (Repository Pattern)
│   ├── camera/               # Analisador de frames de imagem com CameraX e ML Kit
│   └── location/             # Gestores de estados e respostas geográficas
└── ui/                       # Camada de Apresentação (UI Layer)
    ├── screens/              # Ecrãs Jetpack Compose (Garage, Details, Form)
    ├── components/           # Componentes atómicos reutilizáveis
    └── theme/                # Definição do Design System (Material 3)

```

### Compilação Local

```bash
cd MyGarage
# Certifica-te de que configuraste o local.properties conforme a secção de Segurança acima
./gradlew installDebug

```

---

## 🽪 Servidor Backend (`MyGarage-Backend/`)

### Tech Stack do Servidor

* **Runtime & Engine:** Node.js + Express 5 (TypeScript 6)
* **Persistência:** Prisma ORM 7 conectado a PostgreSQL 16
* **Processamento de Imagem:** Sharp (Otimização e compressão de uploads)
* **Infraestrutura:** Docker & Docker Compose para orquestração isolada

### API Endpoints Disponíveis

| Método | Endpoint | Autenticação | Descrição |
| --- | --- | --- | --- |
| `GET` | `/api/health` | ❌ | Estado de saúde da API e Base de Dados |
| `POST` | `/api/auth/register` | ❌ | Criação de conta de utilizador |
| `POST` | `/api/auth/login` | ❌ | Autenticação de utilizador (Retorna JWT) |
| `GET/PUT` | `/api/user/profile` | ✅ | Gestão de dados do perfil |
| `POST` | `/api/sync/push` | ✅ | Envio de alterações locais offline para a cloud |
| `GET` | `/api/sync/pull` | ✅ | Obtenção de novos dados registados noutros dispositivos |
| `POST` | `/api/images/upload` | ✅ | Upload de fotografias para o bucket AWS S3 |

### Inicialização Rápida (Ambiente Contentorizado)

```bash
cd MyGarage-Backend
cp .env.docker .env
docker compose up --build -d

```

---

## 🎓 Ficha Técnica e Autor

Trabalho desenvolvido no âmbito da Unidade Curricular de **Desenvolvimento de Aplicações Móveis Avançadas (DAMA)**.

* **Autor:** António Gonçalves (Cyb3rWolf — @Cyb3rWolf945)
* **Instituição:** IPT (Instituto Politécnico de Tomar)
* **Ano Letivo:** 2026

---

## 📄 Licença

Este projeto está protegido sob os termos detalhados no ficheiro [LICENSE](https://www.google.com/search?q=LICENSE).
