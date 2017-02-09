# quartzite-test

## Description

This started out as an experiment involving the Quartztite library to see if it fit the needs of another projects. Specifically, we needed the following features:

* A simple way to invoke jobs asychronously and immediately, not requiring any scheduling configuration such as cron.
* A built-in way of storing job state in the database so that it could be queried later even if the application was restarted.

Investigation of this library led to having to research other things such as how to automate the running of database migrations, as well as needing to easily start up and shutdown a local database server.

Quartzite is built on top of Quartz, which requires a database schema and only provides DDL scripts which have to be run outside of Leiningen... that is, until I tried out the Ragtime library. To get everything set up properly I needed to:

* Include the Ragtime dependency in `project.clj`.
* Create a `resources\migrations` directory from the project root.
* Move the Quartz DDL script there and name it according to the convention required by Ragtime. (I also decided to split up the `create` and `down` statements into separate up and down scripts, `001-create-quartz-schema.up.sql` and `001-create-quartz-schema.down.sql`.)
* Implement `migrate` and `rollback` functions in one of the namespaces in this project; I chose the only one used, `quartite-test.core`.
* Set up task aliases in `profile.clj` to invoke those functions.

I also wanted to facilitate the need for a local database, and it turns out that that problem has been solved too, namely the `lein-postgres` plugin. To get this working, I needed to:

* 

## Getting it running

This project requires Leiningen; you can find instructions on how to install it here: http://www.leiningen.org/

Download the project to a local directory:

    git clone https://github.com/quephird/quartzite-test

In one session, move into that directory and run the following:

    lein postgres

This will start up a local instance of Postgres without needing to explicitly download or install any external software.
Running this task will block so you will need to start another session to continue

Once in the Clojure REPL, issue the following:

    lein do migrate, run

Future possibilities

* How to run `lein postgres` such that control returns to the invoking process and that subsequent Leiningen tasks can be chained and not requiring a user to spin up multiple terminal sessions.
* Be able to run the scheduler such that control can return to the invoking process.
* Being able to write the dynamic class loader purely in Clojure.


## Useful links

Quartzite, https://github.com/quartzite

Quartz, 

lein-postgres, 

Ragtime

## License

Copyright (C) 2017, ⅅ₳ℕⅈⅇℒℒⅇ Ҝⅇℱℱoℜⅆ.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
