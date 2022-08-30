import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.Request;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class ShuffleTestRunner {
   public static void main(String[] args) throws Exception {
      String testClasses = System.getProperty("classes");
      List<String> testClassesList = Arrays.asList(testClasses.split(","));
      Collections.shuffle(testClassesList); //shuffle test classes
      System.out.println(testClassesList);
      int pass = 0;
      int fail = 0;

      for(String testClass : testClassesList){
         Class clazz = Class.forName(testClass);
         Method[] methods = clazz.getDeclaredMethods();
         List<Method> testMethodsList = Arrays.asList(methods);
         Collections.shuffle(testMethodsList); //shuffle test cases
         // System.out.println(testMethodsList);

         for (Method testMethod : testMethodsList){
            // need to check annotations (as of now EvoSuite does not have @Before/@After so it should be working fine)
            // Annotation[] annotations = testMethod.getDeclaredAnnotations();
            
            Request request = Request.method(clazz, testMethod.getName());
            Result result = new JUnitCore().run(request);
         
            for (Failure failure : result.getFailures()) {
               System.out.println("");
               System.out.println(failure.toString());
            }
            
            if(result.wasSuccessful() == true) {
               pass++;
               System.out.print(".");
            } else {
               fail++;
               System.out.print("E");
            }
         }
      }
      System.out.println("");
      System.out.println("Pass: " + pass + ", Fail: " + fail);
   }
}  	