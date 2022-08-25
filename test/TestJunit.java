import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TestJunit {
   static int x = 0;

   @Test
   public void testAdd() {
      String str = "Junit is working fine";
      assertEquals("Junit is working fine",str);
      x++;
      System.out.println(x);
   }
   @Test
   public void testAdd2() {
      String str = "Junit is working fine1";
      assertEquals("Junit is working fine1",str);
      x = x + 2;
      System.out.println(x);
   }
   @Test
   public void testAdd3() {
      String str = "Junit is working fine1";
      assertEquals("Junit is working fine1",str);
      x++;
      System.out.println(x);
   }
}