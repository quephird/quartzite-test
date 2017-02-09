# quartzite-test

## Description

This started out as an experiment involving the Quartztite library to see if it fit the needs of another project. Specifically, we needed the following features:

* A simple way to invoke jobs asychronously and on demand, not requiring any scheduling configuration such as cron.
* A built-in way of storing job state in the database so that it could be queried later even if the application was restarted.

Investigation of this library led to having to research other things such as how to automate the running of database migrations, as well as needing to easily start up and shutdown a local database server. So the original scope of this little project widened into something surprisingly more interesting, and I felt warranted promotion to a project to be shared with others.

## Background

Quartzite is built on top of Quartz, which requires a database schema and only provides a DDL script (one per RDBMS implementation) which has to be run outside of Leiningen and there is no other means provided to migrate it... that is, until I tried out the Ragtime library. To get everything set up properly I needed to:

* Include the Ragtime dependency in `project.clj`.
* Create a `resources/migrations` directory from the project root.
* Move the Quartz DDL script there and name it according to the convention required by Ragtime. (I also decided to split up the `create` and `down` statements into separate up and down scripts, `001-create-quartz-schema.up.sql` and `001-create-quartz-schema.down.sql`.)
* Implement `migrate` and `rollback` functions in one of the namespaces in this project; I chose the only one used, `quartite-test.core`.
* Set up task aliases in `profile.clj` to invoke those functions.

I also wanted to simplify the setup of a local database, and it turns out that that problem has been addressed too, namely the `lein-postgres` plugin. To get this working, I needed to:

* Set up the dependency in `:plugins` section of `project.clj`.
* Configure the port on which the Postgres instance would listen.
* Insure that the port specified in `project.clj` was consistent with that specified in the `quartzite.core` namespace for the Ragtime configuration. 
* Craft the JDBC URL in that same configuration to include the `postgres` user as a query parameter. (As far as I can tell, the user cannot be configured to be anything else in the `lein-postgres` section of `project.clj` nor any other way in the Ragtime configuration. If I am wrong, I'm open to corrections.)

Once I finally got al that working, the actual thing I wanted to demonstrate was being able to author and run jobs easily in Quartzite. Specifically, I hoped that the APIs weren't too complicated and that I didn't need to go through too much ceremony to accomplish this. 

Quartzite (and Quartz) supports running of jobs with and without so-called triggers. You can either create a job type and submit an instance of it to the scheduler, or if you desire you can create a trigger to be associated with that job type and run jobs on a scheduled basis. I only wanted to do the former, and it took me a little while for figure out how.

## Running the demo

This project requires Leiningen; you can find instructions on how to install it [here](http://www.leiningen.org/).

Download the project to a local directory:

    git clone https://github.com/quephird/quartzite-test

In one session, move into that directory and run the following:

    lein postgres

This will start up a local instance of Postgres without needing to explicitly download or install any external software. Running this task will block so you will need to start another session to continue.

In another terminal session, run the following:

    lein do clean, migrate, run

This will create the schema for Quartz, then run the demo and will also block. 

At this point, you should be able to see various things echoed to the screen, including the parameters that were passed to the job:

![](./images/leiningen_run.png)

You should also be able to open up a Postgres session and see that a record for the job should have been persisted:

![](./images/postgres_query.png)

Under the covers, even though a job can be submitted immediately without an explicit, predefined trigger, an "anonymous" one is created on the fly and managed in the database. (I discovered this in the beginning only because I had not used the correct DDL script and Quartz wasn't able to properly manage the database; more on this below.) I was able to do this using the `.triggerJob` method of the `Scheduler` class, which Quartzite curiously omits. Unfortunately, 1) there is nothing on the job details record that itself shows whether or not the job failed, and 2) the trigger that _was_ associated with the job where the status _is_ recorded is deleted immediately after the job runs or fails.

As an aside, one of the trickiest problems I encountered was some strange behavior where trigger records were _not_ being deleted from the database after a job was submitted, and the trigger remained in `WAITING` status. It turned out that I carelessly used the DDL script from Quartz 2.2.3 instead of the proper one for Quartzite, which depends on 2.1.7. There was a small change made to the schema between those two versions, and my using the newer one ultimately caused failures when trying to save records to the database. That problem was not immediately apparent because those errors were being swallowed due to insufficient logging set up which a coworker helped me resolve. That is why I left in the `slf4j` dependency in this project. Lesson learned: _always properly log errors and always look at log files for troubleshooting!!!_

*UPDATE:* Thanks to @mdpendergrass' help, I was able to write the dynamic class loader (`quartzite-test.MyDynamicClassLoader`) entirely in Clojure, and now the project has no Java source code. To clarify, in order for Quartzite (actually it's Quartz) to find and load Clojure classes, you need to create a class which extends `` and implements ``; you can read the details [here](http://clojurequartz.info/articles/durable_quartz_stores.html#how_to_use_durable_job_stores_with_quartzite). Some important things to note:

* Neither Clojure's `reify` nor `proxy` helps here as I needed to both extend one class and implement another interface as well as insure the compiled class is named such that the classloader can find it. `gen-class` was the only way to accomplish all of this.
* The class needs to be nominated in the `:aot` section of `project.clj` to insure its compilation is completed _before_ the classloader tries to find it.

## Conclusions

One of the things that was most disappointing about Quartzite (and really Quartz) was discovering that once jobs complete successfully, trigger records are deleted from the database. It is the trigger records which record the statuses of running jobs, but even if jobs fail, the corresponding trigger records are deleted. Moreover, there is nothing in the `qrtz_job_details` table that denotates final state nor otherwise differentiates successfully completed jobs from failed ones. That is a key feature that we need for another project and in a sense a dealbreaker for our using this library.

## Future plans

There are a few more things I'd like to try out for this project:

* Be able to run `lein postgres` such that control returns to the invoking process and that subsequent Leiningen tasks can be chained and not requiring a user to spin up multiple terminal sessions.
* Be able to run the scheduler such that control can return to the invoking process.
* Be able to demonstrate querying the database, including figuring out how to query by parameter values.

## Useful links

Quartzite, [https://github.com/quartzite](https://github.com/quartzite)

Quartz, [http://www.quartz-scheduler.org/](http://www.quartz-scheduler.org/)

Ragtime, [https://github.com/weavejester/ragtime](https://github.com/weavejester/ragtime)

`lein-postgres`, [https://github.com/whostolebenfrog/lein-postgres](https://github.com/whostolebenfrog/lein-postgres)

## License

Copyright (C) 2017, ⅅ₳ℕⅈⅇℒℒⅇ Ҝⅇℱℱoℜⅆ.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
