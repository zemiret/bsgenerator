-- drop database if exists bsgenerator;
-- create database bsgenerator;

create table if not exists sites
(
    id      serial primary key,
    baseUrl varchar(2083) not null
);

-- Allowed base urls to crawl at the given site
create table if not exists allowedBases
(
    id     serial primary key,
    url    varchar(2083)                not null,
    siteId serial references sites (id) not null
);

create table if not exists articles
(
    id      serial primary key,
    siteId  serial references sites (id) not null,
    url     varchar(2083)                not null,
    header  text                         not null,
    content text                         not null
);

create table if not exists visitedLinks
(
    id      serial primary key,
    siteId  serial references sites (id) not null,
    url     varchar(2083)                not null,
    visited boolean                      not null default false
);

