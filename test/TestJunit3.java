import org.junit.After;
import org.junit.AfterClass;

import org.junit.Before;
import org.junit.BeforeClass;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TestJunit3 {
   static int x = 0;

   // @After
   // public void after() {
   //    System.out.println("in after");
   // }

   @Test
   public void testAdd() {
      x++;
      System.out.println(x);
      String str = "Junit is working fine";
      assertEquals("Junit is working fine",str);
   }
   @Test
   public void testAdd2() {
      x = x + 2;
      System.out.println(x);
      String str = "Junit is working fine1";
      assertEquals("Junit is working fine1",str);
   }
   @Test
   public void testAdd3() {
      x++;
      System.out.println(x);
      String str = "Junit is working fine1";
      assertEquals("Junit is working fine1",str);
   }
}