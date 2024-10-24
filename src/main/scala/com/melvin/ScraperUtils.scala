package com.melvin

import scala.concurrent.Future
import scala.sys.process._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.sys.process.Process
import scala.util.matching.Regex
import java.io.{File, PrintWriter}
import scala.io.Source
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.ObjectMapper
import com.opencsv.CSVWriter
import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.{HttpURLConnection, URL}

import scala.util.Try
object ScraperUtils {
  private var process: Option[Process] = None
  def installRequirements(requirementsFilePath: String): Int = {
    // This command installs Flask using pip
    val installRequirementsCommand = s"python3 -m pip install -r $requirementsFilePath"

    val exitCode = installRequirementsCommand.!

    if (exitCode == 0) {
      println("Requirements installed successfully.")
    } else {
      println("Failed to install Requirements.")
    }
    exitCode
  }

  def startInshortsServer(runServerCommand: String): Future[String] = {
    Future {
      // Run server and Capture output
      val outputBuilder = new StringBuilder
      process = Some(runServerCommand.run(new ProcessIO(
        input => {}, // No input to send to the process
        output => {
          // Read output from the Flask server
          scala.io.Source.fromInputStream(output).getLines().foreach { line =>
            outputBuilder.append(line).append("\n") // Capture each line of output
          }
        },
        error => {
          // You can also capture error output if needed
          scala.io.Source.fromInputStream(error).getLines().foreach { line =>
            outputBuilder.append(line).append("\n")
          }
        }
      )))

      // Wait for the process to start
      Thread.sleep(10000) // Wait for a short period to allow the server to start

      // Extract IP and port from the output
      val output = outputBuilder.toString
      val ipPattern:Regex = """Running on http://([\d.]+):(\d+)""".r

      output match {
        case ipPattern(ip, port) =>
          println(s"Flask server started at: $ip:$port")
          s"$ip:$port"
        case _ =>
          println("Not sure why its not match in the first case. Fix this.")
          ipPattern.findAllIn(output).matchData.toList(1).toString()
          //"localhost:5000" // Fallback to default
      }
    }
  }
  def stopProcess(): Unit = {
    println("Stopping server")
    process.foreach(_.destroy())
    process.foreach(process => println(process.exitValue()))
  }

  def fetchData(url: String): String = {
    var jsonString = ""
    Try {
      val source = Source.fromURL(url)
      try {
        jsonString = source.mkString
        jsonString
      } finally {
        source.close()
      }
    }
    jsonString
  }

  def parseJson(jsonData: String): NewsResponse = {
    val objectMapper = new ObjectMapper()
    objectMapper.registerModule(DefaultScalaModule)
    val newsResponse = objectMapper.readValue(jsonData, classOf[NewsResponse])
    newsResponse
  }

  def writeToCSV(newsResponse: NewsResponse): Unit = {
    val writer = new PrintWriter(new File("output.csv"))
    try {
      writer.write("Category,Title,Author,Content,Date,Time,ReadMoreUrl,ImageUrl,Url\n") // Header
      newsResponse.data.foreach { article =>
        writer.write(s""""${newsResponse.category}","${article.title}","${article.author}","${article.content.replace("\"", "'")}","${article.date}","${article.time}","${article.readMoreUrl}","${article.imageUrl}","${article.url}"\n""")
      }
    } finally {
      writer.close()
    }
  }

  def fetchDataAlternate(url: String): Unit = {
    println(url)
    try {
      val urlObj = new URL(url)
      val connection = urlObj.openConnection().asInstanceOf[HttpURLConnection]

      connection.setRequestMethod("GET")
      connection.setConnectTimeout(5000)
      connection.setReadTimeout(5000)

      val responseCode = connection.getResponseCode

      if (responseCode == HttpURLConnection.HTTP_OK) {
        val reader = new BufferedReader(new InputStreamReader(connection.getInputStream))
        val response = new StringBuilder
        var line: String = null

        while ( {
          line = reader.readLine(); line != null
        }) {
          response.append(line)
        }
        reader.close()
        println("Data fetched successfully.")

        // Write the response to CSV
        writeToCSV2(response.toString)
      } else {
        println(s"Failed to fetch data: HTTP error code $responseCode")
      }

    } catch {
      case e: Exception =>
        println(s"Failed to fetch data: ${e.printStackTrace()}")
    }
  }

  private def writeToCSV2(data: String): Unit = {
    val writer = new PrintWriter("output.json")
    try {
      // Write the data to CSV, you will need to format it appropriately
      writer.write(data) // Adjust this based on your actual data structure
    } finally {
      writer.close()
    }
  }
}