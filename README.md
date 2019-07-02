# bsgenerator
The goal of this project is to generate semantically proper articles in polish language.
Project is divided into 2 parts:

1. parallel web scrapper to gather training data
2. statistical and neural networks approach to generate articles

*Some of the results so far:*

* "w miami miałem spotkania a międzyspotkaniami pochodziłem po wodzie w kranie"
* "pigułka odbiera kobiecie ochotę na mielone wsurowej szynce parmeńskiej z jajkiem"
* "seksistowski horror w uk zabito nawet 4700 dziewczynek ze względu na ciężar historii pomiędzy niemcami a rosją"

## DB setup
Database: postgres

You need to have postgres installed.
When creating user use:

* username: "root",
* password: "root"

For archlinux follow [this](https://wiki.archlinux.org/index.php/PostgreSQL#Installation).

Then create db:
```bash
sudo -iu postgres
pqsl
create database bsgenerator;
```

And finally run `setupdb.sql` from this directory.

## Running
In sbt console: 
```
run
```



