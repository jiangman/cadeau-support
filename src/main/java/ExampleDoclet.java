import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.RootDoc;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/08/15
 */
@Log4j2
public class ExampleDoclet extends Doclet {

    public static boolean start(RootDoc root) {
        ClassDoc[] classes = root.classes();
        //解析classes
        return true;
    }

    public static void main(String[] args) {
        String[] docArgs = new String[]{"-doclet", ExampleDoclet.class.getName(),
                "C:\\java-development\\projects-repo\\deolin-projects\\beginning-mind\\src\\main\\java\\com\\spldeolin\\beginningmind\\core\\controller\\UserController.java"
        };
        com.sun.tools.javadoc.Main.execute(docArgs);
    }

}
