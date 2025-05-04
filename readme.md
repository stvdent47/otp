# OTP management system

## Система для создания, управления и распространения OTP кодов.

### Особенности:
- создание пользователей
- разграничение пользователей по ролям
- ограничение использования эндпоинтов для определенных ролей
- аутентификация и авторизация пользователей с использованием JWT
- для администратора
  - управление конфигурацией OTP-кодов для роли
  - получение всех пользователей
- для пользователя
  - создание OTP-кода и отправка его одним из способов (email, sms, telegram, сохранение в файл)
  - валидация полученного OTP-кода
- наличие планировщика, который изменяет статус для просроченных кодов

### Технологии:
- Java 23
- PostgreSQL 17 + JDBC
- Maven
- JavaMail
- SMPP
- Telegram bot API
- HttpServer
- SLF4J/Logback

### Эндпоинты:
- POST, no auth `/users/register` — регистрация пользователя, `requestBody: { username: string; password: string; role: 'admin' | 'user'}`, `responseBody: { status: 'success' | 'error' }`
- POST, no auth `/users/login` — логин пользователя, `requestBody: { username: string; password: string }`, `responseBody: { status: 'success' | 'error', data: { jwt: string' } }`
- POST, user role `/users/generate-otp` — генерация и отправка OTP-кода, `requestBody: { channel: 'email' | 'file' | 'sms' | 'telegram'; destination: string; }`, `responseBody: { status: 'success' | 'error' }`
- POST, user role `/users/validate-otp` — валидация полученного OTP-кода, `requestBody: { userId: string; otp: string }, responseBody: { status: 'success' | 'error' }`
- POST, admin role `/admin/change-otp-config` — изменения OTP-конфига, `requestBody: { length: number; expiration: number }`, `responseBody: { status: 'success' | 'error' }`
- GET, admin role `/admin/get-users` — получение всех пользователей кроме администратора, `responseBody: { status: 'success' | 'error', data: User[] }`
- DELETE, admin role `/admin/delete/:id` — удаление пользователя по `id` и всех его OTP-кодов, `responseBody: { status: 'success' | 'error' }`

#### Создание таблицы пользователей:
```
create table users ([Main.java](src/main/java/mephi/Main.java)
	id varchar(100) primary key,
	username varchar(50) not null unique,
	password varchar(255) not null,
	role varchar(5) not null check (role in ('admin', 'user'))
)
```

#### Создание таблицы конфигурации OTP-кодов:
```
create table otp_config (
    id varchar(5) primary key check (id = '1'),
    length int not null check (length > 0),
    expiration int not null check (expiration > 0) // in ms
)
```
##### Наполнение:
```
insert into otp_config (id, length, expiration) values ("1", 6, 300)
```

#### Создание таблицы OTP-кодов:
```
create table otp_codes (
    id varchar(100) primary key,
    value varchar(50) not null,
    created_at bigint not null,
    status varchar(50) check (status in ('active', 'expired', 'used')) ,
    user_id varchar(100) references users(id) on delete cascade,
    operation_id varchar(100)
)
```

#### Заполнение конфигурационных файлов:
- `app.properties` — для приложения (старт сервера, подключение к БД)
- `email.properties` — для сервиса отправки OTP-кодов по email
- `sms.properties` — для сервиса отправки OTP-кодов по sms
- `telegram.properties` — для сервиса отправки OTP-кодов через telegram
