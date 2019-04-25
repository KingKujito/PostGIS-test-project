package controllers

import org.openqa.selenium.{By, WebDriver, WebElement}
import org.openqa.selenium.safari.SafariDriver

/**
  * Scrapes the web for random names. Only works for amounts dividable by 10 for now. Required the Safari browser to be installed
  * and to have 'allow for remote automation' turned on.
  */
object DataScraper {
  val driver     : WebDriver                = new SafariDriver()
  val path                                  = "https://www.name-generator.org.uk/quick/"
  def init(): Unit = driver.get(path)

  def scrape(amount: Int): List[String] = {
    (for(_ <- 0 until(amount/10)) yield {
      init()
      get10Names
    }).toList.flatten
  }

  def get10Names: List[String] = {
    val nameElems = driver.findElements(By.className("name_heading"))
    nameElems.toArray.toList.map {
      case e: WebElement =>
        e.getAttribute("innerHTML")
    }
  }
}
