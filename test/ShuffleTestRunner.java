import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.Request;
import org.junit.runner.notification.RunListener;
import org.junit.runner.manipulation.Ordering;
import org.junit.runner.manipulation.Orderer;
import org.junit.runner.manipulation.InvalidOrderingException;
import org.junit.runner.manipulation.Orderable;
import org.junit.runner.OrderWith;
import org.junit.runner.Description;
import org.junit.internal.TextListener;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ShuffleTestRunner {
   public static void main(String[] args) throws Exception {
      String testClasses = System.getProperty("classes");
      String order = System.getProperty("order");
      List<String> testClassesList = Arrays.asList(testClasses.split(","));
      List<Class> classes = new ArrayList<>();

      if (order.equals("shuffle")) {
         Collections.shuffle(testClassesList); //shuffle test classes
      }

      for(String testClass : testClassesList){
         classes.add(Class.forName(testClass));
      }

      JUnitCore junit = new JUnitCore();
      junit.addListener(new TextListener(System.out));
      junit.addListener(new RunListener() {
         @Override
         public void testRunStarted(Description description) throws Exception {
             System.out.println("testRunStarted " + description);
         }
   
         @Override
         public void testStarted(Description description) throws Exception {
             System.out.println("testStarted " + description.getDisplayName());
         }

         @Override
         public void testRunFinished(Result result) throws Exception {
            //  System.out.println("testRunFinished " + stream);
         }
      });

      Result result = junit.run(Request.classes(classes.toArray(new Class[0]))
         .orderWith(new Ordering() {
            public boolean validateOrderingIsCorrect() {
               return false;
            }

            public List<Description> orderItems(Collection<Description> descriptions) {
               List<Description> ordered = new ArrayList<>(descriptions);
               if (order.equals("shuffle")) {
                  Collections.shuffle(ordered);
               }
               return ordered;
            }
      }));
   }
}  	