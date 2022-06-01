package com.cst438;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGrade;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;

/*
 * This example shows how to use selenium testing using the web driver 
 * with Chrome browser.
 * 
 *  - Buttons, input, and anchor elements are located using XPATH expression.
 *  - onClick( ) method is used with buttons and anchor tags.
 *  - Input fields are located and sendKeys( ) method is used to enter test data.
 *  - Spring Boot JPA is used to initialize, verify and reset the database before
 *      and after testing.
 */

@SpringBootTest
public class EndToEndTestAddAssignment {

  public static final String CHROME_DRIVER_FILE_LOCATION = "C:/Users/Ashley/Downloads/chromedriver_win32/chromedriver.exe";
  public static final String URL = "https://cst438-gradebook-fe.herokuapp.com/";
  public static final String TEST_USER_EMAIL = "test@csumb.edu";
  public static final String TEST_INSTRUCTOR_EMAIL = "dwisneski@csumb.edu";
  public static final int SLEEP_DURATION = 1000; // 1 second.

  public static final String TEST_ASSIGNMENT_NAME = "Test Assignment";
  public static final int TEST_COURSE_ID = 123456;
  public static final String TEST_DUE_DATE = "2022-10-25";
  public static final String TEST_STUDENT_NAME = "Test";
  public static final String TEST_COURSE_TITLE = "Test Course";

  @Autowired
  EnrollmentRepository enrollmentRepository;

  @Autowired
  CourseRepository courseRepository;

  @Autowired
  AssignmentGradeRepository assignnmentGradeRepository;

  @Autowired
  AssignmentRepository assignmentRepository;

  @Test
  public void addAssignmentTest() throws Exception {

    // Database setup: create course
    Course c = new Course();
    c.setCourse_id(TEST_COURSE_ID);
    c.setInstructor(TEST_INSTRUCTOR_EMAIL);
    c.setSemester("Fall");
    c.setYear(2022);
    c.setTitle(TEST_COURSE_TITLE);

    // add a student TEST into course
    Enrollment e = new Enrollment();
    e.setCourse(c);
    e.setStudentEmail(TEST_USER_EMAIL);
    e.setStudentName(TEST_STUDENT_NAME);

    courseRepository.save(c);
    e = enrollmentRepository.save(e);

    AssignmentGrade ag = null;

    // set the driver location and start driver
    //@formatter:off
		// browser	property name 				Java Driver Class
		// edge 	webdriver.edge.driver 		EdgeDriver
		// FireFox 	webdriver.firefox.driver 	FirefoxDriver
		// IE 		webdriver.ie.driver 		InternetExplorerDriver
		//@formatter:on

    System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
    WebDriver driver = new ChromeDriver();
    // Puts an Implicit wait for 10 seconds before throwing exception
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

    driver.get(URL);
    Thread.sleep(SLEEP_DURATION);

    try {

      driver.findElement(By.xpath("//AddAssignment[@id='add-assignment']")).click();
      Thread.sleep(SLEEP_DURATION);

      WebElement we = driver.findElement(By.xpath("//TextField[@name='assignmentName']"));
      we.sendKeys(TEST_ASSIGNMENT_NAME);

      we = driver.findElement(By.xpath("//TextField[@name='courseId']"));
      we.sendKeys(Integer.toString(TEST_COURSE_ID));

      we = driver.findElement(By.xpath("//TextField[@name='dueDate']"));
      we.sendKeys(TEST_DUE_DATE);

      driver.findElement(By.xpath("//button[@id='create-assignment']")).click();
      Thread.sleep(SLEEP_DURATION);

      List<WebElement> elements = driver.findElements(By.xpath("//div[@data-field='assignmentName']/div"));
      boolean found_assignment = false;
      for (WebElement web_e : elements) {
        System.out.println(web_e.getText()); // for debug
        if (web_e.getText().equals(TEST_ASSIGNMENT_NAME)) {
          found_assignment = true;
          web_e.findElement(By.xpath("descendant::input")).click();
          break;
        }
      }
      assertTrue(found_assignment, "Unable to locate TEST ASSIGNMENT in list of assignments to be graded.");

    } catch (Exception ex) {
      throw ex;
    } finally {

      // clean up database.
      List<Assignment> list_a = assignmentRepository.findNeedGradingByEmail(TEST_INSTRUCTOR_EMAIL);
      for (Assignment a : list_a) {
        ag = assignnmentGradeRepository.findByAssignmentIdAndStudentEmail(a.getId(), TEST_USER_EMAIL);
        if (ag != null)
          assignnmentGradeRepository.delete(ag);
        assignmentRepository.delete(a);
      }
      enrollmentRepository.delete(e);
      courseRepository.delete(c);

      driver.quit();
    }

  }
}