-- drop database if exists bsgenerator;
-- create database bsgenerator;

create table if not exists sites
(
    id      serial primary key,
    baseUrl varchar(2083) not null
);

create table if not exists articles
(
    id      serial primary key,
    siteId  serial references sites (id) not null,
    url     varchar(2083)                not null,
    content text                         not null
);

create table if not exists visitedLinks
(
    id     serial primary key,
    siteId serial references sites (id) not null,
    url    varchar(2083)                not null
);
