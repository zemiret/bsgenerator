-- drop database if exists bsgenerator;
-- create database bsgenerator;

create table if not exists site
(
    id serial primary key
);

create table if not exists articles
(
    id      serial primary key,
    siteId  serial references site (id),
    url     varchar(2083),
    content text
);

create table if not exists visitedLinks
(
    id     serial primary key,
    siteId serial references site (id),
    url    varchar(2083)
);
