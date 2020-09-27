/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.training.exercises.ridecleansing.scala

import org.apache.flink.core.execution.JobClient
import org.apache.flink.training.exercises.common.sources.TaxiRideGenerator
import org.apache.flink.training.exercises.common.utils.ExerciseBase._
import org.apache.flink.training.exercises.common.utils.{ExerciseBase, GeoUtils, MissingSolutionException}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.training.exercises.common.datatypes.TaxiRide
import org.apache.logging.log4j.LogManager

/**
 * The "Ride Cleansing" exercise of the Flink training in the docs.
 *
 * The task of the exercise is to filter a data stream of taxi ride records to keep only rides that
 * start and end within New York City. The resulting stream should be printed to the
 * standard out.
 *
 */
object RideCleansingExercise extends ExerciseBase {

  def main(args: Array[String]) {

    val logger = LogManager.getLogger(this.getClass)
    logger.info("~~~~~ STARTING {} ~~~~~", this.getClass.getCanonicalName)

    // set up the execution environment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(parallelism)

    // get the taxi ride data stream
    val dataGenerator = new TaxiRideGenerator()
    val rides: DataStream[TaxiRide] = env.addSource(rideSourceOrTest(dataGenerator))

    // filter out rides that do not start and end in NYC
    val filteredRides = rides
      //.filter(ride => throw new MissingSolutionException)
      .filter(ride =>
        GeoUtils.isInNYC(ride.startLon, ride.startLat) &&
        GeoUtils.isInNYC(ride.endLon, ride.endLat)
      )

    // print the filtered stream
    printOrTest(filteredRides)

    val jobName = "Taxi Ride Cleansing"

    // run the cleansing pipeline (run indefinitely)
    //env.execute(jobName)

    // run pipeline (for about 5s and cancel Job, exit gracefully)
    logger.info("~~~~~ EXECUTE ASYNC \"{}\" ~~~~~", jobName)
    val jobClient: JobClient = env.executeAsync(jobName)

    Thread.sleep(5000L)
    logger.info("~~~~~ CANCELLING JOB {}~~~~~", jobClient.getJobID.toString)
    jobClient.cancel()
  }
}
