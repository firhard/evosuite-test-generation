import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.Request;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;


public class TestRunner {
   public static void main(String[] args) throws Exception {
      String testClasses = System.getProperty("classes");
      List<String> testClassesList = Arrays.asList(testClasses.split(","));
      Collections.shuffle(testClassesList); //shuffle test classes
      System.out.println(testClassesList);
      int pass = 0;
      int fail = 0;
      for(String testClass : testClassesList){
         // System.out.println(testClass);
         Class clazz = Class.forName(testClass);
         Method[] methods = clazz.getDeclaredMethods();
         List<String> testMethodsList = new ArrayList<String>();

         for (Method method : methods) {
            testMethodsList.add(method.getName());
         }
         Collections.shuffle(testMethodsList); //shuffle test cases

         for (String testMethod : testMethodsList){
            Request request = Request.method(clazz, testMethod);
            // Request request = Request.method(method.getDeclaringClass(), testMethod);
            Result result = new JUnitCore().run(request);
         
            for (Failure failure : result.getFailures()) {
               System.out.println(failure.toString());
            }
            
            if(result.wasSuccessful() == true) {
               pass++;
               System.out.print(".");
            }
            else {
               fail++;
               System.out.print("E");
            }
         }
      }
      System.out.println("");
      System.out.println("Pass: " + pass + ", Fail: " + fail);
   }
}  	