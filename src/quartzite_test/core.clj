(ns quartzite-test.core
  (:require [clojurewerkz.quartzite.conversion :as conversion]
            [clojurewerkz.quartzite.jobs :as jobs]
            [clojurewerkz.quartzite.scheduler :as scheduler]
            [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]))

(defn load-config []
  {:datastore  (jdbc/sql-database "jdbc:postgresql://localhost:12345/postgres?user=postgres")
   :migrations (jdbc/load-resources "migrations")})

(defn migrate []
  (repl/migrate (load-config)))

(defn rollback []
  (repl/rollback (load-config)))

(jobs/defjob DurableJob [ctx]
  (let [job-data (conversion/from-job-data ctx)]
    (println "Waiting inside job...")
    (Thread/sleep 3000)
    (println "Executing job...")
    (doseq [[k v] job-data]
      (println (format "Parameter %s is %s" k v)))
    (println "Exiting job...")))

(defn make-job-detail [job-id job-data]
  (jobs/build
    (jobs/of-type DurableJob)
    (jobs/store-durably)
    (jobs/using-job-data job-data)
    (jobs/with-identity
      (jobs/key job-id))))

(defn make-scheduler []
  (scheduler/initialize))

(defn -main []
  (let [scheduler (make-scheduler)
        job-id "jobs.durable"
        job-data {:foo "foo" :bar 42}
        job-detail (make-job-detail job-id job-data)
        job-key (jobs/key job-id)]
    (println "Clearing the database from the last run...")
    (scheduler/clear! scheduler)
    (println "Starting the scheduler...")
    (scheduler/start scheduler)
    (println "Adding the new job to the scheduler...")
    (scheduler/add-job scheduler job-detail)
    (println "Triggering the job...")
    (.triggerJob scheduler job-key)))
