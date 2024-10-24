package com.melvin

import com.melvin.ScraperUtils.{fetchData, fetchDataAlternate, installRequirements, parseJson, startInshortsServer, stopProcess, writeToCSV}

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext

/**
 * @author ${Minesh.Melvin}
 */
object InshortsNewsScrapper extends App {
  implicit val ec: ExecutionContext = ExecutionContext.global

  // The category of news to look for
  private val category = "science"
  // Install python requirements
  private val requirementsFilePath = "src/main/scala/com/melvin/inshortsserver/requirements.txt"
  //installRequirements(requirementsFilePath)

  // Start the python inshorts flask app
  println("Starting server")
  private val runServerCommand = "python3 src/main/scala/com/melvin/inshortsserver/app.py"
  //private val server = startInshortsServer(runServerCommand)
  val url = "http://192.168.0.100:5000/news?category=science"
  val listArticles = parseJson(fetchData(url))
  writeToCSV(listArticles)
  //server.onComplete{
  // case Success(ip) =>
  //  val url = s"$ip/news?category=$category"
  //  println(s"Request: $url")
  // Fetch the data from the url
  //   Thread.sleep(3000)
  //      fetchData(url) match {
  //        case Success(jsonData) =>
  //          println(jsonData)
  //          // Proceed with parsing and writing to CSV
  //          val newsArticles = parseJson(jsonData)
  //          // Write data to CSV
  //          writeToCSV(newsArticles, "news_articles.csv")
  //        case Failure(exception) =>
  //          println(s"Failed to fetch data: ${exception.getMessage}")
  //      }
  //     fetchDataAlternate(url)
  //     stopProcess()
  //   case Failure(exception) => println(s"Failed to start Flask server: ${exception.getMessage}")
// }

  // Keep the main thread alive to wait for the future to complete
 // Thread.sleep(2000) // Adjust time as needed to allow async operation to complete
}