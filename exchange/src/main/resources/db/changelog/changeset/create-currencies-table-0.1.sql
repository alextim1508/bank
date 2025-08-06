CREATE SCHEMA IF NOT EXISTS bank;

create table bank.currencies
(
    code        text primary key,
    rus_title    text not null,
    title       text not null,
    country     text not null,
    mark        text not null
);
