(ns quartzite-test.core
  (:require [clojurewerkz.quartzite.conversion :as conversion]
            [clojurewerkz.quartzite.jobs :as jobs]
            [clojurewerkz.quartzite.scheduler :as scheduler]))

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
    (.triggerJob scheduler job-key)

    ; This forces quartz to start the job before attempting to shut down.
    (while (not (scheduler/currently-executing-job? scheduler job-id)))
    (println "Shutting down quartz...")
    (scheduler/shutdown scheduler true)
    (println "Exiting program...")))