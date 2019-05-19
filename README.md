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



