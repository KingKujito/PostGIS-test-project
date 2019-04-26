package controllers

import org.openqa.selenium.{By, WebDriver, WebElement}
import org.openqa.selenium.safari.SafariDriver

/**
  * Scrapes the web for random names. Only works for amounts dividable by 10 for now. Required the Safari browser to be installed
  * and to have 'allow for remote automation' turned on.
  */
object DataScraper {
  val driver          : WebDriver           = new SafariDriver()
  val namepath                              = "https://www.name-generator.org.uk/quick/"
  //makes sure we get a fresh session (urls expire)
  val streetpath : String              = {
    driver.get("https://www.name-generator.org.uk/street/")
    driver.findElement(By.xpath("//*[@id=\"main\"]/div/form/input[15]")).click()
    Thread.sleep(5000)
    println(driver.getCurrentUrl)
    driver.getCurrentUrl
  }

  def init(path : String): Unit = driver.get(path)

  def scrape(amount: Int, path : String = namepath): List[String] = {
    (for(_ <- 0 until(amount/10)) yield {
      init(path)
      path match {
        case `streetpath` => get10Streets
        case `namepath`   => get10Names
        case _            => List.empty[String]
      }
    }).toList.flatten
  }

  def get10Names: List[String] = {
    val nameElems = driver.findElements(By.className("name_heading"))
    nameElems.toArray.toList.map {
      case e: WebElement =>
        e.getAttribute("innerHTML")
    }
  }

  def get10Streets: List[String] = {
    val nameElems = driver.findElements(By.className("name"))
    nameElems.toArray.toList.map {
      case e: WebElement =>
        e.getAttribute("innerHTML").split("\\. ").reverse.head
    }
  }
}
